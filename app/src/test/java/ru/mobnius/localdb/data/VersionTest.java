package ru.mobnius.localdb.data;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

public class VersionTest {

    @Test
    public void isValid() {
        Version version = new Version();
        assertTrue(version.isValid("4.21.0.520"));
        assertFalse(version.isValid("4.21.0"));
        assertFalse(version.isValid("4.21.0.1500"));
        assertFalse(version.isValid("4.21.4.520"));
    }

    @Test
    public void isEmpty() {
        Version version = new Version();
        assertFalse(version.isEmpty("4.21.0.520"));
        assertTrue(version.isEmpty("0.0.0.0"));
    }

    @Test
    public void getBuildDate() {
        Version version = new Version();
        Date buildDate = version.getBuildDate(Version.BIRTH_DAY, "4.4.0.520");
        assertEquals(buildDate.getTime(), Long.parseLong("1592286000000"));
    }

    @Test
    public void getVersionState() {
        Version version = new Version();
        assertEquals(Version.ALPHA, version.getVersionState("4.21.0.520").intValue());
        assertEquals(Version.BETA, version.getVersionState("4.21.1.520").intValue());
        assertEquals(Version.RELEASE_CANDIDATE, version.getVersionState("4.21.2.520").intValue());
        assertEquals(Version.PRODUCTION, version.getVersionState("4.21.3.520").intValue());
    }

    @Test
    public void getVersionParts() {
        Version version = new Version();
        assertEquals(4, version.getVersionParts("4.21.0.520").length);
    }
}