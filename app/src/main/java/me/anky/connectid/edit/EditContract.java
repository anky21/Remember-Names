package me.anky.connectid.edit;

import io.reactivex.Single;
import me.anky.connectid.data.ConnectidConnection;

public interface EditContract {

    interface View {

        Single<ConnectidConnection> getNewConnection();

        void displayError();

        void displaySuccess();
    }

    interface Presenter {

        void deliverNewConnection();

        void unsubscribe();
    }
}