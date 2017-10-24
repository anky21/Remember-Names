package me.anky.connectid.editTag;

import android.graphics.Movie;
import android.util.Log;

import com.facebook.stetho.common.ListUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import butterknife.internal.Utils;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import me.anky.connectid.data.ConnectionTag;
import me.anky.connectid.data.ConnectionsDataSource;

/**
 * Created by anky on 2/10/17.
 */

public class EditTagActivityPresenter implements EditTagActivityMVP.Presenter {

    private EditTagActivityMVP.View view;
    private ConnectionsDataSource dataSource;

    // Create a composite for RxJava subscriber cleanup
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Inject
    public EditTagActivityPresenter(ConnectionsDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setView(EditTagActivityMVP.View view) {
        this.view = view;
    }

    @Override
    public void createNewTag(String input, List<String> connectionTags, List<ConnectionTag> allTags) {
        int existingTagPosition = -1;

        // If the input is already in the allTags with different cases, use the existing one
        if (!input.equals("")) {
            if (allTags != null && allTags.size() > 0) {
                for (ConnectionTag allTagsItem : allTags) {
                    if (allTagsItem.getTag().equalsIgnoreCase(input)) {
                        input = allTagsItem.getTag();
                    }
                }
            }

            if (connectionTags.size() == 0) {
                connectionTags.add(input);
            } else {
                for (int i = 0; i < connectionTags.size(); i++) {
                    if (connectionTags.get(i).equalsIgnoreCase(input)) {
                        existingTagPosition = i;
                    }
                }
                if (existingTagPosition == -1) {
                    connectionTags.add(input);
                } else {
                    connectionTags.remove(existingTagPosition);
                    connectionTags.add(input);
                }
            }
            view.displayConnectionTags();
        }
    }

    @Override
    public void loadTags() {
        DisposableSingleObserver<List<ConnectionTag>> disposableSingleObserver =
                dataSource.getTags().subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<List<ConnectionTag>>() {
                            @Override
                            public void onSuccess(List<ConnectionTag> connectionTags) {
                                if (connectionTags.isEmpty()) {
                                    view.displayNoTags();
                                } else {
                                    view.displayAllTags(connectionTags);
                                }
                            }

                            @Override
                            public void onError(Throwable e) {

                            }
                        });
        compositeDisposable.add(disposableSingleObserver);
    }

    @Override
    public void unsubscribe() {
        compositeDisposable.clear();
    }

    @Override
    public void updateConnectionTags(int id, List<String> connectionTags) {
        if (connectionTags == null || connectionTags.size() == 0) {
            return;
        } else {
            StringBuffer tagString = new StringBuffer();
            for (String tag : connectionTags) {
                tagString.append(tag).append(",");
            }
            dataSource.updateConnection(id, tagString.toString());
        }
    }

    @Override
    public void updateTagTable(String oldTags, List<ConnectionTag> allTags,
                               List<String> connectionTags, int databaseId) {
        // Remove oldTags from connectionTags (no update),
        // Also remove unselected tags from oldTags
        if (!oldTags.equals("")) {

            String[] oldTagsArray = oldTags.split(",");
            List<String> oldTagsList = new ArrayList(Arrays.asList(oldTagsArray));

            Iterator<String> i = connectionTags.iterator();
            while (i.hasNext()) {
                String tag = i.next();
                if (oldTagsList.contains(tag)) {
                    i.remove();
                    oldTagsList.remove(tag);
                }
            }
        }

        // Bulk insert new tags into the Tags table
        if (connectionTags != null && !connectionTags.equals("") && connectionTags.size() != 0) {
            dataSource.insertBulkTags(connectionTags, databaseId);
        }


    }
}
