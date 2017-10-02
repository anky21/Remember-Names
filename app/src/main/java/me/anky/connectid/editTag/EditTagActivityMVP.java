package me.anky.connectid.editTag;

import java.util.List;

import io.reactivex.Single;
import me.anky.connectid.connections.ConnectionsActivityMVP;
import me.anky.connectid.data.ConnectidConnection;
import me.anky.connectid.data.ConnectionTag;
import me.anky.connectid.edit.EditActivityMVP;

/**
 * Created by anky on 2/10/17.
 */

public interface EditTagActivityMVP {

    interface View {

        void displayAllTags(List<ConnectionTag> allTags);

        void displayNoTags();

        void displayError();
    }

    interface Presenter {

        void setView(EditTagActivityMVP.View view);

        void loadTags();

        void unsubscribe();
    }
}
