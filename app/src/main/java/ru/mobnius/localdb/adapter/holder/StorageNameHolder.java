package ru.mobnius.localdb.adapter.holder;

import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import ru.mobnius.localdb.R;
import ru.mobnius.localdb.data.PreferencesManager;
import ru.mobnius.localdb.model.StorageName;
import ru.mobnius.localdb.ui.DialogDownloadFragment;

public class StorageNameHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener {

    private final TextView tvDescription;
    private final TextView tvTable;
    private StorageName mStorageName;
    private final TextView tvCount;
    private final ProgressBar pbProgress;
    private final Button bClearData;
    private final DialogDownloadFragment.OnDownloadStorageListener mListener;
    private final StorageNameHolder.OnDeleteTableListener mOnDeleteTableListener;

    public StorageNameHolder(@NonNull View itemView, DialogDownloadFragment.OnDownloadStorageListener listener,
                             OnDeleteTableListener onDeleteTableListener) {
        super(itemView);
        pbProgress = itemView.findViewById(R.id.item_download_progress);
        mOnDeleteTableListener = onDeleteTableListener;
        mListener = listener;
        tvDescription = itemView.findViewById(R.id.item_download_description);
        tvTable = itemView.findViewById(R.id.item_download_table);
        tvCount = itemView.findViewById(R.id.item_download_count);
        bClearData = itemView.findViewById(R.id.item_clear_data);
        bClearData.setOnClickListener(this);

        tvDescription.setOnClickListener(this);
    }

    public void bind(StorageName storageName) {
        mStorageName = storageName;
        tvDescription.setText(storageName.description);
        tvTable.setText(storageName.table);
        String localRowCount = PreferencesManager.getInstance().getLocalRowCount(storageName.table);
        String remoteRowCount = PreferencesManager.getInstance().getRemoteRowCount(storageName.table);
        String info = localRowCount + " из " + remoteRowCount;
        tvCount.setText(info);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.item_clear_data) {
            mListener.onClearData(mStorageName, mOnDeleteTableListener, getAdapterPosition());
        } else {
            mListener.onDownloadStorage(mStorageName);
        }
    }

    public void showProgress(boolean isInProgress) {
        if (isInProgress) {
            pbProgress.setVisibility(View.VISIBLE);
            tvDescription.setText("Подождите, удаляем записи...");
            tvDescription.setFocusable(false);
            tvDescription.setClickable(false);
            tvTable.setVisibility(View.GONE);
            tvCount.setVisibility(View.GONE);
            bClearData.setVisibility(View.GONE);

        } else {
            pbProgress.setVisibility(View.GONE);
            tvDescription.setVisibility(View.VISIBLE);
            tvTable.setVisibility(View.VISIBLE);
            tvCount.setVisibility(View.VISIBLE);
            tvDescription.setText(mStorageName.description);
            tvDescription.setFocusable(true);
            tvDescription.setClickable(true);
            String localRowCount = PreferencesManager.getInstance().getLocalRowCount(mStorageName.table);
            String remoteRowCount = PreferencesManager.getInstance().getRemoteRowCount(mStorageName.table);
            String info = localRowCount + " из " + remoteRowCount;
            tvCount.setText(info);
            bClearData.setVisibility(View.VISIBLE);
        }


    }

    public interface OnDeleteTableListener {
        void onTableDeleted(int position);

        void onStartDeleting(int position);
    }
}
