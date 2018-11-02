package com.apiherd.tenantdb;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

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
    public void putJsonObject(String key, String value) {
        DatabaseEntry keyEntry = new DatabaseEntry();
        DatabaseEntry dataEntry = new DatabaseEntry();

        keyEntry.setData(key.getBytes(Charset.forName("UTF-8")));
        dataEntry.setData(value.getBytes(Charset.forName("UTF-8")));

        Transaction txn = dbenv.beginTransaction(null, null);
        db.put(null, keyEntry, dataEntry);
        txn.commit();
    }

    @Override
    public String getJsonObject(String key) {
        DatabaseEntry keyEntry = new DatabaseEntry();
        DatabaseEntry dataEntry = new DatabaseEntry();

        keyEntry.setData(key.getBytes(Charset.forName("UTF-8")));

        Transaction txn = dbenv.beginTransaction(null, null);
        db.get(txn, keyEntry, dataEntry, LockMode.DEFAULT);
        txn.commit();

        return new String(dataEntry.getData(), Charset.forName("UTF-8"));
    }

    public String[] searchJsons(String key) {
        List<String> objs = new ArrayList<>();
        Cursor cursor = this.db.openCursor(null, null);
        BDBSearchConstraint constraint = new BDBSearchConstraint(key);
        try {
            DatabaseEntry foundKey = new DatabaseEntry();
            DatabaseEntry foundData = new DatabaseEntry();
            OperationStatus retVal = cursor.getNext(foundKey, foundData, LockMode.DEFAULT);
            if (retVal == OperationStatus.NOTFOUND) {
                cursor.close();
                return null;
            }
            while (retVal == OperationStatus.SUCCESS) {
                if (constraint.inBounds(foundKey.getData())) {
                    String Key = new String(foundKey.getData(), Charset.forName("UTF-8"));
                    String value = new String(foundData.getData(), Charset.forName("UTF-8"));
                    int end = value.lastIndexOf("}");
                    objs.add(value.substring(0, end) + ", \"ResourceKey\":\"" + Key +"\"}");
                }
                retVal = cursor.getNext(foundKey, foundData, LockMode.DEFAULT);
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
        if (0 == objs.size())
            return null;
        return objs.toArray(new String[1]);
    }

    @Override
    public void deleteObject(String key) {
        DatabaseEntry keyEntry = new DatabaseEntry();

        keyEntry.setData(key.getBytes(Charset.forName("UTF-8")));

        Transaction txn = dbenv.beginTransaction(null, null);
        db.delete(txn, keyEntry);
        subDB.delete(txn, keyEntry);
        txn.commit();
    }

    public String[] getDescendObjects(String key) {
        List<String> objs = new ArrayList<>();
        DatabaseEntry theData = new DatabaseEntry();
        DatabaseEntry theKey = new DatabaseEntry(key.getBytes(Charset.forName("UTF-8")));

        Transaction txn = dbenv.beginTransaction(null, null);
        Cursor cursor = subDB.openCursor(txn, null);
        OperationStatus retVal = cursor.getSearchKey(theKey, theData, LockMode.DEFAULT);

        if (retVal == OperationStatus.NOTFOUND) {
            cursor.close();
            txn.commit();
            return null;
        }

        while (retVal == OperationStatus.SUCCESS) {
            objs.add(new String(theData.getData(), Charset.forName("UTF-8")));
            retVal = cursor.getNextDup(theKey, theData, LockMode.DEFAULT);
        }

        cursor.close();
        txn.commit();

        return objs.toArray(new String[1]);
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
