package me.anky.connectid.events;

/**
 * Created by Anky An on 31/10/2017.
 * anky25@gmail.com
 */

public class SetToUpdateTagTable {
    private  int databaseId = -1;

    public SetToUpdateTagTable(int databaseId) {
        this.databaseId = databaseId;
    }

    public int getDatabaseId() {
        return databaseId;
    }
}
