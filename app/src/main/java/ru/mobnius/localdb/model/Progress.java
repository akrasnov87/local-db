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

    public double getPercent() {
        double result = (double) (current * 100) / total;
        if(result > 100) {
            result = 100;
        }
        return result;
    }
}
