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
    private final String zipFile;
    private final String location;
    private String absPath;

    public String getAbsPath() {
        return absPath;
    }

    public UnzipUtil(String zipFile, String location) {
        this.zipFile = zipFile;
        this.location = location;

        dirChecker("");
    }

    public String unzip() {

        try {
            FileInputStream fin = new FileInputStream(zipFile);
            ZipInputStream zin = new ZipInputStream(fin);
            ZipEntry ze;
            while ((ze = zin.getNextEntry()) != null) {
                Log.v("Decompress", "Unzipping " + ze.getName());
                if (ze.isDirectory()) {
                    dirChecker(ze.getName());
                    absPath = location + "/" + ze.getName();
                } else {
                    FileOutputStream fileOutputStream = new FileOutputStream(location + "/" + ze.getName());
                    absPath = location + "/" + ze.getName();
                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = zin.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, len);
                    }
                    fileOutputStream.close();
                    zin.closeEntry();
                }
            }
            zin.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (absPath != null) {
            try {
                return readFile(absPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
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
                fileContents.append(scanner.nextLine()).append(System.lineSeparator());
            }
            return fileContents.toString();
        }
    }
}
