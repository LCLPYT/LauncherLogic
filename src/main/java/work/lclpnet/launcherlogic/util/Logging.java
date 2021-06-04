package work.lclpnet.launcherlogic.util;

import java.io.*;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class Logging {

    private static FileOutputStream outOut, outErr;
    private static PrintStream origOut, origErr;

    public static void setupLogging() {
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "launcher_logic");
        if(!tempDir.exists() && !tempDir.mkdirs()) throw new IllegalStateException("Could not create temp directory");

        new Thread(() -> deleteOldFiles(tempDir), "Log file cleanup").start();

        File logFile = new File(tempDir, String.format("launcher_logic_%s_stdout.txt", System.currentTimeMillis() / 1000L));
        File errFile = new File(tempDir, String.format("launcher_logic_%s_stderr.txt", System.currentTimeMillis() / 1000L));

        System.out.println("Writing logs to: " + tempDir.getAbsolutePath());

        try {
            outOut = new FileOutputStream(logFile);
            outErr = new FileOutputStream(errFile);
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
            System.exit(1);
            return;
        }

        origOut = System.out;
        origErr = System.err;
        LoggingPrintStream.origErr = origErr;

        System.setOut(new LoggingPrintStream(origOut, outOut));
        System.setErr(new LoggingPrintStream(origErr, outErr));
    }

    private static void deleteOldFiles(File tempDir) {
        File[] children = tempDir.listFiles();
        if(children == null) return;

        Arrays.stream(children)
                .filter(file -> System.currentTimeMillis() - file.lastModified() > TimeUnit.DAYS.toMillis(14))
                .forEach(file -> {
                    if(!file.delete())
                        System.err.printf("Failed to delete old log file '%s'.%n", file.getName());
                });
    }

    public static void closeLogging() {
        try {
            outOut.close();
            outErr.close();
            System.setOut(origOut);
            System.setErr(origErr);
        } catch (IOException e) {
            e.printStackTrace(origErr);
        }
    }

}
