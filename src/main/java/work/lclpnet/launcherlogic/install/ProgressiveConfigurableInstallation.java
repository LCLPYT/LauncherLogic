package work.lclpnet.launcherlogic.install;

import work.lclpnet.launcherlogic.util.Progress;
import work.lclpnet.launcherlogic.util.ProgressCallbackClient;

import java.io.IOException;
import java.util.function.Supplier;

public abstract class ProgressiveConfigurableInstallation extends ConfigureableInstallation{

	protected Progress progress;
	private final Supplier<String> host;
	private final Supplier<Integer> port;
	private final int steps;
	
	public ProgressiveConfigurableInstallation(String id, String configUrl, int steps, Supplier<String> host, Supplier<Integer> port) {
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
