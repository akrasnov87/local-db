package ru.mobnius.localdb.data.exception;

import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

public class FileExceptionManagerTest {

    private OnFileExceptionManagerListener fileExceptionManager;

    @Before
    public void setUp(){
        fileExceptionManager = FileExceptionManager.getInstance(InstrumentationRegistry.getInstrumentation().getTargetContext());
        fileExceptionManager.deleteFolder();
    }

    @Test
    public void writeExceptionTest() {
        ExceptionModel model = ExceptionModel.getInstance(new Date(), "Ошибка", ExceptionGroup.NONE, ExceptionCode.ALL);
        String str = model.toString();
        String fileName = model.getFileName();
        fileExceptionManager.writeBytes(fileName, str.getBytes());
        Assert.assertTrue(fileExceptionManager.exists(fileName));
        byte[] bytes = fileExceptionManager.readPath(fileName);
        Assert.assertNotNull(bytes);
        String result = new String(bytes);
        ExceptionModel exceptionModel = ExceptionUtils.toModel(result);
        assert exceptionModel != null;
        Assert.assertEquals(exceptionModel.getMessage(), model.getMessage());
        fileExceptionManager.deleteFile(fileName);
        Assert.assertFalse(fileExceptionManager.exists(fileName));
    }

    @Test
    public void getExceptionTest() {
        String exceptionID = "";
        for(int i = 0; i < 2; i++){
            long time = new Date().getTime();
            ExceptionModel model = ExceptionModel.getInstance(new Date(time + (1000 * 60 * (i + 1))), "Ошибка #" + i, ExceptionGroup.NONE, ExceptionCode.ALL);
            String str = model.toString();
            String fileName = model.getFileName();
            fileExceptionManager.writeBytes(fileName, str.getBytes());
            if(i == 1) {
                exceptionID = model.getId();
            }
        }

        OnExceptionManagerListener exceptionManager = (OnExceptionManagerListener)fileExceptionManager;
        Assert.assertEquals(exceptionManager.getExceptionList().size(), 2);
        Assert.assertEquals(exceptionManager.getException(exceptionID).getMessage(), "Ошибка #1");
        Assert.assertEquals(exceptionManager.getLastException().getMessage(), "Ошибка #1");
    }
}

