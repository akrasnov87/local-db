package ru.mobnius.localdb;

public interface Tags {
    String ERROR_TAG = "error_tag";
    String CANCEL_TASK_TAG = "cancel_task_tag";

    String ERROR_TEXT = "Error_text";

    String ERROR_TYPE = "error_type";

    String AUTH_ERROR = "auth_error";
    String SQL_ERROR = "sql_error";
    String RPC_ERROR = "rpc_error";
    String STORAGE_ERROR = "storage_error";
    String CRITICAL_ERROR = "critical_error";
}
