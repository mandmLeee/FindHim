package com.scott.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "findhim";
	private static final int DATABASE_VERSION = 1;
	private static final String TAG = "DBHelper";

	public DBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		// 第三个参数null 表示使用默认游标对象
	}

	// 数据库第一次被创建时onCreate会被调用
	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.i(TAG, "DBHelper onCreate");
		// 创建一个device表
		db.execSQL("CREATE TABLE IF NOT EXISTS device"
				+ "(_id VARCHAR PRIMARY KEY, device_name VARCHAR,device_photo VARCHAR,device_time LONG,device_intro VARCHAR)");
	}

	// 如果DATABASE_VERSION值被改为2,系统发现现有数据库版本不同,即会调用onUpgrade
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.i(TAG, "DBHelper onUpgrade");
		db.execSQL("ALTER TABLE person ADD COLUMN other STRING");
	}

}
