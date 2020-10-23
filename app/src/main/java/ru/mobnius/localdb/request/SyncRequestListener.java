package ru.mobnius.localdb.request;

import android.os.Handler;
import android.os.Looper;

import org.greenrobot.greendao.AbstractDao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.mobnius.localdb.App;
import ru.mobnius.localdb.HttpService;
import ru.mobnius.localdb.data.ConnectionChecker;
import ru.mobnius.localdb.data.InsertHandler;
import ru.mobnius.localdb.data.LoadAsyncTask;
import ru.mobnius.localdb.data.PreferencesManager;
import ru.mobnius.localdb.model.DefaultResult;
import ru.mobnius.localdb.model.Response;
import ru.mobnius.localdb.observer.EventListener;
import ru.mobnius.localdb.observer.Observer;
import ru.mobnius.localdb.utils.NetworkUtil;
import ru.mobnius.localdb.utils.UrlReader;

/**
 * запуск синхронизации
 */
public class SyncRequestListener extends AuthFilterRequestListener
        implements LoadAsyncTask.OnLoadListener, ConnectionChecker.CheckConnection,
        InsertHandler.ZipDownloadListener, EventListener {

    private final App mApp;
    private UrlReader mUrlReader;
    private boolean isCanceled = false;
    private InsertHandler mInsertHandler;

    public SyncRequestListener(App app, SyncStatusRequestListener statusRequestListener) {
        mApp = app;
        mApp.getConnectionReceiver().setListener(this);
        mApp.getObserver().subscribe(Observer.ERROR, statusRequestListener);
        mApp.getObserver().subscribe(Observer.STOP_THREAD, this);
    }

    @Override
    public boolean isValid(String query) {
        Pattern pattern = Pattern.compile("^/sync\\?table=", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(query);
        return matcher.find();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Response getResponse(UrlReader urlReader) {
        Response response = super.getResponse(urlReader);
        if (response != null) {
            return response;
        }
        if (PreferencesManager.getInstance().isDownloadingLocalDB() || PreferencesManager.getInstance().isLocalDBReadyToUpdate() ||
                PreferencesManager.getInstance().isMOReadyToUpdate() || PreferencesManager.getInstance().isDownloadingMO()) {
            return Response.getErrorInstance(urlReader, "Сначала вам необходимо выполнить обновление приложений до последних версий", Response.RESULT_FAIL);
        }
        mUrlReader = urlReader;
        // TODO: 17.06.2020 нужно достать из запроса логин и пароль
        ArrayList<String> tableName = new ArrayList<>();
        String table = urlReader.getParam("table");
        if (table.equals("allTables")) {
            for (int i = 0; i < HttpService.getDaoSession().getAllDaos().size(); i++) {
                AbstractDao dao = (AbstractDao) HttpService.getDaoSession().getAllDaos().toArray()[i];
                if (!dao.getTablename().equals("sd_client_errors")) {
                    tableName.add(dao.getTablename());
                }
            }
            Collections.sort(tableName);
            PreferencesManager.getInstance().setIsAllTables(true);
            PreferencesManager.getInstance().setAllTablesArray(tableName.toArray(new String[0]));
        } else {
            tableName.add(table);
            PreferencesManager.getInstance().setIsAllTables(false);
            PreferencesManager.getInstance().setAllTablesArray(null);
        }
        if (NetworkUtil.isNetworkAvailable(mApp)) {
            if (urlReader.getParam("restore") != null) {
                PreferencesManager.getInstance().setProgress(null);
                LoadAsyncTask task = new LoadAsyncTask(tableName.toArray(new String[0]), SyncRequestListener.this, mApp);
                mApp.getObserver().subscribe(Observer.STOP_ASYNC_TASK, task);
                task.executeOnExecutor(Executors.newSingleThreadExecutor(), PreferencesManager.getInstance().getLogin(), PreferencesManager.getInstance().getPassword());
            } else {
                PreferencesManager.getInstance().setProgress(null);
                if (mInsertHandler != null) {
                    mInsertHandler.quit();
                    mInsertHandler = null;
                }
                mInsertHandler = new InsertHandler(mApp, this, new Handler(Looper.getMainLooper()), tableName.get(tableName.size()-1));
                mInsertHandler.start();
                mInsertHandler.getLooper();
                LoadAsyncTask task = new LoadAsyncTask(tableName.toArray(new String[0]), this, mApp);
                mApp.getObserver().subscribe(Observer.STOP_ASYNC_TASK, task);
                mApp.getObserver().subscribe(Observer.STOP_THREAD, mInsertHandler);
                task.executeOnExecutor(Executors.newSingleThreadExecutor(), PreferencesManager.getInstance().getLogin(), PreferencesManager.getInstance().getPassword());
                response = Response.getInstance(urlReader, DefaultResult.getSuccessInstance().toJsonString());
                isCanceled = false;
            }
        } else {
            response = Response.getErrorInstance(urlReader, "Не подключения к сети интернет", Response.RESULT_FAIL);
        }
        return response;
    }

    @Override
    public void onInsertProgress(int progress, int total) {
        mApp.onDownLoadProgress(mUrlReader, progress, total);
    }

    @Override
    public void onInsertFinish(String tableName) {
        mApp.onDownLoadFinish(tableName, mUrlReader);
    }

    @Override
    public void onLoadError(String[] message) {
        mApp.getObserver().notify(Observer.ERROR, message);
    }

    @Override
    public void onConnectionChange(boolean isConnected) {
        if (!isConnected) {
            mApp.getObserver().notify(Observer.STOP_ASYNC_TASK, "stopping async task");
            isCanceled = true;
        } else {
            if (PreferencesManager.getInstance().getDownloadProgress() != null && isCanceled) {

                new Handler().postDelayed(() -> {
                    String[] allTables = PreferencesManager.getInstance().getAllTablesArray();
                    if (allTables == null) {
                        allTables = new String[]{PreferencesManager.getInstance().getDownloadProgress().tableName};
                    }
                    LoadAsyncTask task = new LoadAsyncTask(allTables, SyncRequestListener.this, mApp);
                    mApp.getObserver().subscribe(Observer.STOP_ASYNC_TASK, task);
                    task.executeOnExecutor(Executors.newSingleThreadExecutor(), PreferencesManager.getInstance().getLogin(), PreferencesManager.getInstance().getPassword());
                    isCanceled = false;
                }, 2000);
            }
        }
    }

    @Override
    public void onZipDownloaded(String tableName, String zipFilePath) {
        mInsertHandler.insert(tableName, zipFilePath);
    }

    @Override
    public void update(String eventType, String... args) {
        if (eventType.equals(Observer.STOP_THREAD)) {
            if (mInsertHandler != null) {
                mInsertHandler.quit();
                mInsertHandler = null;
            }
            if (PreferencesManager.getInstance().getProgress() != null) {
                PreferencesManager.getInstance().setProgress(null);
            }
        }
    }
}