package ru.mobnius.localdb.model;

public class Progress {

    public Progress(int current, int total, String tableName) {
        this.current = current;
        this.total = total;
        this.tableName = tableName;
    }

    public final int current;
    public final int total;
    public final String tableName;
    private int filesCount;

    public int getDownloadRowsCount() {
        return downloadRowsCount;
    }

    public void setDownloadRowsCount(int downloadRowsCount) {
        this.downloadRowsCount = downloadRowsCount;
    }

    private int downloadRowsCount;

    public int getFilesCount() {
        return filesCount;
    }

    public void setFilesCount(int filesCount) {
        this.filesCount = filesCount;
    }


}
