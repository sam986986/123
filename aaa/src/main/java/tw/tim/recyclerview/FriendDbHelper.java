package tw.tim.recyclerview;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class FriendDbHelper extends SQLiteOpenHelper {
    public String sCreateTableCommand;    // 資料庫名稱
    private static final String DB_FILE = "friends.db";
    // 資料庫版本，資料結構改變的時候要更改這個數字，通常是加一
    public static final int VERSION = 1;    // 資料表名稱
    private static SQLiteDatabase database;

    //----------------news--------------------------------------------------------------------------------------------------------------------------------------
    private static final String DB_TABLE = "news_info";    // 資料庫物件，固定的欄位變數
    private static final String crTBsql= "CREATE     TABLE   " + DB_TABLE + "   ( "
            + "id    INTEGER   PRIMARY KEY," + "title TEXT ," + "content TEXT,"
            + "url TEXT," + "createdate TEXT);";


    public FriendDbHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        //傳入的參數說明
        //  context: 用來開啟或建立資料庫的應用程式物件，如 Activity 物件
        //  name: 資料庫檔案名稱，若傳入 null 表示將資料庫暫存在記憶體
        //  factory: 用來建立指標物件的類別，若傳入 null 表示使用預設值
        //  version: 即將要建立的資料庫版本 (版本編號從 1 開始)
        //          若資料庫檔案不存在，就會呼叫 onCreate() 方法
        //          若即將建立的資料庫版本比現存的資料庫版本新，就會呼叫 onUpgrade() 方法
        //          若即將建立的資料庫版本比現存的資料庫版本舊，就會呼叫 onDowngrade() 方法
        //  errHandler: 當資料庫毀損時的處理程式，若傳入 null 表示使用預設的處理程式
    }

    // 需要資料庫的元件呼叫這個方法，這個方法在一般的應用都不需要修改
    public static SQLiteDatabase getDatabase(Context context) {
        if (database == null || !database.isOpen()) {
            database = new FriendDbHelper(context, DB_FILE, null, VERSION)
                    .getWritableDatabase();
        }
        return database;
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(crTBsql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE);
        onCreate(db);
    }

    //-----------------範例-------------------------------------------------------------------------------------------------------------------------------------------------------------------
    public int RecCount() {
        SQLiteDatabase db = getWritableDatabase();
        String sql = "SELECT * FROM " + DB_TABLE;
        Cursor recSet = db.rawQuery(sql, null);
        int a = recSet.getCount();
        recSet.close();
        db.close();
        return a;
    }

    public ArrayList<String> getRecSet() {
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT * FROM " + DB_TABLE;
        Cursor recSet = db.rawQuery(sql, null);
        ArrayList<String> recAry = new ArrayList<String>();
        //----------------------------------------------------
        int columnCount = recSet.getColumnCount();
        int aa = recSet.getCount();
        while (recSet.moveToNext()) {
            String fldSet = "";
            for (int i = 0; i < columnCount; i++) {
                fldSet += recSet.getString(i) + "#";//分割記號
            }
            recAry.add(fldSet);
            //----------------------------------------
        }
        recSet.close();
        db.close();
        return recAry;//傳回SQLITE的結果---陣列
    }

    public int clearRec() {
        SQLiteDatabase db = getWritableDatabase();
        String sql = "SELECT * FROM " + DB_TABLE;
        Cursor recSet = db.rawQuery(sql, null);
        if (recSet.getCount() != 0) {
            int rowsAffected = db.delete(DB_TABLE, "1", null); //
            // From the documentation of SQLiteDatabase delete method:
            // To remove all rows and get a count pass "1" as the whereClause.
            recSet.close();
            db.close();
            return rowsAffected;
        } else {
            recSet.close();
            db.close();
            return -1;
        }
    }

    public int deleteRec() {
        SQLiteDatabase db = getWritableDatabase();
        String sql = "SELECT * FROM " + DB_TABLE;
        Cursor recSet = db.rawQuery(sql, null);
        if (recSet.getCount() != 0) {
            int rowsAffected = db.delete(DB_TABLE, "1", null);
            recSet.close();
            db.close();
            return rowsAffected;
        } else {
            recSet.close();
            db.close();
            return -1;
        }
    }

    public int updateRec(String b_id, String b_name, String b_grp, String b_address) {
        SQLiteDatabase db = getWritableDatabase();
        String sql = "SELECT * FROM " + DB_TABLE;
        Cursor recSet = db.rawQuery(sql, null);

        if (recSet.getCount() != 0) {
            ContentValues rec = new ContentValues();
            rec.put("name", b_name);
            rec.put("grp", b_grp);
            rec.put("address", b_address);
            String whereClause = "id = '" + b_id + "'";

            int rowsAffected = db.update(DB_TABLE, rec, whereClause, null);

            recSet.close();
            db.close();
            return rowsAffected;

        } else {
            recSet.close();
            db.close();
            return -1;
        }

    }

    public long insertRec_news(String b_title, String b_content, String b_url) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues rec = new ContentValues();
        rec.put("title", b_title);
        rec.put("content", b_content);
        rec.put("url", b_url);
        long rowID = db.insert(DB_TABLE, null, rec);
        db.close();
        return rowID;
    }
}