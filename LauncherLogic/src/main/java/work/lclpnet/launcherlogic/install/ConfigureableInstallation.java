package work.lclpnet.launcherlogic.install;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public abstract class ConfigureableInstallation extends InstallationObject{

	public String configUrl;
	protected InstallationConfiguration config;
	
	public ConfigureableInstallation(String id, String configUrl) {
		super(id);
		this.configUrl = configUrl;
	}
	
	public JsonObject loadConfigJson() throws IOException{
		URL url = new URL(configUrl);
		try (InputStream in = url.openStream();
				InputStreamReader reader = new InputStreamReader(in)) {
			return new Gson().fromJson(reader, JsonObject.class);
		}
	}
	
	public void loadConfig() throws IOException{
		loadConfig(InstallationConfiguration.class);
	}
	
	public void loadConfig(Class<? extends InstallationConfiguration> configClass) throws IOException{
		JsonObject obj = loadConfigJson();
		InstallationConfiguration newConfig = new Gson().fromJson(obj, configClass);
		if(!newConfig.getId().equals(this.getId())) throw new IllegalStateException("Error loading config, configuration id's are mismatching");
		
		this.config = newConfig;
		this.config.setJson(obj);
	}
	
	public InstallationConfiguration getConfig() {
		return config;
	}
	
}
