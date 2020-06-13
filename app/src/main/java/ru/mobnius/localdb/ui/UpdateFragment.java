package ru.mobnius.localdb.ui;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import ru.mobnius.localdb.R;
import ru.mobnius.localdb.model.Progress;
import ru.mobnius.localdb.model.fias.FiasAsyncTask;

public class UpdateFragment extends Fragment
        implements View.OnClickListener,
        FiasAsyncTask.OnFiasListener {

    private Button btnCancel;
    private TextView tvStatus;
    private ContentLoadingProgressBar mBar;
    private FiasAsyncTask mFiasAsyncTask;

    public UpdateFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_update, container, false);
        btnCancel = view.findViewById(R.id.log_cancel);
        btnCancel.setOnClickListener(this);
        tvStatus = view.findViewById(R.id.log_status);
        mBar = view.findViewById(R.id.log_progress);
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mFiasAsyncTask != null) {
            mFiasAsyncTask.cancel(true);
            mFiasAsyncTask = null;
        }
    }

    public void startProcess(String login, String password) {
        mBar.setProgress(0);
        tvStatus.setText("Идет обработка...");
        btnCancel.setVisibility(View.VISIBLE);
        tvStatus.setVisibility(View.VISIBLE);
        mBar.setVisibility(View.VISIBLE);
        getView().setVisibility(View.VISIBLE);

        mFiasAsyncTask = new FiasAsyncTask(this);
        mFiasAsyncTask.execute(login, password);
    }

    public void updateProcess(Progress progress) {
        tvStatus.setText(String.format("%s из %s", progress.current, progress.total));
        mBar.setProgress((int)progress.getPercent());
    }

    @Override
    public void onClick(View v) {
        String message = "После отмены процесс требуется выполнить заново. Остановить загрузку данных?";
        confirmDialog(message, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(DialogInterface.BUTTON_POSITIVE == which) {
                    stopProcess();
                }
            }
        });
    }

    @Override
    public void onFiasProgress(Progress progress) {
        updateProcess(progress);
    }

    @Override
    public void onFiasLoaded() {
        stopProcess();
        alert("Загрузка завершена");
    }

    private void confirmDialog(String message, DialogInterface.OnClickListener listener) {
        AlertDialog dialog = new AlertDialog.Builder(requireContext()).create();
        dialog.setTitle("Сообщение");
        dialog.setMessage(message);
        dialog.setCancelable(false);
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, requireContext().getString(R.string.yes), listener);
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.no), listener);
        dialog.setIcon(R.drawable.ic_baseline_warning_24);
        dialog.show();
    }

    private void alert(String message) {
        new android.app.AlertDialog.Builder(requireContext())
                .setIcon(R.drawable.ic_baseline_info_24)
                .setMessage(message)
                .setPositiveButton("OK", null).show();
    }

    private void stopProcess() {
        tvStatus.setText("");
        btnCancel.setVisibility(View.GONE);
        tvStatus.setVisibility(View.GONE);
        mBar.setVisibility(View.GONE);
        getView().setVisibility(View.GONE);
        if(!mFiasAsyncTask.isCancelled()) {
            mFiasAsyncTask.cancel(true);
        }
    }
}