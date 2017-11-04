package me.anky.connectid.tags;

import java.util.List;

import me.anky.connectid.data.ConnectionTag;

/**
 * Created by Anky An on 5/11/2017.
 * anky25@gmail.com
 */

public interface TagsActivityMVP {

    interface View {
        void displayTags(List<ConnectionTag> allTags);

        void displayNoTags();
    }

    interface Presenter {
        void setView(TagsActivityMVP.View view);

        void loadTags();

        void unsubscribe();
    }
}
