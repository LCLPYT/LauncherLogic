package work.lclpnet.launcherlogic.util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

public class ImageUtil {

	/**
	 * Prepends <code>data:image/png;base64,</code> to the base 64 encoded image
	 */
	public static String toStringWithPrefix(BufferedImage img) throws IOException {
		String b64 = toString(img);
		return b64 == null ? null : "data:image/png;base64," + b64;
	}
	
	public static String toString(BufferedImage img) throws IOException {
		byte[] imageBytes;
		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			ImageIO.write(img, "png", out);
			imageBytes = out.toByteArray();
		}
		
		return Base64.getEncoder().encodeToString(imageBytes);
	}
	
}
