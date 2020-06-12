package ru.mobnius.localdb.utils;

import org.junit.Test;

import static org.junit.Assert.*;

public class NetworkUtilTest {

    @Test
    public void getIPAddress() {
        assertNotEquals(NetworkUtil.getIPv4Address(), "");
    }
}