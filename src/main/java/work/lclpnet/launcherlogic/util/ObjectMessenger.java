package work.lclpnet.launcherlogic.util;

public class ObjectMessenger extends CallbackHolder {

    public ObjectMessenger() {
        super();
    }

    public ObjectMessenger(ProgressCallbackClient client) {
        super(client);
    }

    public void send(Object o) {
        print(gson.toJson(o));
    }

}
