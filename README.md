# Java Weekend Project Challenge

**Name:** Quadri Ariyo
**Programme:** Semicolon Africa, Software Engineering

Four console projects, one for each topic of the weekend: exception handling, strings, file I/O and the Java Collection Framework. Everything uses plain Java and the standard library. JUnit 5 is used for tests only.

| # | Project | Focus | Folder | Tests |
|---|---------|-------|--------|-------|
| 1 | SafeBank Transaction Simulator | Exception handling | [`SafeBank`](SafeBank) | 23 |
| 2 | TextLab, Text Analysis & Cleanup | Strings | [`TextLab`](TextLab) | 26 |
| 3 | StudyVault, File-Based Notes | File handling and I/O streams | [`StudyVault`](StudyVault) | 29 |
| 4 | CohortHub, Learners & Study Groups | Java Collection Framework | [`CohortHub`](CohortHub) | 27 |

Each folder has its own README with what the project does, the rules it enforces, the design decisions, the acceptance checklist (expected vs actual), known limitations and the stretch challenge. Each folder also has a `sample/` folder with the exact input I typed (`input.txt`) and the console output it produced (`output.txt`).

## How to run

You need Java 17 or newer and Maven.

**In IntelliJ IDEA:** open this folder, let Maven import the project, then run the `main` method of `SafeBankApp`, `TextLabApp`, `StudyVaultApp` or `CohortHubApp`. To run tests, right-click a project's `src/test/java` folder and choose *Run Tests*.

**From a terminal:**

```bash
mvn test                                   # compile and run all 105 tests
mvn compile                                # compile only
java -cp SafeBank/target/classes safebank.SafeBankApp
java -cp TextLab/target/classes textlab.TextLabApp
java -cp StudyVault/target/classes studyvault.StudyVaultApp
java -cp CohortHub/target/classes cohorthub.CohortHubApp
```

The apps print the ₦ sign. If it shows as `?` in a Windows terminal, run `chcp 65001` first or run the apps from IntelliJ.

StudyVault saves its files in a `data/` folder next to where you start it (`data/notes` and `data/backups`). That folder is ignored by git.

## Test results

All tests were run and passed before submission:

| Project | Tests run | Passed | Failed |
|---------|-----------|--------|--------|
| SafeBank | 23 | 23 | 0 |
| TextLab | 26 | 26 | 0 |
| StudyVault | 29 | 29 | 0 |
| CohortHub | 27 | 27 | 0 |

## Reflection

### 1. Which project was most challenging, and why?

> CohortHub was the hardest project for me because it had several pieces to manage at once: learners, study groups and the support queue. I had to choose the right collection for each job and make sure changes, like removing a learner, were reflected everywhere. Getting the sorting and duplicate checks right also took some thought.

### 2. Where did you use concepts from another topic?

- **Exception handling inside every project.** StudyVault turns file problems (`IOException`) into its own `NoteException` types so the menu can show friendly messages. CohortHub and TextLab never let a bad entry crash the menu loop.
- **Strings in StudyVault and CohortHub.** StudyVault validates note names with string checks (blank names, `..`, slashes) so a name cannot escape the notes folder. CohortHub compares emails ignoring case.
- **Collections in StudyVault and TextLab.** StudyVault returns sorted `List`s of titles and search hits. TextLab uses a `LinkedHashMap` to remove duplicate longest words while keeping their order.
- **StringBuilder in TextLab** builds the cleaned and replaced text one piece at a time.

> Adjust this list so it matches how *you* remember using these ideas.

### 3. What would you improve with more time?

> With more time, I would improve TextLab's sentence detection. The current rule can mistake periods in abbreviations like "Dr." or numbers like "3.5" for the end of a sentence.
# Java_Weekend_Tasks
