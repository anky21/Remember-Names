package me.anky.connectid.data.source.local;

import net.simonvt.schematic.annotation.Database;
import net.simonvt.schematic.annotation.Table;

@Database(version = ConnectidDatabase.VERSION)
public final class ConnectidDatabase {
    private ConnectidDatabase(){}

    public static final int VERSION = 2;

    @Table(ConnectidColumns.class)
    public static final String CONNECTIONS = "connections";

    @Table(TagsColumns.class)
    public static final String TAGS = "tags";
}
