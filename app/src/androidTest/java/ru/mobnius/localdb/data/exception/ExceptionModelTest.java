package ru.mobnius.localdb.data.exception;

import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

public class ExceptionModelTest {

    @Test
    public void toStringTest(){
        ExceptionModel model = ExceptionModel.getInstance(new Date(), "Ошибка", ExceptionGroup.NONE, ExceptionCode.ALL);
        String str = model.toString();

        Assert.assertNotNull(str);
    }

    @Test
    public void toModelTest(){
        ExceptionModel model = ExceptionModel.getInstance(new Date(), "Ошибка", ExceptionGroup.NONE, ExceptionCode.ALL);
        String str = model.toString();

        ExceptionModel model1 = ExceptionUtils.toModel(str);
        assert model1 != null;
        Assert.assertEquals(model.getId(), model1.getId());
        Assert.assertEquals(model.getDate().getTime() / 1000, model1.getDate().getTime() / 1000);
        Assert.assertEquals(model.getMessage(), model1.getMessage());
    }
}
