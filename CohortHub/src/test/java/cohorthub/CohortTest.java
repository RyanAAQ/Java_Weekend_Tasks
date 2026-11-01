package cohorthub;

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
        cohort.register("L1", "Zainab", "zainab@mail.com");
        cohort.register("L2", "Ade", "ade@mail.com");
        cohort.register("L3", "Ade", "ade2@mail.com");
    }

    private static List<String> ids(List<Learner> learners) {
        return learners.stream().map(Learner::getId).toList();
    }

    @Test
    void duplicateLearnerIdIsRejected() {
        assertThrows(CohortException.class, () -> cohort.register("L1", "Someone", "new@mail.com"));
        assertEquals(3, cohort.totalLearners());
    }

    @Test
    void duplicateEmailIsRejectedIgnoringCase() {
        CohortException e = assertThrows(CohortException.class,
                () -> cohort.register("L9", "Someone", "ZAINAB@MAIL.COM"));
        assertTrue(e.getMessage().contains("already registered"));
        assertEquals(3, cohort.totalLearners());
    }

    @Test
    void blankFieldsAndBadEmailsAreRejected() {
        assertThrows(CohortException.class, () -> cohort.register(" ", "N", "n@mail.com"));
        assertThrows(CohortException.class, () -> cohort.register("L9", "", "n@mail.com"));
        assertThrows(CohortException.class, () -> cohort.register("L9", "N", "not-an-email"));
        assertThrows(CohortException.class, () -> cohort.register("L9", "N", "a@@b.com"));
        assertThrows(CohortException.class, () -> cohort.register("L9", "N", null));
    }

    @Test
    void findingAnUnknownLearnerGivesAHelpfulError() {
        assertThrows(CohortException.class, () -> cohort.find("NOPE"));
        assertEquals("Zainab", assertDoesNotThrow(() -> cohort.find("L1")).getName());
    }

    @Test
    void updateChangesNameAndEmailButNeverTheId() throws Exception {
        Learner updated = cohort.update("L1", "Zainab Bello", "zb@mail.com");
        assertEquals("L1", updated.getId());
        assertEquals("Zainab Bello", updated.getName());
        assertEquals("zb@mail.com", updated.getEmail());
    }

    @Test
    void updateCannotTakeAnotherLearnersEmail() {
        assertThrows(CohortException.class, () -> cohort.update("L1", "", "ADE@mail.com"));
    }

    @Test
    void updatingToYourOwnEmailIsFine() throws Exception {
        assertDoesNotThrow(() -> cohort.update("L1", "New Name", "zainab@mail.com"));
    }

    @Test
    void updatingANameDoesNotBreakGroupMembership() throws Exception {
        cohort.createGroup("Java");
        cohort.addToGroup("Java", "L1");
        cohort.update("L1", "Completely Different", "");
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
        assertEquals("Ade", cohort.find("L2").getName());
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
    void setOperationsShowLearnersInBothAndInEitherButNotBoth() throws Exception {
        cohort.createGroup("Java");
        cohort.createGroup("Algorithms");
        cohort.addToGroup("Java", "L1");
        cohort.addToGroup("Java", "L2");
        cohort.addToGroup("Algorithms", "L2");
        cohort.addToGroup("Algorithms", "L3");

        assertEquals(List.of("L2"), ids(cohort.inBoth("Java", "Algorithms")));
        assertEquals(List.of("L3", "L1"), ids(cohort.inEitherButNotBoth("Java", "Algorithms")));
    }

    @Test
    void learnersWithTheSameIdAreEqualRegardlessOfNameAndEmail() {
        Learner a = new Learner("X", "One", "one@mail.com");
        Learner b = new Learner("X", "Two", "two@mail.com");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, new Learner("Y", "One", "one@mail.com"));
    }
}
