package ru.mobnius.localdb.utils;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import ru.mobnius.localdb.Logger;

public class UnzipUtil {
    private String zipFile;
    private String location;

    public UnzipUtil(String zipFile, String location) {
        this.zipFile = zipFile;
        this.location = location;

        dirChecker("");
    }

    public String unzip() {

        String path = null;
        try {
            FileInputStream fin = new FileInputStream(zipFile);
            ZipInputStream zin = new ZipInputStream(fin);
            ZipEntry ze;
            while ((ze = zin.getNextEntry()) != null) {
                Log.v("Decompress", "Unzipping " + ze.getName());
                if (ze.isDirectory()) {
                    dirChecker(ze.getName());
                    path = location + "/" + ze.getName();
                } else {
                    FileOutputStream fout = new FileOutputStream(location + "/" + ze.getName());
                    path = location + "/" + ze.getName();
                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = zin.read(buffer)) != -1) {
                        fout.write(buffer, 0, len);
                    }
                    fout.close();
                    zin.closeEntry();
                }
            }
            zin.close();
        } catch (Exception e) {
            Logger.error(e);
        }
        if (path != null) {
            try {
                return readFile(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private void dirChecker(String dir) {
        File f = new File(location + dir);
        if (!f.isDirectory()) {
            f.mkdirs();
        }
    }

    private String readFile(String pathname) throws IOException {

        File file = new File(pathname);
        StringBuilder fileContents = new StringBuilder((int) file.length());

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                fileContents.append(scanner.nextLine() + System.lineSeparator());
            }
            return fileContents.toString();
        }
    }
}
