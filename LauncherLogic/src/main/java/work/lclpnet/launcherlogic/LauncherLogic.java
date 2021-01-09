package work.lclpnet.launcherlogic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.Callable;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import work.lclpnet.launcherlogic.cmd.CommandCheckUpdate;
import work.lclpnet.launcherlogic.cmd.CommandEcho;
import work.lclpnet.launcherlogic.cmd.CommandInstall;
import work.lclpnet.launcherlogic.cmd.CommandPreparePlay;
import work.lclpnet.launcherlogic.util.LoggingPrintStream;
import work.lclpnet.launcherlogic.util.ProgressCallbackClient;

@Command(
		name = "java -jar LauncherLogic.jar",
		mixinStandardHelpOptions = true,
		version = LauncherLogic.VERSION,
		description = "Main command for launcher logic.",
		subcommands = {
				CommandEcho.class,
				CommandInstall.class,
				CommandPreparePlay.class,
				CommandCheckUpdate.class
		}
		)
public class LauncherLogic implements Callable<Integer>{

	public static final String VERSION = "1.0";
	public static final boolean DEBUG = true;
	private static LauncherLogic instance = null;
	
	public static void main(String[] args) {
		//args = new String[] {"install", "ls5", "/home/lukas/lclpserver", "--debug", "--java-exe", "/home/lukas/Documents/projects/lclpserver5/LCLPLauncher/bin/launcherlogic/runtime/bin/java", "--launcher-forge-installer-jar", "/home/lukas/Documents/projects/lclpserver5/LCLPLauncher/bin/launcherlogic/launcherlogic-forge_installer.jar", "--profile-java", "/usr/lib/jvm/java-8-openjdk-amd64/bin/java"};
		//args = new String[] {"install", "ls5", "C:\\Users\\Lukas\\lclpserver5", "--debug", "--java-exe", "C:\\Users\\Lukas\\Documents\\Electron\\lclplauncher\\bin\\launcherlogic\\runtime\\bin\\java.exe", "--launcher-forge-installer-jar", "C:\\Users\\Lukas\\Documents\\Electron\\lclplauncher\\bin\\launcherlogic\\launcherlogic-forge_installer.jar"};
		
		File tempDir = new File(System.getProperty("java.io.tmpdir"), "launcherlogic");
		tempDir.mkdirs();
		File logFile = new File(tempDir, String.format("launcherlogic_%s_stdout.txt", System.currentTimeMillis() / 1000L));
		File errFile = new File(tempDir, String.format("launcherlogic_%s_stderr.txt", System.currentTimeMillis() / 1000L));
		
		System.out.println("Writing logs to: " + tempDir.getAbsolutePath());
		
		FileOutputStream outOut, outErr;
		try {
			outOut = new FileOutputStream(logFile);
			outErr = new FileOutputStream(errFile);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			System.exit(1);
			return;
		}
		
		PrintStream origOut = System.out, origErr = System.err;
		
		System.setOut(new LoggingPrintStream(origOut, outOut));
		System.setErr(new LoggingPrintStream(origErr, outErr));

		System.out.printf("Running LauncherLogic version %s using java %s\n", VERSION, System.getProperty("java.version"));

		if(args.length <= 0) System.out.println("Supply '--help' as argument to get help.");

		instance = new LauncherLogic();
		int exitCode = new CommandLine(instance).execute(args);
		if(ProgressCallbackClient.hasOpenSockets()) {
			try {
				Thread.sleep(1000L); //Delay to send potential pending tcp stuff (is this necessary?)
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			ProgressCallbackClient.closeAllSockets();
		}
		
		try {
			outOut.close();
			outErr.close();
			System.setOut(origOut);
			System.setErr(origErr);
		} catch (IOException e) {
			e.printStackTrace(origErr);
		}
		
		System.exit(exitCode);
	}

	@Override
	public Integer call() throws Exception {
		return 0;
	}

}
