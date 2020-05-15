package work.lclpnet.launcherlogic.util;

import java.util.Objects;
import java.util.function.Consumer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import work.lclpnet.launcherlogic.cmd.CommandInstall;

public class Progress implements Consumer<Double>{

	public int steps = 0, step = 0;
	public String status = "Initializing";
	public double stepProgress = 0D;
	private transient String lastOutput = null;
	private transient ProgressCallbackClient progressCallbackClient;
	private transient Gson gson = new GsonBuilder()
			.registerTypeAdapter(Double.class, new RoundedDoubleTypeAdapter(2))
			.create();
	
	public Progress(ProgressCallbackClient client) {
		this.progressCallbackClient = Objects.requireNonNull(client);
		this.progressCallbackClient.setGson(gson);
	}
	
	public Progress() {
		this.progressCallbackClient = null;
	}

	public void printProgress() {
		if(!CommandInstall.doProgressCallback || progressCallbackClient == null) return;
		
		String progress = getProgressString();
		if(lastOutput != null && lastOutput.equals(progress)) return;
		
		lastOutput = progress;
		progressCallbackClient.send(progress);
	}

	private String getProgressString() {
		return gson.toJson(this);
	}

	public void update(double progress) {
		if(progress < 0D) progress = 0D;
		else if(progress > 1D) progress = 1D;
		stepProgress = progress;
		printProgress();
	}
	
	public void nextStep(String title) {
		step++;
		if(steps > steps) throw new IllegalStateException("Step count is greater than steps.");
		stepProgress = 0D;
		status = title;
		printProgress();
	}
	
	public void initialPrint() {
		step = 0;
		stepProgress = 0D;
		printProgress();
	}
	
	public void end() {
		if(progressCallbackClient == null) return;
		this.progressCallbackClient.stop();
	}

	@Override
	public void accept(Double d) {
		if(d != null) update(d);
	}

}
