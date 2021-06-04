package work.lclpnet.launcherlogic.ls5;

import com.google.gson.Gson;

public class Modification {

    private String name, url, sha256;
    private boolean optional;

    public String getName() {
        return name;
    }

    public String getSha256() {
        return sha256;
    }

    public String getUrl() {
        return url;
    }

    public boolean isOptional() {
        return optional;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
