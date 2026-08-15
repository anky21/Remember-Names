package me.anky.connectid.flashcards;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DailyGoalTrackerTest {
    private static final long TODAY = 100L;

    @Test
    public void reachingGoalContinuesYesterdayStreak() {
        DailyGoalTracker.Result result = DailyGoalTracker.recordReview(
                TODAY, 4, TODAY - 1L, 2, 4, 5, TODAY);

        assertEquals(5, result.reviewsOnActivityDay);
        assertEquals(TODAY, result.lastGoalDay);
        assertEquals(3, result.currentStreak);
        assertEquals(4, result.bestStreak);
    }

    @Test
    public void reviewBelowGoalDoesNotStartStreak() {
        DailyGoalTracker.Result result = DailyGoalTracker.recordReview(
                TODAY, 1, Long.MIN_VALUE, 0, 0, 5, TODAY);

        assertEquals(2, result.reviewsOnActivityDay);
        assertEquals(0, result.currentStreak);
    }

    @Test
    public void staleStreakRestartsWhenGoalIsReached() {
        DailyGoalTracker.Result result = DailyGoalTracker.recordReview(
                TODAY, 4, TODAY - 3L, 8, 8, 5, TODAY);

        assertEquals(1, result.currentStreak);
        assertEquals(8, result.bestStreak);
    }

    @Test
    public void loweringGoalCanCompleteToday() {
        DailyGoalTracker.Result result = DailyGoalTracker.applyGoal(
                TODAY, 5, TODAY - 1L, 3, 3, 5, TODAY);

        assertEquals(TODAY, result.lastGoalDay);
        assertEquals(4, result.currentStreak);
        assertEquals(4, result.bestStreak);
    }
}
