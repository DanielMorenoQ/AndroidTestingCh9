# Course Script: Continuous Integration (Class 4, 2.5 hours)

**Project:** `SDGKUAndroidTestingCh8`  
**Package:** `com.example.sdgkuandroidtestingch8`  
**Builds on:** JUnit, Compose/Espresso UI tests, Robolectric, and Class 3's DI and Mockito exercises.  
**CI platform:** GitHub Actions.

This is a live-coding script, not an installed pipeline. Create the workflow during class.
No app distribution, release signing, paid service, or production credentials are needed.

**Outcome:** A pull request automatically runs local tests, Android lint, and a debug build.
Students diagnose a deliberately failing test, repair it, inspect reports, and configure a
required check. An optional emulator job runs the device UI tests.

## Timeline

| Time      | Block       | Activity                                            |
| --------- | ----------- | --------------------------------------------------- |
| 0:00–0:10 | Welcome     | Recap Class 3 and verify the local baseline         |
| 0:10–0:25 | Theory      | CI, jobs, runners, feedback, and merge gates        |
| 0:25–0:50 | Live demo   | Add the workflow and open the first pull request    |
| 0:50–1:00 | Break       | Let the first remote build finish                   |
| 1:00–1:30 | Exercise 1  | Introduce a failure, inspect reports, and repair it |
| 1:30–2:00 | Exercise 2  | Require the check and explore device testing        |
| 2:00–2:20 | Reliability | Caches, security, flaky tests, and troubleshooting  |
| 2:20–2:30 | Wrap-up     | Demonstrate acceptance criteria and assign homework |

Total: **150 minutes**. The emulator extension is included in Exercise 2, not extra time.
If hosted runs are slow, use the instructor's prepared run for discussion while student
runs finish. Do not remove tests or bypass checks to fit the schedule.

## Instructor Preparation

1. Use a GitHub repository with Actions enabled and a default branch called `main`.
   Adapt the branch filters below if its name differs. Students need permission to push
   branches and open pull requests; the instructor needs repository administration access
   for rulesets. Check Actions quotas and branch-rule availability for the repository plan.
2. Ensure the project and Gradle wrapper are committed, including the wrapper JAR and
   properties. Do not commit `local.properties`, SDK paths, signing keys, or credentials.
3. Sync and run the local baseline below before class. Resolve existing failures before
   presenting CI as a new variable. Prepare a successful remote run as a fallback.
4. Confirm the baseline branch actually contains the tests you intend to demonstrate.
   Class 3's Markdown does not create its example classes. Use an existing validator test
   for the failure exercise; use `LoginServiceTest` only if students implemented it.
5. Confirm the Android SDK repository offers `platforms;android-37` and that the project's
   pinned AGP and dependencies resolve from a clean machine. The workflow deliberately
   matches the current project; do not silently lower `compileSdk` to make CI pass.

### Toolchain facts for this workspace

| Setting                          | Current value    | CI implication                           |
| -------------------------------- | ---------------- | ---------------------------------------- |
| Compile/target SDK               | 37               | Install Android platform 37              |
| Gradle daemon JVM criteria       | JDK 25           | Provision JDK 25                         |
| Java source/target compatibility | 11               | This is not the JDK used to run Gradle   |
| Local Android resources          | Enabled          | Robolectric can load the merged manifest |
| Compose test host manifest       | Debug dependency | Run debug tests                          |

Use the committed wrapper, not a separately installed Gradle version. Gradle's daemon JVM
criteria can override `JAVA_HOME`; inspect `gradle/gradle-daemon-jvm.properties` when a
runner uses an unexpected JVM. Do not change toolchain pins as part of this CI lesson.

**Compatibility checkpoint:** Class 3 pins an older Mockito/Byte Buddy combination. Verify
its tests on JDK 25 before class. An unsupported Java/class-file error is a dependency and
toolchain compatibility issue, not a reason to ignore test failures. Agree on and validate
a compatible dependency update separately if the baseline encounters it.

## Welcome: Establish the Baseline (10 min)

**Say:** "Last class we isolated collaborators. Today we make the same checks run every
time someone proposes a change, on a machine that does not have our Android Studio setup."

From the project root:

```bash
./gradlew --version
./gradlew --no-daemon :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
```

**Expected:** All three tasks succeed. Inspect the local test report and confirm tests
actually ran; `NO-SOURCE` is not proof of test coverage. On a warmed local build, tasks
may be up to date; the later deliberate failure demonstrates that a changed test runs.

| Task                        | What it checks                                                | Needs an emulator?         |
| --------------------------- | ------------------------------------------------------------- | -------------------------- |
| `testDebugUnitTest`         | JVM tests, Mockito tests, and Robolectric tests in `src/test` | No                         |
| `lintDebug`                 | Android static analysis                                       | No                         |
| `assembleDebug`             | Whether a debug APK can be built                              | No                         |
| `connectedDebugAndroidTest` | Tests in `src/androidTest`, including Compose/Espresso        | Yes, or a connected device |

**Ask:** "Does a successful APK build prove our login validation works?"

**Answer:** No. Compilation and packaging do not assert behaviour. That is why the
pipeline runs tests separately. Robolectric tests are UI tests that can run locally;
not every UI test needs the emulator job.

## Theory: What CI Adds (15 min)

- **Continuous integration:** Integrate small changes frequently and automatically check
  each proposed integration. A pipeline alone does not compensate for long-lived branches.
- **Workflow:** The YAML definition of triggers, permissions, and jobs.
- **Runner:** The machine executing a job. Hosted runners start with a fresh job environment.
- **Job:** A sequence of steps on one runner. Separate jobs do not share files automatically.
- **Step:** An action or shell command. A nonzero command exit code normally fails the step.
- **Artifact:** A retained output, such as a test report or APK, associated with a run.
- **Cache:** Reusable dependencies/build data that improve speed, not evidence that tests passed.
- **Required check:** A repository rule that prevents merging without a successful named check.

**Say:** "CI reports whether a change passed. A branch rule makes that result a merge
requirement. Neither one deploys the app: delivery and deployment are separate decisions."

**Connect to Class 3:** Fakes and Mockito stubs keep unit tests independent of real accounts,
network services, and mutable databases. CI makes accidental dependencies on those things
more visible. Do not add a real account password to get an isolated test passing.

## Live Demo: Add GitHub Actions (25 min)

### 1. Create a branch and workflow (10 min)

Create a feature branch using your normal Git workflow. Add
`.github/workflows/android-ci.yml` at the repository root with this complete content:

```yaml
name: Android CI

on:
  pull_request:
    branches: [main]
  push:
    branches: [main]
  workflow_dispatch:

permissions:
  contents: read

concurrency:
  group: android-ci-${{ github.workflow }}-${{ github.ref }}
  cancel-in-progress: true

jobs:
  quality:
    name: Tests, lint, and debug build
    runs-on: ubuntu-latest
    timeout-minutes: 30

    steps:
      - name: Check out source
        uses: actions/checkout@v4
        with:
          persist-credentials: false

      - name: Set up Java
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '25'

      - name: Set up Android SDK
        uses: android-actions/setup-android@v3

      - name: Install compile SDK
        run: sdkmanager "platform-tools" "platforms;android-37"

      - name: Set up Gradle
        uses: gradle/actions/setup-gradle@v4

      - name: Make wrapper executable
        run: chmod +x gradlew

      - name: Show toolchain
        run: ./gradlew --version

      - name: Run local tests, lint, and build
        run: ./gradlew --no-daemon --stacktrace --continue :app:testDebugUnitTest :app:lintDebug :app:assembleDebug

      - name: Upload reports
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: quality-reports
          path: |
            app/build/reports/tests/testDebugUnitTest/
            app/build/test-results/testDebugUnitTest/*.xml
            app/build/reports/lint-results-debug.*
          if-no-files-found: warn
          retention-days: 7

      - name: Upload debug APK
        if: success()
        uses: actions/upload-artifact@v4
        with:
          name: debug-apk
          path: app/build/outputs/apk/debug/*.apk
          if-no-files-found: error
          retention-days: 7
```

**Explain while typing:**

- PRs targeting `main` receive feedback before merging; pushes to `main` check the merged
  result. `workflow_dispatch` supports manual runs once the workflow is on the default branch.
- Explicit SDK installation avoids relying on the runner image having this new API already.
  AGP can install required build tools when SDK licenses are accepted by the setup action.
- `setup-gradle` manages Gradle caching. Do not also enable a second Gradle cache through
  `setup-java` for the same directories.
- `--continue` allows independent Gradle tasks to finish after a task fails, but Gradle
  still exits unsuccessfully. It is not `continue-on-error`.
- `if: always()` attempts report upload even after a failure. Compilation failures may
  produce no reports; the logs then contain the useful evidence.
- The APK is an unsigned-for-release, debug-signed classroom build, not a release artifact.
  It is uploaded only when the quality steps succeed.

The examples use readable major action tags for class. For a production repository,
review actions and pin them to verified full commit SHAs, with a dependency update process.

### 2. Open the first pull request (10 min)

Commit the workflow, push the branch, and open a PR targeting `main`. In GitHub, open the
PR's checks and then the job log. Do not run `connectedDebugAndroidTest` in this job:
there is no device attached.

**Expected:** A check named **Tests, lint, and debug build** starts. A first run can take
longer because dependencies and Robolectric SDK artifacts are downloaded.

### 3. Read the result (5 min)

Open the Actions run, expand the Gradle step, and locate the tasks. Download
`quality-reports`, extract it, and open the test report's `index.html`. Inspect the lint
report too. Download the debug APK only after a successful run.

**Ask:** "Which evidence distinguishes a failing test from a compiler error?"

**Answer:** A failing assertion names the test and expected/actual result; compilation
fails before tests execute. Read the first relevant failure, not just the final Gradle summary.

## Exercise 1: Make CI Catch a Regression (30 min)

### Task 1.1: Introduce one controlled failure (10 min)

Open an existing `LoginValidator` JVM test in `app/src/test`. Pick a passing assertion,
write down its original value, and deliberately change only the expected result to a wrong
one. Do not change production code or remove the test.

Run the test locally. Confirm it fails for the intended assertion, then commit the test
change on the PR branch and push it.

**Expected:** The PR's new run fails. A new APK artifact is not uploaded; test reports are
uploaded if Gradle generated them. Artifacts from older successful runs may still exist.

### Task 1.2: Diagnose from GitHub (10 min)

Without using the local IDE's test result, identify:

1. The failing job and Gradle task.
2. The test class and method.
3. The expected and actual values.
4. The commit SHA that produced this failure.

Download the reports and find the same failure in the HTML report. Pair up and explain
why rebuilding the APK alone would not have caught the wrong expectation.

### Task 1.3: Restore and verify (10 min)

Restore the original assertion, rerun it locally, then commit and push the repair.
Confirm the newest run is green. Do not use `|| true`, disable tests, add
`continue-on-error`, or repeatedly rerun a deterministic failure until a run happens to pass.

**Deliverable:** One PR with a failing run followed by a passing run and a brief diagnosis.

## Exercise 2: Make the Check Matter (30 min)

### Task 2.1: Require the quality check (10 min)

With repository administration permission, create an active branch ruleset or branch
protection rule targeting `main`:

1. Require changes through a pull request.
2. Require the **Tests, lint, and debug build** check after it has appeared in a run.
3. Require the branch to be up to date before merging if that is the class policy.
4. Check whether administrator or other bypass permissions would exempt your demo account.

Keep the job name stable: renaming it requires updating the required-check selection.
Avoid path filters on this required workflow that could leave a PR waiting for a check
which never runs. If using a merge queue later, add the appropriate `merge_group` trigger
and validate it before requiring the workflow for queue entries.

Demonstrate that a deliberately failing PR cannot merge under the rule, then repair it.
If the repository plan or your permissions prevent enforcement, document that limitation
and demonstrate the failing check; do not claim the merge is protected.

### Task 2.2: Optional emulator job (15 min)

**Say:** "Our local tests are not the complete Android environment. We can also run the
existing Compose and Espresso instrumentation tests on an emulator. This costs more time."

Insert this job **inside the existing `jobs` mapping**, alongside `quality`. Do not replace
the quality job. It depends on that job passing and uses a separate fresh runner:

```yaml
  device-tests:
    name: Device UI tests (API 35)
    needs: quality
    runs-on: ubuntu-latest
    timeout-minutes: 30

    steps:
      - name: Check out source
        uses: actions/checkout@v4
        with:
          persist-credentials: false

      - name: Set up Java
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '25'

      - name: Set up Android SDK
        uses: android-actions/setup-android@v3

      - name: Install compile SDK
        run: sdkmanager "platform-tools" "platforms;android-37"

      - name: Set up Gradle
        uses: gradle/actions/setup-gradle@v4

      - name: Make wrapper executable
        run: chmod +x gradlew

      - name: Enable KVM access on the hosted runner
        run: |
          echo 'KERNEL=="kvm", GROUP="kvm", MODE="0666", OPTIONS+="static_node=kvm"' | sudo tee /etc/udev/rules.d/99-kvm4all.rules
          sudo udevadm control --reload-rules
          sudo udevadm trigger --name-match=kvm

      - name: Run instrumentation tests
        uses: reactivecircus/android-emulator-runner@v2
        with:
          api-level: 35
          target: google_apis
          arch: x86_64
          disable-animations: true
          script: ./gradlew --no-daemon --stacktrace :app:connectedDebugAndroidTest

      - name: Upload device reports
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: device-test-reports
          path: |
            app/build/reports/androidTests/connected/
            app/build/outputs/androidTest-results/connected/
          if-no-files-found: warn
          retention-days: 7
```

The KVM commands are for an ephemeral GitHub-hosted Linux runner, not students' Macs or
an unmanaged shared server. They allow emulator hardware acceleration.

API 35 is the **device runtime**, while 37 is the **compile SDK**. The app's minimum SDK
is 26, so running it on 35 is valid, but this does not verify API 37-specific behaviour.
The instrumentation task runs `src/androidTest`; it does not rerun Robolectric tests.

Push the job and inspect its run, or discuss the instructor's prepared result if time is
short. The classroom's required check remains the quality job unless the group explicitly
decides to require device tests too. Merely adding this job does not make it a merge gate.

### Task 2.3: Explain the tradeoff (5 min)

Each pair proposes which checks should run on every PR, on the default branch, or on a
nightly schedule. Explain the cost of slower feedback and the risk of deferring UI coverage.
Use evidence from run durations; do not assume all device tests are flaky.

## Reliability and Security (20 min)

### Troubleshooting lab (10 min)

| Symptom                                        | First check                                                 | Appropriate response                                                            |
| ---------------------------------------------- | ----------------------------------------------------------- | ------------------------------------------------------------------------------- |
| `Permission denied: ./gradlew`                 | Wrapper executable permission                               | Keep the workflow's permission step; commit the executable bit when appropriate |
| SDK location or platform not found             | SDK setup step and platform installation log                | Provision the required SDK; do not commit a developer's `local.properties`      |
| Dependency cannot be resolved                  | Coordinates, repositories, network, and clean baseline      | Fix the concrete resolution problem; do not substitute arbitrary versions       |
| Mockito reports unsupported Java version       | Daemon JDK and Byte Buddy compatibility                     | Align supported toolchain/dependencies and rerun the baseline                   |
| Robolectric cannot resolve `ComponentActivity` | Android resource inclusion and debug test manifest          | Keep `unitTests.isIncludeAndroidResources = true` and `ui-test-manifest`        |
| UI test says no devices connected              | Which job ran `connectedDebugAndroidTest`                   | Run it inside the emulator action                                               |
| Emulator startup timeout                       | KVM access and emulator boot logs                           | Diagnose infrastructure before changing app assertions                          |
| Test passes locally but fails in CI            | Locale, time zone, order, network, static state, and timing | Remove nondeterminism; use proper UI synchronization, not fixed sleeps          |
| No test report artifact                        | Whether compilation/test discovery completed                | Read logs; missing reports are not passing tests                                |
| Required check remains pending                 | Trigger filters and exact required job name                 | Correct rule/workflow configuration                                             |

**Ask:** "What does a cache miss change?"

**Answer:** Normally duration, not correctness. A build must still work without the cache.
An artifact preserves evidence from a particular run; a cache is not a substitute for it.

### Security and maintenance discussion (10 min)

- Keep `contents: read` and avoid credentials in the workflow. None of these tests need
  a personal access token, release keystore, or production service password.
- Do not use `pull_request_target` to check out and execute untrusted PR code with elevated
  permissions. Fork PR workflows can require approval and do not normally receive secrets.
- Keep untrusted PR code off persistent, privileged self-hosted runners.
- Treat downloaded APKs and reports as potentially sensitive artifacts. Use short retention
  and avoid logging credentials or private test data. Artifact access depends on repository visibility.
- Review action/dependency updates. Major tags are convenient for teaching, but verified SHA
  pins make the executed action revision explicit.
- A flaky test needs an owner and investigation. Blind retries can hide a regression;
  any quarantine should be visible, time-limited, and tracked.
- Do not turn lint warnings into an unexplained blanket suppression. Existing lint errors
  should be resolved or deliberately managed before adopting the check as a gate.

## Wrap-Up and Assessment (10 min)

### Acceptance criteria

- A PR triggers the workflow without a student running it manually.
- The quality job runs local tests, lint, and a debug build with the repository's toolchain.
- A deliberately wrong assertion produces a failed check and an inspectable test report.
- Restoring the assertion produces a passing check on the latest commit.
- A required-check rule blocks a failing PR, or the student explicitly documents why the
  repository cannot enforce it.
- Students distinguish local Robolectric tests from emulator-based instrumentation tests.
- No secrets or machine-specific SDK paths were committed.

**Closing prompt:** "Show the failed run, explain the root cause, and show the repaired run.
Then explain what your pipeline still does not test."

### Homework

1. Complete the emulator extension if it was only demonstrated. Report its duration and
   one instrumentation test it actually executed.
2. Add a second supported emulator API as a matrix exercise. Give artifact names an API
   suffix so parallel jobs do not attempt to upload the same artifact name. Explain whether
   you would require both checks before merging.
3. Write a short CI policy: required checks, artifact retention, who handles failures,
   and how flaky tests are tracked. Include the difference between CI and deployment.
4. Propose a nightly device-test schedule, accounting for GitHub cron's UTC time and
   default-branch behaviour. Preserve the PR quality gate.

## Commands Cheat Sheet

```bash
./gradlew --version
./gradlew :app:testDebugUnitTest
./gradlew :app:lintDebug
./gradlew :app:assembleDebug
./gradlew --continue :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
./gradlew :app:connectedDebugAndroidTest
```

The final command needs a running emulator or connected device. The other test/build
commands do not. Use `--tests '*ActualTestClassName*'` with the local test task only after
confirming that class exists; instrumentation tests use runner arguments instead.

## References

- [GitHub Actions workflow syntax](https://docs.github.com/en/actions/writing-workflows/workflow-syntax-for-github-actions)
- [Gradle setup action](https://github.com/gradle/actions/tree/main/setup-gradle)
- [Android SDK setup action](https://github.com/android-actions/setup-android)
- [Android emulator runner](https://github.com/ReactiveCircus/android-emulator-runner)
- [Android local testing](https://developer.android.com/training/testing/local-tests)