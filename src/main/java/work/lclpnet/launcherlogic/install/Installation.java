package work.lclpnet.launcherlogic.install;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import com.google.gson.Gson;

public class Installation {

	private String name, version;
	private int versionNumber;
	
	public String getName() {
		return name;
	}
	
	public String getVersion() {
		return version;
	}
	
	public int getVersionNumber() {
		return versionNumber;
	}
	
	public static Installation fromInputStream(InputStream in) throws IOException {
		try (Reader reader = new InputStreamReader(in)) {
			return new Gson().fromJson(reader, Installation.class);
		}
	}
	
	@Override
	public String toString() {
		return new Gson().toJson(this);
	}
	
}
