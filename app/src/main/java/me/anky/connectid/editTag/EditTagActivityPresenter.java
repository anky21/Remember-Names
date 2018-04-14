package me.anky.connectid.editTag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import me.anky.connectid.Utilities;
import me.anky.connectid.data.ConnectionTag;
import me.anky.connectid.data.ConnectionsDataSource;

/**
 * Created by anky on 2/10/17.
 */

public class EditTagActivityPresenter implements EditTagActivityMVP.Presenter {
    private final static String TAG = "EditTagActivityPresenter";

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
        Utilities.eventsOneParam("name", input, "Create New Tag");
        Utilities.logFirebaseEvents("Create New Tag", input);

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
                        break;
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
                                Utilities.logFirebaseError("error_load_tags", TAG + ".loadTags", e.getMessage());
                            }
                        });
        compositeDisposable.add(disposableSingleObserver);
    }

    @Override
    public void unsubscribe() {
        compositeDisposable.clear();
    }

    @Override
    public void insertBulkNewTags(List<String> connectionTags, List<ConnectionTag> allTags) {
        // Bulk insert new tags into the Tags table
        if (connectionTags != null && connectionTags.size() != 0) {
            if (allTags != null && allTags.size() != 0) {
                for (ConnectionTag tag : allTags) {
                    if (connectionTags.contains(tag.getTag())){
                        connectionTags.remove(tag.getTag());
                    }
                }
            }
            dataSource.insertBulkNewTags(connectionTags);
        }
    }

    @Override
    public void updateConnectionTags(int id, List<String> connectionTags) {
        if (connectionTags == null || connectionTags.size() == 0) {
            dataSource.updateConnection(id, null);
        } else {
            String tagString = connectionTags.toString();
            tagString = tagString.substring(1, tagString.length() - 1);

            dataSource.updateConnection(id, tagString);
        }
    }

    @Override
    public void updateTagTable(String oldTags, List<ConnectionTag> allTags,
                               List<String> connectionTags, int databaseId) {
        List<String> oldTagsList = null;
        // Remove oldTags from connectionTags (no update),
        // Have oldTagsList with just unselected tags
        if (oldTags != null && !oldTags.equals("")) {

            String[] oldTagsArray = oldTags.split(", ");
            oldTagsList = new ArrayList(Arrays.asList(oldTagsArray));

            Iterator<String> i = connectionTags.iterator();
            while (i.hasNext()) {
                String tag = i.next();
                if (oldTagsList.contains(tag)) {
                    i.remove();
                    oldTagsList.remove(tag);
                }
            }
        }

        // Update unselected tags from old tags in the Tags table
        if (oldTagsList != null && oldTagsList.size() != 0) {
            for (ConnectionTag tag : allTags) {
                if (oldTagsList.contains(tag.getTag())) {
                    String[] databaseIdsArray = tag.getConnection_ids().split(", ");
                    List<String> databaseIdsList = new ArrayList(Arrays.asList(databaseIdsArray));
                    databaseIdsList.remove(String.valueOf(databaseId));
                    String databaseIdsString = Utilities.createStringFromList(databaseIdsList);
                    ConnectionTag modifiedTag = new ConnectionTag(tag.getDatabaseId(), tag.getTag(), databaseIdsString);
                    dataSource.updateTag(modifiedTag);
                }
            }
        }

        // Update selected existing tags
        if (connectionTags != null && connectionTags.size() != 0){
            for (ConnectionTag connectionTag : allTags) {
                if (connectionTags.contains(connectionTag.getTag())) {
                    String ids = connectionTag.getConnection_ids();
                    if (ids == null || ids.length() == 0){
                        ids = String.valueOf(databaseId);
                    } else {
                        ids = ids + ", " + String.valueOf(databaseId);
                    }
                    connectionTag.setConnection_ids(ids);
                    dataSource.updateTag(connectionTag);

                    // Remove this tag from connectionTags List (not a new tag)
                    connectionTags.remove(connectionTag.getTag());
                }
            }
        }

        // Bulk insert new tags into the Tags table
        if (connectionTags != null && connectionTags.size() != 0) {
            dataSource.insertBulkTags(connectionTags, databaseId);
        }
    }
}
