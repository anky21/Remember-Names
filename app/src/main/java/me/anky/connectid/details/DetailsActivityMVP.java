package me.anky.connectid.details;

import io.reactivex.Single;

public interface DetailsActivityMVP {

    interface View {

        Single<Integer> getConnectionToDelete();

        void displayError();

        void displaySuccess();
    }

    interface Presenter {

        void setView(DetailsActivityMVP.View view);

        void deliverDatabaseIdtoDelete();

        void unsubscribe();
    }
}
