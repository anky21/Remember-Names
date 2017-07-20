package me.anky.connectid.root;

import android.content.Context;

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
    public ConnectionsActivityMVP.Presenter provideConnectionsActivityPresenter(ConnectionsDataSource connectionsDataSource){
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
    @Singleton
    ConnectionsDataSource provideConnectionsDataSource(Context context) {
        return connectionsLocalRepository;
    }
}
