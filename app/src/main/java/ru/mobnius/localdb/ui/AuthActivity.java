package ru.mobnius.localdb.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.Objects;

import ru.mobnius.localdb.R;
import ru.mobnius.localdb.data.BaseActivity;
import ru.mobnius.localdb.data.PreferencesManager;

public class AuthActivity extends BaseActivity {

    private boolean doubleBackToExitPressedOnce = false;

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

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            finishAffinity();
            finish();
            super.onBackPressed();
            return;
        }

        doubleBackToExitPressedOnce = true;

        Toast.makeText(this, getString(R.string.sign_out_message), Toast.LENGTH_LONG).show();

        int TOAST_DURATION = 2750;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, TOAST_DURATION);
    }
}