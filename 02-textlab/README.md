# 02. TextLab: Text Analysis & Cleanup

**Focus: strings.** A tool that helps a learning support team inspect and clean written submissions.

## What it does

- Show character counts (with and without whitespace), word count and sentence count.
- Clean whitespace: trim the ends and collapse repeated spaces, tabs and line breaks into one space.
- Search for a **whole word** and report how many times it appears, ignoring case.
- Replace every whole-word occurrence of a word, ignoring case.
- Check whether text is a palindrome, ignoring case, spaces and punctuation.
- Show the longest word(s) without duplicates.
- Keep the **original passage** untouched so it can be compared with the edited one.
- Stretch: find the first position where the original and edited passages differ.

## Processing rules (as given, plus my assumptions)

- A **word** is a run of letters or digits. Punctuation separates words.
- One or more consecutive `.`, `!` or `?` form **one** sentence boundary.
- A final fragment without closing punctuation counts as a sentence.
- A whole-word search for `Java` does **not** match `JavaScript`.
- Empty or whitespace-only input never crashes the program.

Additional assumptions (the brief asks for these to be stated):

1. A fragment only counts as a sentence if it contains at least one letter or digit. So `"!!! ... ???"` has 0 sentences.
2. A hyphen is punctuation, so `java-script` is the two words `java` and `script`. A search for `Java` therefore matches the `java` in `java-script`.
3. An apostrophe is also punctuation, so `don't` counts as the two words `don` and `t`.
4. "Longest words" reports each distinct word once (ignoring case), using the spelling that appears first.
5. The palindrome check needs at least one letter or digit, so text made only of punctuation is not a palindrome.
6. The first-difference position is counted from 0. If one passage is a prefix of the other, the position is the length of the shorter one.

## Design decisions

- **Logic and console are separate.** `TextProcessor` has no `Scanner` and no `System.out`, so every rule is tested without the menu. `TextLabApp` only reads and prints.
- **`StringBuilder` is used** in `clean`, `replaceWord`, and the palindrome check, because each builds text one piece at a time.
- **No regular-expression or third-party library is needed.** Words and sentences are found by walking through the characters once, which makes the rules easy to explain.
- **The original is a separate field.** Strings are immutable, so cleaning or replacing creates a new string and the original is never altered.

## Acceptance checklist

| Check | Input | Expected | Actual |
|-------|-------|----------|--------|
| Repeated spaces, tabs, line breaks | passage with extra spaces, a tab and a line break, then option `3` | One space between words, no leading or trailing whitespace | `I like Java and JavaScript. Java is fun!! JAVA rocks... java too` Passed |
| Counts | option `2` | Characters, no-whitespace characters, 12 words, 4 sentences | 72 / 53 / 12 / 4 (before cleaning) and 64 / 53 / 12 / 4 (after). Passed |
| Mixed case `Java`, `JAVA`, `java` | search `java` in a passage with `Java` (twice), `JAVA`, `java` and `JavaScript` | 4 matches, and `JavaScript` is not counted | `"java" appears 4 times` Passed |
| Java vs JavaScript | search in a passage containing both | `JavaScript` is not counted as `Java` | Passed (unit tests) |
| Replace whole words | replace `java` with `Kotlin` | 4 replaced, `JavaScript` unchanged | `Replaced 4 occurrences.` and `JavaScript` stays. Passed |
| Original preserved | option `8` | Original and current both shown | both shown, original unchanged. Passed |
| Palindrome | `A man, a plan, a canal: Panama!` | Yes | `Yes, it is a palindrome` Passed |
| Punctuation but no words | `!!! ... ???` | 0 words, 0 sentences, no crash | Passed (unit tests) |
| Empty or blank passage | blank line, then `2` and `7` | Zeros and a message, no crash | `Words: 0`, `Sentences: 0`, `There are no words in the passage yet.` Passed |
| Longest words | option `7` | `JavaScript` (10 characters) | `JavaScript` Passed |
| Invalid menu choice | `99` | Helpful message | `Invalid option '99'...` Passed |

The full console session is in [`sample/output.txt`](sample/output.txt) and the typed input in [`sample/input.txt`](sample/input.txt).

## Tests

26 JUnit 5 tests in `src/test/java/textlab`: `TextProcessorTest` (every rule, including all the cases from the brief) and `TextLabAppTest` (the menu).

## Stretch goal: completed

Option `9` compares the original and the current passage and reports the first position where they differ (`TextProcessor.firstDifference`).

## Known limitations

- Sentence detection is rule-based, so abbreviations like `Dr.` or decimals like `3.5` are treated as sentence boundaries.
- Words with apostrophes or hyphens are split (see assumptions 2 and 3).
- The passage is typed or pasted in the console and ends with a line containing only `END`.
