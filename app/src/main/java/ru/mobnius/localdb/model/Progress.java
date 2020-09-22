package ru.mobnius.localdb.model;

public class Progress {

    public Progress(int current, int total, String tableName) {
        this.current = current;
        this.total = total;
        this.tableName = tableName;
    }

    public int current;
    public final int total;
    public final String tableName;
    private int filesCount;

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

    public double getPercent() {
        double result = (double) (current * 100) / total;
        if(result > 100) {
            result = 100;
        }
        return result;
    }
}
