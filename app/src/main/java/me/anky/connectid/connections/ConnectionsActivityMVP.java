package me.anky.connectid.connections;

import java.util.List;

import me.anky.connectid.data.ConnectidConnection;

/**
 * Created by Anky An on 14/07/2017.
 * anky25@gmail.com
 */

public interface ConnectionsActivityMVP {
    interface View {

        void displayConnections(List<ConnectidConnection> connections);

        void displayNoConnections();

        void displayError();

    }

    interface Presenter {

        void setView(ConnectionsActivityMVP.View view);

        void loadConnections();

        void unsubscribe();

    }

    interface Model {

        // ToDo: Load data from database
        // ToDo: Save data into database

    }
}
