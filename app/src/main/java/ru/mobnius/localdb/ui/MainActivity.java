package ru.mobnius.localdb.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.util.Objects;

import ru.mobnius.localdb.App;
import ru.mobnius.localdb.HttpService;
import ru.mobnius.localdb.Names;
import ru.mobnius.localdb.R;
import ru.mobnius.localdb.adapter.LogAdapter;
import ru.mobnius.localdb.data.AvailableTimerTask;
import ru.mobnius.localdb.data.BaseActivity;
import ru.mobnius.localdb.data.HttpServerThread;
import ru.mobnius.localdb.data.OnHttpListener;
import ru.mobnius.localdb.data.OnLogListener;
import ru.mobnius.localdb.data.PreferencesManager;
import ru.mobnius.localdb.data.component.MySnackBar;
import ru.mobnius.localdb.model.LogItem;
import ru.mobnius.localdb.model.Progress;
import ru.mobnius.localdb.model.Response;
import ru.mobnius.localdb.storage.FiasDao;
import ru.mobnius.localdb.utils.Loader;
import ru.mobnius.localdb.utils.NetworkUtil;
import ru.mobnius.localdb.utils.UrlReader;
import ru.mobnius.localdb.utils.VersionUtil;

public class MainActivity extends BaseActivity
        implements OnLogListener,
        AvailableTimerTask.OnAvailableListener,
        View.OnClickListener,
        OnHttpListener {

    public static Intent getIntent(Context context) {
        return new Intent(context, MainActivity.class);
    }


    private LogAdapter mLogAdapter;
    private RecyclerView mRecyclerView;
    private Button btnStart;
    private Button btnStop;
    private UpdateFragment mUpdateFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(Names.TAG, "Запуск главного экрана");

        ((App)getApplication()).registryAvailableListener(this);
        ((App)getApplication()).registryLogListener(this);
        ((App)getApplication()).registryHttpListener(this);

        mUpdateFragment = (UpdateFragment)getSupportFragmentManager().findFragmentById(R.id.log_upload);

        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.app_name);
        getSupportActionBar().setSubtitle(NetworkUtil.getIPv4Address() + ":" + HttpServerThread.HTTP_SERVER_PORT);

        mRecyclerView = findViewById(R.id.log_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mLogAdapter = new LogAdapter(this);
        mRecyclerView.setAdapter(mLogAdapter);

        btnStart = findViewById(R.id.service_start);
        btnStart.setOnClickListener(this);
        btnStop = findViewById(R.id.service_stop);
        btnStop.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(SettingActivity.getIntent(this));
                return true;

            case R.id.action_fias:
                mUpdateFragment.startProcess();
                startService(HttpService.getIntent(this, FiasDao.TABLENAME));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(PreferencesManager.getInstance().isDebug()) {
            btnStop.setVisibility(View.VISIBLE);
            btnStart.setVisibility(View.VISIBLE);
        } else {
            btnStop.setVisibility(View.GONE);
            btnStart.setVisibility(View.GONE);
        }

        new ServerAppVersionAsyncTask().execute();
    }

    protected void onDestroy() {
        super.onDestroy();
        ((App)getApplication()).unRegistryLogListener(this);
        ((App)getApplication()).unRegistryAvailableListener(this);
        ((App)getApplication()).unRegistryHttpListener(this);
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
                        if(DialogInterface.BUTTON_POSITIVE == which) {
                            PreferencesManager.getInstance().setProgress(null);
                            mUpdateFragment.stopProcess();
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
    public void onDownLoadFinish(UrlReader reader) {
        mUpdateFragment.stopProcess();
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

            if(VersionUtil.isUpgradeVersion(MainActivity.this, s, PreferencesManager.getInstance().isDebug())) {
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
        }
    }
}