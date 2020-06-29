package ru.mobnius.localdb.data;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

import ru.mobnius.localdb.data.exception.ExceptionGroup;
import ru.mobnius.localdb.data.exception.FileExceptionManager;
import ru.mobnius.localdb.data.exception.OnExceptionIntercept;
import ru.mobnius.localdb.data.exception.MyUncaughtExceptionHandler;
import ru.mobnius.localdb.ui.ExceptionActivity;

/**
 * абстрактный класс для реализации обработчиков ошибок
 */
public abstract class ExceptionInterceptActivity extends AppCompatActivity implements OnExceptionIntercept {

    public void onExceptionIntercept() {
        Thread.setDefaultUncaughtExceptionHandler(new MyUncaughtExceptionHandler(Thread.getDefaultUncaughtExceptionHandler(), getExceptionGroup(), getExceptionCode(), this));
        File root = FileExceptionManager.getInstance(this).getRootCatalog();
        String[] files = root.list();
        if (files != null) {
            for (String fileName : files) {
                byte[] bytes = FileExceptionManager.getInstance(this).readPath(fileName);
                if (bytes != null) {
                    finish();
                    String message = new String(bytes);
                    startActivity(ExceptionActivity.getExceptionActivityIntent(this, message));
                }
            }
        }

    }

    @Override
    public String getExceptionGroup() {
        return ExceptionGroup.USER_INTERFACE;
    }

    /**
     * Числовой код ошибки из IExceptionCode
     * @return строка
     */
    public abstract int getExceptionCode();
}