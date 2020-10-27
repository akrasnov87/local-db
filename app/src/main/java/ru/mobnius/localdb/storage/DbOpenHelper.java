package ru.mobnius.localdb.storage;

import android.content.Context;

import org.greenrobot.greendao.database.Database;

import ru.mobnius.localdb.data.PreferencesManager;

import static ru.mobnius.localdb.storage.DaoMaster.dropAllTables;

public class DbOpenHelper extends DaoMaster.OpenHelper {

    public DbOpenHelper(Context context, String name) {
        super(context, name);
    }

    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {
        super.onUpgrade(db, oldVersion, newVersion);
        switch (oldVersion) {
            case 1:
                String sql = "DROP TABLE " + "IF EXISTS " + "\"ED_Network_Routes\"";
                db.execSQL(sql);
                if (PreferencesManager.getInstance() != null) {
                    PreferencesManager.getInstance().setLocalRowCount("0", "ED_Network_Routes");
                }
                db.execSQL("CREATE TABLE " + "\"ED_Network_Routes\" (" + //
                        "\"LINK\" TEXT," + // 0: LINK
                        "\"F_Parent\" TEXT," + // 1: F_Parent
                        "\"C_Network_Path\" TEXT," + // 2: C_Network_Path
                        "\"F_Prev_Item_Types\" TEXT," + // 3: F_Prev_Item_Types
                        "\"F_Division\" TEXT," + // 4: F_Division
                        "\"F_Subdivision\" TEXT);"); // 5: F_Su

                String sql1 = "DROP TABLE " + "IF EXISTS " + "\"UI_SV_FIAS\"";
                db.execSQL(sql1);
                if (PreferencesManager.getInstance() != null) {
                    PreferencesManager.getInstance().setLocalRowCount("0", "UI_SV_FIAS");
                }
                db.execSQL("CREATE TABLE " + "\"UI_SV_FIAS\" (" + //
                        "\"LINK\" TEXT PRIMARY KEY NOT NULL ," + // 0: LINK
                        "\"C_Full_Address\" TEXT," + // 1: C_Full_Address
                        "\"C_House_Number\" TEXT," + // 2: C_House_Number
                        "\"F_Structure\" TEXT," + // 3: F_Structure
                        "\"F_Municipality\" TEXT," + // 4: F_Municipality
                        "\"F_Town\" TEXT," + // 5: F_Town
                        "\"F_Region\" TEXT);"); // 6: F_Region
        }
    }
}

