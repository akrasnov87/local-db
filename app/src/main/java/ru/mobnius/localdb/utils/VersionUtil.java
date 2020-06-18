package ru.mobnius.localdb.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.util.Date;

import ru.mobnius.localdb.data.Version;

/**
 * Вспомогательная утилита для работы версией
 */
public class VersionUtil {
    /**
     * Возврщается версия приложения для пользователя (versionName)
     *
     * @param context activity
     * @return возвращается версия
     */
    public static String getVersionName(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "0.0.0.0";
        }
    }

    /**
     * Возврщается укороченая версия приложения для пользователя (versionName)
     *
     * @param context activity
     * @return Возвращаются только первые два числа
     */
    public static String getShortVersionName(Context context) {
        String version = getVersionName(context);
        String[] data = version.split("\\.");
        return data[0] + "." + data[1];
    }

    /**
     * Проверка на обновление версии
     * @param context контекст
     * @param newVersion новая версия на сервере
     * @param debug режим отладки
     * @return обновлять версию или нет
     */
    public static boolean isUpgradeVersion(Context context, String newVersion, boolean debug) {
        Version mVersion = new Version();
        String currentVersion = VersionUtil.getVersionName(context);
        Date currentDate = mVersion.getBuildDate(Version.BIRTH_DAY, currentVersion);
        Date serverDate = mVersion.getBuildDate(Version.BIRTH_DAY, newVersion);

        return serverDate.getTime() > currentDate.getTime()
                && (mVersion.getVersionState(currentVersion) == Version.PRODUCTION || debug);
    }
}
