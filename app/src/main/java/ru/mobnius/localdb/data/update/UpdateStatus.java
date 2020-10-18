package ru.mobnius.localdb.data.update;

public class UpdateStatus {

    private static boolean downloadStatusMO;
    private static boolean downloadStatusLDB;
    private static boolean needUpdateMO;
    private static boolean needUpdateLDB;
    private final DownloadUpdateStatus downloadUpdateStatusMO;
    private final DownloadUpdateStatus downloadUpdateStatusLDB;

    public UpdateStatus(String localVersionMO, String localVersionLDB, String remoteVersionMO, String remoteVersionLDB){
        downloadUpdateStatusMO = new DownloadUpdateStatus(localVersionMO, remoteVersionMO);
        downloadUpdateStatusLDB = new DownloadUpdateStatus(localVersionLDB, remoteVersionLDB);

    }


}
