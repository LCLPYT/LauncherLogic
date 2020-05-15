package work.lclpnet.launcherlogic.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.function.Consumer;

import work.lclpnet.launcherlogic.cmd.CommandInstall;

public class NetworkUtil {

	public static void transferWithProgress(InputStream in, OutputStream out, long completeFileSize, Consumer<Double> progress) throws IOException {
		byte[] data = new byte[1024];
		long downloadedFileSize = 0;
		int read = 0;
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
	
	public static void transferFromFileToFile(File from, File dest, Consumer<Double> progress) throws IOException {
		try (InputStream in = new FileInputStream(from);
				OutputStream out = new FileOutputStream(dest)) {
			if(CommandInstall.doProgressCallback) transferWithProgress(in, out, from.length(), progress);
			else in.transferTo(out);
		}
	}

}
