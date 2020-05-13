package work.lclpnet.launcherlogic.install;

import java.io.File;

public abstract class InstallationObject {

	protected String id;
	
	public InstallationObject(String id) {
		this.id = InstallationObjects.notDuplicate(id);
		InstallationObjects.register(this);
	}
	
	public String getId() {
		return id;
	}
	
	public abstract void installInto(File baseDir) throws Exception;
	
}
