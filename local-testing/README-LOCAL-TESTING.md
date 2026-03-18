# Testing SpockFormatter locally in your existing projects

Two approaches — pick the one that fits your workflow.

---

## Approach A — Spotless `custom {}` step via `buildSrc` ✅ Recommended

This integrates the formatter directly into your Spotless pipeline.
No JAR publishing, no extra tooling.

### Step 1 — Copy one file into your project

```
your-project/
└── buildSrc/
    ├── build.gradle                        ← create this (2 lines)
    └── src/main/java/com/diffplug/spotless/groovy/
        └── SpockFormatter.java             ← copy this from the formatter repo
```

Create `buildSrc/build.gradle`:
```groovy
plugins { id 'java' }
```

Copy `SpockFormatter.java`:
```bash
# From the Formatter repo root:
mkdir -p your-project/buildSrc/src/main/java/com/diffplug/spotless/groovy
cp src/main/java/com/diffplug/spotless/groovy/SpockFormatter.java \
   your-project/buildSrc/src/main/java/com/diffplug/spotless/groovy/
```

### Step 2 — Add the custom step to your `build.gradle`

```groovy
spotless {
    groovy {
        // your existing steps...

        custom('spockFormat') { String content ->
            com.diffplug.spotless.groovy.SpockFormatter.format(content)
        }

        // target only spec files (optional):
        target 'src/test/groovy/**/*Spec.groovy'
    }
}
```

### Step 3 — Run it

```bash
# Check which files would change:
./gradlew spotlessCheck

# Apply formatting:
./gradlew spotlessApply

# Apply only the Groovy formatter:
./gradlew spotlessGroovyApply
```

That's it. Every time you run `spotlessApply` the Spock blocks will be
formatted consistently alongside your other Spotless rules.

---

## Approach B — Fat JAR CLI tool

Use this when you want to format files outside Gradle, in CI, or in
editors/scripts.

### Step 1 — Build the fat JAR

From the Formatter repo root:

```bash
./gradlew shadowJar
# Produces: build/libs/spock-formatter-0.1.0-SNAPSHOT.jar
```

### Step 2 — Format files

```bash
JAR=path/to/spock-formatter-0.1.0-SNAPSHOT.jar

# Format one file in-place:
java -jar $JAR src/test/groovy/MySpec.groovy

# Format all spec files recursively:
find src/test/groovy -name '*Spec.groovy' | xargs java -jar $JAR

# Check mode (exits 1 if any file would change — useful in CI):
find src/test/groovy -name '*Spec.groovy' | xargs java -jar $JAR --check

# 2-space indent:
java -jar $JAR --indent 2 src/test/groovy/MySpec.groovy

# Preview what would change (prints to stdout, no file modification):
java -jar $JAR --stdout src/test/groovy/MySpec.groovy | diff src/test/groovy/MySpec.groovy -
```

### Shell alias (optional convenience)

Add to your `.bashrc` / `.zshrc`:

```bash
alias spockfmt='java -jar /absolute/path/to/spock-formatter-0.1.0-SNAPSHOT.jar'

# Then use as:
spockfmt src/test/groovy/MySpec.groovy
spockfmt --check src/test/groovy/**/*Spec.groovy
```

---

## Approach C — `mavenLocal()` (if you prefer a proper dependency)

```bash
# Publish to ~/.m2 from the Formatter repo:
./gradlew publishToMavenLocal
```

Then in the **target project's** `build.gradle`:

```groovy
buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
    }
    dependencies {
        classpath 'com.diffplug.spotless:spotless-spock-formatter:0.1.0-SNAPSHOT'
    }
}

spotless {
    groovy {
        custom('spockFormat') { content ->
            com.diffplug.spotless.groovy.SpockFormatter.format(content)
        }
    }
}
```

---

## Approach D — IntelliJ / IDE File Watcher

Use the fat JAR with IntelliJ's **File Watchers** plugin:

1. Install the *File Watchers* plugin (Settings → Plugins).
2. Settings → Tools → File Watchers → `+` → `<custom>`.
3. Configure:
   - **File type**: Groovy
   - **Scope**: Project files matching `*Spec.groovy`
   - **Program**: `java`
   - **Arguments**: `-jar /path/to/spock-formatter.jar $FilePath$`
   - **Working directory**: `$ProjectFileDir$`
4. Save. The formatter runs automatically on save.

---

## Verifying the formatter is working

Run this one-liner to see the before/after on a real file:

```bash
# With CLI:
java -jar spock-formatter.jar --stdout MySpec.groovy

# With Spotless:
./gradlew spotlessGroovyCheck 2>&1 | grep -A5 "WOULD CHANGE"
```
