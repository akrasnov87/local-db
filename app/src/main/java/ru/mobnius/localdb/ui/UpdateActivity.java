package ru.mobnius.localdb.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.util.Objects;

import ru.mobnius.localdb.BuildConfig;
import ru.mobnius.localdb.R;
import ru.mobnius.localdb.data.PreferencesManager;
import ru.mobnius.localdb.request.VersionRequestListener;

public class UpdateActivity extends AppCompatActivity {

    private Button btnUpdateLocalDB;
    private Button btnUpdateMO;
    private TextView tvUpdateInfo;
    private final int LOCAL_DB_REQUEST_CODE = 87;
    private final int MO_REQUEST_CODE = 86;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);
        tvUpdateInfo = findViewById(R.id.update_info);
        btnUpdateLocalDB = findViewById(R.id.update_localDB);
        btnUpdateMO = findViewById(R.id.update_MO);
        btnUpdateLocalDB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                installApk(VersionRequestListener.LOCAL_DB_APK, LOCAL_DB_REQUEST_CODE);
            }
        });
        btnUpdateMO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                installApk(VersionRequestListener.MO_APK, MO_REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (PreferencesManager.getInstance().isAuthorized()) {
            if (PreferencesManager.getInstance().isLocalDBReadyToUpdate()) {
                btnUpdateLocalDB.setVisibility(View.VISIBLE);
            } else {
                btnUpdateLocalDB.setVisibility(View.GONE);
            }
            if (PreferencesManager.getInstance().isMOReadyToUpdate()) {
                btnUpdateMO.setVisibility(View.VISIBLE);
            } else {
                btnUpdateMO.setVisibility(View.GONE);
            }
        }else {
            tvUpdateInfo.setText("Чтобы устновить обновления сначала необходимо авторизоваться");
        }
    }

    private void installApk(String apkType, int requestCode) {

        File folder = new File(Objects.requireNonNull(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)).toString());

        File file = new File(folder.getAbsolutePath(), apkType);
        final Uri uri = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) ?
                FileProvider.getUriForFile(UpdateActivity.this, BuildConfig.APPLICATION_ID + ".provider", file) : Uri.fromFile(file);
        Intent install = new Intent(Intent.ACTION_INSTALL_PACKAGE)
                .setData(uri)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        if (requestCode == LOCAL_DB_REQUEST_CODE) {
            PreferencesManager.getInstance().setLocalDBReadyToUpdate(false);
        }
        if (requestCode == MO_REQUEST_CODE) {
            PreferencesManager.getInstance().setMOReadyToUpdate(false);
        }
        startActivity(install);

    }

}
