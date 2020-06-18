package ru.mobnius.localdb.ui;

import android.os.Bundle;

import androidx.core.widget.ContentLoadingProgressBar;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import ru.mobnius.localdb.R;
import ru.mobnius.localdb.model.Progress;

public class UpdateFragment extends Fragment {

    private Button btnCancel;
    private TextView tvStatus;
    private ContentLoadingProgressBar mBar;

    public UpdateFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_update, container, false);
        btnCancel = view.findViewById(R.id.log_cancel);
        btnCancel.setOnClickListener((View.OnClickListener) getActivity());

        tvStatus = view.findViewById(R.id.log_status);
        mBar = view.findViewById(R.id.log_progress);
        return view;
    }

    public void startProcess() {
        mBar.setProgress(0);
        tvStatus.setText("Идет обработка...");
        btnCancel.setVisibility(View.VISIBLE);
        tvStatus.setVisibility(View.VISIBLE);
        mBar.setVisibility(View.VISIBLE);
    }

    public void updateProcess(Progress progress) {
        if(mBar.getVisibility() != View.VISIBLE) {
            startProcess();
        }
        tvStatus.setText(String.format("%s из %s", progress.current, progress.total));
        mBar.setProgress((int)progress.getPercent());
    }

    /**
     * остановка процесса
     */
    public void stopProcess() {
        tvStatus.setText("");
        btnCancel.setVisibility(View.GONE);
        tvStatus.setVisibility(View.GONE);
        mBar.setVisibility(View.GONE);
    }
}