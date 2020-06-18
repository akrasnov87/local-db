package ru.mobnius.localdb.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ru.mobnius.localdb.R;
import ru.mobnius.localdb.adapter.holder.LogItemHolder;
import ru.mobnius.localdb.model.LogItem;

public class LogAdapter extends RecyclerView.Adapter<LogItemHolder> {
    private List<LogItem> mList;
    private final Context mContext;

    public LogAdapter(Context context) {
        mContext = context;
        mList = new ArrayList<>();
    }

    public void addItem(LogItem item) {
        int MAX_COUNT = 100;
        int size = mList.size();
        if(size > MAX_COUNT) {
            for(int i = size - 1; ; i-- ) {
                if(MAX_COUNT / 2 > i) {
                    break;
                }
                mList.remove(i);
            }
        }
        mList.add(item);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LogItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.log_list_item, parent, false);
        return new LogItemHolder(mContext, view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogItemHolder holder, int position) {
        LogItem itemModel = mList.get(position);
        holder.bind(itemModel);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }
}
