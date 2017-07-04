package me.anky.connectid.data;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.ConflictResolutionType;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.NotNull;
import net.simonvt.schematic.annotation.PrimaryKey;
import net.simonvt.schematic.annotation.Unique;

public interface ConnectidColumns {

    @DataType(DataType.Type.INTEGER) @PrimaryKey @AutoIncrement
    public static final String _ID = "_id";

    @DataType(DataType.Type.TEXT) @NotNull @Unique(onConflict = ConflictResolutionType.IGNORE)
    public static final String NAME = "name";

    @DataType(DataType.Type.TEXT) @NotNull
    public static final String DESCRIPTION = "description";

    // TODO: Implement later or remove
//    @DataType(DataType.Type.INTEGER) @NotNull
//    public static final String IMAGE_RESOURCE = "image_resource";
}
