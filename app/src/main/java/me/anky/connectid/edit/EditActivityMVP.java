package me.anky.connectid.edit;

import java.util.List;

import io.reactivex.Single;
import me.anky.connectid.data.ConnectidConnection;
import me.anky.connectid.data.ConnectionTag;

public interface EditActivityMVP {

    interface View {

        Single<ConnectidConnection> getNewConnection();

        Single<ConnectidConnection> getUpdatedConnection();

        void displayError();

        void displaySuccess(int id);

        void handleAllTags(List<ConnectionTag> connectionTags);
    }

    interface Presenter {

        void setView(EditActivityMVP.View view);

        void deliverNewConnection();

        void unsubscribe();

        void updateConnection();

        void loadTags();

        void updateTagTable(List<ConnectionTag> allTags, List<String> connectionTags, int databaseId);
    }
}
