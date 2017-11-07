package me.anky.connectid.selectedConnections;

import java.util.List;

import me.anky.connectid.data.ConnectidConnection;

/**
 * Created by Anky An on 8/11/2017.
 * anky25@gmail.com
 */

public interface SelectedConnectionsActivityMVP {

    interface View {

        void displayConnections(List<ConnectidConnection> connections);

        void displayNoConnections();

        void displayError();
    }

    interface Presenter {

        void setView(SelectedConnectionsActivityMVP.View view);

        void loadTag(int tagId);

        void loadConnections(List<String> idsList);

        void unsubscribe();
    }
}
