package ru.mobnius.localdb.utils;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;

import java.util.concurrent.TimeUnit;

import ru.mobnius.localdb.HttpCheckJobService;
import ru.mobnius.localdb.SendErrorsJobService;
import ru.mobnius.localdb.UpdateJobService;

public class JobSchedulerUtil {
    private final static int UPDATE_ID = 1411;
    private final static int SERVICE_CHECK_ID = 1412;
    private final static int SEND_ERRORS_ID = 1413;

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
                new ComponentName(app, HttpCheckJobService.class))
                .setPeriodic(TimeUnit.MINUTES.toMillis(16))
                .setBackoffCriteria(TimeUnit.MINUTES.toMillis(8),
                        JobInfo.BACKOFF_POLICY_LINEAR)
                .build();
        JobScheduler scheduler = (JobScheduler) app.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        scheduler.schedule(jobInfo);
    }

    public static void scheduleSendErrorsJob(Context app){
        JobInfo jobInfo = new JobInfo.Builder(SEND_ERRORS_ID,
                new ComponentName(app, SendErrorsJobService.class))
                .setPeriodic(TimeUnit.HOURS.toMillis(2))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setBackoffCriteria(TimeUnit.MINUTES.toMillis(8),
                        JobInfo.BACKOFF_POLICY_LINEAR)
                .build();
        JobScheduler scheduler = (JobScheduler) app.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        scheduler.schedule(jobInfo);
    }
}
