package ru.mobnius.localdb.ui;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;

import org.greenrobot.greendao.database.Database;

import java.util.Objects;

import ru.mobnius.localdb.HttpService;
import ru.mobnius.localdb.R;
import ru.mobnius.localdb.data.BaseActivity;
import ru.mobnius.localdb.data.SqlQueryAsyncTask;

public class SQLViewActivity extends BaseActivity implements TextWatcher, SqlQueryAsyncTask.OnSqlQuery {
    private EditText etQuery;
    private TextView tvList;
    private MenuItem miQuery;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sql_view);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Работа с базой данных");
        etQuery = findViewById(R.id.sql_viewer_edit_text);
        etQuery.addTextChangedListener(this);
        tvList = findViewById(R.id.sql_viewer_main_list);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sql_query_menu, menu);
        miQuery = menu.findItem(R.id.sql_query_action);
        setIconEnabled(etQuery.getText().length() != 0);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.sql_query_action:
                String query = etQuery.getText().toString();
                Database database = HttpService.getDaoSession().getDatabase();
                startProgress(getDrawable(R.drawable.ic_query_24dp), "Подождите идет чтение БД");
                SqlQueryAsyncTask asyncTask = new SqlQueryAsyncTask(database, this);
                asyncTask.execute(query);
                setIconEnabled(false);
                etQuery.removeTextChangedListener(this);
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (miQuery != null) {
            setIconEnabled(s.length() != 0);
        }

    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    @Override
    public void onSqlQueryCompleted(final String queryResult, final boolean isError) {
        tvList.setText(queryResult);
        tvList.setTextColor(isError ? Color.RED : Color.BLACK);
        setIconEnabled(true);
        etQuery.addTextChangedListener(SQLViewActivity.this);
        stopProgress();
    }

    private void setIconEnabled(boolean enabled) {
        if (enabled) {
            miQuery.setEnabled(true);
            miQuery.getIcon().setAlpha(200);
        } else {
            miQuery.setEnabled(false);
            miQuery.getIcon().setAlpha(50);
        }
    }
}

