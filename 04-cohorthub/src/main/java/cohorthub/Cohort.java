package cohorthub;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;

/**
 * The CohortHub rules. No console code here.
 *
 * Why each collection was chosen:
 *  - Map<String, Learner> learners   : HashMap gives O(1) lookup by ID, and keys are unique by definition.
 *  - Map<String, Set<Learner>> groups: each group's members are a HashSet, so adding the same learner
 *                                      twice is automatically ignored (no duplicates). Learner.equals/hashCode
 *                                      use the immutable ID, so the set stays correct even if a name changes.
 *  - Queue<Learner> supportQueue     : ArrayDeque is first-in, first-out, which is exactly the "attend in
 *                                      arrival order" rule.
 *  - List<Learner> (sorted view)     : a List is the only one of these that has an order, so it is used only
 *                                      for displaying. It is a COPY, so sorting it never touches the map.
 */
public class Cohort {

    private final Map<String, Learner> learners = new HashMap<>();
    private final Map<String, Set<Learner>> groups = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private final Queue<Learner> supportQueue = new ArrayDeque<>();

    // ============================================================== learners

    public Learner register(String id, String name, String email) throws CohortException {
        String cleanId = requireText(id, "Learner ID");
        String cleanName = requireText(name, "Name");
        String cleanEmail = requireEmail(email);

        if (learners.containsKey(cleanId)) {
            throw new CohortException("A learner with ID '" + cleanId + "' is already registered.");
        }
        if (emailInUse(cleanEmail, null)) {
            throw new CohortException("The email '" + cleanEmail + "' is already registered.");
        }
        Learner learner = new Learner(cleanId, cleanName, cleanEmail);
        learners.put(cleanId, learner);
        return learner;
    }

    public Learner find(String id) throws CohortException {
        Learner learner = id == null ? null : learners.get(id.trim());
        if (learner == null) {
            throw new CohortException("No learner is registered with ID '" + id + "'.");
        }
        return learner;
    }

    /** Updates name and email. The ID can never be changed. A blank value means "keep the current one". */
    public Learner update(String id, String newName, String newEmail) throws CohortException {
        Learner learner = find(id);
        String name = (newName == null || newName.isBlank()) ? learner.getName() : newName.trim();
        String email = (newEmail == null || newEmail.isBlank()) ? learner.getEmail() : requireEmail(newEmail);

        if (emailInUse(email, learner)) {
            throw new CohortException("The email '" + email + "' is already registered to another learner.");
        }
        learner.setName(name);
        learner.setEmail(email);
        return learner;
    }

    /** Removes a learner AND every trace of them: group memberships and any pending support request. */
    public void remove(String id) throws CohortException {
        Learner learner = find(id);
        learners.remove(learner.getId());
        for (Set<Learner> members : groups.values()) {
            members.remove(learner);
        }
        supportQueue.remove(learner);
    }

    /** A sorted COPY for display. Sorting it cannot affect the ID map. */
    public List<Learner> learnersSorted() {
        List<Learner> sorted = new ArrayList<>(learners.values());
        sorted.sort(Learner.BY_NAME_THEN_ID);
        return sorted;
    }

    // ================================================================ groups

    public void createGroup(String groupName) throws CohortException {
        String name = requireText(groupName, "Group name");
        if (groups.containsKey(name)) {
            throw new CohortException("A group called '" + name + "' already exists.");
        }
        groups.put(name, new HashSet<>());
    }

    /** Returns true if the learner was added, false if they were already a member (no duplicate is created). */
    public boolean addToGroup(String groupName, String learnerId) throws CohortException {
        Set<Learner> members = findGroup(groupName);
        Learner learner = find(learnerId); // only registered learners may join
        return members.add(learner);
    }

    public List<String> groupsOf(String learnerId) throws CohortException {
        Learner learner = find(learnerId);
        List<String> names = new ArrayList<>();
        for (Map.Entry<String, Set<Learner>> entry : groups.entrySet()) {
            if (entry.getValue().contains(learner)) {
                names.add(entry.getKey());
            }
        }
        return names; // already alphabetical because the group map is a TreeMap
    }

    public List<Learner> membersOf(String groupName) throws CohortException {
        List<Learner> members = new ArrayList<>(findGroup(groupName));
        members.sort(Learner.BY_NAME_THEN_ID);
        return members;
    }

    public List<String> groupNames() {
        return new ArrayList<>(groups.keySet());
    }

    // ======================================================= mentor-support queue

    /** Adds a learner to the back of the queue. A learner may not have two pending requests. */
    public void requestSupport(String learnerId) throws CohortException {
        Learner learner = find(learnerId); // only registered learners may enter the queue
        if (supportQueue.contains(learner)) {
            throw new CohortException(learner.getName() + " already has a pending support request.");
        }
        supportQueue.add(learner);
    }

    /** Attends to the learner who has waited longest. Empty when nobody is waiting. */
    public Optional<Learner> attendNext() {
        return Optional.ofNullable(supportQueue.poll());
    }

    public List<Learner> waitingLearners() {
        return new ArrayList<>(supportQueue); // front of the queue first
    }

    // ============================================================== reports

    public int totalLearners() {
        return learners.size();
    }

    public int totalGroups() {
        return groups.size();
    }

    public int waitingCount() {
        return supportQueue.size();
    }

    /** Group name -> number of members. */
    public Map<String, Integer> membershipCounts() {
        Map<String, Integer> counts = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (Map.Entry<String, Set<Learner>> entry : groups.entrySet()) {
            counts.put(entry.getKey(), entry.getValue().size());
        }
        return counts;
    }

    // ============================================================ stretch goal

    /** Learners who belong to BOTH groups. */
    public List<Learner> inBoth(String groupA, String groupB) throws CohortException {
        Set<Learner> result = new HashSet<>(findGroup(groupA));
        result.retainAll(findGroup(groupB));
        return sorted(result);
    }

    /** Learners in one group or the other, but NOT in both. */
    public List<Learner> inEitherButNotBoth(String groupA, String groupB) throws CohortException {
        Set<Learner> a = findGroup(groupA);
        Set<Learner> b = findGroup(groupB);
        Set<Learner> result = new HashSet<>(a);
        result.addAll(b);
        Set<Learner> both = new HashSet<>(a);
        both.retainAll(b);
        result.removeAll(both);
        return sorted(result);
    }

    // ============================================================== helpers

    private Set<Learner> findGroup(String groupName) throws CohortException {
        Set<Learner> members = groupName == null ? null : groups.get(groupName.trim());
        if (members == null) {
            throw new CohortException("No group called '" + groupName + "' exists.");
        }
        return members;
    }

    /** True if some OTHER learner already uses this email (case ignored). */
    private boolean emailInUse(String email, Learner except) {
        String wanted = email.toLowerCase(Locale.ROOT);
        for (Learner learner : learners.values()) {
            if (learner != except && learner.getEmail().toLowerCase(Locale.ROOT).equals(wanted)) {
                return true;
            }
        }
        return false;
    }

    private static List<Learner> sorted(Collection<Learner> learners) {
        List<Learner> list = new ArrayList<>(learners);
        list.sort(Learner.BY_NAME_THEN_ID);
        return Collections.unmodifiableList(list);
    }

    private static String requireText(String value, String label) throws CohortException {
        if (value == null || value.isBlank()) {
            throw new CohortException(label + " cannot be blank.");
        }
        return value.trim();
    }

    private static String requireEmail(String email) throws CohortException {
        String clean = requireText(email, "Email");
        int at = clean.indexOf('@');
        boolean looksValid = at > 0 && at == clean.lastIndexOf('@') && clean.indexOf('.', at) > at + 1
                && !clean.endsWith(".") && !clean.contains(" ");
        if (!looksValid) {
            throw new CohortException("'" + clean + "' is not a valid email address.");
        }
        return clean;
    }
}
