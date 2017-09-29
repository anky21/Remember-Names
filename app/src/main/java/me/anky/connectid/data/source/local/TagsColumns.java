package me.anky.connectid.data.source.local;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.DefaultValue;
import net.simonvt.schematic.annotation.NotNull;
import net.simonvt.schematic.annotation.PrimaryKey;

/**
 * Created by Anky An on 30/09/2017.
 * anky25@gmail.com
 */

public interface TagsColumns {

    @DataType(DataType.Type.INTEGER)
    @PrimaryKey
    @AutoIncrement
    String _ID = "_id";

    @DataType(DataType.Type.TEXT)
    @NotNull
    String TAG = "tag";

    @DataType(DataType.Type.TEXT)
    @DefaultValue("null")
    String CONNECTION_IDS = "connection_ids";
}
