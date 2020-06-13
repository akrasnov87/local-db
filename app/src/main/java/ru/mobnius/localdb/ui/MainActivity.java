package ru.mobnius.localdb.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
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
import ru.mobnius.localdb.data.HttpServerThread;
import ru.mobnius.localdb.data.OnLogListener;
import ru.mobnius.localdb.data.PreferencesManager;
import ru.mobnius.localdb.data.component.MySnackBar;
import ru.mobnius.localdb.model.LogItem;
import ru.mobnius.localdb.utils.Loader;
import ru.mobnius.localdb.utils.NetworkUtil;
import ru.mobnius.localdb.utils.VersionUtil;

public class MainActivity extends AppCompatActivity
        implements OnLogListener,
        AvailableTimerTask.OnAvailableListener,
        View.OnClickListener {

    private static String TAG = "LOCAL_DB";

    private boolean doubleBackToExitPressedOnce = false;

    private LogAdapter mLogAdapter;
    private RecyclerView mRecyclerView;
    private Button btnStart;
    private Button btnStop;
    private UpdateFragment mUpdateFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.app_name);
        getSupportActionBar().setSubtitle(NetworkUtil.getIPv4Address() + ":" + HttpServerThread.HTTP_SERVER_PORT);

        ((App)getApplication()).registryAvailableListener(this);
        ((App)getApplication()).registryLogListener(this);

        mRecyclerView = findViewById(R.id.log_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mLogAdapter = new LogAdapter(this);
        mRecyclerView.setAdapter(mLogAdapter);

        btnStart = findViewById(R.id.service_start);
        btnStart.setOnClickListener(this);
        btnStop = findViewById(R.id.service_stop);
        btnStop.setOnClickListener(this);

        mUpdateFragment = (UpdateFragment)getSupportFragmentManager().findFragmentById(R.id.log_upload);
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
                mUpdateFragment.startProcess("iserv", "iserv");
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

    protected void onDestroy() {
        super.onDestroy();
        ((App)getApplication()).unRegistryLogListener(this);
        ((App)getApplication()).unRegistryAvailableListener(this);
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
        }
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