package il.ac.huji.yudaleh;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.Serializable;
import java.util.Date;

/**
 * Helper class for managing the database.
 */
public class DBHelper extends SQLiteOpenHelper {

    public static final String I_OWE_TABLE = "i_owe";
    public static final String OWE_ME_TABLE = "owe_me";
    public static final int TITLE_COLUMN_INDEX = 1;
    public static final int DUE_COLUMN_INDEX = 2;
    public static final int DESCRIPTION_COLUMN_INDEX = 3;
    public static final int OWNER_COLUMN_INDEX = 4;
    public static final int OBJECT_ID_COLUMN_INDEX = 5;
    public static final String KEY_ROWID = "_id";
    public static final String KEY_TITLE = "title";
    public static final String KEY_DUE = "due";
    public static final String KEY_DESCRIPTION = "desc";
    public static final String KEY_OWNER = "owner";
    public static final String KEY_OBJECT_ID = "obj_id";
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "yudaleh_notes";
    private static final String COLUMNS = " ( " +
            KEY_ROWID + " integer primary key autoincrement, " +
            KEY_TITLE + " text, " +
            KEY_DUE + " long, " +
            KEY_DESCRIPTION + " text," +
            KEY_OWNER + " text," +
            KEY_OBJECT_ID + " text );";
    private static final String I_OWE_TABLE_CREATE = "create table " + I_OWE_TABLE + COLUMNS;
    private static final String OWE_ME_TABLE_CREATE = "create table " + OWE_ME_TABLE + COLUMNS;
    private static final String TAG = "il.ac.huji.yudaleh";
    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(I_OWE_TABLE_CREATE);
        db.execSQL(OWE_ME_TABLE_CREATE);
    }

    public void onUpgrade(
            SQLiteDatabase db, int oldVer, int newVer) {
        Log.w(TAG, oldVer + " to " + newVer
                + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + I_OWE_TABLE_CREATE);
        db.execSQL("DROP TABLE IF EXISTS " + OWE_ME_TABLE_CREATE);
        onCreate(db);
    }

    /**
     * Helper method. Returns an updated cursor to the db.
     * TODO use sparsely
     *
     * @param table which table to use
     * @return the new cursor
     */
    public Cursor getCursor(String table) {
        return getWritableDatabase().query(table, null,
                null, null, null, null, null);
    }

    /**
     * Inserts a new item to the db
     *
     * @param table which table to use
     * @param rec   item's record
     * @return the rowId of the created item
     */
    public long insert(String table, Record rec) {
        ContentValues newItem = new ContentValues();
        newItem.put(KEY_TITLE, rec.title);
        if (rec.dueDate != null) {
            newItem.put(KEY_DUE, rec.dueDate.getTime());
        } else {
            newItem.putNull(KEY_DUE);
        }
        newItem.put(KEY_DESCRIPTION, rec.desc);
        newItem.put(KEY_OWNER, rec.owner);
        return getWritableDatabase().insert(table, null, newItem);
    }

    /**
     * Updates an item by the given id
     *
     * @param table which table to use
     * @param rowId the index of the row
     * @param rec   item's record
     */
    public void update(String table, long rowId, Record rec) {
        ContentValues newItem = new ContentValues();
        newItem.put(KEY_TITLE, rec.title);
        if (rec.dueDate != null) {
            newItem.put(KEY_DUE, rec.dueDate.getTime());
        } else {
            newItem.putNull(KEY_DUE);
        }
        newItem.put(KEY_DESCRIPTION, rec.desc);
        newItem.put(KEY_OWNER, rec.owner);
        getWritableDatabase().update(table, newItem, KEY_ROWID + "=" + rowId, null);
    }

    /**
     * Updates an item by the given key
     *
     * @param table which table to use
     * @param key the key column
     * @param oldValue the value to find
     * @param newValue the new value to put
     */
    public void updateStringValue(String table, String key, String oldValue, String newValue) {
        ContentValues newItem = new ContentValues();
        newItem.put(key, newValue);
        System.out.println("****************************************** where: "+key + "=" + oldValue+ ". *****************");
        getWritableDatabase().update(table, newItem, key + "=" + oldValue, null);
    }

    /**
     * Deletes an item with the given id
     *
     * @param table which table to use
     * @param rowId the index of the row
     */
    public void delete(String table, long rowId) {
        getWritableDatabase().delete(table, KEY_ROWID + "=" + rowId, null);
    }

    /**
     * TODO don't use
     *
     * @param table which table to use
     * @param rowId the index of the row
     * @return a cursor pointing to the requested item
     */
    public Cursor getItem(String table, long rowId) {
        Cursor mCursor = getWritableDatabase().query(true, table, null, KEY_ROWID + "=" + rowId,
                null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    /**
     * A single record/row in the DB
     */
    public static class Record implements Serializable {
        private String title;
        private Date dueDate;
        private String desc;
        private String owner;
        private String objId;

        /**
         * Default constructor
         */
        public Record() {
            this.title = null;
            this.dueDate = null;
            this.desc = null;
            this.owner = null;
            this.objId = null;
        }

        /**
         * Parametric constructor
         *
         * @param title
         * @param dueDate
         * @param desc
         * @param owner
         * @param objId
         */
        public Record(String title, Date dueDate, String desc, String owner, String objId) {
            this.title = title;
            this.dueDate = dueDate;
            this.desc = desc;
            this.owner = owner;
            this.objId = objId;
        }

        /**
         * Getters
         */
        public String getTitle() {
            return title;
        }

        /**
         * Setters
         */
        public void setTitle(String title) {
            this.title = title;
        }

        public Date getDueDate() {
            return dueDate;
        }

        public void setDueDate(Date dueDate) {
            this.dueDate = dueDate;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public String getOwner() {
            return owner;
        }

        public void setOwner(String owner) {
            this.owner = owner;
        }

        public String getObjId() {
            return objId;
        }

        public void setObjId(String objId) {
            this.objId = objId;
        }
    }
}
