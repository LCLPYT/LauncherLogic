package work.lclpnet.launcherlogic.cmd;

import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

@Command(name = "echo")
public class CommandEcho implements Callable<Integer>{

	@Override
	public Integer call() {
		System.out.println("Echo from LauncherLogic running in java " + System.getProperty("java.version") + ".");
		return 0;
	}

}
