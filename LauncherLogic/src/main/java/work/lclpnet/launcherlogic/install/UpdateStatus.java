package work.lclpnet.launcherlogic.install;

public class UpdateStatus {

	public static final UpdateStatus INSTALLATION_NOT_EXIST = new UpdateStatus("Installation does not exist."),
			INSTALLATION_UP_TO_DATE = new UpdateStatus("Installation is up to date."),
			INSTALLATION_OUTDATED = new UpdateStatus("Installation is outdated."),
			INSTALLATION_FUTURE = new UpdateStatus("Installation is ahead of index.");
	
	public String status;
	
	public UpdateStatus(String status) {
		this.status = status;
	}
	
}
