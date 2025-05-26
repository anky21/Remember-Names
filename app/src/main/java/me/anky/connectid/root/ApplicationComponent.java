package me.anky.connectid.root;

import javax.inject.Singleton;

import dagger.Component;
import me.anky.connectid.connections.ConnectionsActivity;
import me.anky.connectid.connections.ConnectionsActivityPresenter;
import me.anky.connectid.data.SharedPrefsHelper;
import me.anky.connectid.details.DetailsActivity;
import me.anky.connectid.edit.EditActivity;
import me.anky.connectid.editTag.EditTagActivity;
import me.anky.connectid.selectedConnections.SelectedConnectionsActivity;
import me.anky.connectid.tags.TagsActivity;

/**
 * Created by Anky An on 10/07/2017.
 * anky25@gmail.com
 */

@Singleton
@Component(modules = {ApplicationModule.class})
public interface ApplicationComponent {

    void inject(ConnectidApplication application);

    void inject(ConnectionsActivityPresenter target);

    void inject(ConnectionsActivity connectionsActivity);

    void inject(EditActivity editActivity);

    void inject(DetailsActivity detailsActivity);

    void inject(EditTagActivity editTagActivity);

    void inject(TagsActivity tagsActivity);

    void inject(SelectedConnectionsActivity selectedConnectionsActivity);

    void inject(me.anky.connectid.settings.SettingsActivity settingsActivity); // Added for SettingsActivity

    SharedPrefsHelper getPreferenceHelper();
}
