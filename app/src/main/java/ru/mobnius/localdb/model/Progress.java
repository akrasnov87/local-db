package ru.mobnius.localdb.model;

public class Progress {

    public Progress(int current, int total) {
        this.current = current;
        this.total = total;
    }

    public int current;
    public int total;

    public double getPercent() {
        double result = (double) (current * 100) / total;
        if(result > 100) {
            result = 100;
        }
        return result;
    }
}
