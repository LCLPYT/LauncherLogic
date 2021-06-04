package work.lclpnet.launcherlogic.util;

import java.util.function.BiConsumer;

public class CommonHelper {

    public static void parsePC(String raw, BiConsumer<String, Integer> parsed) {
        String[] split = raw.split(":");
        if (split.length != 2)
            System.err.printf("The specified progressCallback '%s' does not match the format <host>:<port>", raw);
        parsed.accept(split[0], Integer.parseInt(split[1]));
    }

}
