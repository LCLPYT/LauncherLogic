package work.lclpnet.launcherlogic;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import work.lclpnet.launcherlogic.cmd.CommandCheckUpdate;
import work.lclpnet.launcherlogic.cmd.CommandEcho;
import work.lclpnet.launcherlogic.cmd.CommandInstall;
import work.lclpnet.launcherlogic.cmd.CommandPreparePlay;
import work.lclpnet.launcherlogic.util.Logging;
import work.lclpnet.launcherlogic.util.ProgressCallbackClient;

import java.util.concurrent.Callable;

@Command(
        name = "launcher_logic",
        mixinStandardHelpOptions = true,
        description = "Main command for launcher logic.",
        subcommands = {
                CommandEcho.class,
                CommandInstall.class,
                CommandPreparePlay.class,
                CommandCheckUpdate.class
        }
)
public class LauncherLogic implements Callable<Integer> {

    public static void main(String[] args) {
        // args = new String[] {"install", "ls5", "/home/lukas/lclpserver", "--debug", "--java-exe", "/home/lukas/Documents/projects/lclpserver5/LCLPLauncher/bin/launcherlogic/runtime/bin/java", "--launcher-forge-installer-jar", "/home/lukas/Documents/projects/lclpserver5/LCLPLauncher/bin/launcherlogic/launcherlogic-forge_installer.jar", "--profile-java", "/usr/lib/jvm/java-8-openjdk-amd64/bin/java"};
        // args = new String[]{"install", "ls5", "C:\\Users\\Lukas\\lclpserver5", "--debug", "--java-exe", "C:\\Users\\lukas\\Documents\\projects\\misc\\LCLPLauncher\\bin\\launcherlogic\\runtime\\bin\\java.exe", "--launcher-forge-installer-jar", "C:\\Users\\lukas\\Documents\\projects\\misc\\LCLPLauncher\\bin\\launcherlogic\\launcherlogic-forge_installer.jar"};

        Logging.setupLogging();

        System.out.printf("Running LauncherLogic version %s using java %s\n", "1.1.1", System.getProperty("java.version"));

        if (args.length <= 0) System.out.println("Supply '--help' as argument to get help.");

        LauncherLogic instance = new LauncherLogic();
        int exitCode = new CommandLine(instance).execute(args);
        if (ProgressCallbackClient.hasOpenSockets()) {
            try {
                Thread.sleep(1000L); // Delay to send potential pending tcp stuff (is this necessary?)
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ProgressCallbackClient.closeAllSockets();
        }

        Logging.closeLogging();

        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        return 0;
    }

}
