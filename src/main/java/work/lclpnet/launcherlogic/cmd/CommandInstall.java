package work.lclpnet.launcherlogic.cmd;

import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Visibility;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import work.lclpnet.launcherlogic.install.InstallationObject;
import work.lclpnet.launcherlogic.install.InstallationObjects;
import work.lclpnet.launcherlogic.util.CommonHelper;
import work.lclpnet.launcherlogic.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

@Command(
		name = "install",
		mixinStandardHelpOptions = true,
		description = "Installs an object with the given ID."
		)
public class CommandInstall implements Callable<Integer>{

	public static boolean debugMode = false, doProgressCallback = false;
	public static String pcHost = null;
	public static int pcPort = 0;
	
	@Parameters(index = "0", paramLabel = "installId", description = "ID of the object to install.")
	String installId;
	
	@Parameters(index = "1", paramLabel = "installDirectory", description = "The directory to install into.")
	File dir;
	
	@Option(names = {"--debug"}, description = "Enables debug mode", showDefaultValue = Visibility.ON_DEMAND)
	boolean debug = false;

	@Option(names = {"--progress-callback"}, description = "Specifies progress callback host and port. <host>:<port>")
	String progressCallback = null;
	
	@Option(names = {"--java-exe"}, description = "Path to the java executable", showDefaultValue = Visibility.ALWAYS)
	public File javaExe = new File(FileUtils.getCurrentDir(), "runtime/bin/java.exe");
	
	@Option(names = {"--launcher-forge-installer-jar"}, description = "Path to the launcher logic forge installer jar.", showDefaultValue = Visibility.ALWAYS)
	public File llForgeInstallerJar = new File(FileUtils.getCurrentDir(), "launcherlogic-forge_installer.jar");

	@Option(names = {"--profile-java"}, description = "The path to the java executable to print to the profile.")
	public String profileJavaExecPath = null;
	
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
		
		CommandInstall.doProgressCallback = progressCallback != null;
		if(CommandInstall.doProgressCallback) System.out.println("Enabling progress callback.");
		
		System.out.println("Found installable object " + install + ".");
		
		System.out.println("Installing into '" + dir.getAbsolutePath() + "'...");
		try {
			install.setCommandDelegate(this);
			install.installInto(dir);
			System.out.println("Installation complete.");
			return 0;
		} catch (IOException e) {
			if(debugMode) e.printStackTrace();
			System.err.println("Installation failed: " + e.getMessage());
			return 1;
		}
	}

	public static String getPcHost() {
		return pcHost;
	}
	
	public static int getPcPort() {
		return pcPort;
	}

}
