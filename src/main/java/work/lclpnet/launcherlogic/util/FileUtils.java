package work.lclpnet.launcherlogic.util;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Locale;

import work.lclpnet.launcherlogic.LauncherLogic;

public class FileUtils {

	public static boolean recursiveDelete(File file) {
		if (file == null || !file.exists()) return true;

		if (file.isDirectory()) { 
			File[] files = file.listFiles();
			if(files != null && files.length > 0) {
				for (File f : files) {
					if (f.isDirectory()) recursiveDelete(f);
					else delete(f);
				}
			}
		}

		return delete(file);
	}

	public static boolean delete(File file) {
		if (file.exists() && !file.delete()) {
			System.err.println("Unable to delete file '" + file.getAbsolutePath() + "'.");
			return false;
		}
		return true;
	}
	
	public static File getMCDir() {
        String userHomeDir = System.getProperty("user.home", ".");
        String osType = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
        String mcDir = ".minecraft";
        if (osType.contains("win") && System.getenv("APPDATA") != null)
            return new File(System.getenv("APPDATA"), mcDir);
        else if (osType.contains("mac"))
            return new File(new File(new File(userHomeDir, "Library"),"Application Support"),"minecraft");
        return new File(userHomeDir, mcDir);
    }
	
	public static File getCurrentDir() {
		try {
			File file = new File(LauncherLogic.class.getProtectionDomain().getCodeSource().getLocation().toURI());
			if(file.isFile()) file = file.getParentFile();
			return file;
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return null;
		}
	}

}
