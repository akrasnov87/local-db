package ru.mobnius.localdb.utils;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import ru.mobnius.localdb.model.UI_SV_FIAS.UI_SV_FIAS_Result;

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

    @Test
    public void rpc() {
        Loader loader = Loader.getInstance();
        loader.auth("iserv", "iserv");
        UI_SV_FIAS_Result[] results = loader.rpc("Domain.UI_SV_FIAS", "Query", "[{}]", UI_SV_FIAS_Result[].class);
        assertNotNull(results);
        assertTrue(results[0].result.total > 0);
        results = loader.rpc("Domain.UI_SV_FIAS", "Query", "[]", UI_SV_FIAS_Result[].class);
        assertNull(results);
    }
}