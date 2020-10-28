package ru.mobnius.localdb.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ru.mobnius.localdb.App;
import ru.mobnius.localdb.HttpService;
import ru.mobnius.localdb.R;
import ru.mobnius.localdb.adapter.ErrorsAdapter;
import ru.mobnius.localdb.storage.ClientErrors;


public class ErrorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error);
        RecyclerView rvErrors = findViewById(R.id.rv_errors);
        App app = (App) getApplicationContext();
        if (HttpService.getDaoSession()!=null) {
            if (HttpService.getDaoSession().getClientErrorsDao()!=null) {
                List<ClientErrors> errors = HttpService.getDaoSession().getClientErrorsDao().loadAll();
                if (errors.size() == 0) {
                    TextView tvNoErrors = findViewById(R.id.tv_no_errors);
                    tvNoErrors.setVisibility(View.VISIBLE);
                } else {
                    rvErrors.setLayoutManager(new LinearLayoutManager(this));
                    rvErrors.setAdapter(new ErrorsAdapter(errors));
                }
            }
        }
    }
}
