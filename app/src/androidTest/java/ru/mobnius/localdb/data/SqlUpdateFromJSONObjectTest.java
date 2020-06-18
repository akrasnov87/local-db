package ru.mobnius.localdb.data;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class SqlUpdateFromJSONObjectTest {

    @Test
    public void sql() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", 1);
        jsonObject.put("name", "Имя");
        jsonObject.put("age", 31);

        SqlUpdateFromJSONObject insert = new SqlUpdateFromJSONObject(jsonObject, "users", "id");
        String query = insert.convertToQuery();

        Assert.assertEquals(query, "UPDATE users set name  = ?, age  = ? where id = ?");
        Object[] values = insert.getValues(jsonObject);
        Assert.assertEquals(values[1] , 31);
        Assert.assertEquals(values[0] , "Имя");
        Assert.assertEquals(values[2] , 1);
    }
}
