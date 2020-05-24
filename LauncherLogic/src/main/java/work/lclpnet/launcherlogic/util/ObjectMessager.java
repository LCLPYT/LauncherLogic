package work.lclpnet.launcherlogic.util;

import com.google.gson.JsonObject;

public class ObjectMessager extends CallbackHolder{

	public ObjectMessager() {
		super();
	}
	
	public ObjectMessager(ProgressCallbackClient client) {
		super(client);
	}
	
	public void send(Object o) {
		print(gson.toJson(o));
	}
	
	public void send(JsonObject o) {
		print(o.toString());
	}
	
}
