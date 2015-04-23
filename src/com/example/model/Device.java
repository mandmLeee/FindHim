package com.example.model;

import java.io.File;
import java.util.Date;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

/**
 * 设备类 包含设备的所有信息
 */
public class Device {

	private static final String TAG = "Device";
	private String mDeviceId; // 设备id
	private String mDeviceName; // 设备名称
	private String mDevicePhoto; // 设备图片路径
	private Date mDeviceTime; // 绑定时间
	private String mDeviceIntro; // 设备介绍
	private Context mContext;

	public Device(String deviceId, Context context) {
		this.mDeviceId = deviceId;
		mContext = context;
	}

	public String getDeviceId() {
		return mDeviceId;
	}

	public String getDeviceName() {
		return mDeviceName;
	}

	public void setDeviceName(String deviceName) {
		this.mDeviceName = deviceName;
	}

	public String getDevicePhoto() {
		return mDevicePhoto;
	}

	public void setDevicePhoto(String devicePhoto) {
		this.mDevicePhoto = devicePhoto;
	}

	public File getPhotoFile() {
		return new File(mDevicePhoto);
	}

	public Date getDeviceTime() {
		return mDeviceTime;
	}

	public void setDeviceTime(Date mDeviceTime) {
		this.mDeviceTime = mDeviceTime;
	}

	public String getDeviceIntro() {
		return mDeviceIntro;
	}

	public void setDeviceIntro(String mDeviceIntro) {
		this.mDeviceIntro = mDeviceIntro;
	}

}
