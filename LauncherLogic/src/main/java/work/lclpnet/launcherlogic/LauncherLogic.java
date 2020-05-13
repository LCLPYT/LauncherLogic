package work.lclpnet.launcherlogic;

import java.util.concurrent.Callable;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import work.lclpnet.launcherlogic.cmd.CommandEcho;
import work.lclpnet.launcherlogic.cmd.CommandInstall;

@Command(
		name = "java -jar LauncherLogic.jar", 
		mixinStandardHelpOptions = true, 
		version = LauncherLogic.VERSION, 
		description = "Main command for launcher logic.",
		subcommands = {
				CommandEcho.class,
				CommandInstall.class
		}
		)
public class LauncherLogic implements Callable<Integer>{

	public static final String VERSION = "1.0";
	public static final boolean DEBUG = true;
	private static LauncherLogic instance = null;

	public static void main(String[] args) {
		args = new String[] {"install", "ls5", "C:\\Users\\Lukas\\Desktop\\install", "--debug"};
		System.out.println("Running LauncherLogic version " + VERSION + " using java " + System.getProperty("java.version"));

		instance = new LauncherLogic();
		System.exit(new CommandLine(instance).execute(args));
	}

	@Override
	public Integer call() throws Exception {
		return 0;
	}
	
}
