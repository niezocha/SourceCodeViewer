package janicka.zofia.sourcecodeviewer.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.concurrent.Callable;

public class WebsiteDataSource {

    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;

    public WebsiteDataSource(Context context) {
        dbHelper = new MySQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public void clear() {
        database.delete(MySQLiteHelper.TABLE, null, null);
    }

    public void saveSourceCode(String address, String sourcecode) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_ADDRESS, address);
        values.put(MySQLiteHelper.COLUMN_SOURCE_CODE, sourcecode);
        database.insert(MySQLiteHelper.TABLE, null, values);
    }

    public String getSourceCode(String url) {

        Cursor cursor = database.query(MySQLiteHelper.TABLE, null,
                MySQLiteHelper.COLUMN_ADDRESS + " = ?", new String[]{url}, null, null, null, null);

        if (cursor.moveToFirst()) {
            cursor.moveToLast();
            Website website = new Website();
            website.setId(cursor.getInt(cursor.getColumnIndex(MySQLiteHelper.COLUMN_ID)));
            website.setAddress(cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_ADDRESS)));
            website.setSourcecode(cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_SOURCE_CODE)));
            return website.getSourcecode();
        }
        return "";
    }
}

