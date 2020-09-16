package ru.mobnius.localdb.storage;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;

import ru.mobnius.localdb.data.Storage;

@SuppressWarnings("unused")
@Entity(nameInDb = "UI_SV_FIAS")
@Storage(description = "ФИАС", table = "UI_SV_FIAS")
    public class Fias {

        @Id
        public String LINK;

        @Property(nameInDb = "C_Full_Address")
        public String C_Full_Address;

        @Property(nameInDb = "C_House_Number")
        public String C_House_Number;

        @Property(nameInDb = "F_Structure")
        public String F_Structure;

        @Property(nameInDb = "F_Municipality")
        public String F_Municipality;

        @Property(nameInDb = "F_Town")
        public String F_Town;
    public Fias() {
    }

    @Generated(hash = 415324961)
    public Fias(String LINK, String C_Full_Address, String C_House_Number,
            String F_Structure, String F_Municipality, String F_Town) {
        this.LINK = LINK;
        this.C_Full_Address = C_Full_Address;
        this.C_House_Number = C_House_Number;
        this.F_Structure = F_Structure;
        this.F_Municipality = F_Municipality;
        this.F_Town = F_Town;
    }

    public String getLINK() {
        return this.LINK;
    }

    public void setLINK(String LINK) {
        this.LINK = LINK;
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

    public String getF_Structure() {
        return this.F_Structure;
    }

    public void setF_Structure(String F_Structure) {
        this.F_Structure = F_Structure;
    }

    public String getF_Municipality() {
        return this.F_Municipality;
    }

    public void setF_Municipality(String F_Municipality) {
        this.F_Municipality = F_Municipality;
    }

    public String getF_Town() {
        return this.F_Town;
    }

    public void setF_Town(String F_Town) {
        this.F_Town = F_Town;
    }
}
