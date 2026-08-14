package me.anky.connectid.flashcards;

public final class FlashcardScheduler {
    static final long MINUTE_MS = 60L * 1000L;
    static final long DAY_MS = 24L * 60L * MINUTE_MS;
    private static final int MAX_BOX = 6;
    private static final long[] GOT_IT_INTERVALS = {
            DAY_MS,
            2L * DAY_MS,
            4L * DAY_MS,
            8L * DAY_MS,
            16L * DAY_MS,
            30L * DAY_MS
    };

    private FlashcardScheduler() {
    }

    public static ReviewResult schedule(int currentBox, int attempts, int correct,
                                        long reviewedAt, FlashcardRating rating) {
        int safeBox = Math.max(0, Math.min(MAX_BOX, currentBox));
        int nextBox;
        long interval;
        int nextCorrect = Math.max(0, correct);

        switch (rating) {
            case AGAIN:
                nextBox = 0;
                interval = 10L * MINUTE_MS;
                break;
            case HARD:
                nextBox = Math.max(1, safeBox);
                interval = DAY_MS;
                break;
            case GOT_IT:
                nextBox = Math.min(MAX_BOX, safeBox + 1);
                interval = GOT_IT_INTERVALS[nextBox - 1];
                nextCorrect++;
                break;
            default:
                throw new IllegalArgumentException("Unknown flashcard rating");
        }

        return new ReviewResult(nextBox, reviewedAt, reviewedAt + interval,
                Math.max(0, attempts) + 1, nextCorrect);
    }

    public static final class ReviewResult {
        private final int box;
        private final long lastReviewed;
        private final long nextReview;
        private final int attempts;
        private final int correct;

        ReviewResult(int box, long lastReviewed, long nextReview, int attempts, int correct) {
            this.box = box;
            this.lastReviewed = lastReviewed;
            this.nextReview = nextReview;
            this.attempts = attempts;
            this.correct = correct;
        }

        public int getBox() {
            return box;
        }

        public long getLastReviewed() {
            return lastReviewed;
        }

        public long getNextReview() {
            return nextReview;
        }

        public int getAttempts() {
            return attempts;
        }

        public int getCorrect() {
            return correct;
        }
    }
}
