package ru.mobnius.localdb.data;

import androidx.appcompat.app.AppCompatActivity;

import ru.mobnius.localdb.data.exception.ExceptionGroup;
import ru.mobnius.localdb.data.exception.MyUncaughtExceptionHandler;
import ru.mobnius.localdb.data.exception.OnExceptionIntercept;
/**
 * абстрактный класс для реализации обработчиков ошибок
 */
public abstract class ExceptionInterceptActivity extends AppCompatActivity implements OnExceptionIntercept {

    public void onExceptionIntercept() {
        Thread.setDefaultUncaughtExceptionHandler(new MyUncaughtExceptionHandler(Thread.getDefaultUncaughtExceptionHandler(), getExceptionGroup(), getExceptionCode(), this));
    }

    @Override
    public String getExceptionGroup() {
        return ExceptionGroup.USER_INTERFACE;
    }

    /**
     * Числовой код ошибки из IExceptionCode
     *
     * @return строка
     */
    public abstract int getExceptionCode();
}