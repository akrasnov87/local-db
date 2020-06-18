package ru.mobnius.localdb.adapter.holder;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import ru.mobnius.localdb.R;
import ru.mobnius.localdb.model.QueryResult;

public class QueryResultHolder extends RecyclerView.ViewHolder {
    private TextView tvText;
    private ImageView ivImage;

    public QueryResultHolder(@NonNull View itemView) {
        super(itemView);
        tvText = itemView.findViewById(R.id.query_item_text_view);
        ivImage = itemView.findViewById(R.id.query_item_image_view);
    }

    public void bind(QueryResult result) {
        tvText.setText(result.getValues());
        if (result.getBytes() != null) {
            ivImage.setVisibility(View.VISIBLE);
            Bitmap bitmap = BitmapFactory.decodeByteArray(result.getBytes(), 0, result.getBytes().length);
            ivImage.setImageBitmap(bitmap);
        }
    }
}
