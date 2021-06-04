package work.lclpnet.launcherlogic.install;

import work.lclpnet.launcherlogic.ls5.LS5Installation;

import java.util.ArrayList;
import java.util.List;

public class InstallationObjects {

    private static final List<InstallationObject> register = new ArrayList<>();

    static {
        // installations register themselves
        new LS5Installation();
    }

    static void register(InstallationObject installation) {
        if (installation != null && !register.contains(installation)) register.add(installation);
    }

    public static InstallationObject getInstallation(String id) {
        for (InstallationObject install : register) {
            if (id.equals(install.getId())) {
                return install;
            }
        }
        return null;
    }

    public static String notDuplicate(String id) {
        for (InstallationObject install : register) {
            System.out.println(install);
            if (id.equals(install.getId())) {
                throw new IllegalArgumentException("Identifier is duplicate.");
            }
        }
        return id;
    }

}
