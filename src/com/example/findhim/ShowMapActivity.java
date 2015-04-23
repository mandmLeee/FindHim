package com.example.findhim;

import java.util.ArrayList;
import java.util.List;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.example.utils.NetworkState;

import android.support.v4.app.NavUtils;
import android.text.TextPaint;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class ShowMapActivity extends Activity {

	protected static final String TAG = "ShowMapActivity";
	private static final String KEY_IsShowTrack = "isShowTrack";
	private static final String KEY_HistoryPoints = "historyPoints";
	private static final String KEY_IsShowSatellite = "isShowSatellite";
	private MapView mMapView = null; // 百度地图的主控件
	private BaiduMap mBaiduMap = null; // 百度地图的控制器
	private View mProgressContainer; // 进度条控件
	private List<LatLng> mHistoryPoints; // 历史轨迹
	private LatLng mCurrentLocation; // 当前位置
	private boolean mIsShowTrack; // 是否显示轨迹
	private boolean mIsShowSatellite; // 是否显示卫星地图
	private String mCookieString; // 身份验证
	private String mDeviceId; // 当前要显示的设备号
	private GetInfoFromServer mGetInfoFromServer; // 获取信息的类

	Handler draw_current_handler = new Handler();
	Runnable draw_current_thread = new Runnable() {
		public void run() {
			if (mGetInfoFromServer.isWorkDone()) { // 数据已经更新完了
				mHistoryPoints = mGetInfoFromServer.getHistoryPoints();
				if (mHistoryPoints.isEmpty()) { // 当轨迹为空时
					Toast.makeText(ShowMapActivity.this, "您还未留下任何足迹",
							Toast.LENGTH_SHORT).show();
				} else {
					mCurrentLocation = mHistoryPoints
							.get(mHistoryPoints.size() - 1);
					mBaiduMap.clear();
					mProgressContainer.setVisibility(View.INVISIBLE); // 隐藏进度条
					Toast.makeText(ShowMapActivity.this, "加载成功",
							Toast.LENGTH_SHORT).show();
					DrawCurrentPoint();
					if (mIsShowTrack == true) {
						DrawTrack();
					}
				}
				draw_current_handler.removeCallbacks(draw_current_thread);
			} else {
				draw_current_handler.postDelayed(draw_current_thread, 1);
			}
		}
	};

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		// 在使用SDK各组件之前初始化context信息，传入ApplicationContext
		// 注意该方法要再setContentView方法之前实现
		SDKInitializer.initialize(getApplicationContext());
		// requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
		setContentView(R.layout.activity_show_map);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		Log.i(TAG, "onCreate");
		mCookieString = getIntent().getStringExtra(
				LoginActivity.EXTRA_KEY_COOKIE);
		mDeviceId = getIntent().getStringExtra(
				ShowTrackFragment.EXTRA_KEY_DEVICEID);

		mIsShowTrack = false;
		mIsShowSatellite = false;
		mProgressContainer = (View) findViewById(R.id.show_map_progressContainer);
		// 设置进度条不可见
		mProgressContainer.setVisibility(View.INVISIBLE);
		mMapView = (MapView) findViewById(R.id.bmapView);// 获取地图控件引用
		mBaiduMap = mMapView.getMap();

		// 绘出当前点
		mHistoryPoints = new ArrayList<LatLng>();
		if (!NetworkState.isNetworkConnected(getApplication())) { // 如果设备未连接网络
			Toast.makeText(ShowMapActivity.this, "未连接网络", Toast.LENGTH_SHORT)
					.show();
		} else {
			// 启动连接服务器获取信息
			mGetInfoFromServer = new GetInfoFromServer(mCookieString, mDeviceId);
			mProgressContainer.setVisibility(View.VISIBLE); // 显示进度条
			mGetInfoFromServer.startWork();
			draw_current_handler.post(draw_current_thread); // 提交作图Handler
		}

	}

	/* 描出当前位置 */
	public void DrawCurrentPoint() {
		Log.i(TAG, "DrawCurrentPoint");
		// 弹出窗覆盖物
		// 在地图中找到此位置
		MapStatus mMapStatus = new MapStatus.Builder().target(mCurrentLocation)
				.zoom(12).build(); // 定义地图状态
		// 定义MapStatusUpdate对象，以便描述地图状态将要发生的变化
		MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory
				.newMapStatus(mMapStatus);
		// 改变地图状态
		mBaiduMap.setMapStatus(mMapStatusUpdate);
		TextView tv = new TextView(ShowMapActivity.this);
		tv.setBackgroundResource(R.drawable.location_tips);
		tv.setPadding(10, 10, 10, 0);
		tv.setText("当前位置");
		tv.setTextColor(0xffff0000);
		TextPaint tp = tv.getPaint();
		tp.setFakeBoldText(true); // 粗体
		InfoWindow infoWindow = new InfoWindow(tv, mCurrentLocation, -47);
		mBaiduMap.showInfoWindow(infoWindow);

		BitmapDescriptor bitmap = BitmapDescriptorFactory
				.fromResource(R.drawable.icon_current_pt);// 构建Marker图标
		// 构建MarkerOption,用于在地图上添加Marker
		OverlayOptions option = new MarkerOptions().position(mCurrentLocation)
				.icon(bitmap);
		mBaiduMap.addOverlay(option); // 添加覆盖物选项
	}

	/* 描出历史轨迹 */
	public void DrawTrack() {
		Log.i(TAG, "DrawTrack");
		LatLng startPoint = mHistoryPoints.get(0);
		BitmapDescriptor bitmap = BitmapDescriptorFactory
				.fromResource(R.drawable.icon_start_pt);// 构建起点图标
		OverlayOptions option = new MarkerOptions().position(startPoint).icon(
				bitmap);
		mBaiduMap.addOverlay(option); // 添加覆盖物选项
		// 构建用户绘制线段的Option对象
		OverlayOptions PolylineOption = new PolylineOptions().points(
				mHistoryPoints).color(0xffff0000);
		// 在地图上添加直线Option,用于显示
		mBaiduMap.addOverlay(PolylineOption);
	}

	/* 当配置发生变化时，不会重新启动Activity。但是会回调此方法，用户自行进行对屏幕旋转后进行处理 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.i(TAG, "onCreateOptionsMenu");
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.show_map, menu);
		MenuItem track = menu.findItem(R.id.menu_item_track);
		MenuItem mapType = menu.findItem(R.id.menu_item_map_type);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_item_track:
			if (item.getTitle().toString().equals("显示轨迹")) {
				mIsShowTrack = true;
				item.setTitle(R.string.hide_track);
				DrawTrack();
			} else {
				item.setTitle(R.string.show_track);
				mIsShowTrack = false;
				mBaiduMap.clear();
				DrawCurrentPoint();
			}
			return true;
		case R.id.menu_item_map_type:
			if (item.getTitle().toString().equals("卫星地图")) {
				mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
				mIsShowSatellite = true;
				item.setTitle("普通地图");
			} else {
				mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
				mIsShowSatellite = false;
				item.setTitle("卫星地图");
			}
			return true;
		case R.id.menu_item_fresh:
			if (!NetworkState.isNetworkConnected(getApplication())) { // 如果设备未连接网络
				Toast.makeText(ShowMapActivity.this, "未连接网络",
						Toast.LENGTH_SHORT).show();
			} else {
				if (mGetInfoFromServer == null
						|| mGetInfoFromServer.isWorkDone()) {
					mGetInfoFromServer = new GetInfoFromServer(mCookieString,
							mDeviceId);
					mProgressContainer.setVisibility(View.VISIBLE); // 显示进度条
					mGetInfoFromServer.startWork();
				}
				draw_current_handler.post(draw_current_thread);
			}
			return true;
		case android.R.id.home:
			if(NavUtils.getParentActivityName(this)!=null){
				NavUtils.navigateUpFromSameTask(this);
			}
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
		Log.i(TAG, "onDestroy");
		mMapView.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// 在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
		Log.i(TAG, "onResume");
		mMapView.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		// 在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
		Log.i(TAG, "onPause");
		mMapView.onPause();
	}
}
