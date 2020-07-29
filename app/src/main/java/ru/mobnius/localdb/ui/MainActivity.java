package ru.mobnius.localdb.ui;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import ru.mobnius.localdb.App;
import ru.mobnius.localdb.HttpService;
import ru.mobnius.localdb.Names;
import ru.mobnius.localdb.R;
import ru.mobnius.localdb.Tags;
import ru.mobnius.localdb.adapter.LogAdapter;
import ru.mobnius.localdb.data.AvailableTimerTask;
import ru.mobnius.localdb.data.BaseActivity;
import ru.mobnius.localdb.data.HttpServerThread;
import ru.mobnius.localdb.data.OnHttpListener;
import ru.mobnius.localdb.data.OnLogListener;
import ru.mobnius.localdb.data.PreferencesManager;
import ru.mobnius.localdb.data.component.MySnackBar;
import ru.mobnius.localdb.data.exception.ExceptionCode;
import ru.mobnius.localdb.data.exception.FileExceptionManager;
import ru.mobnius.localdb.model.LogItem;
import ru.mobnius.localdb.model.Progress;
import ru.mobnius.localdb.model.Response;
import ru.mobnius.localdb.model.StorageName;
import ru.mobnius.localdb.utils.Loader;
import ru.mobnius.localdb.utils.NetworkUtil;
import ru.mobnius.localdb.utils.UrlReader;
import ru.mobnius.localdb.utils.VersionUtil;

public class MainActivity extends BaseActivity
        implements OnLogListener,
        AvailableTimerTask.OnAvailableListener,
        View.OnClickListener,
        OnHttpListener,
        DialogDownloadFragment.OnDownloadStorageListener {

    public static Intent getIntent(Context context) {
        return new Intent(context, MainActivity.class);
    }

    private LogAdapter mLogAdapter;
    private RecyclerView mRecyclerView;
    private Button btnStart;
    private Button btnStop;
    private TextView tvError;
    private MenuItem miSyncDB;
    private ScrollView svError;
    private UpdateFragment mUpdateFragment;
    private DialogDownloadFragment mDialogDownloadFragment;
    private ServerAppVersionAsyncTask mServerAppVersionAsyncTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Log.d(Names.TAG, "Запуск главного экрана");

        ((App) getApplication()).registryAvailableListener(this);
        ((App) getApplication()).registryLogListener(this);
        ((App) getApplication()).registryHttpListener(this);

        mUpdateFragment = (UpdateFragment) getSupportFragmentManager().findFragmentById(R.id.log_upload);

        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.app_name);

        mRecyclerView = findViewById(R.id.log_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mLogAdapter = new LogAdapter(this);
        mRecyclerView.setAdapter(mLogAdapter);
        tvError = findViewById(R.id.activity_main_error_message);
        svError = findViewById(R.id.activity_main_scroll_view);
        Button btnCloseError = findViewById(R.id.activity_main_close_error);
        btnCloseError.setOnClickListener(this);
        btnStart = findViewById(R.id.service_start);
        btnStart.setOnClickListener(this);
        btnStop = findViewById(R.id.service_stop);
        btnStop.setOnClickListener(this);
        String message = "";
        File root = FileExceptionManager.getInstance(this).getRootCatalog();
        String[] files = root.list();
        if (files != null) {
            for (String fileName : files) {
                byte[] bytes = FileExceptionManager.getInstance(this).readPath(fileName);
                if (bytes != null) {
                    message = new String(bytes);
                    if (message.length()>2000){
                        message = message.substring(0, 1000)+".........\n"+ message.substring(message.length()-1000, message.length()-1);
                    }
                    message = "При последнем запуске приложения возникла следующая критическая ошибка:\n" + message;

                    tvError.setText(message);
                    svError.setVisibility(View.VISIBLE);
                }
            }
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter(Tags.ERROR_TAG));
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter(Tags.CANCEL_TASK_TAG));
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        //  alert(getString(R.string.android_8));
        // }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        miSyncDB = menu.findItem(R.id.action_fias);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(SettingActivity.getIntent(this));
                return true;

            case R.id.action_fias:
                mDialogDownloadFragment = new DialogDownloadFragment(this);
                mDialogDownloadFragment.show(getSupportFragmentManager(), "storage");
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (PreferencesManager.getInstance().getProgress() != null) {
            mUpdateFragment.updateProcess(PreferencesManager.getInstance().getProgress());
            setMenuItemVisible(false);
        } else {
            setMenuItemVisible(true);
        }
        if(mUpdateFragment.isVisible()){
            setMenuItemVisible(false);
        }

        Objects.requireNonNull(getSupportActionBar()).setSubtitle(NetworkUtil.getIPv4Address() + ":" + HttpServerThread.HTTP_SERVER_PORT);

        if (PreferencesManager.getInstance().isDebug()) {
            btnStop.setVisibility(View.VISIBLE);
            btnStart.setVisibility(View.VISIBLE);
        } else {
            btnStop.setVisibility(View.GONE);
            btnStart.setVisibility(View.GONE);
        }

        mServerAppVersionAsyncTask = new ServerAppVersionAsyncTask();
        mServerAppVersionAsyncTask.execute();
    }

    protected void onDestroy() {
        super.onDestroy();
        if (mServerAppVersionAsyncTask != null) {
            mServerAppVersionAsyncTask.cancel(true);
            mServerAppVersionAsyncTask = null;
        }
        ((App) getApplication()).unRegistryLogListener(this);
        ((App) getApplication()).unRegistryAvailableListener(this);
        ((App) getApplication()).unRegistryHttpListener(this);
    }

    @Override
    public void onAddLog(final LogItem item) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mLogAdapter.addItem(item);
                mRecyclerView.scrollToPosition(mLogAdapter.getItemCount() - 1);
            }
        });
    }

    @Override
    public void onAvailable(final boolean available) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btnStart.setEnabled(!available);
                btnStop.setEnabled(available);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_main_close_error:
                svError.setVisibility(View.GONE);
                break;
            case R.id.service_start:
                startService(HttpService.getIntent(this, HttpService.MANUAL));
                break;

            case R.id.service_stop:
                stopService(HttpService.getIntent(this, HttpService.MANUAL));
                break;

            case R.id.log_cancel:
                String message = "После отмены процесс требуется выполнить заново. Остановить загрузку данных?";
                confirm(message, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DialogInterface.BUTTON_POSITIVE == which) {
                            Intent intent = new Intent(Tags.CANCEL_TASK_TAG);
                            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                            PreferencesManager.getInstance().setProgress(null);
                            mUpdateFragment.stopProcess();
                            setMenuItemVisible(true);
                        }
                    }
                });
                break;
        }
    }

    @Override
    public void onHttpRequest(UrlReader reader) {

    }

    @Override
    public void onHttpResponse(Response response) {

    }

    @Override
    public void onDownLoadProgress(UrlReader reader, Progress progress) {
        mUpdateFragment.updateProcess(progress);
    }

    @Override
    public void onDownLoadFinish(String tableName, UrlReader reader) {
        mUpdateFragment.stopProcess();
        setMenuItemVisible(true);
    }

    @Override
    public void onDownloadStorage(final StorageName name) {

        if (NetworkUtil.isNetworkAvailable(this)) {
            confirm("Убедительсь в стабильном подключении к сети интернет. Загрузить таблицу " + name.getName() + "?", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == DialogInterface.BUTTON_POSITIVE) {
                        mDialogDownloadFragment.dismiss();
                        if (svError.isShown()) {
                            svError.setVisibility(View.GONE);
                        }

                        mUpdateFragment.startProcess();
                        setMenuItemVisible(false);
                        startService(HttpService.getIntent(MainActivity.this, name.table));
                    }
                }
            });
        } else {
            alert("Нет подключения к интернету");
        }
    }

    @Override
    public int getExceptionCode() {
        return ExceptionCode.MAIN;
    }

    @SuppressLint("StaticFieldLeak")
    private class ServerAppVersionAsyncTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            try {
                return Loader.getInstance().version();
            } catch (IOException e) {
                return "0.0.0.0";
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                if (VersionUtil.isUpgradeVersion(MainActivity.this, s, PreferencesManager.getInstance().isDebug())) {
                    // тут доступно новая версия
                    MySnackBar.make(mRecyclerView, "Доступна новая версия " + s, Snackbar.LENGTH_LONG).setAction("Загрузить", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String url = Names.UPDATE_URL;
                            Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setData(Uri.parse(url));
                            startActivity(i);
                        }
                    }).show();
                }
            } catch (Exception ignored) {

            }
        }
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (Objects.requireNonNull(intent.getAction())) {
                case Tags.ERROR_TAG:
                    svError.setVisibility(View.VISIBLE);
                    tvError.setText(intent.getStringExtra(Tags.ERROR_TEXT));
                    break;
                case Tags.CANCEL_TASK_TAG:
                    if (mUpdateFragment != null && mUpdateFragment.isVisible()) {
                        mUpdateFragment.stopProcess();
                    }
                    break;
            }
        }
    };

    private void setMenuItemVisible(boolean visible) {
        if (miSyncDB != null) {
            miSyncDB.setVisible(visible);
        }
    }
}