package me.anky.connectid.flashcards;

final class DailyGoalTracker {
    private DailyGoalTracker() {
    }

    static Result recordReview(long activityDay, int reviewsOnActivityDay,
                               long lastGoalDay, int currentStreak, int bestStreak,
                               int dailyGoal, long today) {
        int reviewsToday = activityDay == today ? reviewsOnActivityDay + 1 : 1;
        int safeGoal = Math.max(1, dailyGoal);
        int effectiveStreak = isStreakCurrent(lastGoalDay, today)
                ? Math.max(0, currentStreak) : 0;
        long nextGoalDay = lastGoalDay;

        if (reviewsToday >= safeGoal && lastGoalDay != today) {
            effectiveStreak = lastGoalDay == today - 1L ? effectiveStreak + 1 : 1;
            nextGoalDay = today;
        }

        return new Result(today, reviewsToday, nextGoalDay,
                effectiveStreak, Math.max(bestStreak, effectiveStreak));
    }

    static Result applyGoal(long activityDay, int reviewsOnActivityDay,
                            long lastGoalDay, int currentStreak, int bestStreak,
                            int dailyGoal, long today) {
        int reviewsToday = activityDay == today ? reviewsOnActivityDay : 0;
        int effectiveStreak = isStreakCurrent(lastGoalDay, today)
                ? Math.max(0, currentStreak) : 0;
        long nextGoalDay = lastGoalDay;

        if (reviewsToday >= Math.max(1, dailyGoal) && lastGoalDay != today) {
            effectiveStreak = lastGoalDay == today - 1L ? effectiveStreak + 1 : 1;
            nextGoalDay = today;
        }

        return new Result(activityDay, reviewsOnActivityDay, nextGoalDay,
                effectiveStreak, Math.max(bestStreak, effectiveStreak));
    }

    static boolean isStreakCurrent(long lastGoalDay, long today) {
        return lastGoalDay == today || lastGoalDay == today - 1L;
    }

    static final class Result {
        final long activityDay;
        final int reviewsOnActivityDay;
        final long lastGoalDay;
        final int currentStreak;
        final int bestStreak;

        Result(long activityDay, int reviewsOnActivityDay, long lastGoalDay,
               int currentStreak, int bestStreak) {
            this.activityDay = activityDay;
            this.reviewsOnActivityDay = reviewsOnActivityDay;
            this.lastGoalDay = lastGoalDay;
            this.currentStreak = currentStreak;
            this.bestStreak = bestStreak;
        }
    }
}
