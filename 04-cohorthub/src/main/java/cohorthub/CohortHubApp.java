package cohorthub;

import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Scanner;

/** Console menu for CohortHub. Typing and printing only; every rule is enforced by {@link Cohort}. */
public class CohortHubApp {

    private final Scanner in;
    private final PrintStream out;
    private final Cohort cohort = new Cohort();

    public CohortHubApp(InputStream input, PrintStream output) {
        this.in = new Scanner(input, StandardCharsets.UTF_8);
        this.out = output;
    }

    public static void main(String[] args) {
        new CohortHubApp(System.in, new PrintStream(System.out, true, StandardCharsets.UTF_8)).run();
    }

    public void run() {
        out.println("=== CohortHub: Learners & Study Groups ===");
        try {
            boolean running = true;
            while (running) {
                printMenu();
                String choice = prompt("Choose an option: ");
                try {
                    switch (choice) {
                        case "1" -> register();
                        case "2" -> find();
                        case "3" -> update();
                        case "4" -> remove();
                        case "5" -> showSorted();
                        case "6" -> createGroup();
                        case "7" -> addToGroup();
                        case "8" -> showLearnerGroups();
                        case "9" -> requestSupport();
                        case "10" -> attendNext();
                        case "11" -> showReport();
                        case "12" -> compareGroups();
                        case "0" -> {
                            out.println("Goodbye!");
                            running = false;
                        }
                        default -> out.println("Invalid option '" + choice + "'. Please enter a number from the menu.");
                    }
                } catch (CohortException e) {
                    out.println("Problem: " + e.getMessage());
                }
            }
        } catch (NoSuchElementException endOfInput) {
            out.println("\nNo more input. Goodbye!");
        }
    }

    private void printMenu() {
        out.println();
        out.println(" 1. Register learner          7. Add learner to a group");
        out.println(" 2. Find learner              8. Show a learner's groups");
        out.println(" 3. Update learner            9. Request mentor support");
        out.println(" 4. Remove learner           10. Attend to next support request");
        out.println(" 5. List learners (sorted)   11. Show report");
        out.println(" 6. Create group             12. Compare two groups (stretch)");
        out.println(" 0. Exit");
    }

    // ---------------------------------------------------------------- actions

    private void register() throws CohortException {
        Learner learner = cohort.register(prompt("Learner ID: "), prompt("Name: "), prompt("Email: "));
        out.println("Registered: " + learner);
    }

    private void find() throws CohortException {
        out.println(cohort.find(prompt("Learner ID: ")));
    }

    private void update() throws CohortException {
        String id = prompt("Learner ID: ");
        cohort.find(id); // fail early if the learner does not exist
        String name = prompt("New name (blank to keep): ");
        String email = prompt("New email (blank to keep): ");
        out.println("Updated: " + cohort.update(id, name, email));
    }

    private void remove() throws CohortException {
        String id = prompt("Learner ID to remove: ");
        Learner learner = cohort.find(id);
        cohort.remove(id);
        out.println("Removed " + learner.getName() + " and all their group memberships and support requests.");
    }

    private void showSorted() {
        List<Learner> learners = cohort.learnersSorted();
        if (learners.isEmpty()) {
            out.println("No learners are registered yet.");
            return;
        }
        for (Learner learner : learners) {
            out.println("  " + learner);
        }
    }

    private void createGroup() throws CohortException {
        String name = prompt("Group name: ");
        cohort.createGroup(name);
        out.println("Group '" + name + "' created.");
    }

    private void addToGroup() throws CohortException {
        String group = prompt("Group name: ");
        String id = prompt("Learner ID: ");
        if (cohort.addToGroup(group, id)) {
            out.println("Added to '" + group + "'.");
        } else {
            out.println("That learner is already in '" + group + "', so nothing was changed.");
        }
    }

    private void showLearnerGroups() throws CohortException {
        String id = prompt("Learner ID: ");
        List<String> groups = cohort.groupsOf(id);
        out.println(groups.isEmpty() ? "That learner is not in any group." : "Groups: " + String.join(", ", groups));
    }

    private void requestSupport() throws CohortException {
        cohort.requestSupport(prompt("Learner ID: "));
        out.println("Added to the mentor-support queue. Learners waiting: " + cohort.waitingCount());
    }

    private void attendNext() {
        Optional<Learner> next = cohort.attendNext();
        if (next.isPresent()) {
            out.println("Now attending to: " + next.get().getName() + " (" + next.get().getId() + ")");
        } else {
            out.println("No learners are waiting for support right now.");
        }
    }

    private void showReport() {
        out.println("Total learners:         " + cohort.totalLearners());
        out.println("Number of groups:       " + cohort.totalGroups());
        for (Map.Entry<String, Integer> entry : cohort.membershipCounts().entrySet()) {
            out.println("  " + entry.getKey() + ": " + entry.getValue() + " member(s)");
        }
        out.println("Waiting for support:    " + cohort.waitingCount());
    }

    private void compareGroups() throws CohortException {
        String a = prompt("First group: ");
        String b = prompt("Second group: ");
        out.println("In both: " + names(cohort.inBoth(a, b)));
        out.println("In one but not both: " + names(cohort.inEitherButNotBoth(a, b)));
    }

    private static String names(List<Learner> learners) {
        if (learners.isEmpty()) {
            return "(nobody)";
        }
        StringBuilder text = new StringBuilder();
        for (Learner learner : learners) {
            if (text.length() > 0) {
                text.append(", ");
            }
            text.append(learner.getName());
        }
        return text.toString();
    }

    private String prompt(String message) {
        out.print(message);
        return in.nextLine().trim();
    }
}
