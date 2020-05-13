package work.lclpnet.launcherlogic.ls5;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.CookieManager;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.imageio.ImageIO;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import work.lclpnet.launcherlogic.LauncherLogic;
import work.lclpnet.launcherlogic.cmd.CommandInstall;
import work.lclpnet.launcherlogic.install.ConfigureableInstallation;
import work.lclpnet.launcherlogic.install.SimpleConfiguration;
import work.lclpnet.launcherlogic.util.ChecksumUtil;
import work.lclpnet.launcherlogic.util.FileUtils;
import work.lclpnet.launcherlogic.util.ImageUtil;
import work.lclpnet.launcherlogic.util.URLUtil;
import work.lclpnet.launcherlogic.util.ZIPUtil;

public class LS5Installation extends ConfigureableInstallation{

	private static final String optionsURL = "https://lclpnet.work/dl/installation-ls5-options", //TODO maybe create own page to manage...?
			iconURL = "https://lclpnet.work/dl/ls5-profile-icon",
			resourcesURL = "https://lclpnet.work/dl/ls5-gamedir-resources";

	public LS5Installation() {
		super("ls5", optionsURL);
	}

	@Override
	public void installInto(File baseDir) throws Exception {
		loadConfig(LS5Configuration.class);
		SimpleConfiguration config = (SimpleConfiguration) super.config;

		String forgeInstaller = (String) config.getVariable("forgeInstaller");

		File tmp = new File(baseDir, "_tmp"),
				dest = new File(tmp, "forge_installer.jar"),
				resourcesFile = new File(tmp, "gamdir_resources.zip");

		if(LauncherLogic.DEBUG) {
			tmp.mkdirs();
			if(!dest.exists()) downloadForgeInstaller(forgeInstaller, dest);
		} else {
			if(tmp.exists()) FileUtils.recursiveDelete(tmp);
			tmp.mkdirs();
			downloadForgeInstaller(forgeInstaller, dest);
		}

		System.out.println("Installing forge...");
		installForge(dest);

		System.out.println("Injecting game profile...");
		injectProfile(baseDir);

		System.out.println("Downloading game directory resources...");
		downloadResources(resourcesFile);

		System.out.println("Extracting game directory resources...");
		extractResources(resourcesFile, baseDir);

		try {
			System.out.println("Downloading optifine... (optional)");
			downloadOptifine(baseDir);
		} catch (Exception e) {
			if(CommandInstall.debugMode) throw e;
			else System.err.println("Optifine could not be installed. Installation will continue.");
		}

		System.out.println("Installing mods...");
		installMods(baseDir, tmp);

		System.out.println("Deleting temporary files...");
		//FileUtils.recursiveDelete(tmp);
		if(tmp.exists()) System.out.println("WARNING: The tmp folder could not be deleted entirely.");
	}

	private void installMods(File baseDir, File tmp) throws Exception {
		LS5Configuration modConfig = (LS5Configuration) super.config;
		File modsDir = new File(baseDir, "mods");
		modsDir.mkdirs();

		File tmpModsDir = new File(tmp, "mods");
		tmpModsDir.mkdirs();

		Modifications modifications = modConfig.getModifications();
		for(Modification mod : modifications.getMods()) {
			File tmpDest = new File(mod.getSha256() != null ? tmpModsDir : modsDir, mod.getName());
			System.out.println(tmpDest);

			System.out.println("Downloading '" + mod.getName() + "' from '" + mod.getUrl() + "'...");
			try(InputStream in = new URL(mod.getUrl()).openStream();
					OutputStream out = new FileOutputStream(tmpDest)) {
				in.transferTo(out);
			}
			System.out.println("'" + mod.getName() + "' downloaded.");

			if(mod.getSha256() != null) {
				System.out.println("Validating modification file with SHA256 " + mod.getSha256() + " ...");
				String sha256 = ChecksumUtil.getSha256(tmpDest);

				if(!mod.getSha256().equals(sha256)) throw new SecurityException("Checksum mismatching for mod '" + mod.getName() + "'.");

				System.out.println("Modification file '" + mod.getName() + "' is valid.");

				System.out.println("Installing modification '" + mod.getName() + "' ...");
				File dest = new File(modsDir, mod.getName());
				try (InputStream in = new FileInputStream(tmpDest);
						OutputStream out = new FileOutputStream(dest)) {
					in.transferTo(out);
				}
				System.out.println("Modification '" + mod.getName() + "' installed.");
			}
		}

		if(modifications.getOther() == null) return;

		System.out.println("Downloading other modifications...");
		File otherDest = new File(tmp, "ls5_client_mods.zip");

		try(InputStream in = new URL(modifications.getOther()).openStream();
				OutputStream out = new FileOutputStream(otherDest)) {
			in.transferTo(out);
		}
		System.out.println("Other mods have been downloaded.");

		String otherMD5 = modifications.getOtherMD5();
		if(otherMD5 != null) {
			System.out.println("Validating other modifications with MD5 " + otherMD5 + " ...");
			String md5 = ChecksumUtil.getMD5(otherDest);

			if(!otherMD5.equals(md5)) throw new SecurityException("Checksum mismatching for other mods");

			System.out.println("Other mods are valid.");

			System.out.println("Extracting other mods...");
			ZIPUtil.extract(otherDest, modsDir);
			System.out.println("Successfully extracted other mods into the mods directory.");
		}


	}

	private void downloadOptifine(File baseDir) throws Exception {
		String optifine = (String) ((SimpleConfiguration) config).getVariable("optifine");
		System.out.println("Looking for " + optifine + "...");

		CookieManager manager = new CookieManager();
		HttpClient client = HttpClient.newBuilder()
				.cookieHandler(manager)
				.build();

		/* First request */

		HttpRequest downloadsRequest = HttpRequest.newBuilder()
				.uri(new URI("https://optifine.net/downloads"))
				.GET()
				.timeout(Duration.ofSeconds(10))
				.build();
		System.out.println("GET " + downloadsRequest.uri() + "...");
		var downloadsResponse = client.send(downloadsRequest, BodyHandlers.ofString());
		System.out.println("Server responded with response code " + downloadsResponse.statusCode() + ".");

		/* Second request */

		HttpRequest adloadxRequest = HttpRequest.newBuilder()
				.uri(new URI("https://optifine.net/adloadx"))
				.header("f", optifine)
				.GET()
				.build();
		System.out.println("GET " + adloadxRequest.uri() + "...");
		var adloadxReponse = client.send(adloadxRequest, BodyHandlers.ofString());
		System.out.println("Server responded with response code " + adloadxReponse.statusCode() + ".");

		Document d = Jsoup.parse(adloadxReponse.body());
		Elements elements = d.getElementsByTag("a");
		Element downloadAnchor = null;
		for(Element e : elements) {
			if(!e.hasAttr("onclick") || !e.attr("onclick").equals("onDownload()")) continue;
			downloadAnchor = e;
			break;
		}

		if(downloadAnchor == null) throw new IllegalStateException("Could not find download anchor inside optifines html.");

		String href = downloadAnchor.attr("href");

		var params = URLUtil.splitQuery(href.split("\\?")[1]);
		params.put("f", optifine);

		String query = URLUtil.joinQuery(params);
		String downloadUrl = "https://optifine.net/downloadx?" + query;
		System.out.println("Final OptiFine download url has been built: " + downloadUrl);

		/* - */

		File dest = new File(baseDir + "/mods", optifine);
		dest.getParentFile().mkdirs();

		System.out.println("Downloading " + optifine + "...");

		try (InputStream in = new URL(downloadUrl).openStream();
				OutputStream out = new FileOutputStream(dest)) {
			in.transferTo(out);
		}

		System.out.println(optifine + " has been downloaded into '" + dest.getAbsolutePath() + "'.");
	}

	private void extractResources(File resources, File baseDir) throws IOException {
		ZIPUtil.extract(resources, baseDir);
	}

	private void downloadResources(File resources) throws Exception {
		try (InputStream in = new URL(resourcesURL).openStream();
				OutputStream out = new FileOutputStream(resources)) {
			in.transferTo(out);
		}
	}

	private void injectProfile(File dir) throws Exception{
		File baseDir = FileUtils.getMCDir();
		File launcherProfilesFile = new File(baseDir, "launcher_profiles.json");

		JsonObject json;
		try (InputStream in = new FileInputStream(launcherProfilesFile);
				Reader reader = new InputStreamReader(in)) {
			json = new Gson().fromJson(reader, JsonObject.class);
			JsonObject profiles = json.get("profiles").getAsJsonObject();
			if(profiles.has("ls5")) profiles.remove("ls5");
			JsonObject profile = getProfile(dir);
			profiles.add("ls5", profile);
		}

		try (OutputStream out = new FileOutputStream(launcherProfilesFile)) {
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			String jsonString = gson.toJson(json);
			byte[] bytes = jsonString.getBytes(StandardCharsets.UTF_8);
			out.write(bytes, 0, bytes.length);
		}
	}

	private JsonObject getProfile(File dir) throws Exception {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
		format.setTimeZone(TimeZone.getTimeZone("UTC"));
		Date date = new Date();
		String formatted = format.format(date);

		BufferedImage img = ImageIO.read(new URL(iconURL));


		JsonObject profile = new JsonObject();
		profile.addProperty("created", formatted);
		profile.addProperty("gameDir", dir.getAbsolutePath().replaceFirst("\\\\", "/"));
		profile.addProperty("icon", ImageUtil.toStringWithPrefix(img));
		profile.addProperty("javaArgs", "-Xmx2G -XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode -XX:-UseAdaptiveSizePolicy -Xmn128M");
		profile.addProperty("lastUsed", formatted);
		profile.addProperty("lastVersionId", (String) ((SimpleConfiguration) config).getVariable("lastVersionId"));
		profile.addProperty("name", "LCLPServer 5.0");
		profile.addProperty("type", "custom");

		return profile;
	}

	private void installForge(File installerJar) throws Exception {
		ProcessBuilder builder;
		if(LauncherLogic.DEBUG) {
			File dir = new File("C:\\Users\\Lukas\\Documents\\Electron\\lclplauncher\\bin\\launcherlogic");
			builder = new ProcessBuilder(dir.getAbsolutePath() + "\\runtime\\bin\\java.exe", "-cp", "launcherlogic-forge_installer.jar;" + installerJar.getAbsolutePath(), "work.lclpnet.forgeinstaller.ForgeInstaller");
			builder.directory(dir);
		} else {
			builder = new ProcessBuilder("runtime\\bin\\java.exe", "-cp", "launcherlogic-forge_installer.jar;" + installerJar.getAbsolutePath(), "work.lclpnet.forgeinstaller.ForgeInstaller");
		}
		builder.inheritIO();
		Process p = builder.start();
		int exit = p.waitFor();
		System.out.println("Process finished with exit code " + exit + ".");
	}

	private void downloadForgeInstaller(String installerUrl, File dest) throws IOException{
		System.out.println("Downloading forge installer...");
		URL url = new URL(installerUrl);
		try (InputStream in = url.openStream();
				OutputStream out = new FileOutputStream(dest)) {
			in.transferTo(out);
		}
	}

}
