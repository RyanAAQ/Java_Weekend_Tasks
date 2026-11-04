package cohorthub;

import cohorthub.exceptions.CohortException;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CohortTest {

    private Cohort cohort;

    @BeforeEach
    void setUp() throws Exception {
        cohort = new Cohort();
        cohort.register("L1", "Ryan", "ryan@mail.com");
        cohort.register("L2", "Quadri Ariyo", "quadri@mail.com");
        cohort.register("L3", "Quadri Ariyo", "quadri2@mail.com");
    }

    private static List<String> ids(List<Learner> learners) {
        return learners.stream().map(Learner::getId).toList();
    }

    @Test
    void duplicateLearnerIdIsRejected() {
        assertThrows(CohortException.class, () -> cohort.register("L1", "Quadri Ariyo", "new@mail.com"));
        assertEquals(3, cohort.totalLearners());
    }

    @Test
    void duplicateEmailIsRejectedIgnoringCase() {
        CohortException error = assertThrows(CohortException.class,
                () -> cohort.register("L9", "Quadri Ariyo", "RYAN@MAIL.COM"));
        assertTrue(error.getMessage().contains("already registered"));
        assertEquals(3, cohort.totalLearners());
    }

    @Test
    void blankFieldsAndBadEmailsAreRejected() {
        assertThrows(CohortException.class, () -> cohort.register(" ", "Ryan", "ryan@mail.com"));
        assertThrows(CohortException.class, () -> cohort.register("L9", "", "n@mail.com"));
        assertThrows(CohortException.class, () -> cohort.register("L9", "Ryan", "not-an-email"));
        assertThrows(CohortException.class, () -> cohort.register("L9", "Ryan", "a@@b.com"));
        assertThrows(CohortException.class, () -> cohort.register("L9", "Ryan", null));
    }

    @Test
    void findingAnUnknownLearnerGivesAHelpfulError() {
        assertThrows(CohortException.class, () -> cohort.find("NOPE"));
        assertEquals("Ryan", assertDoesNotThrow(() -> cohort.find("L1")).getName());
    }

    @Test
    void updateChangesNameAndEmailButNeverTheId() throws Exception {
        Learner updated = cohort.update("L1", "Quadri Ariyo", "quadri.ariyo@mail.com");
        assertEquals("L1", updated.getId());
        assertEquals("Quadri Ariyo", updated.getName());
        assertEquals("quadri.ariyo@mail.com", updated.getEmail());
    }

    @Test
    void updateCannotTakeAnotherLearnersEmail() {
        assertThrows(CohortException.class, () -> cohort.update("L1", "", "QUADRI@mail.com"));
    }

    @Test
    void updatingToYourOwnEmailIsFine() throws Exception {
        assertDoesNotThrow(() -> cohort.update("L1", "Quadri Ariyo", "ryan@mail.com"));
    }

    @Test
    void updatingANameDoesNotBreakGroupMembership() throws Exception {
        cohort.createGroup("Java");
        cohort.addToGroup("Java", "L1");
        cohort.update("L1", "Quadri Ariyo", "");
        assertEquals(List.of("Java"), cohort.groupsOf("L1"));
        assertFalse(cohort.addToGroup("Java", "L1"));
    }

    @Test
    void learnersAreSortedByNameThenById() {
        assertEquals(List.of("L2", "L3", "L1"), ids(cohort.learnersSorted()));
    }

    @Test
    void sortingDoesNotCorruptIdLookup() throws Exception {
        List<Learner> sorted = cohort.learnersSorted();
        sorted.clear();
        assertEquals("Quadri Ariyo", cohort.find("L2").getName());
        assertEquals(3, cohort.totalLearners());
    }

    @Test
    void addingTheSameLearnerToAGroupTwiceCreatesNoDuplicate() throws Exception {
        cohort.createGroup("Java");
        assertTrue(cohort.addToGroup("Java", "L1"));
        assertFalse(cohort.addToGroup("Java", "L1"));
        assertEquals(1, cohort.membersOf("Java").size());
    }

    @Test
    void aLearnerCanBelongToManyGroups() throws Exception {
        cohort.createGroup("Java");
        cohort.createGroup("Algorithms");
        cohort.addToGroup("Java", "L1");
        cohort.addToGroup("Algorithms", "L1");
        assertEquals(List.of("Algorithms", "Java"), cohort.groupsOf("L1"));
    }

    @Test
    void onlyRegisteredLearnersMayJoinGroupsAndGroupsMustExist() throws Exception {
        cohort.createGroup("Java");
        assertThrows(CohortException.class, () -> cohort.addToGroup("Java", "GHOST"));
        assertThrows(CohortException.class, () -> cohort.addToGroup("Nope", "L1"));
        assertEquals(0, cohort.membersOf("Java").size());
    }

    @Test
    void duplicateGroupNamesAreRejectedIgnoringCase() throws Exception {
        cohort.createGroup("Java");
        assertThrows(CohortException.class, () -> cohort.createGroup("JAVA"));
        assertEquals(1, cohort.totalGroups());
    }

    @Test
    void supportRequestsAreHandledInArrivalOrder() throws Exception {
        cohort.requestSupport("L2");
        cohort.requestSupport("L1");
        cohort.requestSupport("L3");

        assertEquals("L2", cohort.attendNext().orElseThrow().getId());
        assertEquals("L1", cohort.attendNext().orElseThrow().getId());
        assertEquals("L3", cohort.attendNext().orElseThrow().getId());
    }

    @Test
    void aLearnerCannotHaveTwoPendingRequestsButCanRequestAgainAfterBeingAttended() throws Exception {
        cohort.requestSupport("L1");
        assertThrows(CohortException.class, () -> cohort.requestSupport("L1"));
        assertEquals(1, cohort.waitingCount());

        cohort.attendNext();
        assertDoesNotThrow(() -> cohort.requestSupport("L1"));
    }

    @Test
    void onlyRegisteredLearnersMayEnterTheQueue() {
        assertThrows(CohortException.class, () -> cohort.requestSupport("GHOST"));
        assertEquals(0, cohort.waitingCount());
    }

    @Test
    void attendingAnEmptyQueueIsHandledWithoutAnException() {
        Optional<Learner> next = cohort.attendNext();
        assertTrue(next.isEmpty());
    }

    @Test
    void removingALearnerLeavesNoStaleMembershipsOrQueueEntries() throws Exception {
        cohort.createGroup("Java");
        cohort.createGroup("Algorithms");
        cohort.addToGroup("Java", "L1");
        cohort.addToGroup("Algorithms", "L1");
        cohort.addToGroup("Java", "L2");
        cohort.requestSupport("L2");
        cohort.requestSupport("L1");
        cohort.requestSupport("L3");

        cohort.remove("L1");

        assertThrows(CohortException.class, () -> cohort.find("L1"));
        assertEquals(List.of("L2"), ids(cohort.membersOf("Java")));
        assertEquals(0, cohort.membersOf("Algorithms").size());
        assertEquals(List.of("L2", "L3"), ids(cohort.waitingLearners()));
        assertEquals(2, cohort.totalLearners());
    }

    @Test
    void removingAnUnknownLearnerFailsCleanly() {
        assertThrows(CohortException.class, () -> cohort.remove("GHOST"));
        assertEquals(3, cohort.totalLearners());
    }

    @Test
    void reportShowsTotalsGroupCountsAndWaitingNumber() throws Exception {
        cohort.createGroup("Java");
        cohort.createGroup("Algorithms");
        cohort.addToGroup("Java", "L1");
        cohort.addToGroup("Java", "L2");
        cohort.addToGroup("Algorithms", "L3");
        cohort.requestSupport("L1");

        assertEquals(3, cohort.totalLearners());
        assertEquals(2, cohort.totalGroups());
        assertEquals(Map.of("Java", 2, "Algorithms", 1), cohort.membershipCounts());
        assertEquals(1, cohort.waitingCount());
    }

    @Test
    void learnersWithTheSameIdAreEqualRegardlessOfNameAndEmail() {
        Learner firstLearner = new Learner("X", "Ryan", "ryan@mail.com");
        Learner secondLearner = new Learner("X", "Quadri Ariyo", "quadri@mail.com");
        assertEquals(firstLearner, secondLearner);
        assertEquals(firstLearner.hashCode(), secondLearner.hashCode());
        assertNotEquals(firstLearner, new Learner("Y", "Ryan", "ryan@mail.com"));
    }
}
