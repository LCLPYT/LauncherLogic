package work.lclpnet.launcherlogic.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class ZIPUtil {

    public static void extract(File zipFile, File outFolder, Consumer<Double> progress) throws IOException {
        byte[] buffer = new byte[1024];

        int entryCount = 0;
        try (ZipFile zip = new ZipFile(zipFile)) {
            final Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements())
                if (!entries.nextElement().isDirectory())
                    entryCount++;
        }

        ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
        ZipEntry zipEntry;
        int count = 0;
        while ((zipEntry = zis.getNextEntry()) != null) {
            File newFile = newFile(outFolder, zipEntry);
            if (zipEntry.isDirectory()) {
                newFile.mkdirs();
                continue;
            }

            newFile.getParentFile().mkdirs();
            FileOutputStream fos = new FileOutputStream(newFile);
            int len;
            while ((len = zis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
            fos.close();

            if (entryCount > 0) progress.accept((double) ++count / (double) entryCount);
        }
        zis.closeEntry();
        zis.close();
    }

    private static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

}
