package ru.mobnius.localdb.storage;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;

import ru.mobnius.localdb.data.Storage;

@Entity(nameInDb = "ED_Network_Routes")
@Storage(description = "Схема Сети", table = "ED_Network_Routes")
public class NetworkRoutes {

    public String LINK;
    public String F_Parent;
    public String C_Network_Path;
    public String F_Prev_Item_Types;

    @Generated(hash = 10540881)
    public NetworkRoutes(String LINK, String F_Parent, String C_Network_Path,
                         String F_Prev_Item_Types) {
        this.LINK = LINK;
        this.F_Parent = F_Parent;
        this.C_Network_Path = C_Network_Path;
        this.F_Prev_Item_Types = F_Prev_Item_Types;
    }

    @Generated(hash = 1842325579)
    public NetworkRoutes() {
    }

    public String getLINK() {
        return this.LINK;
    }

    public void setLINK(String LINK) {
        this.LINK = LINK;
    }

    public String getF_Parent() {
        return this.F_Parent;
    }

    public void setF_Parent(String F_Parent) {
        this.F_Parent = F_Parent;
    }


    public String getC_Network_Path() {
        return this.C_Network_Path;
    }

    public void setC_Network_Path(String C_Network_Path) {
        this.C_Network_Path = C_Network_Path;
    }

    public String getF_Prev_Item_Types() {
        return this.F_Prev_Item_Types;
    }

    public void setF_Prev_Item_Types(String F_Prev_Item_Types) {
        this.F_Prev_Item_Types = F_Prev_Item_Types;
    }
}