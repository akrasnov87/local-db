package ru.mobnius.localdb.storage;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Property;

import ru.mobnius.localdb.data.Storage;

@Entity(nameInDb = "ED_Network_Routes")
@Storage(description = "Схема Сети", table = "ED_Network_Routes")
public class NetworkRoutes {

    public String LINK;

    @Property(nameInDb = "F_Parent")
    public String F_Parent;

    @Property(nameInDb = "C_Network_Path")
    public String C_Network_Path;

    @Property(nameInDb = "F_Prev_Item_Types")
    public String F_Prev_Item_Types;

    @Property(nameInDb = "F_Division")
    public String F_Division;
    
    @Property(nameInDb = "F_Subdivision")
    public String F_Subdivision;

    @Generated(hash = 1858121259)
    public NetworkRoutes(String LINK, String F_Parent, String C_Network_Path,
            String F_Prev_Item_Types, String F_Division, String F_Subdivision) {
        this.LINK = LINK;
        this.F_Parent = F_Parent;
        this.C_Network_Path = C_Network_Path;
        this.F_Prev_Item_Types = F_Prev_Item_Types;
        this.F_Division = F_Division;
        this.F_Subdivision = F_Subdivision;
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

    public String getF_Division() {
        return this.F_Division;
    }

    public void setF_Division(String F_Division) {
        this.F_Division = F_Division;
    }

    public String getF_Subdivision() {
        return this.F_Subdivision;
    }

    public void setF_Subdivision(String F_Subdivision) {
        this.F_Subdivision = F_Subdivision;
    }


}