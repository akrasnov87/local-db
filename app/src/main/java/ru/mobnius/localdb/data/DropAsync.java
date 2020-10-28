package ru.mobnius.localdb.data;

import android.os.AsyncTask;
import android.os.Build;

import org.greenrobot.greendao.database.Database;

import ru.mobnius.localdb.App;
import ru.mobnius.localdb.HttpService;
import ru.mobnius.localdb.model.LogItem;

import static ru.mobnius.localdb.storage.DaoMaster.createAllTables;
import static ru.mobnius.localdb.storage.DaoMaster.dropAllTables;

public class DropAsync extends AsyncTask<Void, Void, Void> {
    private final Database mDatabase;
    private App mApp;

    public DropAsync(Database database, App app) {
        mDatabase = database;
        mApp = app;
    }

    @Override
    protected Void doInBackground(Void... strings) {
        mApp.onAddLog(new LogItem("Производится очистка невалидных данных", false));
        dropAllTables(mDatabase, true);
        createAllTables(mDatabase, true);
        return null;
    }

    @Override
    protected void onPostExecute(Void s) {
        super.onPostExecute(s);
        if (mApp != null) {
            mApp.onAddLog(new LogItem("Очистка данных прошла успешно", false));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mApp.startForegroundService(HttpService.getIntent(mApp, HttpService.AUTO));
            }else {
                mApp.startService(HttpService.getIntent(mApp, HttpService.AUTO));
            }
        }
        if (PreferencesManager.getInstance() != null) {
            PreferencesManager.getInstance().setLocalRowCount("0", "ED_Network_Routes");
            PreferencesManager.getInstance().setRemoteRowCount("0", "ED_Network_Routes");
            PreferencesManager.getInstance().setLocalRowCount("0", "ED_Device_Billing");
            PreferencesManager.getInstance().setRemoteRowCount("0", "ED_Device_Billing");
            PreferencesManager.getInstance().setLocalRowCount("0", "UI_SV_FIAS");
            PreferencesManager.getInstance().setRemoteRowCount("0", "UI_SV_FIAS");
            PreferencesManager.getInstance().setLocalRowCount("0", "ED_Registr_Pts");
            PreferencesManager.getInstance().setRemoteRowCount("0", "ED_Registr_Pts");
        }


    }

    public interface OnSqlCreatedListener {
        void onSqlCreateStarted();

        void onSqlCreateCompleted();
    }
}
