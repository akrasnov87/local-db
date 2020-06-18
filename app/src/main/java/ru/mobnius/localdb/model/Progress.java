package ru.mobnius.localdb.model;

public class Progress {

    public Progress(int current, int total, String tableName) {
        this.current = current;
        this.total = total;
        this.tableName = tableName;
    }

    public int current;
    public int total;
    public String tableName;

    public double getPercent() {
        double result = (double) (current * 100) / total;
        if(result > 100) {
            result = 100;
        }
        return result;
    }
}
