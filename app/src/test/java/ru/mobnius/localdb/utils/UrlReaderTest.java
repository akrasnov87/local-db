package ru.mobnius.localdb.utils;

import org.junit.Test;

import static org.junit.Assert.*;

public class UrlReaderTest {
    //GET /?idx=0 HTTP/1.1

    @Test
    public void getParts() {
        UrlReader urlReader = new UrlReader("GET /?idx=0 HTTP/1.1");
        assertEquals(urlReader.getParts().length, 3);
    }

    @Test
    public void getMethod() {
        UrlReader urlReader = new UrlReader("GET /?idx=0 HTTP/1.1");
        assertEquals(urlReader.getMethod(), "GET");
    }

    @Test
    public void getSegments() {
        UrlReader urlReader = new UrlReader("GET /?idx=0 HTTP/1.1");
        String[] segments = urlReader.getSegments();
        assertEquals(segments.length, 1);
        assertEquals("?idx=0", segments[0]);

        urlReader = new UrlReader("GET /12/25 HTTP/1.1");
        segments = urlReader.getSegments();
        assertEquals("12", segments[0]);
        assertEquals("25", segments[1]);
    }

    @Test
    public void getParams() {
        UrlReader urlReader = new UrlReader("GET /?idx=0 HTTP/1.1");
        assertEquals(urlReader.getParams().length, 1);
        assertEquals(urlReader.getParam("idx"), "0");

        urlReader = new UrlReader("GET /?idx=0&t=9 HTTP/1.1");
        assertEquals(urlReader.getParams().length, 2);
        assertEquals(urlReader.getParam("idx"), "0");
        assertEquals(urlReader.getParam("t"), "9");
        assertNull(urlReader.getParam("m"));
    }
}