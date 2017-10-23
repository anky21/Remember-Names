package me.anky.connectid.editTag;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

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
    public EditTagActivityPresenter(ConnectionsDataSource dataSource){
        this.dataSource = dataSource;
    }

    public void setView(EditTagActivityMVP.View view) {
        this.view = view;
    }

    @Override
    public void createNewTag(String input, List<String> connectionTags, List<ConnectionTag> allTags) {
        int existingTagPosition = -1;

        // If the input is already in the allTags with different cases, use the existing one
        if (!input.equals("")){
            if (allTags != null && allTags.size() > 0){
                for (ConnectionTag allTagsItem : allTags){
                    if(allTagsItem.getTag().equalsIgnoreCase(input)){
                        input = allTagsItem.getTag();
                    }
                }
            }

            if (connectionTags.size() == 0){
                connectionTags.add(input);
            } else {
                for (int i=0; i<connectionTags.size();i++){
                    if (connectionTags.get(i).equalsIgnoreCase(input)){
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
                        if (connectionTags.isEmpty()){
                            view.displayNoTags();
                        }else {
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
        if (connectionTags == null || connectionTags.size() == 0){
            return;
        } else {
            StringBuffer tagString = new StringBuffer();
            for (String tag : connectionTags){
                tagString.append(tag).append(",");
            }
            dataSource.updateConnection(id, tagString.toString());
        }
    }

    @Override
    public void updateTagTable(String oldTags, List<ConnectionTag> allTags,
                               List<String> connectionTags, int databaseId) {
        // Remove oldTags from connectionTags (no need to update these tags)
        if(!oldTags.equals("")){
            List<Integer> positions = new ArrayList<>();
            List<String> removedTags = new ArrayList<>();
            String[] oldTagsArray = oldTags.split(",");
            for (String tag : oldTagsArray){
//                Log.v("testing", "old tag is " + tag);
                for (int i=0; i<connectionTags.size(); i++){
                    if (tag != null && !tag.trim().equals("")
                            && tag.trim().equalsIgnoreCase(connectionTags.get(i))){
                        positions.add(i);
                    }
                }
            }
            if (positions != null && positions.size()>0){
                for (int i=0; i<positions.size(); i++){
//                    Log.v("testing", "remove " + connectionTags.get(i));
                    connectionTags.remove(positions.get(i));
                }
            }
        }
    }
}
