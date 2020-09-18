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
    public final String version;

    public double getPercent() {
        double result = (double) (current * 100) / total;
        if(result > 100) {
            result = 100;
        }
        return result;
    }
}
