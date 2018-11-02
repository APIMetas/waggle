package com.apiherd.api;

public class MQTTTopic {
    private static final String MULTI_LEVEL = "#";
    private static final String SIGNLE_LEVEL = "+";
    private static final String PREFIX_WORD = "$";

    public static boolean keyContains(String sub, String key) {
        String[] partsSub = sub.split("/");
        String[] partsKey = key.split("/");

        int lenSub = partsSub.length;
        int lenKey = partsKey.length;

        int endsCount = 0;
        if (sub.endsWith(MULTI_LEVEL)) {
            endsCount = 2;
            lenSub --;
        } else if (sub.endsWith(SIGNLE_LEVEL)) {
            endsCount = 1;
            lenSub --;
        }
        if (lenSub > lenKey)
            return false;
        for (int i = 0; i < lenSub; i++) {
            if (partsSub[i].startsWith(PREFIX_WORD)) {
                if (!partsKey[i].startsWith(partsSub[i].substring(1)))
                    return false;
            } else {
                if (!partsKey[i].equals(partsSub[i]))
                    return false;
            }
        }
        if (endsCount == 0 || (1 == endsCount && lenKey != (lenSub + 1)))
            return false;
        return true;
    }

}
