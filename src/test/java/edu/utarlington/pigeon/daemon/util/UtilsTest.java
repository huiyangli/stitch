package edu.utarlington.pigeon.daemon.util;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class UtilsTest {

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testHashCode() {
        String requestId = "10.0.0.58_12";
        String taskId = "20";

        String requestId1 = "10.0.0.58_20";
        String taskId1 = "12";
        double key = Utils.hashCode(requestId, taskId);
        double key1 = Utils.hashCode(requestId1, taskId1);
        assertNotSame(key1, key);
        assertEquals(key, key);
    }
}