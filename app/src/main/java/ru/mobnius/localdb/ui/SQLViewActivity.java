package ru.mobnius.localdb.ui;

import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.greenrobot.greendao.database.Database;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import ru.mobnius.localdb.AutoRunReceiver;
import ru.mobnius.localdb.R;

public class SQLViewActivity extends AppCompatActivity implements View.OnClickListener, TextWatcher {
    private EditText etQuery;
    private TextView tvList;
    private Button btnQuery;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sql_view);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Работа с базой данных");
        btnQuery = findViewById(R.id.sql_viewer_query);
        btnQuery.setOnClickListener(this);
        etQuery = findViewById(R.id.sql_viewer_edit_text);
        etQuery.addTextChangedListener(this);
        tvList = findViewById(R.id.sql_viewer_main_list);
        tvList.setMovementMethod(new ScrollingMovementMethod());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.sql_viewer_query) {
            tvList.setTextColor(Color.BLACK);
            String query = etQuery.getText().toString();
            Database database = AutoRunReceiver.getDaoSession().getDatabase();
            JSONArray array = getResults(database, query);
            if (array == null) {
                return;
            }
            if (array.length() == 0) {
                errorText("Результат пуст");
                return;
            }
            try {
                String s = array.toString(4);
                tvList.setText(s);
            } catch (JSONException e) {
                e.printStackTrace();
                errorText(e.toString());
            }
        }
    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        btnQuery.setEnabled(count != 0);
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    private void errorText(String message) {
        tvList.setText(message);
        tvList.setTextColor(getResources().getColor(R.color.colorAccent));
    }

    /**
     * Преобразование результата запроса в JSON
     *
     * @param database БД
     * @param query    запрос
     * @return объект JSON
     */
    public JSONArray getResults(Database database, String query) {
        Cursor cursor;
        try {
            cursor = database.rawQuery(query, null);
        } catch (SQLException e) {
            errorText(e.toString());
            return null;
        }
        JSONArray resultSet = new JSONArray();
        int x = 0;
        cursor.moveToFirst();
        while (!cursor.isAfterLast() && x < 100) {
            int totalColumn = cursor.getColumnCount();
            JSONObject rowObject = new JSONObject();
            x++;
            for (int i = 0; i < totalColumn; i++) {
                if (cursor.getColumnName(i) != null) {
                    try {
                        if (cursor.getString(i) != null) {
                            rowObject.put(cursor.getColumnName(i), cursor.getString(i));
                        } else {
                            rowObject.put(cursor.getColumnName(i), "");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        errorText(e.toString());
                    }
                }
            }
            resultSet.put(rowObject);
            cursor.moveToNext();
        }
        cursor.close();
        return resultSet;
    }
}

