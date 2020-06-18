package ru.mobnius.localdb.utils;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class VersionUtilTest {
    private Context mContext;

    @Before
    public void setUp() {
        mContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    @Test
    public void getVersionName() {
        assertNotEquals(VersionUtil.getVersionName(mContext), "0.0.0.0");
    }

    @Test
    public void getShortVersionName() {
        assertNotEquals(VersionUtil.getShortVersionName(mContext), "0.0");
    }

    @Test
    public void isUpgradeVersion() {
        String max = "1.99999.3.0";
        String max_debug = "1.99999.0.0";
        String min = "1.0.0.0";
        String min_debug = "1.0.3.0";

        assertTrue(VersionUtil.isUpgradeVersion(mContext, max, true));

        assertFalse(VersionUtil.isUpgradeVersion(mContext, min, false));
        assertFalse(VersionUtil.isUpgradeVersion(mContext, min, true));

        assertTrue(VersionUtil.isUpgradeVersion(mContext, max_debug, true));

        assertFalse(VersionUtil.isUpgradeVersion(mContext, min_debug, false));
        assertFalse(VersionUtil.isUpgradeVersion(mContext, min_debug, true));
    }
}