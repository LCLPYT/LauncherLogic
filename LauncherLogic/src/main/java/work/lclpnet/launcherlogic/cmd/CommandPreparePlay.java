package work.lclpnet.launcherlogic.cmd;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.Callable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import work.lclpnet.launcherlogic.ls5.LS5Installation;
import work.lclpnet.launcherlogic.util.FileUtils;

@Command(
		name = "preparePlay",
		mixinStandardHelpOptions = true,
		description = "Prepares the minecraft launcher profile for a specified installation. This is done by modifying the timestamp of a profile."
		)
public class CommandPreparePlay implements Callable<Integer>{

	@Parameters(index = "0", paramLabel = "profileId", description = "The id of the profile to set the timestamp on.")
	String profileId;
	
	@Override
	public Integer call() throws Exception {
		System.out.println("Preparing profile with id '" + profileId + "'...");
		
		LS5Installation.backupProfilesFile();
		
		File baseDir = FileUtils.getMCDir();
		File launcherProfilesFile = new File(baseDir, "launcher_profiles.json");

		JsonObject json;
		try (InputStream in = new FileInputStream(launcherProfilesFile);
				Reader reader = new InputStreamReader(in)) {
			json = new Gson().fromJson(reader, JsonObject.class);
			JsonObject profiles = json.get("profiles").getAsJsonObject();
			if(!profiles.has(profileId)) {
				System.err.println("The launchers's profile file does not contain a profile with the id '" + profileId + "'");
				return 1;
			}
			
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
			format.setTimeZone(TimeZone.getTimeZone("UTC"));
			Date date = new Date();
			String formatted = format.format(date);
			
			JsonObject profile = profiles.get(profileId).getAsJsonObject();
			profile.remove("lastUsed");
			profile.addProperty("lastUsed", formatted);
		}

		try (OutputStream out = new FileOutputStream(launcherProfilesFile)) {
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			String jsonString = gson.toJson(json);
			byte[] bytes = jsonString.getBytes(StandardCharsets.UTF_8);
			out.write(bytes, 0, bytes.length);
		}
		System.out.println("Profile with id '" + profileId + "' has been updated.");
		return 0;
	}
	
}
