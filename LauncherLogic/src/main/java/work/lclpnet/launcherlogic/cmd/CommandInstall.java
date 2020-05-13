package work.lclpnet.launcherlogic.cmd;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Visibility;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import work.lclpnet.launcherlogic.install.InstallationObject;
import work.lclpnet.launcherlogic.install.InstallationObjects;

@Command(
		name = "install",
		mixinStandardHelpOptions = true,
		description = "Installs an object with the given ID."
		)
public class CommandInstall implements Callable<Integer>{

	public static boolean debugMode = false;
	
	@Parameters(index = "0", paramLabel = "installId", description = "ID of the object to install.")
	String installId;
	
	@Parameters(index = "1", paramLabel = "installDirectory", description = "The directory to install into.")
	File dir;
	
	@Option(names = {"--debug"}, description = "Enables debug mode", showDefaultValue = Visibility.ON_DEMAND)
	boolean debug = false;
	
	@Override
	public Integer call() throws Exception {
		System.out.println("Searching for an installable object in the registry...");
		InstallationObject install = InstallationObjects.getInstallation(installId);
		if(install == null) {
			System.err.println("Error, could not find any installation object with id '" + installId + "'.");
			return 1;
		}
		
		CommandInstall.debugMode = debug;
		if(CommandInstall.debugMode) System.out.println("Enabling debug mode...");
		
		System.out.println("Found installable object " + install + ".");
		
		System.out.println("Installing into '" + dir.getAbsolutePath() + "'...");
		try {
			install.installInto(dir);
			System.out.println("Installation complete.");
			return 0;
		} catch (IOException e) {
			if(debugMode) e.printStackTrace();
			System.err.println("Installation failed.");
			return 1;
		}
	}

}
