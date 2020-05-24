package work.lclpnet.launcherlogic;

import java.util.concurrent.Callable;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import work.lclpnet.launcherlogic.cmd.CommandCheckUpdate;
import work.lclpnet.launcherlogic.cmd.CommandEcho;
import work.lclpnet.launcherlogic.cmd.CommandInstall;
import work.lclpnet.launcherlogic.cmd.CommandPreparePlay;
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
		//args = new String[] {"install", "ls5", "C:\\Users\\Lukas\\lclpserver5", "--debug", "--java-exe", "C:\\Users\\Lukas\\Documents\\Electron\\lclplauncher\\bin\\launcherlogic\\runtime\\bin\\java.exe", "--launcher-forge-installer-jar", "C:\\Users\\Lukas\\Documents\\Electron\\lclplauncher\\bin\\launcherlogic\\launcherlogic-forge_installer.jar"};
		System.out.println("Running LauncherLogic version " + VERSION + " using java " + System.getProperty("java.version"));

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
		System.exit(exitCode);
	}

	@Override
	public Integer call() throws Exception {
		return 0;
	}

}
