package ru.mobnius.localdb.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.util.Objects;

import ru.mobnius.localdb.R;
import ru.mobnius.localdb.data.BaseActivity;
import ru.mobnius.localdb.data.PreferencesManager;

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

        if(PreferencesManager.getInstance().isAuthorized()) {
            startActivity(MainActivity.getIntent(this));
        }
    }
}