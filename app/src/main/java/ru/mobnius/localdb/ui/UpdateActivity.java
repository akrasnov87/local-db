package ru.mobnius.localdb.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.util.Objects;

import ru.mobnius.localdb.BuildConfig;
import ru.mobnius.localdb.R;
import ru.mobnius.localdb.data.PreferencesManager;
import ru.mobnius.localdb.request.VersionRequestListener;
import ru.mobnius.localdb.utils.VersionUtil;

public class UpdateActivity extends AppCompatActivity {

    private Button btnUpdateLocalDB;
    private Button btnUpdateMO;
    private TextView tvUpdateInfo;
    private final int LOCAL_DB_REQUEST_CODE = 87;
    private final int MO_REQUEST_CODE = 86;
    private boolean isTryingToUpdateLDB = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Установка обновлений");
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.update_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.update_exit) {
            if (PreferencesManager.getInstance().isAuthorized()) {
                startActivity(MainActivity.getIntent(this));
            } else {
                finish();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isTryingToUpdateLDB) {
            boolean isNotUpdated = VersionUtil.isUpgradeVersion(this, PreferencesManager.getInstance().getRemoteLocalDBVersion(), true);
            if (isNotUpdated) {
                PreferencesManager.getInstance().setLocalDBReadyToUpdate(true);
                isTryingToUpdateLDB = false;
            }
        }
        if (!PreferencesManager.getInstance().isLocalDBReadyToUpdate() && !PreferencesManager.getInstance().isMOReadyToUpdate()) {
            String noUpdates = "Нет доступных  для установки обновлений";
            tvUpdateInfo.setText(noUpdates);
        } else {
            if (PreferencesManager.getInstance().isLocalDBReadyToUpdate() && PreferencesManager.getInstance().isMOReadyToUpdate()) {
                String message = "Вам доступны обновления LocalDB и Мобильного обходчика. Сначала необходимо установить обновление для Мобильного обходчика";
                tvUpdateInfo.setText(message);
                btnUpdateMO.setVisibility(View.VISIBLE);
                btnUpdateLocalDB.setEnabled(false);
                btnUpdateLocalDB.setVisibility(View.VISIBLE);
            } else {
                String oneUpdate = "Вам доступно обновление ";
                if (PreferencesManager.getInstance().isLocalDBReadyToUpdate()) {
                    btnUpdateLocalDB.setVisibility(View.VISIBLE);
                    btnUpdateLocalDB.setEnabled(true);
                    oneUpdate = oneUpdate + "LocalDB до версии " + PreferencesManager.getInstance().getRemoteLocalDBVersion();
                    tvUpdateInfo.setText(oneUpdate);
                } else {
                    btnUpdateLocalDB.setVisibility(View.GONE);
                }
                if (PreferencesManager.getInstance().isMOReadyToUpdate()) {
                    btnUpdateMO.setVisibility(View.VISIBLE);
                    oneUpdate = oneUpdate + " Мобильного обходчика до версии " + PreferencesManager.getInstance().getRemoteMOVersion();
                    tvUpdateInfo.setText(oneUpdate);
                } else {
                    btnUpdateMO.setVisibility(View.GONE);
                }
            }
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
            btnUpdateLocalDB.setVisibility(View.GONE);
            isTryingToUpdateLDB = true;
            startActivity(install);
        }
        if (requestCode == MO_REQUEST_CODE) {
            btnUpdateMO.setVisibility(View.GONE);
            PreferencesManager.getInstance().setMOReadyToUpdate(false);
            startActivity(install);
        }
    }

}
