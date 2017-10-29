package me.anky.connectid.details;

import java.util.List;

import io.reactivex.Single;
import me.anky.connectid.data.ConnectidConnection;
import me.anky.connectid.data.ConnectionTag;

public interface DetailsActivityMVP {

    interface View {

        Single<Integer> getConnectionToDelete();

        void displayConnection(ConnectidConnection connection);

        void displayError();

        void displaySuccess();
    }

    interface Presenter {

        void setView(DetailsActivityMVP.View view);

        void loadConnection(int data_id);

        void deliverDatabaseIdtoDelete();

        void unsubscribe();
    }
}
