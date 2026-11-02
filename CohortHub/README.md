# CohortHub

CohortHub is a console app for keeping track of learners, study groups, and requests for mentor support. You can register, find, update, and remove learners; add them to groups; and attend to support requests in the order they came in. It also provides group and cohort summaries, including a comparison of learners shared by two groups.

## Keeping track of learners and groups

Learner IDs and email addresses must be unique. Email checks ignore case, and only registered learners can join a group or request support. A learner can join several groups but can have only one pending support request. Removing a learner also removes them from their groups and the queue.

The app uses a map to find learners by ID, sets to keep group membership unique, and a queue for support requests. It makes a separate list for sorted displays, so sorting the output doesn’t change the stored learners. Group names are kept in alphabetical order and compared without regard to case. Learners are sorted by name, then by ID when names match.

Learners are identified by ID in the sets. Since an ID doesn’t change when a learner’s name or email is updated, their group membership remains valid. If nobody is waiting for support, the app displays a message instead of trying to remove an item from an empty queue.

## Sample and tests

See [`sample/input.txt`](sample/input.txt) and [`sample/output.txt`](sample/output.txt) for a sample session. There are 27 JUnit 5 tests under `src/test/java/cohorthub`, covering the learner and group rules as well as the menu.

## Limits

Data is kept in memory and disappears when the app exits. Group names can’t be renamed or deleted. Email validation checks for a basic format (one `@`, a dot after it, and no spaces), but it doesn’t check every valid or invalid email format.
