package ru.mobnius.localdb.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

import ru.mobnius.localdb.R;
import ru.mobnius.localdb.adapter.StorageNameAdapter;
import ru.mobnius.localdb.adapter.holder.StorageNameHolder;
import ru.mobnius.localdb.data.BaseDialogFragment;
import ru.mobnius.localdb.model.StorageName;

public class DialogDownloadFragment extends BaseDialogFragment implements StorageNameHolder.OnDeleteTableListener {
    private final OnDownloadStorageListener mListener;
    private RecyclerView mRecyclerView;

    public DialogDownloadFragment(OnDownloadStorageListener listener) {
        mListener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_dialog_download, container, false);
        mRecyclerView = view.findViewById(R.id.download_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(new StorageNameAdapter(requireContext(), mListener, this));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(requireContext(),
                DividerItemDecoration.VERTICAL));

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        Objects.requireNonNull(Objects.requireNonNull(Objects.requireNonNull(getDialog())).getWindow()).setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
    }

    @Override
    public void onTableDeleted(int position) {
        StorageNameHolder holder = (StorageNameHolder) mRecyclerView.findViewHolderForAdapterPosition(position);
        if (holder != null) {
            holder.showProgress(false);
        }
        mRecyclerView.setFocusable(true);
        mRecyclerView.setClickable(true);
    }

    @Override
    public void onStartDeleting(int position) {
        StorageNameHolder holder = (StorageNameHolder) mRecyclerView.findViewHolderForAdapterPosition(position);
        if (holder != null) {
            holder.showProgress(true);
        }
        mRecyclerView.setFocusable(false);
        mRecyclerView.setClickable(false);
    }

    public interface OnDownloadStorageListener {

        void onDownloadStorage(StorageName name);

        void onClearData(StorageName name, StorageNameHolder.OnDeleteTableListener onDeleteTableListener, int position);
    }
}