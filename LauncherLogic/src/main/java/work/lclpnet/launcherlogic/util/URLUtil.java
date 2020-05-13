package work.lclpnet.launcherlogic.util;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;

public class URLUtil {

	public static Map<String, String> splitQuery(URL url) throws UnsupportedEncodingException {
		return splitQuery(url.getQuery());
	}

	public static Map<String, String> splitQuery(String query) throws UnsupportedEncodingException {
		Map<String, String> query_pairs = new LinkedHashMap<String, String>();
		String[] pairs = query.split("&");
		for (String pair : pairs) {
			int idx = pair.indexOf("=");
			query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
		}
		return query_pairs;
	}

	public static String joinQuery(Map<String, String> params) throws UnsupportedEncodingException {
		StringBuilder builder = new StringBuilder();
		for (var entry : params.entrySet()) {
			if(builder.length() > 0) builder.append("&");
			builder
			.append(URLEncoder.encode(entry.getKey(), "UTF-8"))
			.append("=")
			.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
		}
		return builder.toString();
	}
	
}
