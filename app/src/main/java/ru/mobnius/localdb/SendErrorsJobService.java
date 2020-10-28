package ru.mobnius.localdb;

import android.app.job.JobParameters;
import android.app.job.JobService;

import ru.mobnius.localdb.utils.Loader;

public class SendErrorsJobService extends JobService {

    @Override
    public boolean onStartJob(JobParameters params) {
        sendErrors(params);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    private void sendErrors(JobParameters jobParameters) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (HttpService.getDaoSession() != null) {
                    Loader.getInstance().sendErrors(HttpService.getDaoSession());
                    SendErrorsJobService.this.jobFinished(jobParameters, false);
                }
            }
        });
        thread.start();
    }
}