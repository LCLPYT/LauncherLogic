package work.lclpnet.launcherlogic.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

import javax.imageio.ImageIO;

public class ImageUtil {

	public static BufferedImage toImageWithPrefix(String base64WithPrefix) throws IOException {
		String[] split = base64WithPrefix.split(",");
		if(split.length != 2) throw new IllegalArgumentException("String must have a prefix like: data:image/png;base64,<base64 encoded png content>");
		return toImage(split[1]);
	}
	
	public static BufferedImage toImage(String base64) throws IOException {
		byte[] imageBytes = Base64.getDecoder().decode(base64);
		
		try (ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes)) {
			return ImageIO.read(bis);
		}
	}
	
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
