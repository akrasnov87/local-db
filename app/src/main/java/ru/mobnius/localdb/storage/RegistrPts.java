package ru.mobnius.localdb.storage;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Property;

import ru.mobnius.localdb.data.Storage;

@Entity(nameInDb = "ED_Registr_Pts")
@Storage(description = "Потребители", table = "ED_Registr_Pts")
public class RegistrPts {

    public String LINK;

    @Property(nameInDb = "C_Name")
    public String C_Name;

    @Property(nameInDb = "N_Code")
    public String N_Code;

    @Property(nameInDb = "B_EE")
    public String B_EE;

    @Property(nameInDb = "F_Division")
    public String F_Division;

    @Property(nameInDb = "F_SubDivision")
    public String F_SubDivision;

    @Generated(hash = 1419436180)
    public RegistrPts(String LINK, String C_Name, String N_Code, String B_EE,
            String F_Division, String F_SubDivision) {
        this.LINK = LINK;
        this.C_Name = C_Name;
        this.N_Code = N_Code;
        this.B_EE = B_EE;
        this.F_Division = F_Division;
        this.F_SubDivision = F_SubDivision;
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

    public String getN_Code() {
        return this.N_Code;
    }

    public void setN_Code(String N_Code) {
        this.N_Code = N_Code;
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

    public String getB_EE() {
        return this.B_EE;
    }

    public void setB_EE(String B_EE) {
        this.B_EE = B_EE;
    }

}
