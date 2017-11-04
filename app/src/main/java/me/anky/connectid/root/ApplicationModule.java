package me.anky.connectid.root;

import android.content.Context;
import android.content.SharedPreferences;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import me.anky.connectid.connections.ConnectionsActivityMVP;
import me.anky.connectid.connections.ConnectionsActivityPresenter;
import me.anky.connectid.data.ConnectionsDataSource;
import me.anky.connectid.data.source.local.ConnectionsLocalRepository;
import me.anky.connectid.details.DetailsActivityMVP;
import me.anky.connectid.details.DetailsActivityPresenter;
import me.anky.connectid.edit.EditActivityMVP;
import me.anky.connectid.edit.EditActivityPresenter;
import me.anky.connectid.editTag.EditTagActivityMVP;
import me.anky.connectid.editTag.EditTagActivityPresenter;
import me.anky.connectid.tags.TagsActivityMVP;
import me.anky.connectid.tags.TagsActivityPresenter;

/**
 * Created by Anky An on 10/07/2017.
 * anky25@gmail.com
 */

@Module
public class ApplicationModule {

    private final ConnectidApplication application;

    private ConnectionsLocalRepository connectionsLocalRepository;

    public ApplicationModule(ConnectidApplication application){
        this.application = application;

        connectionsLocalRepository = new ConnectionsLocalRepository(application);
    }

    @Provides
    @Singleton
    public Context provideContext(){
        return application;
    }

    @Provides
    public ConnectionsActivityMVP.Presenter provideConnectionsActivityPresenter(
            ConnectionsDataSource connectionsDataSource){
        return new ConnectionsActivityPresenter(connectionsDataSource);
    }

    @Provides
    public DetailsActivityMVP.Presenter provideDetailsActivityPresenter(ConnectionsDataSource connectionsDataSource){
        return new DetailsActivityPresenter(connectionsDataSource);
    }

    @Provides
    public EditActivityMVP.Presenter provideEditActivityPresenter(ConnectionsDataSource connectionsDataSource){
        return new EditActivityPresenter(connectionsDataSource);
    }

    @Provides
    public EditTagActivityMVP.Presenter provideEditTagActivityPresenter(ConnectionsDataSource dataSource){
        return new EditTagActivityPresenter(dataSource);
    }

    @Provides
    public TagsActivityMVP.Presenter provideTagsActivityPresenter(ConnectionsDataSource dataSource){
        return new TagsActivityPresenter(dataSource);
    }

    @Provides
    @Singleton
    ConnectionsDataSource provideConnectionsDataSource(Context context) {
        return connectionsLocalRepository;
    }

    @Provides
    SharedPreferences provideSharedPrefs() {
        return application.getSharedPreferences("shared-prefs", Context.MODE_PRIVATE);
    }
}
