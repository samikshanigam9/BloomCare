# BloomCare - Smart Postpartum Fitness Tracker

**Resume project name:** Smart Postpartum Fitness Tracker  
**Project name:** BloomCare - Smart Postpartum Fitness Tracker

BloomCare - Smart Postpartum Fitness Tracker is a Java Android wellness application focused on postpartum fitness and recovery support. It combines user-specific wellness tracking, Firebase-backed persistence, progress history, and personalized AI wellness guidance.

The app lets an authenticated user track hydration, weight, gentle workouts, sleep, energy, mood, pain/discomfort, and an app-level recovery score. Firebase Authentication keeps accounts separate, Firebase Realtime Database stores current values and timestamped history, and Firebase AI Logic generates personalized wellness insights from the user's saved data.

> BloomCare is a wellness-support portfolio project, not a medical device. Its recovery score is an app-defined wellness indicator and is not clinically validated. AI output is constrained to general wellness guidance and is instructed not to diagnose conditions or prescribe medication.

## Resume-Aligned Highlights

- Developed a **Java Android application with 6+ wellness features**, including workout planning, hydration tracking, weight tracking, recovery monitoring, progress/history, profile management, and AI wellness insights.
- Integrated **Firebase Authentication and Firebase Realtime Database** for user-specific storage, real-time synchronization, and timestamped wellness history.
- Implemented a **modular object-oriented Java architecture** across independent Android activities and reusable recovery-score logic.
- Added **personalized AI wellness recommendations** using Firebase AI Logic / Gemini based on saved hydration, sleep, energy, recovery, workout, weight, and profile data.
- Added **Firebase App Check** and per-user Realtime Database security rules.
- Tested Firebase persistence with **120 synthetic wellness records** during development.

## Core Features

- Email/password account creation and login
- User-specific Firebase data under `users/{uid}`
- Hydration tracker
- Weight and progress tracker
- Gentle workout planner
- Recovery tracker
- Sleep, energy, mood, and discomfort inputs
- App-level recovery score
- Progress & History screen
- User profile management
- Personalized AI wellness coach
- AI insight history
- Firebase App Check
- Per-user database access rules

## Tech Stack

- Java 11
- Android Studio
- Android SDK / XML Views
- Material Components
- Firebase Authentication
- Firebase Realtime Database
- Firebase AI Logic
- Gemini
- Firebase App Check
- Google Play Integrity
- JUnit

## Architecture

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

When the user taps **Generate AI Insight**, the app reads the authenticated user's currently saved wellness data and builds a constrained prompt for Gemini.

The prompt can use:
- Name, age, and height when available
- Current weight
- Hydration progress
- Sleep duration
- Energy level
- Recovery score
- Latest workout and completion status

The user's email and Firebase UID are not included in the AI prompt. The prompt explicitly instructs the model not to invent missing data, diagnose disease, prescribe medication, or represent the recovery score as a clinical measurement.

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

- `src/debug` uses the App Check **Debug Provider** for emulator development.
- `src/release` uses **Play Integrity**.

Debug App Check tokens must never be committed to the repository.

## Local Setup

1. Clone the repository and open it in Android Studio.
2. Create a Firebase Android app with package name `com.samiksha.bloomcare`.
3. Enable **Email/Password** authentication.
4. Create a Firebase Realtime Database.
5. Enable Firebase AI Logic with the Gemini Developer API.
6. Register the Android app in Firebase App Check.
7. Copy `app/google-services.example.json` to `app/google-services.json` and replace the placeholders with your Firebase project values.
8. In `app/src/main/res/values/strings.xml`, replace the placeholder `firebase_database_url` with your Realtime Database URL.
9. Publish the rules in `database.rules.json`.
10. For emulator development, register the App Check debug token shown in Logcat. Do not commit or share it.

## Tests

The recovery-score logic is isolated in `RecoveryScoreCalculator` for unit testing.

Current tests cover:
- Strong recovery input
- The 65/100 moderate-recovery example
- Status thresholds at 80 and 60

Run locally with:

```bash
./gradlew testDebugUnitTest
```

## Benchmark

The project was tested with **120 synthetic wellness records** stored under a separate benchmark branch during development.

Observed in one Pixel 7 emulator / API 36 test run:
- Write of 120 synthetic records: **2436 ms**
- Read of 120 records: **120 ms**

These are single-run development measurements, not production latency guarantees. Network conditions, emulator/device performance, cache state, backend region, and other runtime factors can affect latency.

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

## Resume-Ready Project Entry

**BloomCare - Smart Postpartum Fitness Tracker | Java, Android Studio, Firebase, Gemini**

- Developed a Java Android application with 6+ wellness features, including workout planning, hydration tracking, weight tracking, recovery monitoring, progress history, and personalized AI wellness insights.
- Integrated Firebase Authentication and Realtime Database for user-specific storage, real-time synchronization, and timestamped wellness records.
- Implemented personalized AI wellness recommendations using Firebase AI Logic / Gemini, with Firebase App Check and per-user database security rules.

## Current Scope

BloomCare is a portfolio/learning project focused on Android engineering, Firebase integration, data modeling, authentication, and responsible AI integration. It does not replace professional medical care and does not provide clinical recommendations.
