package ru.mobnius.localdb.storage;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

import ru.mobnius.localdb.data.Storage;

@Entity(nameInDb = "ED_Registr_Pts")
@Storage(description = "Пользователи и платежи", table = "ED_Registr_Pts")
public class RegistrPts {
    @Id
    public String LINK;

    public String C_Name;
    public String N_code;
    public String B_EE;

    @Generated(hash = 427330321)
    public RegistrPts(String LINK, String C_Name, String N_code, String B_EE) {
        this.LINK = LINK;
        this.C_Name = C_Name;
        this.N_code = N_code;
        this.B_EE = B_EE;
    }

    @Generated(hash = 925207867)
    public RegistrPts() {
    }

    public String getLINK() {
        return this.LINK;
    }

    public void setLINK(String LINK) {
        this.LINK = LINK;
    }

    public String getC_Name() {
        return this.C_Name;
    }

    public void setC_Name(String C_Name) {
        this.C_Name = C_Name;
    }

    public String getN_code() {
        return this.N_code;
    }

    public void setN_code(String N_code) {
        this.N_code = N_code;
    }

    public String getB_EE() {
        return this.B_EE;
    }

    public void setB_EE(String B_EE) {
        this.B_EE = B_EE;
    }

}
