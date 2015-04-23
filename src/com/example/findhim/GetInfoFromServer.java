package com.example.findhim;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.baidu.mapapi.model.LatLng;

public class GetInfoFromServer {
	private Thread getInfoThread; // 线程:获取来自服务器的信息
	private List<LatLng> mHistoryPoints;
	private String mCookieString; // Cookie值
	private String mDeviceId; // 设备号

	public GetInfoFromServer(String cookieString, String deviceId) {
		mCookieString = cookieString;
		mDeviceId = deviceId;
	}

	public void startWork() { // 开始线程
		getInfoThread = new Thread(new GetInfoTask());
		getInfoThread.start();

	}

	public boolean isWorkDone() { // 线程是否结束
		return !getInfoThread.isAlive();
	}

	public List<LatLng> getHistoryPoints() {
		return mHistoryPoints;
	}

	class GetInfoTask implements Runnable {
		private static final String TAG = "GetInfoTask";

		public void run() {

			try {
				String urlString = "http://mandmlee.nat123.net:40192/findHimm/GetLocation?device_id="
						+ mDeviceId;
				//Log.i(TAG, "URL: " + urlString);
				URL url = new URL(urlString);
				URLConnection conn = url.openConnection();
				conn.setRequestProperty("Cookie", mCookieString);
				InputStream in = conn.getInputStream();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(in));
				String line;
				mHistoryPoints = new ArrayList<LatLng>();
				while ((line = reader.readLine()) != null) {
					//Log.i(TAG, "数据库传来的数据: " + line); // 112.912345,27.891241
					if (line.length() > 0) {
						double longitude, latitude; // 经度纬度
						StringBuffer longitude_S = new StringBuffer();
						StringBuffer latitude_S = new StringBuffer();
						int i = 0;
						while (line.charAt(i) != ',') {
							longitude_S.append(line.charAt(i));
							++i;
						}
						++i;
						while (line.charAt(i) != '#') {
							latitude_S.append(line.charAt(i));
							++i;
						}

						longitude = Double.parseDouble(longitude_S.toString());
						latitude = Double.parseDouble(latitude_S.toString());
						//Log.i(TAG, "解析后的数据: " + longitude + "," + latitude);
						LatLng pt = new LatLng(latitude, longitude);
						mHistoryPoints.add(pt);
					}
				}
				in.close();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}
}
