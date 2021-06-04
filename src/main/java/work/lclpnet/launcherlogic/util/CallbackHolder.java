package work.lclpnet.launcherlogic.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Objects;

public class CallbackHolder {

    protected transient String lastOutput = null;
    protected transient ProgressCallbackClient progressCallbackClient;
    protected transient Gson gson = new GsonBuilder()
            .registerTypeAdapter(Double.class, new RoundedDoubleTypeAdapter(2))
            .create();

    public CallbackHolder(ProgressCallbackClient client) {
        this.progressCallbackClient = Objects.requireNonNull(client);
        this.progressCallbackClient.setGson(gson);
    }

    public CallbackHolder() {
        this.progressCallbackClient = null;
    }

    public void print(String s) {
        if (progressCallbackClient == null || (lastOutput != null && lastOutput.equals(s))) return;

        lastOutput = s;
        progressCallbackClient.send(s);
    }

    public ProgressCallbackClient getProgressCallbackClient() {
        return progressCallbackClient;
    }

    public void end() {
        if (progressCallbackClient != null) this.progressCallbackClient.stop();
    }

}
