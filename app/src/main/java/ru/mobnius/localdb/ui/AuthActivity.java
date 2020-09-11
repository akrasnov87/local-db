package ru.mobnius.localdb.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import java.io.File;
import java.util.Objects;

import ru.mobnius.localdb.R;
import ru.mobnius.localdb.data.BaseActivity;
import ru.mobnius.localdb.data.HttpServerThread;
import ru.mobnius.localdb.data.PreferencesManager;
import ru.mobnius.localdb.data.exception.ExceptionCode;
import ru.mobnius.localdb.data.exception.FileExceptionManager;
import ru.mobnius.localdb.utils.NetworkUtil;

/**
 * Проверка авторизации приложения
 */
public class AuthActivity extends BaseActivity {

    public static Intent getIntent(Context context) {
        return new Intent(context, AuthActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Авторизация");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Objects.requireNonNull(getSupportActionBar()).setSubtitle(NetworkUtil.getIPv4Address() + ":" + HttpServerThread.HTTP_SERVER_PORT);

            if (PreferencesManager.getInstance().isAuthorized()) {
                startActivity(MainActivity.getIntent(this));
            }
    }

    @Override
    public int getExceptionCode() {
        return ExceptionCode.AUTH;
    }
}