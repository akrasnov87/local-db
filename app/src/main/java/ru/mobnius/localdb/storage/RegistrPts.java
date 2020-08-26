package ru.mobnius.localdb.storage;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;

import ru.mobnius.localdb.data.Storage;

@Entity(nameInDb = "ED_Registr_Pts")
@Storage(description = "Потребители", table = "ED_Registr_Pts")
public class RegistrPts {

    @Id
    public String LINK;

    @Property(nameInDb = "C_Name")
    public String C_Name;

    @Property(nameInDb = "N_Code")
    public String N_Code;

    @Property(nameInDb = "B_EE")
    public String B_EE;

    @Generated(hash = 1259861301)
    public RegistrPts(String LINK, String C_Name, String N_Code, String B_EE) {
        this.LINK = LINK;
        this.C_Name = C_Name;
        this.N_Code = N_Code;
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


    public String getB_EE() {
        return this.B_EE;
    }

    public void setB_EE(String B_EE) {
        this.B_EE = B_EE;
    }

    public String getN_Code() {
        return this.N_Code;
    }

    public void setN_Code(String N_Code) {
        this.N_Code = N_Code;
    }
}
