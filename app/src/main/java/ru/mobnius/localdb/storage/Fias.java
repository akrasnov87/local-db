package ru.mobnius.localdb.storage;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

import ru.mobnius.localdb.data.Storage;

@SuppressWarnings("unused")
@Entity(nameInDb = "UI_SV_FIAS")
@Storage(description = "ФИАС", table = "UI_SV_FIAS")
public class Fias {
    @Id
    public String LINK;
    public String F_Street;

    public String C_Full_Address;
    public String C_House_Number;
    public String C_Building_Number;
    @Generated(hash = 1534843169)
    public Fias(String LINK, String F_Street, String C_Full_Address,
            String C_House_Number, String C_Building_Number) {
        this.LINK = LINK;
        this.F_Street = F_Street;
        this.C_Full_Address = C_Full_Address;
        this.C_House_Number = C_House_Number;
        this.C_Building_Number = C_Building_Number;
    }
    @Generated(hash = 274996750)
    public Fias() {
    }
    public String getLINK() {
        return this.LINK;
    }
    public void setLINK(String LINK) {
        this.LINK = LINK;
    }
    public String getF_Street() {
        return this.F_Street;
    }
    public void setF_Street(String F_Street) {
        this.F_Street = F_Street;
    }
    public String getC_Full_Address() {
        return this.C_Full_Address;
    }
    public void setC_Full_Address(String C_Full_Address) {
        this.C_Full_Address = C_Full_Address;
    }
    public String getC_House_Number() {
        return this.C_House_Number;
    }
    public void setC_House_Number(String C_House_Number) {
        this.C_House_Number = C_House_Number;
    }
    public String getC_Building_Number() {
        return this.C_Building_Number;
    }
    public void setC_Building_Number(String C_Building_Number) {
        this.C_Building_Number = C_Building_Number;
    }
}
