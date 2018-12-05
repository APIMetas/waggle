package com.apiherd.tenantdb;

import java.nio.charset.Charset;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.dbi.RangeConstraint;

public class BDBSearchConstraint implements RangeConstraint,BDCursored {
    protected int limit;
    protected String token;
    protected int count = 0;
    private String nextToken;
    private String searchKey;
    private String[] keyParts;

    public BDBSearchConstraint(String key) {
        this.limit = 10;
        this.searchKey = key;
        keyParts = key.split("/");
    }

    public BDBSearchConstraint(String key, int limit) {
        this(key);
        this.limit = limit;
    }

    public String getNextToken() {
        return this.nextToken;
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
            if (keyParts[index].startsWith("&"))
                if (!toParts[index].startsWith(keyParts[index].substring(1)))
                    return false;
            else if (!keyParts[index].contains(toParts[index]))
                return false;
            index +=2;
        }
        this.count ++;
        this.nextToken = toKey;
        return true;
    }

    @Override
    public OperationStatus getNext(Cursor cursor, DatabaseEntry key, DatabaseEntry data, LockMode lockMode) {
        if (count == limit)
            return OperationStatus.NOTFOUND;
        return cursor.getNext(key, data, lockMode);
    }
}
