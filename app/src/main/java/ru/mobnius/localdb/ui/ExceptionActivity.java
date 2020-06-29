package ru.mobnius.localdb.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class ExceptionActivity extends AppCompatActivity {
    public final static String EXCEPTION_MESSAGE_KEY = "exception_message_key";

    public static Intent getExceptionActivityIntent(Context context, String exceptionMessage){
        Intent intent = new Intent(context, ExceptionActivity.class);
        intent.putExtra(EXCEPTION_MESSAGE_KEY, exceptionMessage);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        if (getIntent().hasExtra(EXCEPTION_MESSAGE_KEY)){
            String message = getIntent().getStringExtra(EXCEPTION_MESSAGE_KEY);
            String formattedMessage = message;
            if (message != null) {
                formattedMessage = message.replace("\\n", "\r\n");
            }
            LinearLayout layout = new LinearLayout(this);
            layout.setGravity(Gravity.START);
            layout.setOrientation(LinearLayout.VERTICAL);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            TextView exceptionTV = new TextView(this);
            exceptionTV.setText(formattedMessage);
            ScrollView scroller = new ScrollView(getApplicationContext());
            scroller.addView(exceptionTV);
            layout.addView(scroller, params);
            setContentView(layout);
        }else {
            Toast.makeText(this, "Не удалось получить текст ошибки", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}