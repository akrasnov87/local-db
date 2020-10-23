package ru.mobnius.localdb.storage;

import android.content.Context;

import org.greenrobot.greendao.database.Database;

import static ru.mobnius.localdb.storage.DaoMaster.dropAllTables;

public class DbOpenHelper extends DaoMaster.OpenHelper {

    public DbOpenHelper(Context context, String name) {
        super(context, name);
    }

    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {
        super.onUpgrade(db, oldVersion, newVersion);
        switch (oldVersion){
            case 1:
                String sql = "DROP TABLE " +  "IF EXISTS " + "\"ED_Network_Routes\"";
                db.execSQL(sql);
                db.execSQL("CREATE TABLE " + "\"ED_Network_Routes\" (" + //
                        "\"LINK\" TEXT," + // 0: LINK
                        "\"F_Parent\" TEXT," + // 1: F_Parent
                        "\"C_Network_Path\" TEXT," + // 2: C_Network_Path
                        "\"F_Prev_Item_Types\" TEXT," + // 3: F_Prev_Item_Types
                        "\"F_Division\" TEXT," + // 4: F_Division
                        "\"F_Subdivision\" TEXT);"); // 5: F_Su
        }
    }
}
