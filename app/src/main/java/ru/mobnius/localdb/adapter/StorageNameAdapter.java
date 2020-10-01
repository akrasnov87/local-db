package ru.mobnius.localdb.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ru.mobnius.localdb.R;
import ru.mobnius.localdb.adapter.holder.StorageNameHolder;
import ru.mobnius.localdb.model.StorageName;
import ru.mobnius.localdb.ui.DialogDownloadFragment;
import ru.mobnius.localdb.utils.StorageUtil;

public class StorageNameAdapter extends RecyclerView.Adapter<StorageNameHolder> {
    private final Context mContext;
    private final List<StorageName> mList;
    private final DialogDownloadFragment.OnDownloadStorageListener mListener;
    private final StorageNameHolder.OnDeleteTableListener mOnDeleteTableListener;

    public StorageNameAdapter(Context context, DialogDownloadFragment.OnDownloadStorageListener listener,
                              StorageNameHolder.OnDeleteTableListener onDeleteTableListener) {
        mContext = context;
        mListener = listener;
        mList = new ArrayList<>();
        mList.addAll(Arrays.asList(StorageUtil.getStorage(context, "ru.mobnius.localdb.storage")));
        mOnDeleteTableListener = onDeleteTableListener;
    }

    @NonNull
    @Override
    public StorageNameHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.item_download, parent, false);
        return new StorageNameHolder(view, mListener, mOnDeleteTableListener);
    }

    @Override
    public void onBindViewHolder(@NonNull StorageNameHolder holder, int position) {
        holder.bind(mList.get(position));
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }
}
