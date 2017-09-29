package me.anky.connectid.data;

/**
 * Created by Anky An on 30/09/2017.
 * anky25@gmail.com
 */

public class ConnectionTag {
    private int databaseId = -1;
    private String tag;
    private String connection_ids;

    public ConnectionTag(int databaseId, String tag, String connection_ids) {
        this.databaseId = databaseId;
        this.tag = tag;
        this.connection_ids = connection_ids;
    }

    public ConnectionTag(String tag, String connection_ids) {
        this.tag = tag;
        this.connection_ids = connection_ids;
    }

    public int getDatabaseId() {
        return databaseId;
    }

    public void setDatabaseId(int databaseId) {
        this.databaseId = databaseId;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getConnection_ids() {
        return connection_ids;
    }

    public void setConnection_ids(String connection_ids) {
        this.connection_ids = connection_ids;
    }
}
