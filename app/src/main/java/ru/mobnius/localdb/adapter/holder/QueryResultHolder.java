package ru.mobnius.localdb.adapter.holder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import ru.mobnius.localdb.R;

public class QueryResultHolder extends RecyclerView.ViewHolder {
   private TextView tvText;

    public QueryResultHolder(@NonNull View itemView) {
        super(itemView);
        tvText = itemView.findViewById(R.id.query_item_text_view);
    }

    public void bind(String s) {
        tvText.setText(s);
    }
}
