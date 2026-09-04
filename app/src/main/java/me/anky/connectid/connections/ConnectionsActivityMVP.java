package me.anky.connectid.connections;

import java.util.List;

import me.anky.connectid.data.ConnectidConnection;

public interface ConnectionsActivityMVP {

    interface View {

        void displayConnections(List<ConnectidConnection> connections);

        void displayNoConnections();

        int getSortByOption();

        void displayError();

        void closeNavigationMenu();

        void showExitDialog();

        void startFlashcardsGame(List<ConnectidConnection> flashcards);

        void showFlashcardsNotEnoughProfilesError();

        void displayConnectionDeleted(ConnectidConnection connection);

        void displayConnectionDeleteError(ConnectidConnection connection);
    }

    interface Presenter {

        void setView(ConnectionsActivityMVP.View view);

        void loadConnections(Integer integer);

        void handleSortByOptionChange();

        void onFlashcardsSelected();

        void deleteConnection(ConnectidConnection connection);

        void unsubscribe();
    }
}
