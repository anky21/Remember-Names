package me.anky.connectid.details;

import me.anky.connectid.BasePresenter;

public interface DetailsContract {

    interface Presenter extends BasePresenter {

        void editConnection();

        void deleteConnection();
    }
}
