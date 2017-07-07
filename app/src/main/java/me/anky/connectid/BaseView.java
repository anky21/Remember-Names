package me.anky.connectid;

public interface BaseView<T extends BasePresenter> {

    void setPresenter(T presenter);

}