package com.samiksha.bloomcare;

public final class RecoveryScoreCalculator {

    private RecoveryScoreCalculator() {
    }

    public static int calculate(
            double sleepHours,
            int energy,
            int mood,
            int pain
    ) {
        int sleepScore;

        if (sleepHours >= 7) {
            sleepScore = 10;
        } else if (sleepHours >= 5) {
            sleepScore = 7;
        } else {
            sleepScore = 4;
        }

        int total = sleepScore + energy + mood + pain;
        return (total * 100) / 40;
    }

    public static String status(int score) {
        if (score >= 80) {
            return "Good Recovery";
        } else if (score >= 60) {
            return "Moderate Recovery";
        }
        return "Prioritize Rest";
    }
}
