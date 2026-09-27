package com.samiksha.bloomcare;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RecoveryScoreCalculatorTest {

    @Test
    public void calculate_returns100ForStrongInputs() {
        assertEquals(
                100,
                RecoveryScoreCalculator.calculate(7.0, 10, 10, 10)
        );
    }

    @Test
    public void calculate_matchesModerateRecoveryExample() {
        assertEquals(
                65,
                RecoveryScoreCalculator.calculate(6.5, 5, 7, 7)
        );
    }

    @Test
    public void status_usesExpectedThresholds() {
        assertEquals("Good Recovery", RecoveryScoreCalculator.status(80));
        assertEquals("Moderate Recovery", RecoveryScoreCalculator.status(60));
        assertEquals("Prioritize Rest", RecoveryScoreCalculator.status(59));
    }
}
