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

    @DataType(DataType.Type.INTEGER)
    @NotNull
    @DefaultValue("0")
    String FLASHCARD_BOX = "flashcard_box";

    @DataType(DataType.Type.INTEGER)
    @NotNull
    @DefaultValue("0")
    String FLASHCARD_LAST_REVIEWED = "flashcard_last_reviewed";

    @DataType(DataType.Type.INTEGER)
    @NotNull
    @DefaultValue("0")
    String FLASHCARD_NEXT_REVIEW = "flashcard_next_review";

    @DataType(DataType.Type.INTEGER)
    @NotNull
    @DefaultValue("0")
    String FLASHCARD_ATTEMPTS = "flashcard_attempts";

    @DataType(DataType.Type.INTEGER)
    @NotNull
    @DefaultValue("0")
    String FLASHCARD_CORRECT = "flashcard_correct";
}
