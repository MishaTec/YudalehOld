package il.ac.huji.yudaleh;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Date;

/**
 * Helper class for managing the database.
 */
public class DBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "yudaleh_notes";
    private static final String LIST_TABLE_NAME = "i_owe"; //todo add owe_me table
    private static final String KEY_ROWID = "_id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_DUE = "due";
    private static final String KEY_DESCRIPTION = "desc";
    private static final String KEY_OWNER = "owner";
    private static final String LIST_TABLE_CREATE =
            "create table " + LIST_TABLE_NAME + " ( " +
                    KEY_ROWID + " integer primary key autoincrement, " +
                    KEY_TITLE+" text, " +
                    KEY_DUE+" long, "+
                    KEY_DESCRIPTION+" text," +
                    KEY_OWNER +" text );";
    public static final int TITLE_COLUMN_INDEX = 1;
    public static final int DUE_COLUMN_INDEX = 2;
    public static final int DESCRIPTION_COLUMN_INDEX = 3;
    public static final int OWNER_COLUMN_INDEX = 4;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(LIST_TABLE_CREATE);
    }

    public void onUpgrade(
            SQLiteDatabase db, int oldVer, int newVer) {
        //todo
    }

    /**
     * Helper method. Returns an updated cursor to the db.
     *  TODO use sparsely
     * @return the new cursor
     */
    public Cursor getCursor() {
        return getWritableDatabase().query(LIST_TABLE_NAME, null,
                null, null, null, null, null);
    }

    /**
     * Inserts a new item to the db
     *
     * @param title item's content
     * @param dueDate item's due date
     * @return the rowId of the created item
     */
    public long insert(String title, Date dueDate, String desc, String owner) {
        ContentValues newItem = new ContentValues();
        newItem.put(KEY_TITLE, title);
        if (dueDate != null) {
            newItem.put(KEY_DUE, dueDate.getTime());
        } else {
            newItem.putNull(KEY_DUE);
        }
        newItem.put(KEY_DESCRIPTION, desc);
        newItem.put(KEY_OWNER, owner);
        return getWritableDatabase().insert(LIST_TABLE_NAME, null, newItem);
    }

    /**
     * Updates an item with the given id
     *
     * @param title item's content
     * @param dueDate item's due date
     */
    public void update(long rowId, String title, Date dueDate, String desc, String owner) {
        ContentValues newItem = new ContentValues();
        newItem.put(KEY_TITLE, title);
        if (dueDate != null) {
            newItem.put(KEY_DUE, dueDate.getTime());
        } else {
            newItem.putNull(KEY_DUE);
        }
        newItem.put(KEY_DESCRIPTION, desc);
        newItem.put(KEY_OWNER, owner);
        getWritableDatabase().update(LIST_TABLE_NAME, newItem, KEY_ROWID + "=" + rowId, null);
    }

    /**
     * Deletes an item with the given id
     *
     * @param rowId the index of the row
     */
    public void delete(long rowId) {
        getWritableDatabase().delete(LIST_TABLE_NAME, KEY_ROWID + "=" + rowId, null);
    }

    /**
     * TODO don't use
     * @param rowId the index of the row
     * @return a cursor pointing to the requested item
     */
    public Cursor getItem(long rowId) {
        Cursor mCursor = getWritableDatabase().query(true, LIST_TABLE_NAME, null, KEY_ROWID + "=" + rowId,
                null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
}
