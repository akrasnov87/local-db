package ru.mobnius.localdb.data;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import ru.mobnius.localdb.R;

public abstract class BaseActivity extends AppCompatActivity {
    private final int REQUEST_PERMISSIONS = 1;
    private int mPermissionLength = 0;

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

    protected void alert(String message) {
        new android.app.AlertDialog.Builder(this)
                .setIcon(R.drawable.ic_baseline_info_24)
                .setMessage(message)
                .setPositiveButton("OK", null).show();
    }
}
