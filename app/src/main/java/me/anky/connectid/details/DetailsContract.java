package me.anky.connectid.details;

import io.reactivex.Single;

public interface DetailsContract {

    interface View {

        Single<Integer> getConnectionToDelete();

        void displayError();

        void displaySuccess();
    }

    interface Presenter {

        void deliverDatabaseIdtoDelete();

        void unsubscribe();
    }
}
