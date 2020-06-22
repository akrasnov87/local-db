package ru.mobnius.localdb.utils;

import org.junit.Test;

import static org.junit.Assert.*;

public class StringUtilTest {

    @Test
    public void md5() {
        assertNotNull(StringUtil.md5("test"));
    }

    @Test
    public void exceptionToString() {
        Exception exception = new Exception("my test exception");
        String value = StringUtil.exceptionToString(exception);
        assertTrue(value.contains("java.lang.Exception: my test exception"));
    }
}
