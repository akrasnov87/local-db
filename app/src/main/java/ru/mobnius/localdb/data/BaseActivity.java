package ru.mobnius.localdb.data;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import ru.mobnius.localdb.R;

public abstract class BaseActivity extends ExceptionInterceptActivity {
    private final int REQUEST_PERMISSIONS = 1;
    private int mPermissionLength = 0;
    private boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // предназначено для привязки перехвата ошибок
        onExceptionIntercept();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            String[] permissions = new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE };
            mPermissionLength = permissions.length;

            ActivityCompat.requestPermissions(this,
                    permissions,
                    REQUEST_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.length == mPermissionLength) {
                boolean allGrant = true;
                for (int grant : grantResults) {
                    if (grant != PackageManager.PERMISSION_GRANTED) {
                        allGrant = false;
                        break;
                    }
                }

                if (!allGrant) {
                    Toast.makeText(this, getText(R.string.not_permissions), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, getText(R.string.permissions_grant), Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, getText(R.string.not_permissions), Toast.LENGTH_LONG).show();
            }
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

        Toast.makeText(this, "Нажмите повторно для выхода из приложения.", Toast.LENGTH_LONG).show();

        int TOAST_DURATION = 2750;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, TOAST_DURATION);
    }

    protected void confirm(String message, DialogInterface.OnClickListener listener) {
        AlertDialog dialog = new AlertDialog.Builder(this).create();
        dialog.setTitle("Сообщение");
        dialog.setMessage(message);
        dialog.setCancelable(false);
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.yes), listener);
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.no), listener);
        dialog.setIcon(R.drawable.ic_baseline_warning_24);
        dialog.show();
    }

    @SuppressWarnings("SameParameterValue")
    protected void alert(String message) {
        new android.app.AlertDialog.Builder(this)
                .setIcon(R.drawable.ic_baseline_info_24)
                .setMessage(message)
                .setPositiveButton("OK", null).show();
    }
}
