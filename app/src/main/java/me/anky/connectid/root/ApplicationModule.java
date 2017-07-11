package me.anky.connectid.root;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import me.anky.connectid.data.ConnectionsDataSource;
import me.anky.connectid.data.ConnectionsRepository;

/**
 * Created by Anky An on 10/07/2017.
 * anky25@gmail.com
 */

@Module
public class ApplicationModule {

    private final ConnectidApplication application;

    public ApplicationModule(ConnectidApplication application){
        this.application = application;
    }

    @Provides
    @Singleton
    public Context provideContext(){
        return application;
    }

    @Provides
    @Singleton
    ConnectionsDataSource provideConnectionsRepository(Context context) {
        return new ConnectionsRepository(context);
    }
}
