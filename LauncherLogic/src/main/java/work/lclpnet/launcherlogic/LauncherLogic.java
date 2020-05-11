package work.lclpnet.launcherlogic;

import picocli.CommandLine.Command;

@Command
public class LauncherLogic {

	public static void main(String[] args) {
		System.out.println("Using Java version " + System.getProperty("java.version") + "...");
		
		if(args.length > 0) {
			System.err.println("welp. an argument was passed...");
			System.exit(1);
			return;
		}
		
		System.out.println("Hello from java!");
	}
	
	public static boolean test() {
		return true;
	}
	
}
