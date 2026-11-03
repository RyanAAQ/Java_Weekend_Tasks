package cohorthub;

import cohorthub.exceptions.CohortException;

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

public class Cohort {

    private final Map<String, Learner> learners = new HashMap<>();
    private final Map<String, Set<Learner>> groups = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private final Queue<Learner> supportQueue = new ArrayDeque<>();

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

    public void remove(String id) throws CohortException {
        Learner learner = find(id);
        learners.remove(learner.getId());
        for (Set<Learner> members : groups.values()) {
            members.remove(learner);
        }
        supportQueue.remove(learner);
    }

    public List<Learner> learnersSorted() {
        List<Learner> sorted = new ArrayList<>(learners.values());
        sorted.sort(Learner.BY_NAME_THEN_ID);
        return sorted;
    }

    public void createGroup(String groupName) throws CohortException {
        String name = requireText(groupName, "Group name");
        if (groups.containsKey(name)) {
            throw new CohortException("A group called '" + name + "' already exists.");
        }
        groups.put(name, new HashSet<>());
    }

    public boolean addToGroup(String groupName, String learnerId) throws CohortException {
        Set<Learner> members = findGroup(groupName);
        Learner learner = find(learnerId);
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
        return names;
    }

    public List<Learner> membersOf(String groupName) throws CohortException {
        List<Learner> members = new ArrayList<>(findGroup(groupName));
        members.sort(Learner.BY_NAME_THEN_ID);
        return members;
    }

    public List<String> groupNames() {
        return new ArrayList<>(groups.keySet());
    }

    public void requestSupport(String learnerId) throws CohortException {
        Learner learner = find(learnerId);
        if (supportQueue.contains(learner)) {
            throw new CohortException(learner.getName() + " already has a pending support request.");
        }
        supportQueue.add(learner);
    }

    public Optional<Learner> attendNext() {
        return Optional.ofNullable(supportQueue.poll());
    }

    public List<Learner> waitingLearners() {
        return new ArrayList<>(supportQueue);
    }

    public int totalLearners() {
        return learners.size();
    }

    public int totalGroups() {
        return groups.size();
    }

    public int waitingCount() {
        return supportQueue.size();
    }

    public Map<String, Integer> membershipCounts() {
        Map<String, Integer> counts = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (Map.Entry<String, Set<Learner>> entry : groups.entrySet()) {
            counts.put(entry.getKey(), entry.getValue().size());
        }
        return counts;
    }

    public List<Learner> inBoth(String groupA, String groupB) throws CohortException {
        Set<Learner> result = new HashSet<>(findGroup(groupA));
        result.retainAll(findGroup(groupB));
        return sorted(result);
    }

    public List<Learner> inEitherButNotBoth(String groupA, String groupB) throws CohortException {
        Set<Learner> firstGroup = findGroup(groupA);
        Set<Learner> secondGroup = findGroup(groupB);
        Set<Learner> result = new HashSet<>(firstGroup);
        result.removeAll(secondGroup);

        Set<Learner> onlyInSecondGroup = new HashSet<>(secondGroup);
        onlyInSecondGroup.removeAll(firstGroup);
        result.addAll(onlyInSecondGroup);
        return sorted(result);
    }

    private Set<Learner> findGroup(String groupName) throws CohortException {
        Set<Learner> members = groupName == null ? null : groups.get(groupName.trim());
        if (members == null) {
            throw new CohortException("No group called '" + groupName + "' exists.");
        }
        return members;
    }

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
