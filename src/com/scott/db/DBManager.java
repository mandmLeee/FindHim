package com.scott.db;

import java.util.ArrayList;
import java.util.Date;

import com.example.model.Device;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DBManager {

	private static final String TAG = "DBManager";
	private DBHelper helper;
	private SQLiteDatabase db;
	private Context mContext;

	public DBManager(Context context) {
		mContext = context;
		helper = new DBHelper(context);
		db = helper.getWritableDatabase(); // 获取一个数据库实例
	}

	/**
	 * 添加一条只包含id的记录
	 */
	public void add(String device_id) {
		db.beginTransaction(); // 开始事务
		try {
			db.execSQL("INSERT INTO device VALUES(?,null,null,null,null)",
					new String[] { device_id });
			db.setTransactionSuccessful(); // 设置事务成功完成
		} finally {
			db.endTransaction(); // 结束事务
		}
	}

	/**
	 * 判断此id记录是否存在
	 */
	public boolean isRecordExist(String device_id) {

		Cursor cursor = db.rawQuery("SELECT _id FROM device WHERE _id=?",
				new String[] { device_id });
		if (cursor.moveToNext()) { // 是否存在一条记录
			return true;
		}
		return false;

	}

	/**
	 * 在表中删除此id设备记录
	 */
	public void deleteDevice(String device_id) {
		db.delete("device", "_id=?", new String[] { device_id });
		// 删除
	}

	/**
	 * 读取表中所有的记录，并实例化返回一个ArrayList<Device>对象
	 */
	public ArrayList<Device> getDevices() {
		ArrayList<Device> devices = new ArrayList<Device>();
		Cursor cursor = db.rawQuery("SELECT * FROM device", null);
		while (cursor.moveToNext()) {
			String id = cursor.getString(cursor.getColumnIndex("_id"));
			String name = cursor
					.getString(cursor.getColumnIndex("device_name"));
			String photo = cursor.getString(cursor
					.getColumnIndex("device_photo"));
			Device device = new Device(id, mContext);
			device.setDeviceName(name);
			device.setDevicePhoto(photo);
			devices.add(device); // 添加
		}
		return devices;
	}

	/**
	 * 获取指定id的设备对象
	 */
	public Device getDevice(String deviceId) {
		Device device = new Device(deviceId, mContext);
		Cursor cursor = db.rawQuery("SELECT * FROM device WHERE _id=?",
				new String[] { deviceId });
		while (cursor.moveToNext()) {
			String name = cursor
					.getString(cursor.getColumnIndex("device_name"));
			String photo = cursor.getString(cursor
					.getColumnIndex("device_photo"));
			Long time = cursor.getLong(cursor.getColumnIndex("device_time"));
			Date dateTime = new Date(time);
			String intro = cursor.getString(cursor
					.getColumnIndex("device_intro"));
			device.setDeviceIntro(intro);
			device.setDeviceTime(dateTime);
			device.setDeviceName(name);
			device.setDevicePhoto(photo);
		}
		return device;
	}

	/**
	 * 更新指定id的图片
	 */
	public void updatePhoto(String deviceId, String photoString) {

		ContentValues cv = new ContentValues();
		cv.put("device_photo", photoString);
		db.update("device", cv, "_id=?", new String[] { deviceId });

	}

	/**
	 * 更新指定id的名称
	 */
	public void updateName(String deviceId, String deviceName) {

		ContentValues cv = new ContentValues();
		cv.put("device_name", deviceName);
		db.update("device", cv, "_id=?", new String[] { deviceId });

	}

	public void updateTime(String deviceId, Date deviceTime) {

		ContentValues cv = new ContentValues();
		Long time = deviceTime.getTime();
		cv.put("device_time", time);
		db.update("device", cv, "_id=?", new String[] { deviceId });

	}

	public void updateIntro(String deviceId, String deviceIntro) {

		ContentValues cv = new ContentValues();
		cv.put("device_intro", deviceIntro);
		db.update("device", cv, "_id=?", new String[] { deviceId });

	}

	/**
	 * 关闭数据库
	 */
	public void destroyDB() {
		db.close();
	}
}
