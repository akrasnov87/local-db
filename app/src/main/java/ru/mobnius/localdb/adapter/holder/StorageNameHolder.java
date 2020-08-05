package ru.mobnius.localdb.adapter.holder;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;

import ru.mobnius.localdb.Names;
import ru.mobnius.localdb.R;
import ru.mobnius.localdb.data.PreferencesManager;
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
        Button btnClearData = itemView.findViewById(R.id.item_clear_data);
        btnClearData.setOnClickListener(this);

        itemView.setOnClickListener(this);
    }

    public void bind(StorageName storageName) {
        mStorageName = storageName;
        tvDescription.setText(storageName.description);
        tvTable.setText(storageName.table);
        String rowCount = PreferencesManager.getInstance().getTableRowCount(storageName.table);
        if (rowCount != null) {
            tvCount.setText(rowCount);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.item_clear_data) {
            mListener.onClearData(mStorageName);
        } else {
            mListener.onDownloadStorage(mStorageName);
        }
    }
}
