# TextLab

TextLab is a console app for inspecting and editing a passage. It counts characters, words, and sentences; cleans up whitespace; searches for and replaces whole words without case sensitivity; checks palindromes; and finds the longest words. The original passage stays available after edits. The app can also show the first position where the edited text differs from the original.

## How it treats text

A word is a run of letters or digits, so punctuation separates words. A run of `.`, `!`, or `?` marks one sentence boundary. If the text ends without punctuation, its final fragment still counts as a sentence. A fragment with no letters or digits does not count, so `!!! ... ???` has zero sentences.

Searches match whole words: searching for `Java` won’t match `JavaScript`. Hyphens and apostrophes count as punctuation, so `java-script` is treated as two words and `don't` as `don` and `t`. Longest words are listed once, ignoring case, with the first spelling kept. A passage containing only punctuation is not considered a palindrome. The first-difference position starts at zero; if one passage ends while the rest matches, the position is the length of the shorter passage.

The sentence rule is deliberately simple. At the moment, it treats periods in abbreviations such as `Dr.` and decimals such as `3.5` as sentence boundaries.

## Implementation and sample

`TextProcessor` contains the text logic, separate from the console menu in `TextLabApp`. The original passage is kept separately from the edited one. The app reads a passage until you enter `END` on a line by itself.

See [`sample/input.txt`](sample/input.txt) and [`sample/output.txt`](sample/output.txt) for a sample session. There are 26 JUnit 5 tests under `src/test/java/textlab` covering the text rules and menu.

## Limits

Sentence detection does not account for abbreviations or decimal numbers yet. Apostrophes and hyphens split words, as described above. Text is entered in the console and ends with a line containing only `END`.
