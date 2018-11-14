package gateway.util;

import java.util.UUID;

public class IDgenerator {

    private static IDgenerator iDgenerator = null;

    private int c = 0;

    private IDgenerator() {
    }

    public static IDgenerator getInstance() {
        if (iDgenerator == null) {
            iDgenerator = new IDgenerator();
        }
        return iDgenerator;
    }

    public String getSequentialID() {
        c = c + 1;
        return String.valueOf(c);
    }

    public String getUniqueID() {
        return UUID.randomUUID().toString();
    }

}
