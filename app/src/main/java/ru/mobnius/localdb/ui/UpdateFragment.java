package ru.mobnius.localdb.ui;

import android.os.Bundle;

import androidx.core.widget.ContentLoadingProgressBar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import ru.mobnius.localdb.R;
import ru.mobnius.localdb.data.BaseFragment;
import ru.mobnius.localdb.data.exception.ExceptionCode;

public class UpdateFragment extends BaseFragment {

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

    public void updateProcess(int progress, int total) {
        if(mBar.getVisibility() != View.VISIBLE) {
            startProcess();
        }
        tvStatus.setText(String.format("%s из %s", progress, total));
        mBar.setProgress((int)getPercent(progress, total));
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


    @Override
    public int getExceptionCode() {
        return ExceptionCode.DOWNLOAD_PROGRESS;
    }

    private double getPercent(int progress, int total) {
        double result = (double) (progress * 100) / total;
        if(result > 100) {
            result = 100;
        }
        return result;
    }
}