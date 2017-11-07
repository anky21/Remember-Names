package me.anky.connectid.selectedConnections;

import java.util.List;

import javax.inject.Inject;

import me.anky.connectid.data.ConnectionsDataSource;

/**
 * Created by Anky An on 8/11/2017.
 * anky25@gmail.com
 */

public class SelectedConnectionsActivityPresenter implements SelectedConnectionsActivityMVP.Presenter {
    private SelectedConnectionsActivityMVP.View view;
    private ConnectionsDataSource dataSource;

    @Inject
    public SelectedConnectionsActivityPresenter(ConnectionsDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setView(SelectedConnectionsActivityMVP.View view) {
            this.view = view;
    }

    @Override
    public void loadConnections(List<String> idsList) {

    }

    @Override
    public void unsubscribe() {

    }
}
