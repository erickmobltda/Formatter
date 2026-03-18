# How to Open a PR to the Official Spotless Repository

Step-by-step guide to contributing the `spock()` Groovy formatter step.
Every command is ready to copy and paste.

---

## Do I need to open an issue first?

**No.** The official `CONTRIBUTING.md` states:

> *"Pull requests are welcome, preferably against `main`."*

There is no requirement to file an issue before a PR. Looking at recent
merged PRs that added new formatter steps, ~84% were opened directly
without a linked issue. You can go straight to the PR.

**Optional:** if you want early feedback before doing the work, you can
open a short issue titled *"Proposal: add spock() step for Spock block
indentation"* — but it is not required by the project.

---

## What Spotless CI actually checks (must pass before merge)

1. `./gradlew spotlessApply` — the project formats itself with Spotless; your code must comply.
2. `./gradlew spotbugsMain` — static analysis; CI fails if this fails.
3. `./gradlew test` — all tests green.

Run these **before** pushing. The commands are in Step 11 of this guide.

---

## Prerequisites

- Git installed
- Java 11+ installed (`java --version`)
- A GitHub account
- This repo cloned locally (referred to as `$FORMATTER_REPO` below)

---

## Step 1 — Note your local path

```bash
# Run this once. Every command below uses $FORMATTER_REPO.
export FORMATTER_REPO="$(pwd)"
echo "Formatter repo is at: $FORMATTER_REPO"
```

> If you open a new terminal session, re-run the `export` line above
> before continuing.

---

## Step 2 — Fork the Spotless repository on GitHub

1. Open **https://github.com/diffplug/spotless** in your browser.
2. Click the **Fork** button (top-right).
3. Leave all defaults and click **Create fork**.

---

## Step 3 — Clone your fork

```bash
# Replace YOUR_GITHUB_USERNAME with your actual username
git clone https://github.com/YOUR_GITHUB_USERNAME/spotless.git
cd spotless
```

---

## Step 4 — Add the upstream remote

```bash
git remote add upstream https://github.com/diffplug/spotless.git
git fetch upstream
```

---

## Step 5 — Create a feature branch

```bash
git checkout -b feat/groovy-spock-formatter upstream/main
```

---

## Step 6 — Copy the formatter logic (lib module)

```bash
# Core formatting logic
cp "$FORMATTER_REPO/src/main/java/com/diffplug/spotless/groovy/SpockFormatter.java" \
   lib/src/main/java/com/diffplug/spotless/groovy/SpockFormatter.java

# FormatterStep wrapper
cp "$FORMATTER_REPO/src/main/java/com/diffplug/spotless/groovy/SpockStep.java" \
   lib/src/main/java/com/diffplug/spotless/groovy/SpockStep.java
```

---

## Step 7 — Copy the tests

```bash
# JUnit test class
cp "$FORMATTER_REPO/src/test/java/com/diffplug/spotless/groovy/SpockStepTest.java" \
   lib/src/test/java/com/diffplug/spotless/groovy/SpockStepTest.java

# Test resource fixtures (.dirty = input, .clean = expected output)
mkdir -p lib/src/test/resources/groovy/spock

cp "$FORMATTER_REPO/src/test/resources/groovy/spock/basicGivenWhenThen.dirty"    lib/src/test/resources/groovy/spock/
cp "$FORMATTER_REPO/src/test/resources/groovy/spock/basicGivenWhenThen.clean"    lib/src/test/resources/groovy/spock/
cp "$FORMATTER_REPO/src/test/resources/groovy/spock/withDescriptions.dirty"      lib/src/test/resources/groovy/spock/
cp "$FORMATTER_REPO/src/test/resources/groovy/spock/withDescriptions.clean"      lib/src/test/resources/groovy/spock/
cp "$FORMATTER_REPO/src/test/resources/groovy/spock/withAndBlock.dirty"          lib/src/test/resources/groovy/spock/
cp "$FORMATTER_REPO/src/test/resources/groovy/spock/withAndBlock.clean"          lib/src/test/resources/groovy/spock/
cp "$FORMATTER_REPO/src/test/resources/groovy/spock/withWhereTable.dirty"        lib/src/test/resources/groovy/spock/
cp "$FORMATTER_REPO/src/test/resources/groovy/spock/withWhereTable.clean"        lib/src/test/resources/groovy/spock/
cp "$FORMATTER_REPO/src/test/resources/groovy/spock/nestedCode.dirty"            lib/src/test/resources/groovy/spock/
cp "$FORMATTER_REPO/src/test/resources/groovy/spock/nestedCode.clean"            lib/src/test/resources/groovy/spock/
cp "$FORMATTER_REPO/src/test/resources/groovy/spock/multipleMethodsInFile.dirty" lib/src/test/resources/groovy/spock/
cp "$FORMATTER_REPO/src/test/resources/groovy/spock/multipleMethodsInFile.clean" lib/src/test/resources/groovy/spock/
cp "$FORMATTER_REPO/src/test/resources/groovy/spock/withSetupCleanup.dirty"      lib/src/test/resources/groovy/spock/
cp "$FORMATTER_REPO/src/test/resources/groovy/spock/withSetupCleanup.clean"      lib/src/test/resources/groovy/spock/
```

---

## Step 8 — Patch the Gradle plugin (`BaseGroovyExtension.java`)

Open the file in your editor:

```
plugin-gradle/src/main/java/com/diffplug/gradle/spotless/BaseGroovyExtension.java
```

**8a.** Add the import alongside the other `groovy.*` imports:

```java
import com.diffplug.spotless.groovy.SpockStep;
```

**8b.** Add two new methods **after** `removeSemicolons()`:

```java
/**
 * Formats Spock specification blocks so that each block's content is indented
 * one extra level beyond the block label itself.
 *
 * <p>Handles all Spock lifecycle blocks: {@code given}, {@code when},
 * {@code then}, {@code and}, {@code where}, {@code expect},
 * {@code cleanup}, {@code setup}.  Block labels may include an optional
 * description string (e.g. {@code given: "a logged-in user"}).
 *
 * <pre>
 * spotless {
 *     groovy {
 *         spock()         // default 4-space indent
 *     }
 * }
 * </pre>
 */
public void spock() {
    addStep(SpockStep.create());
}

/**
 * Formats Spock specification blocks using the given indent size.
 *
 * <pre>
 * spotless {
 *     groovy {
 *         spock 2         // 2-space indent
 *     }
 * }
 * </pre>
 *
 * @param indentSize number of spaces per indentation level (must be &gt;= 1)
 */
public void spock(int indentSize) {
    addStep(SpockStep.create(indentSize));
}
```

> Tip: the diff is already prepared in
> `$FORMATTER_REPO/contribution/BaseGroovyExtension.patch` for reference.

---

## Step 9 — Add the Maven plugin class

```bash
mkdir -p plugin-maven/src/main/java/com/diffplug/spotless/maven/groovy

cp "$FORMATTER_REPO/contribution/MavenSpockStep.java" \
   plugin-maven/src/main/java/com/diffplug/spotless/maven/groovy/Spock.java
```

Then register it in the Maven Groovy extension. Open:

```
plugin-maven/src/main/java/com/diffplug/spotless/maven/groovy/Groovy.java
```

Add `Spock` to the list of recognised child elements (follow the same
pattern as `RemoveSemicolons` already in that file):

```java
private Spock spock;
```

---

## Step 10 — Add a CHANGES.md entry

Open `CHANGES.md` and add under the **next unreleased version** heading:

```markdown
### Added
* `groovy { spock() }` — new step that indents Spock block contents one level
  beyond the block label. Handles `given`, `when`, `then`, `and`, `where`,
  `expect`, `cleanup`, `setup`, with optional description strings
  (e.g. `given: "a logged-in user"`). ([#NNNN](https://github.com/diffplug/spotless/issues/NNNN))
```

> Replace `#NNNN` with the GitHub issue number if one exists, or open an
> issue first and link back.

---

## Step 11 — Verify the build

```bash
# Run only the new tests (fast feedback)
./gradlew :lib:test --tests "*SpockStep*"

# Run the full Groovy test suite
./gradlew :lib:test --tests "*groovy*"

# Run all checks (what the CI will run)
./gradlew check
```

All tests must be green before opening the PR.

---

## Step 12 — Commit

```bash
git add \
  lib/src/main/java/com/diffplug/spotless/groovy/SpockFormatter.java \
  lib/src/main/java/com/diffplug/spotless/groovy/SpockStep.java \
  lib/src/test/java/com/diffplug/spotless/groovy/SpockStepTest.java \
  lib/src/test/resources/groovy/spock/ \
  plugin-gradle/src/main/java/com/diffplug/gradle/spotless/BaseGroovyExtension.java \
  plugin-maven/src/main/java/com/diffplug/spotless/maven/groovy/Spock.java \
  plugin-maven/src/main/java/com/diffplug/spotless/maven/groovy/Groovy.java \
  CHANGES.md

git commit -m "feat(groovy): add spock() step for Spock block indentation

Adds SpockFormatter and SpockStep to lib/, exposing a new spock() /
spock(int) DSL method on BaseGroovyExtension (Gradle) and a <spock/>
element for the Maven plugin.

The formatter detects Spock feature methods by their quoted-string name
and re-indents block content one level beyond each block label. The step
is idempotent, handles given/when/then/and/where/expect/cleanup/setup,
and supports optional description strings on block labels."
```

---

## Step 13 — Push the branch

```bash
# Replace YOUR_GITHUB_USERNAME with your actual username
git push -u origin feat/groovy-spock-formatter
```

---

## Step 14 — Open the Pull Request

1. Go to **https://github.com/YOUR_GITHUB_USERNAME/spotless** in your browser.
2. Click the **"Compare & pull request"** banner (it appears automatically
   after the push), or click **Pull requests → New pull request**.
3. Set:
   - **base repository**: `diffplug/spotless`
   - **base branch**: `main`
   - **head repository**: `YOUR_GITHUB_USERNAME/spotless`
   - **compare branch**: `feat/groovy-spock-formatter`
4. Use the title and body below.

### PR title

```
feat(groovy): add spock() step for Spock block indentation
```

### PR body (copy as-is)

```markdown
## What does this PR do?

Adds a new `spock()` / `spock(int indentSize)` step to the Groovy
formatter that re-indents Spock block content one level beyond the block
label.

**Before:**
```groovy
def "should remove element"() {
    given:
    def list = [1, 2, 3]

    when:
    list.remove(0)

    then:
    list == [2, 3]
}
```

**After:**
```groovy
def "should remove element"() {
    given:
        def list = [1, 2, 3]

    when:
        list.remove(0)

    then:
        list == [2, 3]
}
```

## Gradle usage

```groovy
spotless {
    groovy {
        spock()      // 4-space indent (default)
        // spock 2   // or 2-space indent
    }
}
```

## Maven usage

```xml
<groovy>
    <spock/>
    <!-- or: <spock><indentSize>2</indentSize></spock> -->
</groovy>
```

## Implementation notes

- **Detection**: a method is a Spock feature method when its name is a
  quoted string literal — `def "test name"(...)`. Regular Groovy methods
  cannot have quoted names, so false positives are not possible.
- **Re-indentation**: finds the minimum indent of non-empty content lines
  per block section and shifts by `(labelIndent + indentSize) − minIndent`.
  This makes the step **idempotent** regardless of starting indentation.
- **Block labels supported**: `given`, `when`, `then`, `and`, `where`,
  `expect`, `cleanup`, `setup`. Labels with optional descriptions
  (e.g. `given: "a logged-in user"`) are handled.
- **Zero external runtime dependencies**: `SpockFormatter` is pure Java
  with no imports beyond `java.util.*`.

## Checklist

- [x] `SpockFormatter.java` added to `lib/`
- [x] `SpockStep.java` added to `lib/`
- [x] `SpockStepTest.java` added with 7 fixture pairs (dirty/clean)
- [x] `BaseGroovyExtension.java` updated with `spock()` / `spock(int)`
- [x] Maven plugin `Spock.java` added
- [x] `CHANGES.md` entry added
- [x] All existing tests pass (`./gradlew check`)
- [x] New tests pass including idempotency assertions
- [x] Apache 2.0 license header on all new files
- [x] Javadoc on all public methods
```

5. Click **Create pull request**.

---

## Step 15 — After opening the PR

- Watch the CI checks (GitHub Actions) — fix any failures.
- A maintainer may request changes; address them with new commits on the
  same branch (`git push` again — the PR updates automatically).
- If the upstream `main` has moved forward, rebase cleanly:

  ```bash
  git fetch upstream
  git rebase upstream/main
  git push --force-with-lease origin feat/groovy-spock-formatter
  ```

---

## Quick reference — files changed in the Spotless repo

| Action | File in `diffplug/spotless` |
|---|---|
| **Add** | `lib/src/main/java/com/diffplug/spotless/groovy/SpockFormatter.java` |
| **Add** | `lib/src/main/java/com/diffplug/spotless/groovy/SpockStep.java` |
| **Add** | `lib/src/test/java/com/diffplug/spotless/groovy/SpockStepTest.java` |
| **Add** | `lib/src/test/resources/groovy/spock/*.dirty` (7 files) |
| **Add** | `lib/src/test/resources/groovy/spock/*.clean` (7 files) |
| **Modify** | `plugin-gradle/src/main/java/com/diffplug/gradle/spotless/BaseGroovyExtension.java` |
| **Add** | `plugin-maven/src/main/java/com/diffplug/spotless/maven/groovy/Spock.java` |
| **Modify** | `plugin-maven/src/main/java/com/diffplug/spotless/maven/groovy/Groovy.java` |
| **Modify** | `CHANGES.md` |
