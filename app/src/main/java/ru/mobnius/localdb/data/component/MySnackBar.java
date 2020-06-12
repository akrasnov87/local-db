package ru.mobnius.localdb.data.component;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.snackbar.Snackbar;

import ru.mobnius.localdb.R;

public class MySnackBar {
    public static Snackbar make(@NonNull View view, @NonNull CharSequence text, int duration) {
        Snackbar snackbar = Snackbar.make(view, text, duration);
        View v = snackbar.getView();
        TextView tv = v.findViewById(com.google.android.material.R.id.snackbar_text);
        tv.setTextColor(v.getContext().getResources().getColor(R.color.colorSecondaryText));
        return snackbar;
    }
}