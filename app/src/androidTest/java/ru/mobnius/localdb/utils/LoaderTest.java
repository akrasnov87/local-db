package ru.mobnius.localdb.utils;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class LoaderTest {

    @Test
    public void authTest() {
        Loader loader = Loader.getInstance();
        loader.auth("iserv", "iserv");
        assertTrue(loader.isAuthorized());
    }

    @Test
    public void versionTest() throws IOException {
        Loader loader = Loader.getInstance();
        String version = loader.version();
        assertNotEquals(version, "0.0.0.0");
    }
}