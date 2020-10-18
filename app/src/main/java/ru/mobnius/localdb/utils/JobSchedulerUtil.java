package ru.mobnius.localdb.utils;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;

import java.util.concurrent.TimeUnit;

import ru.mobnius.localdb.App;
import ru.mobnius.localdb.ServiceCheckJob;
import ru.mobnius.localdb.UpdateJobService;

public class JobSchedulerUtil {
    private final static int UPDATE_ID = 1411;
    private final static int SERVICE_CHECK_ID = 1412;

    public static void scheduleUpdateJob(Context app){
        JobInfo jobInfo = new JobInfo.Builder(UPDATE_ID,
                new ComponentName(app, UpdateJobService.class))
                .setPeriodic(TimeUnit.MINUTES.toMillis(16))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setBackoffCriteria(TimeUnit.MINUTES.toMillis(8),
                        JobInfo.BACKOFF_POLICY_LINEAR)
                .build();
        JobScheduler scheduler = (JobScheduler) app.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        scheduler.schedule(jobInfo);
    }

    public static void scheduleServiceCheckJob(Context app){
        JobInfo jobInfo = new JobInfo.Builder(SERVICE_CHECK_ID,
                new ComponentName(app, ServiceCheckJob.class))
                .setPeriodic(TimeUnit.MINUTES.toMillis(16))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setBackoffCriteria(TimeUnit.MINUTES.toMillis(8),
                        JobInfo.BACKOFF_POLICY_LINEAR)
                .build();
        JobScheduler scheduler = (JobScheduler) app.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        scheduler.schedule(jobInfo);
    }
}
