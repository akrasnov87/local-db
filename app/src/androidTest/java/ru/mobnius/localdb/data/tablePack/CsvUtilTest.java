package ru.mobnius.localdb.data.tablePack;

import org.junit.Test;

import static org.junit.Assert.*;

public class CsvUtilTest {

    private String input1 = "LINK|C_NAME\n123|Саша";
    private String input2 = "LINK|C_NAME|C_NUMBER\n123||12";
    private String input3 = "LINK|C_NAME|C_NUMBER\n|Саша|12";
    private String input4 = "LINK|C_NAME|C_NUMBER\n123|Саша|";
    private String input5 = "LINK|C_NAME|C_NUMBER\n123|Саша|\n12|Кирилл|1";

    @Test
    public void convert() {
        Table table = CsvUtil.convert(input1);

        assertNull(CsvUtil.convert(null));
        assertNull(CsvUtil.convert(""));

        assertNotNull(table);
        assertEquals(table.getHeaders().length, 2);
        assertEquals(table.getValues().length, 1);
        assertEquals(table.getValue(0)[0], "123");
        assertEquals(table.getValue(0)[1], "Саша");

        table = CsvUtil.convert(input2);

        assertNull(CsvUtil.convert(null));
        assertNull(CsvUtil.convert(""));

        assertNotNull(table);
        assertEquals(table.getHeaders().length, 3);
        assertEquals(table.count(), 1);
        assertEquals(table.getValue(0)[0], "123");
        assertEquals(table.getValue(0)[1], "");
        assertEquals(table.getValue(0)[2], "12");

        table = CsvUtil.convert(input3);

        assertNull(CsvUtil.convert(null));
        assertNull(CsvUtil.convert(""));

        assertNotNull(table);
        assertEquals(table.getHeaders().length, 3);
        assertEquals(table.count(), 1);
        assertEquals(table.getValue(0)[0], "");
        assertEquals(table.getValue(0)[1], "Саша");
        assertEquals(table.getValue(0)[2], "12");

        table = CsvUtil.convert(input4);

        assertNull(CsvUtil.convert(null));
        assertNull(CsvUtil.convert(""));

        assertNotNull(table);
        assertEquals(table.getHeaders().length, 3);
        assertEquals(table.count(), 1);
        assertEquals(table.getValue(0)[0], "123");
        assertEquals(table.getValue(0)[1], "Саша");
        assertNull(table.getValue(0)[2]);

        table = CsvUtil.convert(input5);

        assertNull(CsvUtil.convert(null));
        assertNull(CsvUtil.convert(""));

        assertNotNull(table);
        assertEquals(table.getHeaders().length, 3);
        assertEquals(table.count(), 2);
        assertEquals(table.getValue(0)[0], "123");
        assertEquals(table.getValue(1)[0], "12");
    }
}