package work.lclpnet.launcherlogic.install;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class InstallationConfiguration {

    private transient JsonObject json;
    private String id;

    protected InstallationConfiguration() {
    }

    public String getId() {
        return id;
    }

    public JsonObject getJson() {
        return json;
    }

    public void setJson(JsonObject json) {
        this.json = json;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

}
