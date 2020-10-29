package ru.mobnius.localdb.data.dowloadInfo;

public class TableInfoError {
    public final boolean isError;
    public final String errorMessage;

    public TableInfoError(boolean isError, String errorMessage){
        this.isError = isError;
        this.errorMessage = errorMessage;
    }
}
