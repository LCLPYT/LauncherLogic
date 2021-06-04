package work.lclpnet.launcherlogic.util;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;
import work.lclpnet.launcherlogic.ls5.LS5Configuration;

import java.io.*;
import java.nio.file.Files;

public class OSHooks {

    private static class OSHandler {

        //private String msg = "Unsupported operating system: " + System.getProperty("os.name");

        public String getYTDLDownloadLink() {
            return "https://yt-dl.org/downloads/latest/youtube-dl";
        }

        public String getYTDLExeName() {
            return "youtube-dl";
        }

        public String getFFMPEGUrl(LS5Configuration config) {
            return (String) config.getVariable("ffmpegLinux");
        }

        public String getFFMPEGSha256(LS5Configuration config) {
            return (String) config.getVariable("ffmpegLinuxSha256");
        }

        public String getFFMPEGName() {
            return "ffmpeg.tar.xz";
        }

        public void extractFFMPEG(File from, File ffmpegDir, Progress progress) throws IOException {
            try (InputStream fi = Files.newInputStream(from.toPath());
                 InputStream bi = new BufferedInputStream(fi);
                 InputStream xzi = new XZCompressorInputStream(bi);
                 ArchiveInputStream i = new TarArchiveInputStream(xzi)) {
                ArchiveEntry entry;
                while ((entry = i.getNextEntry()) != null) {
                    if (!i.canReadEntryData(entry)) {
                        continue;
                    }
                    File f = new File(ffmpegDir, entry.getName());
                    if (entry.isDirectory()) {
                        if (!f.isDirectory() && !f.mkdirs()) {
                            throw new IOException("failed to create directory " + f);
                        }
                    } else {
                        File parent = f.getParentFile();
                        if (!parent.isDirectory() && !parent.mkdirs()) {
                            throw new IOException("failed to create directory " + parent);
                        }
                        try (OutputStream o = Files.newOutputStream(f.toPath())) {
                            IOUtils.copy(i, o);
                        }
                    }
                }
            }
        }

        public String getForgeInstallerClasspath(File llForgeInstallerJar, File installerJar) {
            return llForgeInstallerJar.getAbsolutePath() + ":" + installerJar.getAbsolutePath();
        }

    }

    private static class LinuxHandler extends OSHandler {
        // Override methods here
    }

    private static class WinHandler extends OSHandler {
        @Override
        public String getYTDLDownloadLink() {
            return "https://yt-dl.org/latest/youtube-dl.exe";
        }

        @Override
        public String getYTDLExeName() {
            return "youtube-dl.exe";
        }

        @Override
        public String getFFMPEGUrl(LS5Configuration config) {
            return (String) config.getVariable("ffmpegWin");
        }

        @Override
        public String getFFMPEGSha256(LS5Configuration config) {
            return (String) config.getVariable("ffmpegWinSha256");
        }

        @Override
        public String getFFMPEGName() {
            return "ffmpeg.zip";
        }

        @Override
        public void extractFFMPEG(File from, File ffmpegDir, Progress progress) throws IOException {
            ZIPUtil.extract(from, ffmpegDir, progress);
        }

        @Override
        public String getForgeInstallerClasspath(File llForgeInstallerJar, File installerJar) {
            return llForgeInstallerJar.getAbsolutePath() + ";" + installerJar.getAbsolutePath();
        }
    }

    private static final OSHandler handler;

    static {
        if (System.getProperty("os.name").equalsIgnoreCase("Linux")) handler = new LinuxHandler();
        else if (System.getProperty("os.name").contains("Windows")) handler = new WinHandler();
        else handler = new OSHandler();
    }

    public static String getYTDLDownloadLink() {
        return handler.getYTDLDownloadLink();
    }

    public static String getYTDLExeName() {
        return handler.getYTDLExeName();
    }

    public static String getFFMPEGUrl(LS5Configuration config) {
        return handler.getFFMPEGUrl(config);
    }

    public static String getFFMPEGSha256(LS5Configuration config) {
        return handler.getFFMPEGSha256(config);
    }

    public static String getFFMPEGName() {
        return handler.getFFMPEGName();
    }

    public static void extractFFMPEG(File from, File ffmpegDir, Progress progress) throws IOException {
        handler.extractFFMPEG(from, ffmpegDir, progress);
    }

    public static String getForgeInstallerClasspath(File llForgeInstallerJar, File installerJar) {
        return handler.getForgeInstallerClasspath(llForgeInstallerJar, installerJar);
    }

}
