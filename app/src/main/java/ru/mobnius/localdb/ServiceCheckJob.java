package ru.mobnius.localdb;

import android.app.job.JobParameters;
import android.app.job.JobService;

public class ServiceCheckJob extends JobService {

    @Override
    public boolean onStartJob(JobParameters params) {
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

}
