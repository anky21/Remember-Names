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
    public static final String _ID = "_id";

    @DataType(DataType.Type.TEXT)
    @NotNull
    public static final String FIRST_NAME = "first_name";

    @DataType(DataType.Type.TEXT)
    @DefaultValue("Unknown")
    public static final String LAST_NAME = "last_name";

    @DataType(DataType.Type.TEXT)
    @DefaultValue("null")
    public static final String IMAGE_NAME = "image_name";

    @DataType(DataType.Type.TEXT)
    @DefaultValue("null")
    public static final String MEET_WHERE = "meet_venue";

    @DataType(DataType.Type.TEXT)
    @DefaultValue("null")
    public static final String APPEARANCE = "appearance";

    @DataType(DataType.Type.TEXT)
    @DefaultValue("null")
    public static final String FEATURE = "feature";

    @DataType(DataType.Type.TEXT)
    @DefaultValue("null")
    public static final String COMMON_FRIENDS = "common_friends";

    @DataType(DataType.Type.TEXT)
    @DefaultValue("null")
    public static final String DESCRIPTION = "description";
}
