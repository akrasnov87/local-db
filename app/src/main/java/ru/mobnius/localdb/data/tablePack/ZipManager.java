package ru.mobnius.localdb.data.tablePack;

import android.os.Build;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Сжатие строки через ZIP
 */
public class ZipManager {

    public final static int BUFFER_SIZE = 2048;

    public static String getMode() {
        return Build.VERSION.SDK_INT <= Build.VERSION_CODES.M ? "LIB" : "ZIP";
    }

    /**
     * Сжать информацию
     * @param inputText текст
     * @return массив байтов
     */
    public static byte[] compress(String inputText) throws IOException {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            return oldCompress(inputText);
        } else {
            return newCompress(inputText);
        }
    }

    /**
     * Распаковать информацию
     * @param compressed данные
     * @return строка с информацией
     * @throws IOException исключение при обработке
     */
    public static byte[] decompress(byte[] compressed) throws IOException, DataFormatException {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            return oldDeCompress(compressed);
        } else {
            return newDeCompress(compressed);
        }
    }

    private static byte[] oldCompress(String inputText) {
        byte[] input = inputText.getBytes(StandardCharsets.UTF_8);

        Deflater compressor = new Deflater();
        compressor.setInput(input);
        compressor.finish();

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buf = new byte[BUFFER_SIZE];
        while (!compressor.finished()) {
            int byteCount = compressor.deflate(buf);
            byteArrayOutputStream.write(buf, 0, byteCount);
        }
        compressor.end();
        return byteArrayOutputStream.toByteArray();
    }

    private static byte[] newCompress(String inputText) throws IOException {
        byte[] bytes = inputText.getBytes(StandardCharsets.UTF_8);

        InputStream stream = new ByteArrayInputStream(bytes);
        byte[] data = new byte[BUFFER_SIZE];
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream( bos );
        BufferedInputStream entryStream = new BufferedInputStream( stream, BUFFER_SIZE);
        ZipEntry entry = new ZipEntry( "" );
        zos.putNextEntry( entry );
        int count;
        while ( ( count = entryStream.read( data, 0, BUFFER_SIZE) ) != -1 )
        {
            zos.write( data, 0, count );
        }
        entryStream.close();
        zos.closeEntry();
        zos.close();

        return bos.toByteArray();
    }

    private static byte[] newDeCompress(byte[] compressed) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(compressed);
        ZipInputStream zin = new ZipInputStream(bis);

        //noinspection LoopStatementThatDoesntLoop
        while(zin.getNextEntry() != null) {
            // распаковка
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] data = new byte[BUFFER_SIZE];
            int count;
            while ((count = zin.read( data, 0, BUFFER_SIZE)) != -1) {
                byteArrayOutputStream.write(data, 0, count);
            }
            byteArrayOutputStream.flush();
            zin.closeEntry();
            byteArrayOutputStream.close();
            return byteArrayOutputStream.toByteArray();
        }
        return  null;
    }

    private static byte[] oldDeCompress(byte[] compressed) throws DataFormatException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(compressed.length);
        Inflater decompressor = new Inflater();
        try {
            decompressor.setInput(compressed);
            final byte[] buf = new byte[2048];
            while (!decompressor.finished()) {
                int count = decompressor.inflate(buf);
                bos.write(buf, 0, count);
            }
        } finally {
            decompressor.end();
        }
        return bos.toByteArray();
    }
}
