package me.anky.connectid.connections;

import java.util.List;

import me.anky.connectid.data.ConnectidConnection;
import me.anky.connectid.data.ConnectionTag;

public interface ConnectionsActivityMVP {

    interface View {

        void displayConnections(List<ConnectidConnection> connections);

        void displayNoConnections();

        int getSortByOption();

        void displayError();

        void closeNavigationMenu();

        void showExitDialog();
    }

    interface Presenter {

        void setView(ConnectionsActivityMVP.View view);

        void loadConnections(Integer integer);

        void handleSortByOptionChange();

        void unsubscribe();

        void loadConnection(int data_id);

        void deliverDatabaseIdToDelete(int databaseId);

        void loadAndUpdateTagTable(String databaseId, String tags);

        void deleteIdsFromTag(String databaseId, String tags, List<ConnectionTag> allTags);
    }
}
