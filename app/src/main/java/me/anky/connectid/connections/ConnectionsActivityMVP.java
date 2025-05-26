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

        // Callbacks for batch tagging operations
        void onBatchTagOperationCompleted(String message, boolean success);
    }

    interface Presenter {

        void setView(ConnectionsActivityMVP.View view);

        void loadConnections(Integer integer);

        void handleSortByOptionChange();

        void unsubscribe();

        // Batch tagging methods
        void addTagToSelectedConnections(List<Integer> connectionIds, String tagName);

        void removeTagFromSelectedConnections(List<Integer> connectionIds, String tagName);
    }
}
