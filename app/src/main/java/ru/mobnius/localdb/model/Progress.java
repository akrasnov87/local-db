package ru.mobnius.localdb.model;

public class Progress {

    public Progress(int current, int total, String tableName, String version) {
        this.current = current;
        this.total = total;
        this.tableName = tableName;
        this.version = version;
    }

    public int current;
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

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    private String fileName;
    public final String version;

    public double getPercent() {
        double result = (double) (current * 100) / total;
        if(result > 100) {
            result = 100;
        }
        return result;
    }
}
