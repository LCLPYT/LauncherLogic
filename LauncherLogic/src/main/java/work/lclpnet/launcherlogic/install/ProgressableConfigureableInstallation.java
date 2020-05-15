package work.lclpnet.launcherlogic.install;

import java.io.IOException;
import java.util.function.Supplier;

import work.lclpnet.launcherlogic.util.Progress;
import work.lclpnet.launcherlogic.util.ProgressCallbackClient;

public abstract class ProgressableConfigureableInstallation extends ConfigureableInstallation{

	protected Progress progress;
	private Supplier<String> host;
	private Supplier<Integer> port;
	private int steps;
	
	public ProgressableConfigureableInstallation(String id, String configUrl, int steps, Supplier<String> host, Supplier<Integer> port) {
		super(id, configUrl);
		this.host = host;
		this.port = port;
		this.steps = steps;
	}
	
	public void connectProgress() throws IOException {
		ProgressCallbackClient client = new ProgressCallbackClient(host.get(), port.get());
		this.progress = new Progress(client);
		this.progress.steps = steps;
	}
	
	public Progress getProgress() {
		return progress;
	}
	
}
