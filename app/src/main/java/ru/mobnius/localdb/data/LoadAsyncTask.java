package ru.mobnius.localdb.data;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteFullException;
import android.os.AsyncTask;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.greenrobot.greendao.database.Database;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import ru.mobnius.localdb.HttpService;
import ru.mobnius.localdb.Logger;
import ru.mobnius.localdb.Tags;
import ru.mobnius.localdb.model.Progress;
import ru.mobnius.localdb.model.rpc.RPCResult;
import ru.mobnius.localdb.storage.DaoSession;
import ru.mobnius.localdb.utils.Loader;
import ru.mobnius.localdb.utils.StorageUtil;

/**
 * Загрузка данных с сервера
 */
public class LoadAsyncTask extends AsyncTask<String, Integer, ArrayList<String>> implements StorageUtil.OnProgressUpdate {

    private final OnLoadListener mListener;
    @SuppressLint("StaticFieldLeak")
    private Context mContext;
    private final String mTableName;
    private int mTotal;
    private BroadcastReceiver mMessageReceiver;

    public LoadAsyncTask(String tableName, OnLoadListener listener, Context context) {
        mListener = listener;
        mTableName = tableName;
        mContext = context;
        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Tags.CANCEL_TASK_TAG)) {
                    LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiver);
                    LoadAsyncTask.this.cancel(false);
                }
            }
        };
        LocalBroadcastManager.getInstance(mContext).registerReceiver(
                mMessageReceiver, new IntentFilter(Tags.CANCEL_TASK_TAG));
    }

    @Override
    protected ArrayList<String> doInBackground(String... strings) {
        ArrayList<String> message = new ArrayList<>();
        Loader loader = Loader.getInstance();
        boolean isAuthorized = loader.auth(strings[0], strings[1]);
        if (!isAuthorized) {
            message.add(Tags.AUTH_ERROR);
            message.add("Не удалось получить ответ от сервера. Возможно пароль введен неверно, попробуйте авторизоваться повторно");
            return message;
        }
        // нужно ли просто добавлять или удалить все, а затем заливать
        boolean removeBeforeInsert = PreferencesManager.getInstance().getProgress() == null;

        int size = PreferencesManager.getInstance().getSize();
        Progress progress = PreferencesManager.getInstance().getProgress() != null ? PreferencesManager.getInstance().getProgress() : new Progress(0, 1, mTableName);
        if (!removeBeforeInsert) {
            progress.current = progress.current + size;
        }
        int savedProgress = Integer.parseInt(PreferencesManager.getInstance().getLocalRowCount(mTableName));
        if (savedProgress - progress.current == 10000) {
            //значит был обрыв интернет соединения (последние 10000 записей были получены и успешно записаны в БД)
            progress.current = savedProgress;
        }
        if (progress.current != 0 && progress.current % 10000 != 0) {
            // значит произошел краш программы (выключение телефона, необработанная ошибка). В этом случае лучше стереть записи, так как нет гарантии что они корректные.
            removeBeforeInsert = true;
        }
        PreferencesManager.getInstance().setProgress(progress);

        DaoSession daoSession = HttpService.getDaoSession();
        RPCResult[] results;
        try {
            results = rpc(mTableName, progress.current, size);
        } catch (FileNotFoundException e) {
            Logger.error(e);
            message.add(Tags.RPC_ERROR);
            message.add(e.getMessage());
            return message;
        }
        if (results == null) {
            message.add(Tags.RPC_ERROR);
            message.add("Не удалось установить соединение с сервером при старте загрузки");
            return message;
        }
        RPCResult result = results[0];
        mTotal = result.result.total;
        PreferencesManager.getInstance().setRemoteRowCount(String.valueOf(mTotal), mTableName);
        PreferencesManager.getInstance().setProgress(new Progress(progress.current, mTotal, mTableName));
        File cacheDir = mContext.getCacheDir();
        if (cacheDir.getFreeSpace() / 1000000 < 200) {
            message.add(Tags.STORAGE_ERROR);
            message.add("Ошибка: в хранилище телефона осталось свободно менее 5%. Очистите место и повторите загрузку");
            return message;
        } else {
            try {
                StorageUtil.processing(daoSession, result, mTableName, removeBeforeInsert, this);
            } catch (SQLiteFullException | SQLiteConstraintException e) {
                message.add(Tags.SQL_ERROR);
                message.add(e.getMessage());
                Logger.error(e);
                return message;
            }
        }
        for (int i = (progress.current + size); i < mTotal; i += size) {
            if (isCancelled()) {
                return message;
            }
            if (PreferencesManager.getInstance().getProgress() == null) {
                // значит принудительно все было остановлено
                Intent intent = new Intent(Tags.CANCEL_TASK_TAG);
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                break;
            }
            for (int retry = 0; true; retry++) {
                try {
                    results = rpc(mTableName, i, size);
                } catch (FileNotFoundException e) {
                    Logger.error(e);
                    message.add(Tags.RPC_ERROR);
                    message.add(e.getMessage());
                    return message;
                }
                if (results != null) {
                    break;
                } else {
                    if (retry == 10) {
                        message.add(Tags.RPC_ERROR);
                        message.add("Не удалось установить соединение с сервером в процессе загрузки");
                        return message;
                    }
                }
            }

            result = results[0];
            File cacheDir1 = mContext.getCacheDir();
            if (cacheDir1.getFreeSpace() / 1000000 < 200) {
                message.add(Tags.STORAGE_ERROR);
                message.add("Ошибка: в хранилище телефона осталось свободно менее 5%. Удалось загрузить: " + i + " записей (очистите место и повторите загрузку)");
                return message;
            } else {
                try {
                    StorageUtil.processing(daoSession, result, mTableName, false, this);
                } catch (SQLiteFullException | SQLiteConstraintException e) {
                    Logger.error(e);
                    message.add(Tags.SQL_ERROR);
                    message.add(e.getMessage());
                    return message;
                }
            }
            PreferencesManager.getInstance().setProgress(new Progress(i, mTotal, mTableName));
        }
        createIndexes(mTableName, HttpService.getDaoSession().getDatabase());
        return message;
    }

    private RPCResult[] rpc(String tableName, int start, int limit) throws FileNotFoundException {
        String sortColumn =  "\"LINK\"";
        if (tableName.toLowerCase().equals("ui_sv_fias")){
            sortColumn = "\"C_Full_Address\"";
        }
        RPCResult[] results = Loader.getInstance().rpc("Domain." + tableName, "Query", "[{ \"forceLimit\": true, \"start\": " + start + ", \"limit\": " + limit + ", \"sort\": [{ \"property\": " + sortColumn + ", \"direction\": \"ASC\" }] }]");
        return results;
    }


    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        int progress = values[0];
        mListener.onLoadProgress(mTableName, progress, mTotal);
    }

    @Override
    protected void onPostExecute(ArrayList<String> message) {
        super.onPostExecute(message);
        if (message != null && !message.isEmpty()) {
            Intent intent = new Intent(Tags.ERROR_TAG);
            intent.putExtra(Tags.ERROR_TYPE, message.get(0));
            intent.putExtra(Tags.ERROR_TEXT, message.get(1));
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        }
        PreferencesManager.getInstance().setProgress(null);
        mListener.onLoadFinish(mTableName);
    }

    @Override
    public void onUpdateProgress(int progress) {
        publishProgress(progress);
    }

    private void createIndexes(String tableName, Database db) {
        switch (tableName.toLowerCase()) {
            case "ui_sv_fias":
                db.execSQL("CREATE INDEX " + "IDX_UI_SV_FIAS_C_Full_Address ON \"UI_SV_FIAS\"" +
                        " (\"C_Full_Address\" ASC);");
                db.execSQL("CREATE INDEX " + "IDX_UI_SV_FIAS_F_Structure ON \"UI_SV_FIAS\"" +
                        " (\"F_Structure\" ASC);");
                db.execSQL("CREATE INDEX " + "IDX_UI_SV_FIAS_F_Municipality ON \"UI_SV_FIAS\"" +
                        " (\"F_Municipality\" ASC);");
                db.execSQL("CREATE INDEX " + "IDX_UI_SV_FIAS_F_Town ON \"UI_SV_FIAS\"" +
                        " (\"F_Town\" ASC);");
                break;
        }
    }


    public interface OnLoadListener {
        /**
         * Прогресс выполнения
         *
         * @param tableName имя таблицы
         * @param progress  процент
         */
        void onLoadProgress(String tableName, int progress, int total);

        /**
         * Результат загрузки данных
         *
         * @param tableName имя таблицы
         */
        void onLoadFinish(String tableName);
    }


}
