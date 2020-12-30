package ru.mobnius.localdb.data.dowloadInfo;

public class TableInfo {
    public final String actualVersion;
    public final int size;
    public final int fileCount;
    public final int hddSize;
    public final int totalRows;

    public TableInfo(String actualVersion, int size, int fileCount, int hddSize, int totalRows) {
        this.actualVersion = actualVersion;
        this.size = size;
        this.fileCount = fileCount;
        this.hddSize = hddSize;
        this.totalRows = totalRows;
    }
}
