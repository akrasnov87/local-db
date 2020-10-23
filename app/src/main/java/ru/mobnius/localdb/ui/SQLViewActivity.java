package ru.mobnius.localdb.ui;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.greenrobot.greendao.database.Database;

import java.util.Objects;

import ru.mobnius.localdb.HttpService;
import ru.mobnius.localdb.R;
import ru.mobnius.localdb.data.BaseActivity;
import ru.mobnius.localdb.data.SqlQueryAsyncTask;
import ru.mobnius.localdb.data.exception.ExceptionCode;

public class SQLViewActivity extends BaseActivity implements TextWatcher, SqlQueryAsyncTask.OnSqlQuery {
    private EditText etQuery;
    private TextView tvList;
    private MenuItem miStartQuery;

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
        miStartQuery = menu.findItem(R.id.sql_query_action);
        setIconEnabled(etQuery.getText().length() != 0);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                returnToPrevious();
                break;
            case R.id.sql_query_action:
                String query = etQuery.getText().toString();
                Database database = HttpService.getDaoSession().getDatabase();
                startProgress();
                SqlQueryAsyncTask asyncTask = new SqlQueryAsyncTask(database, this);
                asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, query);
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
        if (miStartQuery != null) {
            setIconEnabled(s.length() != 0);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    @Override
    public void onSqlQueryCompleted(final String queryResult, final boolean isError) {
        tvList.setText(queryResult);
        tvList.setTextColor(getResources().getColor(isError ? R.color.colorErrorText: R.color.colorPrimaryText));
        setIconEnabled(true);
        etQuery.addTextChangedListener(SQLViewActivity.this);
        stopProgress();
    }

    private void setIconEnabled(boolean enabled) {
        if (enabled) {
            miStartQuery.setEnabled(true);
            miStartQuery.getIcon().setAlpha(200);
        } else {
            miStartQuery.setEnabled(false);
            miStartQuery.getIcon().setAlpha(50);
        }
    }

}

