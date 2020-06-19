package ru.mobnius.localdb.data;

import java.util.TimerTask;

import ru.mobnius.localdb.HttpService;
import ru.mobnius.localdb.utils.Loader;

public class SendErrorTimerTask extends TimerTask {
    @Override
    public void run() {
        Loader.getInstance().sendErrors(HttpService.getDaoSession());
    }
}