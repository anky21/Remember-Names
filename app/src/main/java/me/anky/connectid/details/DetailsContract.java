package me.anky.connectid.details;

public interface DetailsContract {

    interface View {

        int getConnectionToDelete();

        void displayError();

        void displaySuccess();
    }

    interface Presenter {

        void deliverDatabaseIdtoDelete();

        void unsubscribe();
    }
}
