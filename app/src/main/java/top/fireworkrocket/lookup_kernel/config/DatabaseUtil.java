package top.fireworkrocket.lookup_kernel.config;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据库工具类，用于管理通用数据库。
 */
public class DatabaseUtil extends SQLiteOpenHelper {

   private static final String DB_NAME = "generic_DB";
   private static final int DB_VERSION = 1;
   private static SQLiteDatabase db;

   /**
    * 构造函数，初始化数据库。
    *
    * @param context 上下文对象
    */
   public DatabaseUtil(Context context) {
      super(context, DB_NAME, null, DB_VERSION);
      db = this.getWritableDatabase();
   }

   /**
    * 创建数据库表。
    *
    * @param db 数据库对象
    */
   @Override
   public void onCreate(SQLiteDatabase db) {
      // 初始创建时不创建任何表
   }

   /**
    * 升级数据库。
    *
    * @param db 数据库对象
    * @param oldVersion 旧的版本
    * @param newVersion 新的版本
    */
   @Override
   public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      // 升级时不做任何操作
   }

   /**
    * 创建表。
    *
    * @param tableName 表名
    * @param columns 列定义
    * @return DatabaseUtil 实例
    */
   public DatabaseUtil createTable(String tableName, String columns) {
      String createTableSQL = "CREATE TABLE IF NOT EXISTS " + tableName + " (" + columns + ")";
      db.execSQL(createTableSQL);
      return this;
   }

   /**
    * 插入数据。
    *
    * @param tableName 表名
    * @param values 要插入的数据
    * @return DatabaseUtil 实例
    */
   public DatabaseUtil insertItem(String tableName, ContentValues values) {
      db.insert(tableName, null, values);
      return this;
   }

   /**
    * 更新数据。
    *
    * @param tableName 表名
    * @param values 新的数据
    * @param whereClause 更新条件
    * @param whereArgs 更新条件参数
    * @return DatabaseUtil 实例
    */
   public DatabaseUtil updateItem(String tableName, ContentValues values, String whereClause, String[] whereArgs) {
      db.update(tableName, values, whereClause, whereArgs);
      return this;
   }

   /**
    * 删除数据。
    *
    * @param tableName 表名
    * @param whereClause 删除条件
    * @param whereArgs 删除条件参数
    * @return DatabaseUtil 实例
    */
   public DatabaseUtil deleteItem(String tableName, String whereClause, String[] whereArgs) {
      db.delete(tableName, whereClause, whereArgs);
      return this;
   }

   /**
    * 查询数据。
    *
    * @param tableName 表名
    * @param columns 要查询的列
    * @param selection 查询条件
    * @param selectionArgs 查询条件参数
    * @param groupBy 分组条件
    * @param having 分组条件
    * @param orderBy 排序条件
    * @return 查询结果
    */
   public List<String> queryItems(String tableName, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
      List<String> result = new ArrayList<>();
      Cursor cursor = db.query(tableName, columns, selection, selectionArgs, groupBy, having, orderBy);

      if (cursor.moveToFirst()) {
         do {
            result.add(cursor.getString(cursor.getColumnIndexOrThrow(columns[0])));
         } while (cursor.moveToNext());
      }
      cursor.close();
      return result;
   }

   /**
    * 删除表。
    *
    * @param tableName 表名
    * @return DatabaseUtil 实例
    */
   public DatabaseUtil deleteTable(String tableName) {
      db.execSQL("DROP TABLE IF EXISTS " + tableName);
      return this;
   }

   /**
    * 替换数据。
    *
    * @param oldItem 旧的数据
    * @param newItem 新的数据
    * @return DatabaseUtil 实例
    */
   public DatabaseUtil replaceItem(String oldItem, String newItem) {
      ContentValues values = new ContentValues();
      values.put("item", newItem);
      db.update("tableName", values, "item = ?", new String[]{oldItem});
      return this;
   }
}