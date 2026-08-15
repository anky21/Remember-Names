package me.anky.connectid.flashcards;

import android.content.SharedPreferences;

import java.time.LocalDate;

public final class FlashcardProgressStore {
    private static final String KEY_DAILY_GOAL = "flashcards_daily_goal";
    private static final String KEY_ACTIVITY_DAY = "flashcards_activity_day";
    private static final String KEY_REVIEWS_ON_DAY = "flashcards_reviews_on_day";
    private static final String KEY_LAST_GOAL_DAY = "flashcards_last_goal_day";
    private static final String KEY_CURRENT_STREAK = "flashcards_current_streak";
    private static final String KEY_BEST_STREAK = "flashcards_best_streak";
    private static final int DEFAULT_DAILY_GOAL = 10;

    private final SharedPreferences preferences;

    public FlashcardProgressStore(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    public void recordReview() {
        long today = LocalDate.now().toEpochDay();
        DailyGoalTracker.Result result = DailyGoalTracker.recordReview(
                preferences.getLong(KEY_ACTIVITY_DAY, Long.MIN_VALUE),
                preferences.getInt(KEY_REVIEWS_ON_DAY, 0),
                preferences.getLong(KEY_LAST_GOAL_DAY, Long.MIN_VALUE),
                preferences.getInt(KEY_CURRENT_STREAK, 0),
                preferences.getInt(KEY_BEST_STREAK, 0),
                getDailyGoal(),
                today);
        save(result);
    }

    public Snapshot getSnapshot() {
        long today = LocalDate.now().toEpochDay();
        long activityDay = preferences.getLong(KEY_ACTIVITY_DAY, Long.MIN_VALUE);
        long lastGoalDay = preferences.getLong(KEY_LAST_GOAL_DAY, Long.MIN_VALUE);
        int streak = DailyGoalTracker.isStreakCurrent(lastGoalDay, today)
                ? preferences.getInt(KEY_CURRENT_STREAK, 0) : 0;
        int reviewsToday = activityDay == today
                ? preferences.getInt(KEY_REVIEWS_ON_DAY, 0) : 0;
        return new Snapshot(reviewsToday, getDailyGoal(), streak,
                preferences.getInt(KEY_BEST_STREAK, 0));
    }

    public int getDailyGoal() {
        return preferences.getInt(KEY_DAILY_GOAL, DEFAULT_DAILY_GOAL);
    }

    public void setDailyGoal(int dailyGoal) {
        int safeGoal = Math.max(1, dailyGoal);
        long today = LocalDate.now().toEpochDay();
        DailyGoalTracker.Result result = DailyGoalTracker.applyGoal(
                preferences.getLong(KEY_ACTIVITY_DAY, Long.MIN_VALUE),
                preferences.getInt(KEY_REVIEWS_ON_DAY, 0),
                preferences.getLong(KEY_LAST_GOAL_DAY, Long.MIN_VALUE),
                preferences.getInt(KEY_CURRENT_STREAK, 0),
                preferences.getInt(KEY_BEST_STREAK, 0),
                safeGoal,
                today);
        SharedPreferences.Editor editor = preferences.edit()
                .putInt(KEY_DAILY_GOAL, safeGoal);
        writeResult(editor, result).apply();
    }

    private void save(DailyGoalTracker.Result result) {
        writeResult(preferences.edit(), result).apply();
    }

    private SharedPreferences.Editor writeResult(SharedPreferences.Editor editor,
                                                  DailyGoalTracker.Result result) {
        return editor
                .putLong(KEY_ACTIVITY_DAY, result.activityDay)
                .putInt(KEY_REVIEWS_ON_DAY, result.reviewsOnActivityDay)
                .putLong(KEY_LAST_GOAL_DAY, result.lastGoalDay)
                .putInt(KEY_CURRENT_STREAK, result.currentStreak)
                .putInt(KEY_BEST_STREAK, result.bestStreak);
    }

    public static final class Snapshot {
        private final int reviewsToday;
        private final int dailyGoal;
        private final int currentStreak;
        private final int bestStreak;

        Snapshot(int reviewsToday, int dailyGoal, int currentStreak, int bestStreak) {
            this.reviewsToday = reviewsToday;
            this.dailyGoal = dailyGoal;
            this.currentStreak = currentStreak;
            this.bestStreak = bestStreak;
        }

        public int getReviewsToday() {
            return reviewsToday;
        }

        public int getDailyGoal() {
            return dailyGoal;
        }

        public int getCurrentStreak() {
            return currentStreak;
        }

        public int getBestStreak() {
            return bestStreak;
        }
    }
}
