package work.lclpnet.launcherlogic.util;

import java.io.File;
import java.util.Locale;

public class FileUtils {

	public static void recursiveDelete(File file) {
		if (file == null) return;

		if (file.isDirectory()) { 
			File[] files = file.listFiles();
			if(files != null && files.length > 0) {
				for (File f : files) {
					if (f.isDirectory()) recursiveDelete(f);
					else delete(f);
				}
			}
		}

		delete(file);
	}

	public static void delete(File file) {
		if (!file.delete()) System.err.println("Unable to delete file '" + file.getAbsolutePath() + "'.");
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

}
