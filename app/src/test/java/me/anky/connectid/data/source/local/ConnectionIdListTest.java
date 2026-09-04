package me.anky.connectid.data.source.local;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ConnectionIdListTest {
    @Test
    public void removesOnlyTheExactConnectionId() {
        assertEquals("12, 31", ConnectionIdList.remove("1, 12, 31", 1));
    }

    @Test
    public void returnsNullWhenNoIdsRemain() {
        assertNull(ConnectionIdList.remove("7, 7", 7));
    }

    @Test
    public void normalizesSpacingAndDuplicateIds() {
        assertEquals("2, 3", ConnectionIdList.remove(" 2, 2, 4, 3 ", 4));
    }
}
