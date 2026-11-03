# Java Weekend Projects

This repository contains four small Java console apps from the weekend challenge. Each one focuses on a different topic: exceptions, strings, file handling, and collections. They use plain Java and the standard library; JUnit 5 is used for the tests.

## The projects

- **SafeBank** (`SafeBank/`) handles deposits, withdrawals, and transfers.
- **TextLab** (`TextLab/`) analyzes and cleans text.
- **StudyVault** (`StudyVault/`) saves and manages notes.
- **CohortHub** (`CohortHub/`) manages learners, study groups, and support requests.

Each project folder has its own README with details.

## Reflection

### Which project was hardest?

CohortHub was the hardest project for me because it had several pieces to manage at once: learners, study groups, and the support queue. I had to choose the right collection for each job and make sure changes, like removing a learner, were reflected everywhere. Getting the sorting and duplicate checks right also took some thought.

### Where did I use ideas from other topics?

I used exception handling throughout the projects, especially to handle file errors in StudyVault and invalid entries in the menus. I also used strings to validate note names and collections to organize learners, notes, and search results.

### What would I improve?

With more time, I’d improve TextLab’s sentence detection. Its current rule can mistake periods in abbreviations like “Dr.” or numbers like “3.5” for the end of a sentence.
