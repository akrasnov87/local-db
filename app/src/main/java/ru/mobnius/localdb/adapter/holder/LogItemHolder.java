package ru.mobnius.localdb.adapter.holder;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import ru.mobnius.localdb.R;
import ru.mobnius.localdb.model.LogItem;
import ru.mobnius.localdb.utils.DateUtil;

public class LogItemHolder extends RecyclerView.ViewHolder {
    private TextView tvDate;
    private TextView tvMessage;

    private Context mContext;

    public LogItemHolder(Context context, @NonNull View itemView) {
        super(itemView);
        mContext = context;

        tvDate = itemView.findViewById(R.id.log_list_item_date);
        tvMessage = itemView.findViewById(R.id.log_list_item_message);
    }

    public void bind(LogItem item) {
        tvDate.setText(DateUtil.convertDateToUserString(item.getDate(), "HH:mm:ss"));
        tvMessage.setText(item.getMessage());

        if(item.isError()) {
            tvMessage.setTextColor(mContext.getResources().getColor(R.color.colorAccent));
        } else {
            tvMessage.setTextColor(mContext.getResources().getColor(R.color.colorPrimaryText));
        }
    }
}

