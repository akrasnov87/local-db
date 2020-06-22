package ru.mobnius.localdb.utils;

import android.content.Context;
import android.os.Build;
import android.os.Environment;

import java.io.File;

public class FileUtil {
    /**
     *
     * @param documentFolder Environment.DIRECTORY_PICTURES | Environment.DIRECTORY_DOCUMENTS
     */
    @SuppressWarnings({"deprecation"})
    public static File getRoot(Context context, String documentFolder) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return Environment.getExternalStorageDirectory();
        } else {
            return context.getExternalFilesDir(documentFolder);
        }
    }
}

