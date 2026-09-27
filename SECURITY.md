# Security Notes

BloomCare uses Firebase Authentication, user-scoped Realtime Database rules, and Firebase App Check.

## Do not commit

- `app/google-services.json`
- App Check debug tokens
- keystores or signing credentials
- `local.properties`
- passwords or test-account credentials

A template Firebase configuration is provided as `app/google-services.example.json`.

## App Check

Debug builds use Firebase's App Check debug provider for emulator development. Release builds use Play Integrity. Debug tokens are credentials for development access and should be revoked immediately if exposed.

## Realtime Database

The checked-in rules restrict reads and writes to the authenticated user's own subtree under `users/{uid}`.
