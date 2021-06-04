package work.lclpnet.launcherlogic.cmd;

import java.util.concurrent.Callable;

import picocli.CommandLine.Command;

@Command(name = "echo")
public class CommandEcho implements Callable<Integer>{

	@Override
	public Integer call() throws Exception {
		System.out.println("Echo from LauncherLogic running in java " + System.getProperty("java.version") + ".");
		return 0;
	}

}
