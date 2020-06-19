package ru.mobnius.localdb.adapter.holder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;

import ru.mobnius.localdb.Names;
import ru.mobnius.localdb.R;
import ru.mobnius.localdb.data.StorageDataCountAsyncTask;
import ru.mobnius.localdb.model.StorageName;
import ru.mobnius.localdb.ui.DialogDownloadFragment;

public class StorageNameHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener {

    private final TextView tvDescription;
    private final TextView tvTable;
    private StorageName mStorageName;
    private final TextView tvCount;
    private final DialogDownloadFragment.OnDownloadStorageListener mListener;

    public StorageNameHolder(@NonNull View itemView, DialogDownloadFragment.OnDownloadStorageListener listener) {
        super(itemView);
        mListener = listener;
        tvDescription = itemView.findViewById(R.id.item_download_description);
        tvTable = itemView.findViewById(R.id.item_download_table);
        tvCount = itemView.findViewById(R.id.item_download_count);

        itemView.setOnClickListener(this);
    }

    public void bind(StorageName storageName) {
        mStorageName = storageName;
        tvDescription.setText(storageName.description);
        tvTable.setText(storageName.table);

        new StorageDataCountAsyncTask(new StorageDataCountAsyncTask.OnStorageCountListener() {
            @Override
            public void onStorageCount(Long count) {
                DecimalFormat df = new DecimalFormat(Names.INT_FORMAT);
                tvCount.setText(df.format(count));
            }
        }).execute(storageName.table);
    }

    @Override
    public void onClick(View v) {
        mListener.onDownloadStorage(mStorageName);
    }
}
