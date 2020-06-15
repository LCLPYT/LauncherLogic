package work.lclpnet.launcherlogic.ls5;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.CookieManager;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Base64;
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

import work.lclpnet.launcherlogic.cmd.CommandCheckUpdate;
import work.lclpnet.launcherlogic.cmd.CommandInstall;
import work.lclpnet.launcherlogic.install.Installation;
import work.lclpnet.launcherlogic.install.ProgressableConfigureableInstallation;
import work.lclpnet.launcherlogic.install.SimpleConfiguration;
import work.lclpnet.launcherlogic.install.UpdateStatus;
import work.lclpnet.launcherlogic.util.ChecksumUtil;
import work.lclpnet.launcherlogic.util.FileUtils;
import work.lclpnet.launcherlogic.util.ImageUtil;
import work.lclpnet.launcherlogic.util.NetworkUtil;
import work.lclpnet.launcherlogic.util.ObjectMessager;
import work.lclpnet.launcherlogic.util.Progress;
import work.lclpnet.launcherlogic.util.ProgressCallbackClient;
import work.lclpnet.launcherlogic.util.URLUtil;
import work.lclpnet.launcherlogic.util.ZIPUtil;

public class LS5Installation extends ProgressableConfigureableInstallation {

	private static final String optionsURL = "https://lclpnet.work/lclplauncher/installations/ls5/options",
			iconURL = "https://lclpnet.work/lclplauncher/installations/ls5/profile-icon",
			resourcesURL = "https://lclpnet.work/lclplauncher/installations/ls5/gamedir-resources",
			installationURL = "https://lclpnet.work/lclplauncher/installations/ls5/info";

	public LS5Installation() {
		super("ls5", optionsURL, 13, CommandInstall::getPcHost, CommandInstall::getPcPort);
	}

	protected Installation installation = null;

	@Override
	public void installInto(File baseDir) throws Exception {
		if(super.commandDelegate == null) throw new IllegalAccessError("No command delegate was set. Set it with InstallationObject#setCommandDelegate(CommandInstall)");

		loadConfig(LS5Configuration.class);
		SimpleConfiguration config = (SimpleConfiguration) super.config;

		try (InputStream in = new URL(installationURL).openStream()) {
			installation = Installation.fromInputStream(in);
		}

		if(CommandInstall.doProgressCallback) setupProgress("launcherLogicInstaller");
		else super.progress = new Progress();

		String forgeInstaller = (String) config.getVariable("forgeInstaller");

		File tmp = new File(baseDir, "_tmp"),
				dest = new File(tmp, "forge_installer.jar"),
				resourcesFile = new File(tmp, "gamdir_resources.zip");

		progress.nextStep("Preparing installation");
		if(tmp.exists()) FileUtils.recursiveDelete(tmp);
		tmp.mkdirs();
		progress.update(1D);

		progress.nextStep("Downloading Forge installer");
		downloadForgeInstaller(forgeInstaller, dest);
		progress.update(1D);

		progress.nextStep("Installing Forge");
		System.out.println("Installing forge...");
		installForge(dest);
		progress.update(1D);

		progress.nextStep("Injecting game profile");
		System.out.println("Injecting game profile...");
		injectProfile(baseDir);
		progress.update(1D);

		progress.nextStep("Deleting required directories");
		System.out.println("Deleting required folders...");
		deleteRelevantFolders(baseDir);
		System.out.println("Deletion complete.");
		progress.update(1D);

		progress.nextStep("Downloading resources");
		System.out.println("Downloading game directory resources...");
		downloadResources(resourcesFile);
		progress.update(1D);

		progress.nextStep("Extracting resources");
		System.out.println("Extracting game directory resources...");
		extractResources(resourcesFile, baseDir);
		progress.update(1D);

		progress.nextStep("Downloading optifine");
		try {
			System.out.println("Downloading optifine... (optional)");
			downloadOptifine(baseDir);
		} catch (Exception e) {
			if(CommandInstall.debugMode) throw e;
			else System.err.println("Optifine could not be installed. Installation will continue.");
		}
		progress.update(1D);

		//progress.nextStep("Installing Mods"); this is not necessary, since the mods will have their own steps. When there are more progress bars, re-enable this.
		System.out.println("Installing mods...");
		installMods(baseDir, tmp);
		//progress.update(1D);

		progress.nextStep("Downloading FFMPEG...");
		System.out.println("Downloading FFMPEG...");
		downloadFFMPEG(baseDir, tmp);
		progress.update(1D);

		progress.nextStep("Extracting FFMPEG...");
		System.out.println("Extracting FFMPEG...");
		extractFFMPEG(baseDir, tmp);
		progress.update(1D);

		progress.nextStep("Cleaning up");
		System.out.println("Deleting temporary files...");
		FileUtils.recursiveDelete(tmp);
		if(tmp.exists()) System.out.println("WARNING: The tmp folder could not be deleted entirely.");
		progress.update(1D);

		progress.nextStep("Finishing installation");
		System.out.println("Finishing installation...");
		createInstallationFile(baseDir);
		System.out.println("Finished.");
		progress.update(1D);

		progress.end();
	}

	private void extractFFMPEG(File baseDir, File tmp) throws IOException {
		LS5Configuration modConfig = (LS5Configuration) super.config;
		File from = new File(tmp, "ffmpeg.zip");
		String checksum = ChecksumUtil.getSha256(from);
		String sha256 = (String) modConfig.getVariable("ffmpegSha256");
		if(!sha256.equals(checksum)) throw new SecurityException("Checksum mismatching for ffmpeg.");
		ZIPUtil.extract(from, new File(baseDir, "ffmpeg"), progress);
		
		File locator = new File(new File(baseDir, "ffmpeg"), ".locator");
		String[] parts = ((String) modConfig.getVariable("ffmpeg")).split("/");
		String content = parts[parts.length - 1].split("\\.(?=[^\\.]+$)")[0];
		String base64 = Base64.getEncoder().encodeToString(content.getBytes(StandardCharsets.UTF_8));
		try (OutputStream out = new FileOutputStream(locator)) {
			out.write(base64.getBytes(StandardCharsets.UTF_8));
		}
	}

	private void downloadFFMPEG(File baseDir, File tmp) throws MalformedURLException, IOException {
		LS5Configuration modConfig = (LS5Configuration) super.config;
		String url = (String) modConfig.getVariable("ffmpeg");

		File dest = new File(tmp, "ffmpeg.zip");
		NetworkUtil.transferFromUrlToFile(new URL(url), dest, progress);
	}

	private void createInstallationFile(File baseDir) throws FileNotFoundException, IOException {
		File dest = new File(baseDir, ".installation");
		String json = installation.toString();
		String base64 = Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
		try (OutputStream out = new FileOutputStream(dest)) {
			out.write(base64.getBytes(StandardCharsets.UTF_8));
		}
	}

	private void setupProgress(String mode) throws IOException {
		LS5Configuration modConfig = (LS5Configuration) super.config;
		Modifications modifications = modConfig.getModifications();

		try {
			super.connectProgress();
		} catch (Exception e) {
			if(CommandInstall.debugMode) throw e;
			else {
				this.progress = new Progress();
				System.out.println("WARNING: Could not connect to the progress callback server. Installation will continue without progress callback.");
			}
		}

		if(this.progress.getProgressCallbackClient() != null) 
			this.progress.getProgressCallbackClient().setClientName(mode);

		this.progress.steps += modifications.getMods().size();
		this.progress.initialPrint();
	}

	private void deleteRelevantFolders(File baseDir) {
		FileUtils.recursiveDelete(new File(baseDir, "mods"));
	}

	private void installMods(File baseDir, File tmp) throws Exception {
		LS5Configuration modConfig = (LS5Configuration) super.config;
		File modsDir = new File(baseDir, "mods");
		modsDir.mkdirs();

		File tmpModsDir = new File(tmp, "mods");
		tmpModsDir.mkdirs();

		Modifications modifications = modConfig.getModifications();
		for(Modification mod : modifications.getMods()) {
			progress.nextStep("Downloading '" + mod.getName() + "'");

			File tmpDest = new File(mod.getSha256() != null ? tmpModsDir : modsDir, mod.getName());

			try {
				System.out.println("Downloading '" + mod.getName() + "' from '" + mod.getUrl() + "'...");
				NetworkUtil.transferFromUrlToFile(new URL(mod.getUrl()), tmpDest, progress);
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
			} catch (Exception e) {
				if(CommandInstall.debugMode || !mod.isOptional()) throw e;
				else System.err.println("Optional modification '" + mod.getName() + "' could not be installed. Installation will continue.");
			}

			progress.update(1D);
		}

		if(modifications.getOther() == null) return;

		progress.nextStep("Downloading other modifications");
		System.out.println("Downloading other modifications...");
		File otherDest = new File(tmp, "ls5_client_mods.zip");

		NetworkUtil.transferFromUrlToFile(new URL(modifications.getOther()), otherDest, progress);
		System.out.println("Other mods have been downloaded.");

		String otherMD5 = modifications.getOtherMD5();
		if(otherMD5 != null) {
			System.out.println("Validating other modifications with MD5 " + otherMD5 + " ...");
			String md5 = ChecksumUtil.getMD5(otherDest);

			if(!otherMD5.equals(md5)) throw new SecurityException("Checksum mismatching for other mods");

			System.out.println("Other mods are valid.");

			System.out.println("Extracting other mods...");
			ZIPUtil.extract(otherDest, modsDir, d -> {});
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

		NetworkUtil.transferFromUrlToFile(new URL(downloadUrl), dest, progress);

		System.out.println(optifine + " has been downloaded into '" + dest.getAbsolutePath() + "'.");
	}

	private void extractResources(File resources, File baseDir) throws IOException {
		ZIPUtil.extract(resources, baseDir, progress);
	}

	private void downloadResources(File resources) throws Exception {
		NetworkUtil.transferFromUrlToFile(new URL(resourcesURL), resources, progress);
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
		String classpath = super.commandDelegate.llForgeInstallerJar.getAbsolutePath() + ";" + installerJar.getAbsolutePath();

		ProcessBuilder builder = new ProcessBuilder(
				super.commandDelegate.javaExe.getAbsolutePath(), 
				"-Xms1G",
				"-Xmx2G",
				"-cp", 
				classpath, 
				"work.lclpnet.forgeinstaller.ForgeInstaller", 
				CommandInstall.getPcHost() != null ? CommandInstall.getPcHost() : "none", 
						String.valueOf(CommandInstall.getPcPort()));
		builder.inheritIO();
		Process p = builder.start();
		int exit = p.waitFor();
		System.out.println("Process finished with exit code " + exit + ".");
	}

	private void downloadForgeInstaller(String installerUrl, File dest) throws IOException{
		System.out.println("Downloading forge installer...");

		URL url = new URL(installerUrl);
		NetworkUtil.transferFromUrlToFile(url, dest, progress);
	}

	@Override
	public void checkForUpdate(File baseDir) throws Exception {
		try (InputStream in = new URL(installationURL).openStream()) {
			installation = Installation.fromInputStream(in);
		}

		ObjectMessager messager;
		try {
			messager = CommandCheckUpdate.doProgressCallback 
					? new ObjectMessager(new ProgressCallbackClient(CommandCheckUpdate.pcHost, CommandCheckUpdate.pcPort)) 
							: new ObjectMessager();
		} catch (IOException e) {
			if(CommandInstall.debugMode) throw e;
			else {
				messager = new ObjectMessager();
				System.out.println("WARNING: Could not connect to the progress callback server. Update checking will continue without progress callback.");
			}
		}

		if(messager.getProgressCallbackClient() != null) {
			System.out.println("Successfully connected to progress callback server.");
			messager.getProgressCallbackClient().setClientName("launcherLogicUpdater");
		}

		File dest = new File(baseDir, ".installation");
		if(!dest.exists()) {
			printAndSend(messager, UpdateStatus.INSTALLATION_NOT_EXIST);
			return;
		}

		String base64;
		try (InputStream in = new FileInputStream(dest);
				ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			in.transferTo(out);
			base64 = new String(out.toByteArray(), StandardCharsets.UTF_8);
		}

		String decoded = new String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8);
		Installation localInstall = new Gson().fromJson(decoded, Installation.class);

		if(localInstall.getVersionNumber() < installation.getVersionNumber()) printAndSend(messager, UpdateStatus.INSTALLATION_OUTDATED);
		else if(localInstall.getVersionNumber() == installation.getVersionNumber()) printAndSend(messager, UpdateStatus.INSTALLATION_UP_TO_DATE);
		else printAndSend(messager, UpdateStatus.INSTALLATION_FUTURE);

		System.out.println("Finished.");
		messager.end();
	}

	private static void printAndSend(ObjectMessager messager, UpdateStatus o) {
		messager.send(o);
		System.out.println("[Result]" + o.status);
	}

}
