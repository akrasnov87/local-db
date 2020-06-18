package ru.mobnius.localdb.ui;

import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.greendao.database.Database;

import java.util.ArrayList;
import java.util.Objects;

import ru.mobnius.localdb.R;
import ru.mobnius.localdb.adapter.QueryResultAdapter;
import ru.mobnius.localdb.model.QueryResult;
import ru.mobnius.localdb.storage.DbOpenHelper;
import ru.mobnius.localdb.utils.SQLValidator;
import ru.mobnius.localdb.utils.SQLFieldTypeChecker;

public class SQLViewer extends AppCompatActivity implements View.OnClickListener, TextWatcher {
    private final int MAX_RECYCLER_VIEW_LENGTH = 100;
    private EditText etQuery;
    private Button btnQuery;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sql_viewer);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Работа с базой данных");
        btnQuery = findViewById(R.id.sql_viewer_query);
        btnQuery.setOnClickListener(this);
        etQuery = findViewById(R.id.sql_viewer_edit_text);
        etQuery.addTextChangedListener(this);
        mRecyclerView = findViewById(R.id.sql_viewer_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
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
            String query = etQuery.getText().toString();
            String queryValidated = SQLValidator.primitiveMatcher(query);
            if (queryValidated.isEmpty()) {
                Toast.makeText(this, "Запрос построен не правильно", Toast.LENGTH_SHORT).show();
                return;
            }
            String tableCheckedQuery = SQLValidator.getRightQuery(queryValidated);
            if (tableCheckedQuery.equals(SQLValidator.NO_COLUMN) || tableCheckedQuery.equals(SQLValidator.NO_TABLE)) {
                Toast.makeText(this, tableCheckedQuery, Toast.LENGTH_SHORT).show();
                return;
            }
            Database database = (new DbOpenHelper(this, "local-db.db").getWritableDb());
            try {
                Cursor cursor = database.rawQuery(tableCheckedQuery, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        int columnCount = cursor.getColumnCount();
                        ArrayList<QueryResult> list = new ArrayList<>();
                        int x = 0;
                        do {
                            StringBuilder s = new StringBuilder();
                            byte[] bytes = null;
                            for (int i = 0; i < columnCount; i++) {
                                Object o = SQLFieldTypeChecker.getType(cursor, i);
                                if (o == null) {
                                    continue;
                                }
                                if (o instanceof String) {
                                    s.append(cursor.getColumnName(i)).append(": ").append(o).append("\n");
                                }
                                if (o instanceof byte[]) {
                                    bytes = (byte[]) o;
                                }
                            }
                            String g = s.toString().substring(0, s.length() - 1);
                            QueryResult result = new QueryResult(g,bytes);
                            list.add(result);
                            x++;
                        } while (cursor.moveToNext() && x < MAX_RECYCLER_VIEW_LENGTH);

                        mRecyclerView.setAdapter(new QueryResultAdapter(this, list));
                    } else {
                        Toast.makeText(this, "Результат пуст", Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (SQLiteException e) {
                Toast.makeText(this, "Ошибка чтения базы данных: " + e, Toast.LENGTH_SHORT).show();
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
}

