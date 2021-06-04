package work.lclpnet.launcherlogic.util;

import work.lclpnet.launcherlogic.cmd.CommandInstall;

import java.util.function.Consumer;

public class Progress extends CallbackHolder implements Consumer<Double> {

    public int steps = 0, step = 0;
    public String status = "Initializing";
    public double stepProgress = 0D;

    public Progress() {
        super();
    }

    public Progress(ProgressCallbackClient client) {
        super(client);
    }

    public void printProgress() {
        if (!CommandInstall.doProgressCallback || progressCallbackClient == null) return;

        print(getProgressString());
    }

    private String getProgressString() {
        return gson.toJson(this);
    }

    public void update(double progress) {
        if (progress < 0D) progress = 0D;
        else if (progress > 1D) progress = 1D;
        stepProgress = progress;
        printProgress();
    }

    public void nextStep(String title) {
        if (!CommandInstall.doProgressCallback) return;

        step++;
        if (step > steps) throw new IllegalStateException("Step count is greater than steps.");
        stepProgress = 0D;
        status = title;
        printProgress();
    }

    public void initialPrint() {
        step = 0;
        stepProgress = 0D;
        printProgress();
    }

    @Override
    public void accept(Double d) {
        if (d != null) update(d);
    }

}
