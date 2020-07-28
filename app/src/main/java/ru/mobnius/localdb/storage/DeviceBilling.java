package ru.mobnius.localdb.storage;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Property;

import ru.mobnius.localdb.data.Storage;

@Entity(nameInDb = "ED_Device_Billing")
@Storage(description = "Приборы", table = "ED_Device_Billing")
public class DeviceBilling {
    @Id
    @Index
    public String LINK;

    @Property(nameInDb = "F_Registr_Pts")
    public String F_Registr_Pts;

    @Property(nameInDb = "C_Serial_Number")
    public String C_Serial_Number;

    @Property(nameInDb = "B_EE")
    public String B_EE;

    @Generated(hash = 181201448)
    public DeviceBilling(String LINK, String F_Registr_Pts, String C_Serial_Number,
            String B_EE) {
        this.LINK = LINK;
        this.F_Registr_Pts = F_Registr_Pts;
        this.C_Serial_Number = C_Serial_Number;
        this.B_EE = B_EE;
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
}
