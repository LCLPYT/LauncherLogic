package work.lclpnet.launcherlogic.install;

import work.lclpnet.launcherlogic.cmd.CommandInstall;

import java.io.File;

public abstract class InstallationObject {

    protected String id;
    protected CommandInstall commandDelegate;

    public InstallationObject(String id) {
        this.id = InstallationObjects.notDuplicate(id);
        InstallationObjects.register(this);
    }

    public void setCommandDelegate(CommandInstall commandDelegate) {
        this.commandDelegate = commandDelegate;
    }

    public CommandInstall getCommandDelegate() {
        return commandDelegate;
    }

    public String getId() {
        return id;
    }

    public abstract void installInto(File baseDir) throws Exception;

    public abstract void checkForUpdate(File baseDir) throws Exception;

}
