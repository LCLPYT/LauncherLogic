package work.lclpnet.launcherlogic.cmd;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Help.Visibility;
import work.lclpnet.launcherlogic.install.InstallationObject;
import work.lclpnet.launcherlogic.install.InstallationObjects;
import work.lclpnet.launcherlogic.util.CommonHelper;

@Command(
		name = "checkUpdate",
		mixinStandardHelpOptions = true,
		description = "Checks for an update on the specified installation object."
		)
public class CommandCheckUpdate implements Callable<Integer>{

	public static boolean debugMode = false, doProgressCallback = false; 
	public static String pcHost = null;
	public static int pcPort = 0;
	
	@Parameters(index = "0", paramLabel = "installId", description = "ID of the object to install.")
	String installId;
	
	@Parameters(index = "1", paramLabel = "installDirectory", description = "The directory in which the installation is.")
	File dir;
	
	@Option(names = {"--debug"}, description = "Enables debug mode", showDefaultValue = Visibility.ON_DEMAND)
	boolean debug = false;
	
	@Option(names = {"--progress-callback"}, description = "Specifies progress callback host and port. <host>:<port>")
	String progressCallback = null;
	
	@Override
	public Integer call() throws Exception {
		System.out.println("Searching for an installable object matching '" + installId + "' in the registry...");
		InstallationObject install = InstallationObjects.getInstallation(installId);
		if(install == null) {
			System.err.println("Error, could not find any installation object with id '" + installId + "'.");
			return 1;
		}
		
		if(progressCallback != null) CommonHelper.parsePC(progressCallback, (host, port) -> {
			pcHost = host;
			pcPort = port;
		});
		
		CommandInstall.debugMode = debug;
		if(CommandInstall.debugMode) System.out.println("Enabling debug mode...");
		
		CommandCheckUpdate.doProgressCallback = progressCallback != null;
		if(CommandCheckUpdate.doProgressCallback) System.out.println("Enabling progress callback.");
		
		System.out.println("Found installable object " + install + ".");
		
		System.out.println("Checking for update in directory '" + dir.getAbsolutePath() + "'...");
		try {
			install.checkForUpdate(dir);
			System.out.println("Update checking complete.");
			return 0;
		} catch (IOException e) {
			if(debugMode) e.printStackTrace();
			System.err.println("Installation failed.");
			return 1;
		}
	}
	
}
