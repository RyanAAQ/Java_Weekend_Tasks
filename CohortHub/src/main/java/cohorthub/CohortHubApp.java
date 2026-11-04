package cohorthub;

import cohorthub.exceptions.CohortException;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Scanner;

public class CohortHubApp {

    private final Scanner scanner;
    private final Cohort cohort = new Cohort();

    public CohortHubApp() {
        scanner = new Scanner(System.in);
    }

    public static void main(String[] args) {
        new CohortHubApp().run();
    }

    public void run() {
        System.out.println("=== CohortHub: Learners & Study Groups ===");
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
                        case "0" -> {
                            System.out.println("Goodbye!");
                            running = false;
                        }
                        default -> System.out.println("Invalid option '" + choice + "'. Please enter a number from the menu.");
                    }
                } catch (CohortException e) {
                    System.out.println("Problem: " + e.getMessage());
                }
            }
        } catch (NoSuchElementException e) {
            System.out.println("\nGoodbye!");
        }
    }

    private void printMenu() {
        System.out.println();
        System.out.println(" 1. Register learner          7. Add learner to a group");
        System.out.println(" 2. Find learner              8. Show a learner's groups");
        System.out.println(" 3. Update learner            9. Request mentor support");
        System.out.println(" 4. Remove learner           10. Attend to next support request");
        System.out.println(" 5. List learners (sorted)   11. Show report");
        System.out.println(" 6. Create group");
        System.out.println(" 0. Exit");
    }

    private void register() throws CohortException {
        Learner learner = cohort.register(prompt("Learner ID: "), prompt("Name: "), prompt("Email: "));
        System.out.println("Registered: " + learner);
    }

    private void find() throws CohortException {
        System.out.println(cohort.find(prompt("Learner ID: ")));
    }

    private void update() throws CohortException {
        String learnerId = prompt("Learner ID: ");
        cohort.find(learnerId);
        String name = prompt("New name (blank to keep): ");
        String email = prompt("New email (blank to keep): ");
        System.out.println("Updated: " + cohort.update(learnerId, name, email));
    }

    private void remove() throws CohortException {
        String learnerId = prompt("Learner ID to remove: ");
        Learner learner = cohort.find(learnerId);
        cohort.remove(learnerId);
        System.out.println("Removed " + learner.getName() + " and all their group memberships and support requests.");
    }

    private void showSorted() {
        List<Learner> learners = cohort.learnersSorted();
        if (learners.isEmpty()) {
            System.out.println("No learners are registered yet.");
            return;
        }
        for (Learner learner : learners) {
            System.out.println("  " + learner);
        }
    }

    private void createGroup() throws CohortException {
        String name = prompt("Group name: ");
        cohort.createGroup(name);
        System.out.println("Group '" + name + "' created.");
    }

    private void addToGroup() throws CohortException {
        String group = prompt("Group name: ");
        String learnerId = prompt("Learner ID: ");
        if (cohort.addToGroup(group, learnerId)) {
            System.out.println("Added to '" + group + "'.");
        } else {
            System.out.println("That learner is already in '" + group + "', so nothing was changed.");
        }
    }

    private void showLearnerGroups() throws CohortException {
        String learnerId = prompt("Learner ID: ");
        List<String> groups = cohort.groupsOf(learnerId);
        System.out.println(groups.isEmpty() ? "That learner is not in any group." : "Groups: " + String.join(", ", groups));
    }

    private void requestSupport() throws CohortException {
        cohort.requestSupport(prompt("Learner ID: "));
        System.out.println("Added to the mentor-support queue. Learners waiting: " + cohort.waitingCount());
    }

    private void attendNext() {
        Optional<Learner> next = cohort.attendNext();
        if (next.isPresent()) {
            System.out.println("Now attending to: " + next.get().getName() + " (" + next.get().getId() + ")");
        } else {
            System.out.println("No learners are waiting for support right now.");
        }
    }

    private void showReport() {
        System.out.println("Total learners:         " + cohort.totalLearners());
        System.out.println("Number of groups:       " + cohort.totalGroups());
        for (Map.Entry<String, Integer> entry : cohort.membershipCounts().entrySet()) {
            System.out.println("  " + entry.getKey() + ": " + entry.getValue() + " member(s)");
        }
        System.out.println("Waiting for support:    " + cohort.waitingCount());
    }

    private String prompt(String message) {
        System.out.print(message);
        return scanner.nextLine().trim();
    }
}
