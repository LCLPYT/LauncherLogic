package work.lclpnet.launcherlogic.util;

import work.lclpnet.launcherlogic.cmd.CommandInstall;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.function.Consumer;

public class NetworkUtil {

	public static void transferWithProgress(InputStream in, OutputStream out, long completeFileSize, Consumer<Double> progress) throws IOException {
		byte[] data = new byte[1024];
		long downloadedFileSize = 0;
		int read;
		while ((read = in.read(data, 0, 1024)) != -1) {
			downloadedFileSize += read;
			out.write(data, 0, read);
			progress.accept((double) downloadedFileSize / (double) completeFileSize);
		}
	}
	
	public static void transferFromUrlToFile(URL url, File dest, Consumer<Double> progress) throws IOException {
		URLConnection conn = url.openConnection();
		
		try (InputStream in = conn.getInputStream();
				OutputStream out = new FileOutputStream(dest)) {
			if(CommandInstall.doProgressCallback) transferWithProgress(in, out, conn.getContentLengthLong(), progress);
			else in.transferTo(out);
		}
	}

}
