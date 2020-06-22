package ru.mobnius.localdb.data.exception;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import ru.mobnius.localdb.storage.ClientErrors;
import ru.mobnius.localdb.storage.DaoMaster;
import ru.mobnius.localdb.storage.DaoSession;
import ru.mobnius.localdb.storage.DbOpenHelper;

public class ExceptionUtilsTest {
    private OnFileExceptionManagerListener fileExceptionManager;
    private DaoSession daoSession;
    private Context mContext;

    @Before
    public void setUp() {
        mContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        daoSession = new DaoMaster(new DbOpenHelper(mContext, "loader-exception.db").getWritableDb()).newSession();
        fileExceptionManager = FileExceptionManager.getInstance(mContext);
        fileExceptionManager.deleteFolder();
        daoSession.getClientErrorsDao().deleteAll();
    }

    @Test
    public void saveLocalExceptionTest() {
        ExceptionModel model = ExceptionModel.getInstance(new Date(), "Ошибка", ExceptionGroup.NONE, ExceptionCode.ALL);
        String str = model.toString();
        String fileName = model.getFileName();
        fileExceptionManager.writeBytes(fileName, str.getBytes());
        ExceptionUtils.saveLocalException(mContext, daoSession);

        List<ClientErrors> list = daoSession.getClientErrorsDao().queryBuilder().list();
        Assert.assertEquals(list.size(), 1);
    }

    @Test
    public void saveExceptionTest() {
        ExceptionUtils.saveException(mContext, daoSession, new Exception("тест"), ExceptionGroup.NONE, ExceptionCode.ALL);
        List<ClientErrors> list = daoSession.getClientErrorsDao().queryBuilder().list();
        Assert.assertEquals(list.size(), 1);
    }

    @Test
    public void codeToStringTest() {
        Assert.assertEquals(ExceptionUtils.codeToString(2), "002");
        Assert.assertEquals(ExceptionUtils.codeToString(21), "021");
        Assert.assertEquals(ExceptionUtils.codeToString(215), "215");
        Assert.assertEquals(ExceptionUtils.codeToString(2158), "2158");
    }
}
