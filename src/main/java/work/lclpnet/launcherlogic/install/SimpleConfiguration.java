package work.lclpnet.launcherlogic.install;

import java.util.HashMap;
import java.util.Map;

public class SimpleConfiguration extends InstallationConfiguration {

    private final Map<String, Object> variables = new HashMap<>();

    public Map<String, Object> getVariables() {
        return variables;
    }

    public Object getVariable(String key) {
        return key == null || !variables.containsKey(key) ? null : variables.get(key);
    }

    public void setVariable(String key, Object value) {
        if (key == null) return;
        if (value == null && variables.containsKey(key)) variables.remove(key);
        else variables.put(key, value);
    }

}
