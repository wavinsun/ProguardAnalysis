package cn.mutils.app.proguard.diff;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;

public class FileUtil {

    public static String getString(File file) {
        FileInputStream fis = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            fis = new FileInputStream(file);
            int bufferCount = -1;
            byte[] buffer = new byte[4096];
            while ((bufferCount = fis.read(buffer)) != -1) {
                bos.write(buffer, 0, bufferCount);
            }
            return bos.toString("UTF-8");
        } catch (Exception e) {
            return null;
        } finally {
            close(fis);
            close(bos);
        }
    }

    public static void close(Closeable c) {
        try {
            if (c != null) {
                c.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
