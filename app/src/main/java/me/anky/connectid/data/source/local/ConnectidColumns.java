package me.anky.connectid.data.source.local;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.DefaultValue;
import net.simonvt.schematic.annotation.NotNull;
import net.simonvt.schematic.annotation.PrimaryKey;

public interface ConnectidColumns {

    @DataType(DataType.Type.INTEGER)
    @PrimaryKey
    @AutoIncrement
    String _ID = "_id";

    @DataType(DataType.Type.TEXT)
    @NotNull
    String FIRST_NAME = "first_name";

    @DataType(DataType.Type.TEXT)
    @DefaultValue("Unknown")
    String LAST_NAME = "last_name";

    @DataType(DataType.Type.TEXT)
    String IMAGE_NAME = "image_name";

    @DataType(DataType.Type.TEXT)
    @DefaultValue("null")
    String MEET_WHERE = "meet_venue";

    @DataType(DataType.Type.TEXT)
    @DefaultValue("null")
    String APPEARANCE = "appearance";

    @DataType(DataType.Type.TEXT)
    @DefaultValue("null")
    String FEATURE = "feature";

    @DataType(DataType.Type.TEXT)
    @DefaultValue("null")
    String COMMON_FRIENDS = "common_friends";

    @DataType(DataType.Type.TEXT)
    @DefaultValue("null")
    String DESCRIPTION = "description";

    @DataType(DataType.Type.TEXT)
    @DefaultValue("null")
    String TAGS = "tags";
}
