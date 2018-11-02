package com.apiherd.tenantdb;

import java.nio.charset.Charset;

import com.sleepycat.je.dbi.RangeConstraint;

public class BDBSearchConstraint implements RangeConstraint {
    private String searchKey;
    private String[] keyParts;

    public BDBSearchConstraint(String key) {
        this.searchKey = key;
        keyParts = key.split("/");
    }

    @Override
    public boolean inBounds(byte[] bytes) {
        int index = 1;
        String toKey = new String(bytes, Charset.forName("UTF-8"));
        String[] toParts = toKey.split("/");
        if (keyParts.length != toParts.length)
            return false;
        while(index < keyParts.length) {
            if (! keyParts[index].equals(toParts[index]))
                return false;
            index +=2;
        }
        index = 0;
        while(index < keyParts.length) {
            if ("+".equals(keyParts[index])) {
                index +=2;
                continue;
            }
            if (!keyParts[index].contains(toParts[index]))
                return false;
            index +=2;
        }
        return true;
    }
}
