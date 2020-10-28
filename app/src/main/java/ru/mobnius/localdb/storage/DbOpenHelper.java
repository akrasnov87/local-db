package ru.mobnius.localdb.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.util.Log;

import org.greenrobot.greendao.database.Database;

import ru.mobnius.localdb.App;
import ru.mobnius.localdb.HttpService;
import ru.mobnius.localdb.data.PreferencesManager;
import ru.mobnius.localdb.model.LogItem;


public class DbOpenHelper extends DaoMaster.OpenHelper {
    private Context mContext;

    public DbOpenHelper(Context context, String name) {
        super(context, name);
        mContext = context;
    }

    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {
        super.onUpgrade(db, oldVersion, newVersion);
        if (oldVersion == 1) {//SQLiteDatabase.deleteDatabase(mContext.getDatabasePath("local-db.db"));
            String sql = "ALTER TABLE " + "\"ED_Network_Routes\"" + " ADD COLUMN \"F_Division\" TEXT;";
            String sql1 = "ALTER TABLE " + "\"ED_Network_Routes\"" + " ADD COLUMN \"F_Subdivision\" TEXT;";
            String sql2 = "ALTER TABLE " + "\"UI_SV_FIAS\"" + " ADD COLUMN \"F_Region\" TEXT;";
            db.execSQL(sql);
            db.execSQL(sql1);
            db.execSQL(sql2);
        }
    }
}


