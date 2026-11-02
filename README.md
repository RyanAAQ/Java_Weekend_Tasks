# Java Weekend Projects

**Quadri Ariyo** · Software Engineering, Semicolon Africa

This repository contains four small Java console apps from the weekend challenge. Each one focuses on a different topic: exceptions, strings, file handling, and collections. They use plain Java and the standard library; JUnit 5 is used for the tests.

## The projects

- **SafeBank** (`SafeBank/`) is a transaction simulator built around exception handling. It has 23 tests.
- **TextLab** (`TextLab/`) cleans and analyzes text. It has 26 tests.
- **StudyVault** (`StudyVault/`) saves and manages notes using file I/O. It has 29 tests.
- **CohortHub** (`CohortHub/`) manages learners, study groups, and a mentor-support queue with Java collections. It has 27 tests.

Each project folder has its own README with more detail, and a `sample/` folder containing the input and output from a sample run.

## Running the apps

You’ll need Java 17 or newer and Maven. To open the project in IntelliJ IDEA, open this folder and let Maven import it. You can run an app from its `main` method, or run a project’s tests from its `src/test/java` folder.

From a terminal, run all four projects’ tests with:

```bash
mvn test
```

To compile without running tests:

```bash
mvn compile
```

To start an app after compiling, run the command for it from the repository root:

```bash
java -cp SafeBank/target/classes safebank.SafeBankApp
java -cp TextLab/target/classes textlab.TextLabApp
java -cp StudyVault/target/classes studyvault.StudyVaultApp
java -cp CohortHub/target/classes cohorthub.CohortHubApp
```

The apps display the naira sign (₦). If it appears as `?` in a Windows terminal, run `chcp 65001` first, or launch the app from IntelliJ.

StudyVault creates `data/notes` and `data/backups` relative to the directory where you start it. The `data/` folder is ignored by Git.

## Test results

The four projects have 105 tests in total. All passed before submission: SafeBank (23), TextLab (26), StudyVault (29), and CohortHub (27).

## Reflection

### Which project was hardest?

CohortHub was the hardest project for me because it had several pieces to manage at once: learners, study groups, and the support queue. I had to choose the right collection for each job and make sure changes, like removing a learner, were reflected everywhere. Getting the sorting and duplicate checks right also took some thought.

### Where did I use ideas from other topics?

I used exception handling across the projects. StudyVault handles file errors with its own `NoteException` types so the menu can show a useful message. CohortHub and TextLab also handle invalid entries without stopping the menu.

Some of the other topics overlapped too. StudyVault checks note names so they can’t point outside the notes folder, and CohortHub compares email addresses without regard to case. StudyVault uses sorted lists for titles and search results; TextLab uses a `LinkedHashMap` to keep unique longest words in order. TextLab also uses `StringBuilder` to construct cleaned and replaced text.

### What would I improve?

With more time, I’d improve TextLab’s sentence detection. Its current rule can mistake periods in abbreviations like “Dr.” or numbers like “3.5” for the end of a sentence.
