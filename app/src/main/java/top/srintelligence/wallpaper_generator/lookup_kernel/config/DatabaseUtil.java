package top.srintelligence.wallpaper_generator.lookup_kernel.config;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据库工具类，用于管理 API 配置数据库。
 */
public class DatabaseUtil extends SQLiteOpenHelper {

   private static final String DB_NAME = "api_config_DB";
   private static final int DB_VERSION = 1;
   private static final String TABLE_API_LIST = "api_list";
   private static final String TABLE_FLEXIBLE_API_LIST = "flexible_api_list";
   private static final String COLUMN_ID = "id";
   private static final String COLUMN_API_URL = "api_url";
   private static String tableName = TABLE_API_LIST; // 默认操作表名
   private static SQLiteDatabase db;

   public DatabaseUtil(Context context) {
      super(context, DB_NAME, null, DB_VERSION);
      db = this.getWritableDatabase();
   }

   @Override
   public void onCreate(SQLiteDatabase db) {
      String createTableSQL = "CREATE TABLE IF NOT EXISTS " + TABLE_API_LIST + " (" +
              COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
              COLUMN_API_URL + " TEXT NOT NULL)";
      db.execSQL(createTableSQL);

      String createFlexibleApiTableSQL = "CREATE TABLE IF NOT EXISTS " + TABLE_FLEXIBLE_API_LIST + " (" +
              COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
              COLUMN_API_URL + " TEXT NOT NULL)";
      db.execSQL(createFlexibleApiTableSQL);
   }

   @Override
   public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      db.execSQL("DROP TABLE IF EXISTS " + TABLE_API_LIST);
      db.execSQL("DROP TABLE IF EXISTS " + TABLE_FLEXIBLE_API_LIST);
      onCreate(db);
   }

   public static String[] getApiList() {
      List<String> apiList = new ArrayList<>();
      Cursor cursor = db.query(tableName, new String[]{COLUMN_API_URL}, null, null, null, null, null);

      if (cursor.moveToFirst()) {
         do {
            apiList.add(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_API_URL)));
         } while (cursor.moveToNext());
      }
      cursor.close();
      return apiList.toArray(new String[0]);
   }

   public static String[] getFlexibleApiList() {
      List<String> apiList = new ArrayList<>();
      Cursor cursor = db.query(TABLE_FLEXIBLE_API_LIST, new String[]{COLUMN_API_URL}, null, null, null, null, null);

      if (cursor.moveToFirst()) {
         do {
            apiList.add(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_API_URL)));
         } while (cursor.moveToNext());
      }
      cursor.close();
      return apiList.toArray(new String[0]);
   }

   public static void addItem(String Item) {
      ContentValues values = new ContentValues();
      values.put(COLUMN_API_URL, Item);
      db.insert(tableName, null, values);
   }

   public static void addFlexibleApiItem(String apiUrl) {
      ContentValues values = new ContentValues();
      values.put(COLUMN_API_URL, apiUrl);
      db.insert(TABLE_FLEXIBLE_API_LIST, null, values);
   }

   public static void updateItem(int id, String newApiUrl) {
      ContentValues values = new ContentValues();
      values.put(COLUMN_API_URL, newApiUrl);
      db.update(tableName, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
   }

   public static void updateFlexibleApiItem(String apiUrl, boolean supportsFlexible) {
      if (supportsFlexible) {
         ContentValues values = new ContentValues();
         values.put(COLUMN_API_URL, apiUrl);
         db.insert(TABLE_FLEXIBLE_API_LIST, null, values);
      } else {
         db.delete(TABLE_FLEXIBLE_API_LIST, COLUMN_API_URL + " = ?", new String[]{apiUrl});
      }
   }

   public static void replaceItem(String oldItem, String newItem) {
      ContentValues values = new ContentValues();
      values.put(COLUMN_API_URL, newItem);
      db.update(tableName, values, COLUMN_API_URL + " = ?", new String[]{oldItem});
   }

   public static void deleteItem(int id) {
      db.delete(tableName, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
   }

   public static void deleteItem(String apiUrl) {
      db.delete(tableName, COLUMN_API_URL + " = ?", new String[]{apiUrl});
   }

   public static List<String> searchItem(String keyword) {
      List<String> result = new ArrayList<>();
      Cursor cursor = db.query(tableName, new String[]{COLUMN_API_URL}, COLUMN_API_URL + " LIKE ?", new String[]{"%" + keyword + "%"}, null, null, null);

      if (cursor.moveToFirst()) {
         do {
            result.add(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_API_URL)));
         } while (cursor.moveToNext());
      }
      cursor.close();
      return result;
   }

   public static void setTableName(String newTableName) {
      tableName = newTableName;
   }

   public static void deleteTable(String tableName) {
      db.execSQL("DROP TABLE IF EXISTS " + tableName);
   }
}