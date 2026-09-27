# BloomCare

BloomCare is a Java Android wellness-tracking app that combines everyday recovery data with Firebase-backed persistence and personalized AI wellness guidance.

The app lets a signed-in user track hydration, weight, gentle workouts, sleep, energy, mood, pain, and a simple recovery score. Firebase Authentication keeps accounts separate, Realtime Database stores user-specific current values and timestamped history, and Firebase AI Logic generates a concise wellness insight from the user's saved data.

> BloomCare is a wellness-support project, not a medical device. Its recovery score is an app-defined indicator and is not clinically validated. AI output is constrained to general wellness guidance and is instructed not to diagnose conditions or prescribe medication.

## Highlights

- **Java Android app** using XML layouts and Material Components
- **Firebase Authentication** with email/password sign-in and account creation
- **User-scoped Realtime Database** structure under `users/{uid}`
- **Hydration, weight, workout, and recovery tracking** with timestamped history
- **Progress & History** screen showing recent records
- **Personalized AI wellness coach** powered by Firebase AI Logic / Gemini
- **AI insight history** saved in Firebase
- **Firebase App Check**
  - Debug provider for emulator/development builds
  - Play Integrity provider for release builds
- **Per-user Realtime Database rules** to prevent cross-account reads/writes
- **120-record synthetic benchmark** used during testing; benchmark code removed from the production project
- **JUnit coverage for the recovery-score calculation and status thresholds**

## Tech Stack

- Java 11
- Android SDK / XML Views
- Material Components
- Firebase Authentication
- Firebase Realtime Database
- Firebase AI Logic
- Gemini
- Firebase App Check
- Google Play Integrity

## Core Flow

```text
Login / Create Account
        |
        v
Firebase Authentication
        |
        v
users/{uid}
  |-- profile
  |-- hydration
  |-- weight
  |-- workout
  |-- recovery
  |-- ai
  `-- history
        |
        v
Dashboard + Progress History
        |
        v
Personalized Gemini wellness insight
```

## Firebase Data Model

```text
users
`-- {uid}
    |-- profile
    |   |-- name
    |   |-- age
    |   |-- height
    |   `-- email
    |-- hydration
    |   `-- currentGlasses
    |-- weight
    |   |-- currentWeight
    |   `-- previousWeight
    |-- workout
    |   |-- selectedWorkout
    |   |-- selectedDetails
    |   `-- completed
    |-- recovery
    |   |-- sleepHours
    |   |-- energyLevel
    |   |-- moodScore
    |   |-- painScore
    |   `-- recoveryScore
    |-- ai
    |   |-- latestInsight
    |   `-- generatedAt
    `-- history
        |-- hydration/{pushId}
        |-- weight/{pushId}
        |-- workout/{pushId}
        |-- recovery/{pushId}
        `-- ai/{pushId}
```

## AI Personalization

When the user taps **Generate AI Insight**, BloomCare reads the currently saved profile and wellness values for the authenticated user and builds a constrained prompt for Gemini.

The prompt can use:
- Name, age, and height when available
- Current weight
- Hydration progress
- Sleep duration
- Energy level
- Recovery score
- Latest workout and completion status

The user's email and Firebase UID are not sent in the prompt. The prompt explicitly instructs the model not to invent missing data, diagnose disease, prescribe medication, or represent the app's recovery score as a clinical measurement.

Generated output is displayed on the dashboard and persisted as the latest insight plus a timestamped AI-history entry.

## Security

Realtime Database rules are included in [`database.rules.json`](database.rules.json):

```json
{
  "rules": {
    "users": {
      "$uid": {
        ".read": "auth != null && auth.uid === $uid",
        ".write": "auth != null && auth.uid === $uid"
      }
    }
  }
}
```

App Check is initialized before other Firebase usage through `BloomCareApplication`.

- `src/debug` installs the App Check **Debug Provider** for emulator development.
- `src/release` installs the **Play Integrity** provider.

Debug App Check tokens must never be committed to the repository.

## Local Setup

1. Clone the repository and open it in Android Studio.
2. Create a Firebase Android app with package name `com.samiksha.bloomcare`.
3. Enable **Email/Password** authentication.
4. Create a Firebase Realtime Database.
5. Enable Firebase AI Logic with the Gemini Developer API.
6. Register the Android app in Firebase App Check.
7. Copy `app/google-services.example.json` to `app/google-services.json` and replace the placeholders with the values from your Firebase project. In normal Firebase setup, you can instead download the real `google-services.json` directly from Firebase Console.
8. In `app/src/main/res/values/strings.xml`, replace the placeholder value of `firebase_database_url` with your own Realtime Database URL.
9. Publish the rules in `database.rules.json` to Realtime Database.
10. For emulator development, register the App Check debug token shown in Logcat. Do not commit or share it.

## Tests

The recovery-score logic is isolated in `RecoveryScoreCalculator` so it can be tested without an Android UI dependency. Unit tests cover the high-score case, the 65/100 moderate-recovery example, and status boundaries at 80/60.

Run locally with:

```bash
./gradlew testDebugUnitTest
```

## Benchmark

BloomCare was tested with **120 synthetic wellness records** stored under a separate benchmark branch during development.

Observed in one Pixel 7 emulator / API 36 test run:
- Write of 120 synthetic records: **2436 ms**
- Read of 120 records: **120 ms**

These are single-run development measurements, not production latency guarantees. Network conditions, emulator state, backend region, and device performance can change the results. The temporary benchmark-generation code was removed after the measurement.

See [`docs/BENCHMARK.md`](docs/BENCHMARK.md) for methodology and limitations.

## Project Structure

```text
app/src/main/java/com/samiksha/bloomcare/
|-- LoginActivity.java
|-- MainActivity.java
|-- HydrationActivity.java
|-- WeightActivity.java
|-- WorkoutActivity.java
|-- RecoveryActivity.java
|-- ProfileActivity.java
|-- HistoryActivity.java
`-- RecoveryScoreCalculator.java

app/src/debug/java/com/samiksha/bloomcare/
`-- BloomCareApplication.java

app/src/release/java/com/samiksha/bloomcare/
`-- BloomCareApplication.java
```

## Current Scope

BloomCare is a portfolio/learning project focused on Android engineering, Firebase integration, data modeling, authentication, and responsible AI integration. It does not replace professional medical care and does not provide clinical recommendations.
