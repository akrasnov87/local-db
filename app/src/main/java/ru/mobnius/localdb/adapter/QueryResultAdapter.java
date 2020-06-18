package ru.mobnius.localdb.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ru.mobnius.localdb.R;
import ru.mobnius.localdb.adapter.holder.QueryResultHolder;

public class QueryResultAdapter extends RecyclerView.Adapter<QueryResultHolder> {
    private Context mContext;
    private List<String> mList;
    public QueryResultAdapter (Context context, List<String> list) {
        mContext = context;
        mList = list;
    }

    @NonNull
    @Override
    public QueryResultHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.query_item, parent, false);
        return new QueryResultHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull QueryResultHolder holder, int position) {
        holder.bind(mList.get(position));
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }
}
