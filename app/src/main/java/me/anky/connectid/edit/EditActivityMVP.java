package me.anky.connectid.edit;

import io.reactivex.Single;
import me.anky.connectid.data.ConnectidConnection;

public interface EditActivityMVP {

    interface View {

        Single<ConnectidConnection> getNewConnection();

        void displayError();

        void displaySuccess();
    }

    interface Presenter {

        void setView(EditActivityMVP.View view);

        void deliverNewConnection();

        void unsubscribe();
    }
}
