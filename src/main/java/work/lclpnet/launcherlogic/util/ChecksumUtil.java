package work.lclpnet.launcherlogic.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ChecksumUtil {

	public static String getSha256(File f) throws IOException {
		try {
			return getChecksum(f, "SHA-256");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String getMD5(File f) throws IOException {
		try {
			return getChecksum(f, "MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String getChecksum(File f, String algorith) throws IOException, NoSuchAlgorithmException {
		byte[] buffer = new byte[8192];
		int count;
		MessageDigest digest = MessageDigest.getInstance(algorith);
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f));
		while ((count = bis.read(buffer)) > 0) {
			digest.update(buffer, 0, count);
		}
		bis.close();

		byte[] hash = digest.digest();
		return bytesToHex(hash);
	}

	private static String bytesToHex(byte[] hash) {
		StringBuffer hexString = new StringBuffer();
		for (int i = 0; i < hash.length; i++) {
			String hex = Integer.toHexString(0xff & hash[i]);
			if(hex.length() == 1) hexString.append('0');
			hexString.append(hex);
		}
		return hexString.toString();
	}

}
