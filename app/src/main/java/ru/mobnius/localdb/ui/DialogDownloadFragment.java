package ru.mobnius.localdb.ui;

import android.os.Bundle;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.Objects;

import ru.mobnius.localdb.R;
import ru.mobnius.localdb.adapter.StorageNameAdapter;
import ru.mobnius.localdb.data.BaseDialogFragment;
import ru.mobnius.localdb.data.exception.ExceptionCode;
import ru.mobnius.localdb.model.StorageName;

public class DialogDownloadFragment extends BaseDialogFragment {
    private final OnDownloadStorageListener mListener;

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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dialog_download, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.download_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(new StorageNameAdapter(requireContext(), mListener));
        recyclerView.addItemDecoration(new DividerItemDecoration(requireContext(),
                DividerItemDecoration.VERTICAL));

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        Objects.requireNonNull(Objects.requireNonNull(Objects.requireNonNull(getDialog())).getWindow()).setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
    }

    @SuppressWarnings("unused")
    @Override
    public int getExceptionCode() {
        return ExceptionCode.DOWNLOAD_LIST;
    }

    public interface OnDownloadStorageListener {
        void onDownloadStorage(StorageName name);
    }
}