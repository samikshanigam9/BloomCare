# BloomCare Synthetic Data Benchmark

## Purpose

This test was used to verify that the app's Firebase Realtime Database structure remained responsive with more than 100 wellness records. The records were synthetic and were kept separate from the user's real tracker history.

## Test Setup

- Date: 26 September 2026
- Device: Pixel 7 Android emulator
- Emulator API: 36
- Backend: Firebase Realtime Database
- Records: 120 synthetic wellness records
- Fields per synthetic record:
  - hydration glasses
  - sleep hours
  - energy level
  - weight
  - recovery score
  - timestamp

Temporary test path:

```text
users/{uid}/benchmark/syntheticRecords
```

## Observed Result

```text
WRITE: 120 synthetic records = 2436 ms
READ:  120 records           = 120 ms
```

## Interpretation

The run demonstrated successful bulk persistence and retrieval of 120 records in the development environment. These numbers are not intended as production SLAs or universal performance claims; Firebase region, connection quality, emulator/device performance, cache state, and backend conditions can materially affect latency.

The temporary benchmark execution code was removed after the result was recorded so it does not run during normal app startup.
