package com.example.findhim;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.os.AsyncTask;
import android.util.Log;

public class LoginManager {
	protected static final String TAG = "LoginManager";
	private String mCookieString;
	private ArrayList<String> mDevicesId; // 此用户的设备号列表

	// 内置AsyncTask,实现登陆
	public interface ICallBack {
		/* 登录成功时调用的接口 */
		public void onSuccess();

		/* 登陆过程中将"登录"改为"正在登录.." */
		public void onSetLoginUI();

		/* 登录失败时调用的接口 */
		public void onFailed(String error);
	}

	public void login(final String idString, final String pwdString,
			final ICallBack callBack) {
		new AsyncTask<Void, Void, String>() {
			/* 开始执行异步线程 */
			@Override
			protected void onPreExecute() {
				callBack.onSetLoginUI();
			}

			/* 后台任务：返回参数类型对应第三个参数类型 */
			@Override
			protected String doInBackground(Void... params) {
				URL url = null;
				String loginResult = null;
				HttpURLConnection conn = null;
				try {
					url = new URL(
							"http://mandmlee.nat123.net:40192/findHimm/Login?id="
									+ idString + "&pwd=" + pwdString);
					Log.i(TAG, "url: " + url.toString());
					conn = (HttpURLConnection) url.openConnection();
					conn.setDoInput(true);
					conn.connect();
					if (HttpURLConnection.HTTP_OK != conn.getResponseCode()) { // 当连接不成功时
						loginResult = "连接服务器失败";
					} else {
						// 获取返回信息，验证用户是否正确
						InputStream in = conn.getInputStream();
						BufferedReader reader = new BufferedReader(
								new InputStreamReader(in, "GBK"));
						loginResult = reader.readLine();
						//Log.i(TAG, "loginResult: " + loginResult); 
						if (loginResult.equals("pwd is wrong")) {
							loginResult = "密码错误";
						} else if (loginResult.equals("no such user")) {
							loginResult = "无此用户";
						} else {
							// 登录成功，获取Cookie
							Map<String, List<String>> map = conn
									.getHeaderFields();
							String s = map.get("Set-Cookie").toString();
							mCookieString = s.substring(1, 44);
							// Log.i(TAG, "Cookie: " + mCookieString);
							// 获取此用户的设备号列表
							mDevicesId = new ArrayList<String>();
							String deviceId;
							while ((deviceId = reader.readLine()) != null) {
								Log.i(TAG, "设备号: " + deviceId);
								mDevicesId.add(deviceId);
							}
						}
						in.close();
					}

				} catch (IOException e) {
					e.printStackTrace();
				}
				return loginResult;
			}

			/* 后代任务结束后，可处理UI */
			protected void onPostExecute(String loginResult) {
				if (loginResult.equals("success")) {// 登录成功
					callBack.onSuccess();
				} else {
					callBack.onFailed(loginResult);
				}
			}

		}.execute();
	}

	public String getCookie() {
		return mCookieString;
	}

	public ArrayList<String> getDevicesId() {
		return mDevicesId;
	}
}
