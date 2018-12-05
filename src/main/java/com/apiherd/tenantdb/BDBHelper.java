package com.apiherd.tenantdb;

import java.io.File;
import java.nio.charset.Charset;
import java.util.*;

import com.apiherd.api.DateHelper;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.SecondaryConfig;
import com.sleepycat.je.SecondaryDatabase;
import com.sleepycat.je.SecondaryKeyCreator;
import com.sleepycat.je.Transaction;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BDBHelper extends DBHelper {

    private Database db;
    private Database subDB;
    private Environment dbenv;
    protected static final Logger log = LoggerFactory.getLogger(BDBHelper.class);

    public BDBHelper(String dirPath, String dbName) {
        super(dirPath, dbName);
    }

    @Override
    public void setupDB() {
        EnvironmentConfig envConfig = new EnvironmentConfig();
        envConfig.setAllowCreate(true);
        envConfig.setTransactional(true);
        File file = new File(this.getDbPath());
        dbenv = new Environment(file,envConfig);

        DatabaseConfig dbConfig = new DatabaseConfig();
        dbConfig.setAllowCreate(true);
        dbConfig.setTransactional(true);
        db = dbenv.openDatabase(null, this.getDbName(), dbConfig);

        SecondaryConfig scdConfig = new SecondaryConfig();
        scdConfig.setTransactional(true);
        scdConfig.setAllowCreate(true);
        scdConfig.setSortedDuplicates(true);
        scdConfig.setKeyCreator(new TreeKeyCreator());
        subDB = dbenv.openSecondaryDatabase(
                null, "sub" + this.getDbName(), db, scdConfig);
    }

    @Override
    public void closeDB() {
        if (null != dbenv)
            try {
                subDB.close();
                db.close();
                dbenv.close();
            } catch (Exception exp) {
                ;
            }
    }

    @Override
    public void putJsonObject(String ownerId, TreedJson tree, JSONObject value) {
        super.putJsonObject(ownerId, tree, value);
        String key = tree.getKeyFullPath(ownerId, value);
        DatabaseEntry keyEntry = new DatabaseEntry();
        DatabaseEntry dataEntry = new DatabaseEntry();

        keyEntry.setData(key.getBytes(Charset.forName("UTF-8")));
        dataEntry.setData(value.toString().getBytes(Charset.forName("UTF-8")));

        Transaction txn = dbenv.beginTransaction(null, null);
        db.put(null, keyEntry, dataEntry);
        txn.commit();
    }

    @Override
    public void putJsonArray(String ownerId, TreedJson tree, JSONArray array) {
        for (Iterator<Object> it = array.iterator(); it.hasNext(); ) {
            JSONObject obj = (JSONObject) it.next();
            obj.put("LastUpdate", DateHelper.getISO8601Time(null));
            this.putJsonObject(ownerId, tree, obj);
        }
    }

    @Override
    public void deleteObject(String ownerId, TreedJson tree, JSONObject value) {
        String key = tree.getKeyFullPath(ownerId, value);
        DatabaseEntry keyEntry = new DatabaseEntry();

        keyEntry.setData(key.getBytes(Charset.forName("UTF-8")));
        Transaction txn = dbenv.beginTransaction(null, null);
        db.delete(txn, keyEntry);
        subDB.delete(txn, keyEntry);
        txn.commit();
    }

    @Override
    public JSONObject getJsonObject(String ownerId, TreedJson tree, JSONObject value) {
        String key = tree.getKeyFullPath(ownerId, value);
        DatabaseEntry keyEntry = new DatabaseEntry();
        DatabaseEntry dataEntry = new DatabaseEntry();

        keyEntry.setData(key.getBytes(Charset.forName("UTF-8")));
        Transaction txn = dbenv.beginTransaction(null, null);
        db.get(txn, keyEntry, dataEntry, LockMode.DEFAULT);
        txn.commit();

        return new JSONObject(new String(dataEntry.getData(), Charset.forName("UTF-8")));
    }

    public List<String> getDescendObjects(String key) {
        List<String> objs = new ArrayList<>();
        DatabaseEntry theData = new DatabaseEntry();
        DatabaseEntry theKey = new DatabaseEntry(key.getBytes(Charset.forName("UTF-8")));

        Transaction txn = dbenv.beginTransaction(null, null);
        Cursor cursor = subDB.openCursor(txn, null);
        OperationStatus retVal = cursor.getSearchKey(theKey, theData, LockMode.DEFAULT);

        if (retVal == OperationStatus.NOTFOUND) {
            cursor.close();
            txn.commit();
            return objs;
        }

        while (retVal == OperationStatus.SUCCESS) {
            objs.add(new String(theData.getData(), Charset.forName("UTF-8")));
            retVal = cursor.getNextDup(theKey, theData, LockMode.DEFAULT);
        }
        cursor.close();
        txn.commit();

        return objs;
    }

    @Override
    public JSONObject getJsonArray(String ownerId, TreedJson tree, JSONObject value) {
        String sort = "asc";
        BDBSearchConstraint constraint;
        String key = tree.getKeyFullPath(ownerId, value);
        String tokenId = value.optString("NextToken");
        int limit = value.optInt("Limit", 10);

        if (! "".equals(tokenId)) {
            int index = tokenId.indexOf("/");
            sort = tokenId.substring(0, index);
            tokenId = tokenId.substring(index + 1);
        } else
            tokenId = key;
        if (sort.equals("asc"))
            constraint = new BDBSearchConstraint(key, limit);
        else
            constraint = new BDBSearchDesc(key, limit);
        LinkedHashMap<String, String> map = this.searchJsons(key, tokenId, constraint);
        JSONArray array = new JSONArray();
        JSONObject json = new JSONObject("{}");
        json.put("CurrentSize", map.size());
        json.put("NextToken", sort+"/"+constraint.getNextToken());
        for (String str:map.values())
            array.put(new JSONObject(str));
        json.put(tree.getClass().getSimpleName()+"s", array);

        return json;
    }

    public LinkedHashMap<String, String> searchJsons(String key,
                                                     String token,
                                                     BDBSearchConstraint constraint) {
        DatabaseEntry foundKey = null;
        OperationStatus retVal = null;
        DatabaseEntry foundData = new DatabaseEntry();
        LinkedHashMap<String, String> set = new LinkedHashMap<>();
        Cursor cursor = this.db.openCursor(null, null);
        try {
            foundKey = new DatabaseEntry(token.getBytes(Charset.forName("UTF-8")));
            retVal = cursor.getSearchKey(foundKey, foundData, LockMode.DEFAULT);
            if (retVal != OperationStatus.SUCCESS) {
                cursor.close();
                return set;
            }
            retVal = constraint.getNext(cursor, foundKey, foundData, LockMode.DEFAULT);
            while (retVal == OperationStatus.SUCCESS) {
                if (constraint.inBounds(foundKey.getData())) {
                    String Key = new String(foundKey.getData(), Charset.forName("UTF-8"));
                    String value = new String(foundData.getData(), Charset.forName("UTF-8"));
                    set.put(key, value);
                }
                retVal = constraint.getNext(cursor, foundKey, foundData, LockMode.DEFAULT);
            }
            cursor.close();
        } catch (Exception exp) {
            log.error("", exp);
        } finally {
            try {
                cursor.close();
            } catch (Exception exp) {
                log.error("", exp);
            }
        }
        return set;
    }

    private static class TreeKeyCreator implements SecondaryKeyCreator {

        TreeKeyCreator() {
        }

        public boolean createSecondaryKey(SecondaryDatabase secondaryDb,
                                          DatabaseEntry keyEntry,
                                          DatabaseEntry dataEntry,
                                          DatabaseEntry resultEntry) {

            String primKey = new String(keyEntry.getData(), Charset.forName("UTF-8"));
            if (null == primKey || "" == primKey)
                return false;
            String key = KeyHelper.getFatherKey(primKey);

            if (null == key || "".equals(key))
                return false;
            resultEntry.setData(key.getBytes(Charset.forName("UTF-8")));
            return true;
        }
    }
}
