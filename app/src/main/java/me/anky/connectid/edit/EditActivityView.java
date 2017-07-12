package me.anky.connectid.edit;

import me.anky.connectid.data.ConnectidConnection;

public interface EditActivityView {

    ConnectidConnection getNewConnection();

    void displayError();

    void displaySuccess();
}
