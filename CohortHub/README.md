# 04. CohortHub: Learners & Study Groups

**Focus: the Java Collection Framework.** Helps a cohort coordinator manage learners, study groups and mentor support.

## What it does

- Register learners with a unique ID, a name and a unique email. Find, update and remove learners.
- Display learners sorted by name, then by ID when names match.
- Create study groups, add learners without duplicates, and show each learner's groups.
- Add learners to a mentor-support queue and attend to them first in, first out.
- Report total learners, number of groups, members per group and the number waiting for support.
- Stretch: for two groups, show the learners in **both**, and the learners in **either but not both**.

## Business rules

| Rule | Where it is enforced |
|------|----------------------|
| Learner IDs are unique and cannot be changed | `Cohort.register` rejects duplicates; `Learner.id` is `final` and `update` never touches it |
| Emails are unique, ignoring case | `Cohort.emailInUse` |
| Only registered learners can join groups or the queue | `addToGroup` and `requestSupport` look the learner up first |
| A learner can be in many groups but have only one pending request | a `Set` per group; a `contains` check on the queue |
| Removing a learner removes their group memberships and pending request | `Cohort.remove` |
| Attending to an empty queue gives a helpful message | `attendNext` returns an empty `Optional`; the menu prints a message |

## Collection choices (and why)

| Responsibility | Collection | Why it fits |
|----------------|-----------|-------------|
| Find a learner by ID | `Map<String, Learner>` (`HashMap`) | Direct lookup by ID, and keys are unique, so duplicate IDs are impossible. |
| Members of a group | `Set<Learner>` (`HashSet`), one per group, kept in a `Map` by group name | A set cannot hold the same element twice, so adding a learner twice is ignored automatically. |
| Mentor-support requests | `Queue<Learner>` (`ArrayDeque`) | First in, first out is exactly "attend in arrival order". |
| Sorted display / reports | `List<Learner>` | A list has an order. It is a **copy** made when displaying, so sorting it cannot affect the map. |
| Group names | `TreeMap` with `String.CASE_INSENSITIVE_ORDER` | Keeps groups in alphabetical order and treats `Java` and `JAVA` as the same group. |

- **Generics** are used everywhere (`Map<String, Set<Learner>>`, `Queue<Learner>`, `Optional<Learner>`).
- **A `Comparator`** (`Learner.BY_NAME_THEN_ID`) sorts by name ignoring case, then by ID.
- **`equals()` and `hashCode()` use the ID only**, and the ID is `final`. A learner's hash code therefore never changes while it sits in a `HashSet`, so updating a learner's name or email cannot break group membership. There is a test for exactly this.
- **Data is kept in memory.** Persistence is optional in the brief.

## Acceptance checklist

| Check | Input | Expected | Actual |
|-------|-------|----------|--------|
| Duplicate learner ID | register `L1` twice | Rejected | `A learner with ID 'L1' is already registered.` Passed |
| Duplicate email (different case) | register `ZAINAB@mail.com` | Rejected | `The email 'ZAINAB@mail.com' is already registered.` Passed |
| Sorted by name, then ID | learners Zainab Bello, Ade Smith, Ade Jones, then option `5` | Ade Jones, Ade Smith, Zainab Bello (see note below) | `L3 Ade Jones`, `L2 Ade Smith`, `L1 Zainab Bello` Passed |
| Same learner added to a group twice | add `L1` to `Java` twice | No duplicate | `That learner is already in 'Java', so nothing was changed.` Passed |
| Support requests in arrival order | requests: L2, then L1 | L2 attended first | `Now attending to: Ade Smith (L2)`, then `Zainab Bello (L1)` Passed |
| One pending request per learner | request `L1` twice | Second rejected | `Zainab Bello already has a pending support request.` Passed |
| Empty queue | attend with nobody waiting | Helpful message | `No learners are waiting for support right now.` Passed |
| Removal leaves nothing stale | remove `L2` (in 2 groups) and then report | No memberships or queue entries | report shows Algorithms 1, Java 1, waiting 0. Passed |
| Sorting does not corrupt lookup | (unit test) | Clearing or sorting the display list does not affect `find` | Passed |
| Stretch: set operations | compare `Java` and `Algorithms` | In both: Ade Smith. In one but not both: Ade Jones, Zainab Bello | exactly that. Passed |

*Sorting note:* names are compared ignoring case, and the ID decides only when two names are identical. In the sample, `Ade Jones` comes before `Ade Smith` because the **full names** differ. The ID tie-break is covered by a unit test with two learners who are both named exactly `Ade`.

The full console session is in [`sample/output.txt`](sample/output.txt) and the typed input in [`sample/input.txt`](sample/input.txt).

## Tests

27 JUnit 5 tests in `src/test/java/cohorthub`: `CohortTest` (every rule) and `CohortHubAppTest` (the menu).

## Stretch goal: completed

Menu option `12` uses set operations (`retainAll` for "in both", and a union minus the intersection for "in either but not both").

## Known limitations

- Everything is stored in memory and is lost when the program exits.
- Group names are unique ignoring case, and cannot be renamed or deleted in this version.
- The email check is a simple format check (one `@`, a dot after it, no spaces), not full validation.
