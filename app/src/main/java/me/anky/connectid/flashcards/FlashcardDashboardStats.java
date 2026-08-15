package me.anky.connectid.flashcards;

import java.util.List;

import me.anky.connectid.data.ConnectidConnection;

public final class FlashcardDashboardStats {
    public static final int MASTERED_BOX = 4;

    private final int total;
    private final int due;
    private final int newCards;
    private final int learning;
    private final int mastered;
    private final int accuracyPercent;

    private FlashcardDashboardStats(int total, int due, int newCards, int learning,
                                    int mastered, int accuracyPercent) {
        this.total = total;
        this.due = due;
        this.newCards = newCards;
        this.learning = learning;
        this.mastered = mastered;
        this.accuracyPercent = accuracyPercent;
    }

    public static FlashcardDashboardStats calculate(List<ConnectidConnection> connections,
                                                     long now) {
        int due = 0;
        int newCards = 0;
        int learning = 0;
        int mastered = 0;
        long attempts = 0L;
        long correct = 0L;

        for (ConnectidConnection connection : connections) {
            if (connection.getFlashcardNextReview() == 0L
                    || connection.getFlashcardNextReview() <= now) {
                due++;
            }
            if (connection.getFlashcardAttempts() == 0) {
                newCards++;
            } else if (connection.getFlashcardBox() >= MASTERED_BOX) {
                mastered++;
            } else {
                learning++;
            }
            attempts += Math.max(0, connection.getFlashcardAttempts());
            correct += Math.max(0, connection.getFlashcardCorrect());
        }

        int accuracy = attempts == 0L ? 0 : (int) Math.round(correct * 100.0d / attempts);
        return new FlashcardDashboardStats(connections.size(), due, newCards,
                learning, mastered, Math.min(100, accuracy));
    }

    public int getTotal() {
        return total;
    }

    public int getDue() {
        return due;
    }

    public int getNewCards() {
        return newCards;
    }

    public int getLearning() {
        return learning;
    }

    public int getMastered() {
        return mastered;
    }

    public int getAccuracyPercent() {
        return accuracyPercent;
    }
}
