package ru.mobnius.localdb.request;

import android.content.Context;
import android.content.Intent;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.mobnius.localdb.Names;
import ru.mobnius.localdb.data.PreferencesManager;
import ru.mobnius.localdb.model.Response;
import ru.mobnius.localdb.model.progress.ProgressResult;
import ru.mobnius.localdb.utils.UrlReader;

/**
 * Проверка выполнения синхронизации
 */
public class SyncStopRequestListener extends AuthFilterRequestListener
        implements OnRequestListener {
    private Context mContext;

    public SyncStopRequestListener(Context context){
        mContext = context;
    }

    @Override
    public boolean isValid(String query) {
        Pattern pattern = Pattern.compile("^/sync/stop", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(query);
        return matcher.find();
    }

    @Override
    public Response getResponse(UrlReader urlReader) {
        Intent intent = new Intent(Names.CANCEL_TASK_TAG);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        Response response = super.getResponse(urlReader);
        if(response != null) {
            return response;
        }

        if(PreferencesManager.getInstance().getProgress() != null) {
            PreferencesManager.getInstance().setProgress(null);

            ProgressResult progressResult = ProgressResult.getInstance(PreferencesManager.getInstance().getProgress());
            response = Response.getInstance(urlReader, progressResult.toJsonString());
        } else {
            response = Response.getErrorInstance(urlReader, "Информация о выполнении загрузки не найдена. Возможно она была завершена ранее.", Response.RESULT_FAIL);
        }
        return response;
    }
}
