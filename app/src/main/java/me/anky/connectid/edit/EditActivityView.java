package me.anky.connectid.edit;

import io.reactivex.Single;
import me.anky.connectid.data.ConnectidConnection;

public interface EditActivityView {

    Single<ConnectidConnection> getNewConnection();

    void displayError();

    void displaySuccess();
}
