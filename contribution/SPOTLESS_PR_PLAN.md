# Spotless PR Plan — Spock Block Formatter

## Problem

AI-generated Groovy / Spock tests (and many IDE defaults) produce code where
the content of Spock block labels is at the **same indentation level** as the
label itself:

```groovy
def "Should be able to remove from list"() {
    given:
    def list = [1, 2, 3, 4]      // ← same indent as "given:"

    when:
     list.remove(0)               // ← only 1 extra space

    then:
     list == [2, 3, 4]
}
```

The canonical Spock style (used by the official documentation and most
style guides) indents block content **one extra level**:

```groovy
def "Should be able to remove from list"() {
    given:
        def list = [1, 2, 3, 4]  // ← 4 extra spaces

    when:
        list.remove(0)

    then:
        list == [2, 3, 4]
}
```

Spotless currently offers no step that enforces this rule.

---

## Why Spotless?

Spotless already ships Groovy support (`importOrder`, `removeSemicolons`,
`greclipse`). A `spock()` step fits naturally there and can be chained with
the existing steps.

No other mainstream Java/Groovy formatter (GrEclipse, google-java-format,
Checkstyle) handles this Spock-specific structure — it requires understanding
Spock's block-label semantics, not just general indentation rules.

---

## Spotless Architecture Summary

Spotless is split into several modules:

| Module | Role |
|---|---|
| `lib/` | Pure Java formatting logic (`FormatterStep` implementations) |
| `plugin-gradle/` | Gradle DSL that exposes steps to users |
| `plugin-maven/` | Maven plugin |

A **formatter step** is a `FormatterStep` that:
1. Holds a `Serializable` `State` object (drives equality/hashcode/caching).
2. Implements `format(String rawUnix, File file) → String`.

All Groovy steps live in:
- `lib/src/main/java/com/diffplug/spotless/groovy/`
- Exposed via `plugin-gradle/…/BaseGroovyExtension.java`

---

## Files to Change in the Spotless Repository

### 1. New file — `SpockStep.java`

```
lib/src/main/java/com/diffplug/spotless/groovy/SpockStep.java
```

See full implementation in `../src/main/java/com/diffplug/spotless/groovy/SpockStep.java`.

**Key design decisions:**

- **Detection**: A method is a Spock feature method when its name is a
  string literal — `def "test name"(...)`. Regular Groovy methods cannot
  have quoted names.
- **Block labels**: Recognised by the pattern
  `(given|when|then|and|where|expect|cleanup|setup)\s*:` *at the
  expected indent level* (method body indent). This prevents false
  positives from map literals or other `when:` usages outside a Spock method.
- **Re-indentation**: For each block section the algorithm finds the
  *minimum* indentation of non-empty content lines and shifts all lines by
  `(labelIndent + indentSize) − minContentIndent`. This makes the step
  idempotent and handles variable starting indentations gracefully.
- **Brace depth tracking**: Used to find method boundaries. Handles `//`
  comments and simple string literals. Triple-quoted strings are a known
  edge case noted in the Javadoc.

### 2. Modify — `BaseGroovyExtension.java`

```
plugin-gradle/src/main/java/com/diffplug/gradle/spotless/BaseGroovyExtension.java
```

Add after the `removeSemicolons()` method:

```java
/**
 * Formats Spock block contents with one extra level of indentation
 * beyond the block label, using the default indent size of 4 spaces.
 *
 * <pre>
 * groovy {
 *     spock()
 * }
 * </pre>
 */
public void spock() {
    addStep(SpockStep.create());
}

/**
 * Formats Spock block contents with one extra level of indentation
 * beyond the block label.
 *
 * <pre>
 * groovy {
 *     spock 2   // 2-space indent
 * }
 * </pre>
 *
 * @param indentSize number of spaces per indentation level (must be >= 1)
 */
public void spock(int indentSize) {
    addStep(SpockStep.create(indentSize));
}
```

### 3. New test — `SpockStepTest.java`

```
lib/src/test/java/com/diffplug/spotless/groovy/SpockStepTest.java
```

See full test class in `../src/test/java/com/diffplug/spotless/groovy/SpockStepTest.java`.

### 4. New test resources

```
lib/src/test/resources/groovy/spock/
├── basicGivenWhenThen.dirty / .clean
├── withDescriptions.dirty / .clean
├── withAndBlock.dirty / .clean
├── withWhereTable.dirty / .clean
├── nestedCode.dirty / .clean
├── multipleMethodsInFile.dirty / .clean
└── withSetupCleanup.dirty / .clean
```

### 5. Maven plugin — `GroovyExtension.java` (Maven plugin)

```
plugin-maven/src/main/java/com/diffplug/spotless/maven/groovy/Spock.java
```

Spotless Maven has one Java class per step config:

```java
package com.diffplug.spotless.maven.groovy;

import com.diffplug.spotless.FormatterStep;
import com.diffplug.spotless.groovy.SpockStep;
import com.diffplug.spotless.maven.FormatterStepConfig;

public class Spock implements FormatterStepConfig {
    private int indentSize = 4;

    public void setIndentSize(int indentSize) {
        this.indentSize = indentSize;
    }

    @Override
    public FormatterStep newFormatterStep(SpotlessConfig spotlessConfig) {
        return SpockStep.create(indentSize);
    }
}
```

And register it in `Groovy.java` (the Maven Groovy extension).

### 6. Changelog entry

Add to `CHANGES.md`:
```
### Added
* `groovy { spock() }` — new step that indents Spock block contents
  one level beyond the block label. Handles `given`, `when`, `then`,
  `and`, `where`, `expect`, `cleanup`, `setup`, with optional
  description strings (e.g. `given: "a logged-in user"`). ([#NNNN])
```

---

## Gradle DSL Usage (end-user perspective)

```groovy
// build.gradle
spotless {
    groovy {
        // existing steps
        removeSemicolons()
        greclipse()

        // new step
        spock()          // default 4-space indent
        // or:
        spock 2          // 2-space indent for 2-space projects
    }

    // Groovy test sources often live in a separate source set
    groovyTest {
        target 'src/test/groovy/**/*.groovy'
        removeSemicolons()
        spock()
    }
}
```

---

## Algorithm in Detail

```
INPUT: rawUnix content of one .groovy file
OUTPUT: formatted content

for each line:
  if line matches SPOCK_METHOD_PATTERN:        // def "name"(...) {
    defIndent = leading whitespace
    labelIndent = defIndent + indentSize spaces
    collect all body lines until matching closing '}'
    for each block section in body:
      find minimum indent of non-empty content lines  → minIndent
      delta = (labelIndent.length + indentSize) - minIndent
      shift each non-empty content line by delta spaces
      (empty lines pass through unchanged)
  else:
    emit line unchanged
```

**Idempotency proof**: after one formatting pass, `minIndent` equals
`labelIndent.length + indentSize`, so `delta = 0` and no changes are made.

---

## Known Limitations / Future Work

| Limitation | Notes |
|---|---|
| Triple-quoted strings | Brace counting inside `"""..."""` or `'''...'''` is not handled. Rare in Spock specs. |
| Tab indentation | Currently spaces-only. Tab support follows the same pattern as `IndentStep`. |
| Groovy scripts (no class wrapper) | Top-level `def "..."()` works but is uncommon. |
| Spock `@Unroll` interaction | Formatting is purely textual — `@Unroll` has no effect. |
| Very deep nesting | Edge case if brace-counting gets confused by strings; mitigated by the string-context tracking in `countBraceDepthChange`. |

---

## Testing the Implementation Locally

```bash
# Clone this repo next to a Spotless checkout
git clone https://github.com/diffplug/spotless.git
cd spotless

# Copy the new files
cp /path/to/this/repo/src/main/java/com/diffplug/spotless/groovy/SpockStep.java \
   lib/src/main/java/com/diffplug/spotless/groovy/

cp /path/to/this/repo/src/test/java/com/diffplug/spotless/groovy/SpockStepTest.java \
   lib/src/test/java/com/diffplug/spotless/groovy/

cp /path/to/this/repo/src/test/resources/groovy/spock/* \
   lib/src/test/resources/groovy/spock/

# Apply the patch to BaseGroovyExtension.java (see section above)

# Run just the new tests
./gradlew :lib:test --tests "*SpockStep*"

# Run full Groovy test suite
./gradlew :lib:test --tests "*groovy*"
```

---

## PR Checklist

- [ ] `SpockStep.java` added to `lib/`
- [ ] `SpockStepTest.java` added to `lib/test`
- [ ] All 7 test resource pairs added
- [ ] `BaseGroovyExtension.java` updated with `spock()` / `spock(int)`
- [ ] Maven plugin `Spock.java` added
- [ ] `CHANGES.md` entry added
- [ ] All existing tests still pass (`./gradlew check`)
- [ ] New tests pass including idempotency and equality tests
- [ ] Javadoc complete on all public methods
- [ ] Apache 2.0 license header on all new files
