package me.anky.connectid.editTag;

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
    public void createNewTag(String input, List<String> connectionTags) {
        int existingTagPosition = -1;
        if (!input.equals("")){
            if (connectionTags.size() == 0){
                connectionTags.add(input);
            } else {
                for (int i=0; i<connectionTags.size();i++){
                    if (connectionTags.get(i).equals(input)){
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
}
