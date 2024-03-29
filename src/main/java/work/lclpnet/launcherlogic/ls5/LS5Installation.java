package work.lclpnet.launcherlogic.ls5;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import work.lclpnet.launcherlogic.cmd.CommandCheckUpdate;
import work.lclpnet.launcherlogic.cmd.CommandInstall;
import work.lclpnet.launcherlogic.install.Installation;
import work.lclpnet.launcherlogic.install.ProgressiveConfigurableInstallation;
import work.lclpnet.launcherlogic.install.SimpleConfiguration;
import work.lclpnet.launcherlogic.install.UpdateStatus;
import work.lclpnet.launcherlogic.util.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.CookieManager;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;

public class LS5Installation extends ProgressiveConfigurableInstallation {

    private static final String optionsURL = "https://lclpnet.work/lclplauncher/installations/ls5/options",
            iconURL = "https://lclpnet.work/lclplauncher/installations/ls5/profile-icon",
            resourcesURL = "https://lclpnet.work/lclplauncher/installations/ls5/gamedir-resources",
            installationURL = "https://lclpnet.work/lclplauncher/installations/ls5/info";

    public LS5Installation() {
        super("ls5", optionsURL, 14, CommandInstall::getPcHost, CommandInstall::getPcPort);
    }

    protected Installation installation = null;

    @Override
    public void installInto(File baseDir) throws Exception {
        if (super.commandDelegate == null)
            throw new IllegalAccessError("No command delegate was set. Set it with InstallationObject#setCommandDelegate(CommandInstall)");

        loadConfig(LS5Configuration.class);
        SimpleConfiguration config = (SimpleConfiguration) super.config;

        try (InputStream in = new URL(installationURL).openStream()) {
            installation = Installation.fromInputStream(in);
        }

        if (CommandInstall.doProgressCallback) setupProgress();
        else super.progress = new Progress();

        String forgeInstaller = (String) config.getVariable("forgeInstaller");

        File tmp = new File(baseDir, "_tmp"),
                dest = new File(tmp, "forge_installer.jar"),
                resourcesFile = new File(tmp, "game_dir_resources.zip");

        progress.nextStep("Preparing installation");
        if (tmp.exists()) FileUtils.recursiveDelete(tmp);
        if (!tmp.exists() && !tmp.mkdirs()) throw new IllegalStateException("Could not create temp directory.");
        pushProfilesFile(tmp);
        progress.update(1D);

        progress.nextStep("Downloading Forge installer");
        downloadForgeInstaller(forgeInstaller, dest);
        progress.update(1D);

        progress.nextStep("Installing Forge");
        System.out.println("Installing forge...");
        installForge(dest);
        popProfilesFile(tmp);
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

        progress.nextStep("Downloading OptiFine");
        try {
            System.out.println("Downloading OptiFine... (optional)");
            downloadOptiFine(baseDir);
        } catch (Exception e) {
            if (CommandInstall.debugMode) throw e;
            else System.err.println("OptiFine could not be installed. Installation will continue.");
        }
        progress.update(1D);

        //progress.nextStep("Installing Mods"); this is not necessary, since the mods will have their own steps. When there are more progress bars, re-enable this.
        System.out.println("Installing mods...");
        installMods(baseDir, tmp);
        //progress.update(1D);

        progress.nextStep("Downloading FFMPEG...");
        System.out.println("Downloading FFMPEG...");
        boolean ffmpegFailed = false;
        try {
            downloadFFMPEG(tmp);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Skipping FFMPEG steps, because an error occurred.");
            ffmpegFailed = true;
        }
        progress.update(1D);

        progress.nextStep("Extracting FFMPEG...");
        if (!ffmpegFailed) {
            System.out.println("Extracting FFMPEG...");
            extractFFMPEG(baseDir, tmp);
        }
        progress.update(1D);

        progress.nextStep("Downloading youtube-dl...");
        System.out.println("Downloading youtube-dl...");
        downloadYtDl(baseDir);
        progress.update(1D);

        progress.nextStep("Cleaning up");
        System.out.println("Deleting temporary files...");
        FileUtils.recursiveDelete(tmp);
        if (tmp.exists()) System.out.println("WARNING: The tmp folder could not be deleted entirely.");
        progress.update(1D);

        progress.nextStep("Finishing installation");
        System.out.println("Finishing installation...");
        createInstallationFile(baseDir);
        System.out.println("Finished.");
        progress.update(1D);

        if (System.getProperty("os.name").equals("Linux"))
            System.out.println("Please make sure you are running Minecraft with java 8.");

        progress.end();
    }

    private void popProfilesFile(File tmpDir) throws IOException {
        File baseDir = FileUtils.getMCDir();
        File launcherProfilesFile = new File(baseDir, "launcher_profiles.json");

        File target = new File(tmpDir, "launcher_profiles.json.tmp");
        if (!target.exists()) return;

        try (FileInputStream in = new FileInputStream(target);
             FileOutputStream out = new FileOutputStream(launcherProfilesFile)) {
            in.transferTo(out);
        }
    }

    private void pushProfilesFile(File tmpDir) throws IOException {
        File baseDir = FileUtils.getMCDir();
        File launcherProfilesFile = new File(baseDir, "launcher_profiles.json");
        if (!launcherProfilesFile.exists()) return;

        File target = new File(tmpDir, "launcher_profiles.json.tmp");

        try (FileInputStream in = new FileInputStream(launcherProfilesFile);
             FileOutputStream out = new FileOutputStream(target)) {
            in.transferTo(out);
        }
    }

    private void downloadYtDl(File baseDir) throws IOException {
        File dest = new File(baseDir, "bin" + File.separatorChar + OSHooks.getYTDLExeName());
        NetworkUtil.transferFromUrlToFile(new URL(OSHooks.getYTDLDownloadLink()), dest, progress);
    }

    private void extractFFMPEG(File baseDir, File tmp) throws IOException {
        LS5Configuration modConfig = (LS5Configuration) super.config;
        File from = new File(tmp, OSHooks.getFFMPEGName());
        String checksum = ChecksumUtil.getSha256(from);
        String sha256 = OSHooks.getFFMPEGSha256(modConfig);
        if (sha256 != null && !sha256.equals(checksum)) throw new SecurityException("Checksum mismatching for ffmpeg.");
        File ffmpegDir = new File(baseDir, "bin" + File.separatorChar + "ffmpeg");
        File target = new File(ffmpegDir, "ffmpeg");

        if (ffmpegDir.exists() && !FileUtils.recursiveDelete(ffmpegDir))
            throw new IllegalStateException("Could not delete " + ffmpegDir.getAbsolutePath());
        OSHooks.extractFFMPEG(from, ffmpegDir, progress);

        File[] files = ffmpegDir.listFiles();
        if (files == null || files.length != 1)
            throw new IllegalStateException("More than one file in " + ffmpegDir.getAbsolutePath());
        File dir = files[0];
        if (!dir.renameTo(target))
            throw new IllegalStateException(dir.getAbsolutePath() + " could not be renamed to " + target.getAbsolutePath());
    }

    private void downloadFFMPEG(File tmp) throws IOException {
        LS5Configuration modConfig = (LS5Configuration) super.config;
        String url = OSHooks.getFFMPEGUrl(modConfig);
        System.out.println(url);

        File dest = new File(tmp, OSHooks.getFFMPEGName());
        NetworkUtil.transferFromUrlToFile(new URL(url), dest, progress);
    }

    private void createInstallationFile(File baseDir) throws IOException {
        File dest = new File(baseDir, ".installation");
        String json = installation.toString();
        String base64 = Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
        try (OutputStream out = new FileOutputStream(dest)) {
            out.write(base64.getBytes(StandardCharsets.UTF_8));
        }
    }

    private void setupProgress() throws IOException {
        LS5Configuration modConfig = (LS5Configuration) super.config;
        Modifications modifications = modConfig.getModifications();

        try {
            super.connectProgress();
        } catch (Exception e) {
            if (CommandInstall.debugMode) throw e;
            else {
                this.progress = new Progress();
                System.out.println("WARNING: Could not connect to the progress callback server. Installation will continue without progress callback.");
            }
        }

        if (this.progress.getProgressCallbackClient() != null)
            this.progress.getProgressCallbackClient().setClientName("launcherLogicInstaller");

        List<Modification> mods = modifications.getMods();
        if (mods != null) this.progress.steps += mods.size();

        this.progress.initialPrint();
    }

    private void deleteRelevantFolders(File baseDir) {
        FileUtils.recursiveDelete(new File(baseDir, "mods"));
    }

    private void installMods(File baseDir, File tmp) throws Exception {
        LS5Configuration modConfig = (LS5Configuration) super.config;
        File modsDir = new File(baseDir, "mods");
        if (!modsDir.exists() && !modsDir.mkdirs()) throw new IllegalStateException("Could not create mods directory.");

        File tmpModsDir = new File(tmp, "mods");
        if (!tmpModsDir.exists() && !tmpModsDir.mkdirs())
            throw new IllegalStateException("Could not create temp mods directory.");

        Modifications modifications = modConfig.getModifications();
        downloadMods(modsDir, tmpModsDir, modifications);

        if (modifications.getOther() == null) return;

        progress.nextStep("Downloading other modifications");
        System.out.println("Downloading other modifications...");
        File otherDest = new File(tmp, "ls5_client_mods.zip");

        NetworkUtil.transferFromUrlToFile(new URL(modifications.getOther()), otherDest, progress);
        System.out.println("Other mods have been downloaded.");

        String otherMD5 = modifications.getOtherMD5();
        if (otherMD5 != null) {
            System.out.println("Validating other modifications with MD5 " + otherMD5 + " ...");
            String md5 = ChecksumUtil.getMD5(otherDest);

            if (!otherMD5.equals(md5)) throw new SecurityException("Checksum mismatching for other mods");

            System.out.println("Other mods are valid.");

            System.out.println("Extracting other mods...");
            ZIPUtil.extract(otherDest, modsDir, d -> {
            });
            System.out.println("Successfully extracted other mods into the mods directory.");
        }
    }

    private void downloadMods(File modsDir, File tmpModsDir, Modifications modifications) throws IOException {
        if (modifications.getMods() == null) return;

        for (Modification mod : modifications.getMods()) {
            progress.nextStep("Downloading '" + mod.getName() + "'");

            File tmpDest = new File(mod.getSha256() != null ? tmpModsDir : modsDir, mod.getName());

            try {
                System.out.println("Downloading '" + mod.getName() + "' from '" + mod.getUrl() + "'...");
                NetworkUtil.transferFromUrlToFile(new URL(mod.getUrl()), tmpDest, progress);
                System.out.println("'" + mod.getName() + "' downloaded.");

                if (mod.getSha256() != null) {
                    System.out.println("Validating modification file with SHA256 " + mod.getSha256() + " ...");
                    String sha256 = ChecksumUtil.getSha256(tmpDest);

                    if (!mod.getSha256().equals(sha256))
                        throw new SecurityException("Checksum mismatching for mod '" + mod.getName() + "'.");

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
                if (CommandInstall.debugMode || !mod.isOptional()) throw e;
                else
                    System.err.println("Optional modification '" + mod.getName() + "' could not be installed. Installation will continue.");
            }

            progress.update(1D);
        }
    }

    private void downloadOptiFine(File baseDir) throws Exception {
        String optiFine = (String) ((SimpleConfiguration) config).getVariable("optifine");
        System.out.println("Looking for " + optiFine + "...");

        CookieManager manager = new CookieManager();
        HttpClient client = HttpClient.newBuilder()
                .cookieHandler(manager)
                .build();

        /* First request */

        HttpRequest downloadsRequest = HttpRequest.newBuilder()
                .uri(new URI("https://optiFine.net/downloads"))
                .GET()
                .timeout(Duration.ofSeconds(10))
                .build();
        System.out.println("GET " + downloadsRequest.uri() + "...");
        var downloadsResponse = client.send(downloadsRequest, BodyHandlers.ofString());
        System.out.println("Server responded with response code " + downloadsResponse.statusCode() + ".");

        /* Second request */

        HttpRequest adloadxRequest = HttpRequest.newBuilder()
                .uri(new URI("https://optiFine.net/adloadx"))
                .header("f", optiFine)
                .GET()
                .build();
        System.out.println("GET " + adloadxRequest.uri() + "...");
        var adLoadXResponse = client.send(adloadxRequest, BodyHandlers.ofString());
        System.out.println("Server responded with response code " + adLoadXResponse.statusCode() + ".");

        Document d = Jsoup.parse(adLoadXResponse.body());
        Elements elements = d.getElementsByTag("a");
        Element downloadAnchor = null;
        for (Element e : elements) {
            if (!e.hasAttr("onclick") || !e.attr("onclick").equals("onDownload()")) continue;
            downloadAnchor = e;
            break;
        }

        if (downloadAnchor == null)
            throw new IllegalStateException("Could not find download anchor inside OptiFine's html.");

        String href = downloadAnchor.attr("href");

        var params = URLUtil.splitQuery(href.split("\\?")[1]);
        params.put("f", optiFine);

        String query = URLUtil.joinQuery(params);
        String downloadUrl = "https://optiFine.net/downloadx?" + query;
        System.out.println("Final OptiFine download url has been built: " + downloadUrl);

        /* - */

        File dest = new File(baseDir + "/mods", optiFine);
        if (!dest.getParentFile().exists() && !dest.getParentFile().mkdirs())
            System.err.println("Could not create parent directory of OptiFine mod.");

        System.out.println("Downloading " + optiFine + "...");

        NetworkUtil.transferFromUrlToFile(new URL(downloadUrl), dest, progress);

        System.out.println(optiFine + " has been downloaded into '" + dest.getAbsolutePath() + "'.");
    }

    private void extractResources(File resources, File baseDir) throws IOException {
        ZIPUtil.extract(resources, baseDir, progress);
    }

    private void downloadResources(File resources) throws Exception {
        NetworkUtil.transferFromUrlToFile(new URL(resourcesURL), resources, progress);
    }

    private void injectProfile(File dir) throws Exception {
        backupProfilesFile();

        File baseDir = FileUtils.getMCDir();
        File launcherProfilesFile = new File(baseDir, "launcher_profiles.json");

        JsonObject json;
        try (InputStream in = new FileInputStream(launcherProfilesFile);
             Reader reader = new InputStreamReader(in)) {
            json = new Gson().fromJson(reader, JsonObject.class);
            JsonObject profiles = json.get("profiles").getAsJsonObject();
            if (profiles.has("ls5")) profiles.remove("ls5");
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
        if (super.commandDelegate.profileJavaExecPath != null)
            profile.addProperty("javaDir", super.commandDelegate.profileJavaExecPath);

        return profile;
    }

    public static void backupProfilesFile() throws IOException {
        File baseDir = FileUtils.getMCDir();
        File launcherProfilesFile = new File(baseDir, "launcher_profiles.json");
        if (!launcherProfilesFile.exists()) return;

        File target = new File(baseDir, "launcher_profiles.json.backup");

        try (FileInputStream in = new FileInputStream(launcherProfilesFile);
             FileOutputStream out = new FileOutputStream(target)) {
            in.transferTo(out);
        }
    }

    private void installForge(File installerJar) throws Exception {
        String classpath = OSHooks.getForgeInstallerClasspath(super.commandDelegate.llForgeInstallerJar, installerJar);

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
        if (exit != 0) throw new IllegalStateException("Subprocess exited with exit code " + exit);
        System.out.println("Process finished with exit code " + exit + ".");
    }

    private void downloadForgeInstaller(String installerUrl, File dest) throws IOException {
        System.out.println("Downloading forge installer...");

        URL url = new URL(installerUrl);
        NetworkUtil.transferFromUrlToFile(url, dest, progress);
    }

    @Override
    public void checkForUpdate(File baseDir) throws Exception {
        try (InputStream in = new URL(installationURL).openStream()) {
            installation = Installation.fromInputStream(in);
        }

        ObjectMessenger messenger;
        try {
            messenger = CommandCheckUpdate.doProgressCallback
                    ? new ObjectMessenger(new ProgressCallbackClient(CommandCheckUpdate.pcHost, CommandCheckUpdate.pcPort))
                    : new ObjectMessenger();
        } catch (IOException e) {
            if (CommandInstall.debugMode) throw e;
            else {
                messenger = new ObjectMessenger();
                System.out.println("WARNING: Could not connect to the progress callback server. Update checking will continue without progress callback.");
            }
        }

        if (messenger.getProgressCallbackClient() != null) {
            System.out.println("Successfully connected to progress callback server.");
            messenger.getProgressCallbackClient().setClientName("launcherLogicUpdater");
        }

        File dest = new File(baseDir, ".installation");
        if (!dest.exists()) {
            printAndSend(messenger, UpdateStatus.INSTALLATION_NOT_EXIST);
            return;
        }

        String base64;
        try (InputStream in = new FileInputStream(dest);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            in.transferTo(out);
            base64 = out.toString(StandardCharsets.UTF_8);
        }

        String decoded = new String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8);
        Installation localInstall = new Gson().fromJson(decoded, Installation.class);

        if (localInstall.getVersionNumber() < installation.getVersionNumber())
            printAndSend(messenger, UpdateStatus.INSTALLATION_OUTDATED);
        else if (localInstall.getVersionNumber() == installation.getVersionNumber())
            printAndSend(messenger, UpdateStatus.INSTALLATION_UP_TO_DATE);
        else printAndSend(messenger, UpdateStatus.INSTALLATION_FUTURE);

        System.out.println("Finished.");
        messenger.end();
    }

    private static void printAndSend(ObjectMessenger messenger, UpdateStatus o) {
        messenger.send(o);
        System.out.println("[Result]" + o.status);
    }

}
