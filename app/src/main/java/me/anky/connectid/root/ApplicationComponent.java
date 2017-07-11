package me.anky.connectid.root;

import javax.inject.Singleton;

import dagger.Component;
import me.anky.connectid.connections.ConnectionsActivity;

/**
 * Created by Anky An on 10/07/2017.
 * anky25@gmail.com
 */

@Singleton
@Component(modules = {ApplicationModule.class})
public interface ApplicationComponent {

    void inject(ConnectidApplication application);

    void inject(ConnectionsActivity connectionsActivity);
}
