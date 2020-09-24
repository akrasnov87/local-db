package ru.mobnius.localdb.storage;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.ToMany;
import org.greenrobot.greendao.annotation.ToOne;

import ru.mobnius.localdb.data.Storage;

@Entity(nameInDb = "ED_Device_Billing")
@Storage(description = "Приборы", table = "ED_Device_Billing")
public class DeviceBilling {
    @Id
    public String LINK;

    @Property(nameInDb = "F_Registr_Pts")
    public String F_Registr_Pts;

    @Property(nameInDb = "C_Serial_Number")
    public String C_Serial_Number;

    @Property(nameInDb = "B_EE")
    public String B_EE;

    @Property(nameInDb = "F_Division")
    public String F_Division;

    @Property(nameInDb = "F_SubDivision")
    public String F_SubDivision;

    @Generated(hash = 344954148)
    public DeviceBilling(String LINK, String F_Registr_Pts, String C_Serial_Number,
            String B_EE, String F_Division, String F_SubDivision) {
        this.LINK = LINK;
        this.F_Registr_Pts = F_Registr_Pts;
        this.C_Serial_Number = C_Serial_Number;
        this.B_EE = B_EE;
        this.F_Division = F_Division;
        this.F_SubDivision = F_SubDivision;
    }
    @Generated(hash = 1568224966)
    public DeviceBilling() {
    }
    public String getLINK() {
        return this.LINK;
    }
    public void setLINK(String LINK) {
        this.LINK = LINK;
    }
    public String getF_Registr_Pts() {
        return this.F_Registr_Pts;
    }
    public void setF_Registr_Pts(String F_Registr_Pts) {
        this.F_Registr_Pts = F_Registr_Pts;
    }
    public String getC_Serial_Number() {
        return this.C_Serial_Number;
    }
    public void setC_Serial_Number(String C_Serial_Number) {
        this.C_Serial_Number = C_Serial_Number;
    }
    public String getB_EE() {
        return this.B_EE;
    }
    public void setB_EE(String B_EE) {
        this.B_EE = B_EE;
    }
    public String getF_Division() {
        return this.F_Division;
    }
    public void setF_Division(String F_Division) {
        this.F_Division = F_Division;
    }
    public String getF_SubDivision() {
        return this.F_SubDivision;
    }
    public void setF_SubDivision(String F_SubDivision) {
        this.F_SubDivision = F_SubDivision;
    }
}
