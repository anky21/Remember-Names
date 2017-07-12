package me.anky.connectid.root;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import me.anky.connectid.data.ConnectionsDataSource;
import me.anky.connectid.data.EditDataSource;
import me.anky.connectid.data.source.local.ConnectionsLocalRepository;

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
    @Singleton
    ConnectionsDataSource provideConnectionsRepository(Context context) {
        //return new ConnectionsLocalRepository(context);
        return connectionsLocalRepository;
    }

    @Provides
    @Singleton
    EditDataSource provideEditRepository() {
        return connectionsLocalRepository;
    }
}
