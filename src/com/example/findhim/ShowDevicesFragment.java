package com.example.findhim;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import com.example.model.Device;
import com.example.utils.PictureUtils;
import com.scott.db.DBManager;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ShowDevicesFragment extends ListFragment {

	// 切换Fragment时的生命周期变化

	private static final String TAG = "ShowDevicesFragment";
	public static final String EXTRA_KEY_DEVICE_ID = "device_id";
	private static final int REQUES_CODE = 0;
	private String mCookie;
	private DBManager mDmg; // 数据库管理对象
	private ArrayList<Device> mDevices; // 所有的设备对象
	private MyAdapter mMyAdapter; // listView的设配器
	private View mProgressBar;

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Log.i(TAG, "ShowDevicesFragment onCreate");
		mCookie = getActivity().getIntent().getStringExtra(
				LoginActivity.EXTRA_KEY_COOKIE);
		mDmg = new DBManager(getActivity());
		mDevices = mDmg.getDevices(); // 从数据库中实例化设备对象集合

		mMyAdapter = new MyAdapter(mDevices);
		setListAdapter(mMyAdapter);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.i(TAG, "ShowDevicesFragment onCreateView");
		View view = inflater.inflate(R.layout.fragment_show_device, container,
				false);

		mProgressBar = view.findViewById(R.id.show_device_progressContainer); // 进度条
		mProgressBar.setVisibility(View.INVISIBLE);// 设置为不可见

		ListView listView = (ListView) view.findViewById(android.R.id.list);
		// 在onCreateView()方法完成调用并返回视图之前，getListView()方法返回值为null

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			registerForContextMenu(listView); // 给listView注册上下文菜单
		} else {
			listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL); // listView设置为多选
			listView.setMultiChoiceModeListener(new MultiChoiceModeListener() {

				// 回调方法，视图在选中或者撤销会调用它
				public void onItemCheckedStateChanged(
						android.view.ActionMode mode, int position, long id,
						boolean checked) {

				}

				// 实现的另一个接口 ActionMode.Callback 下面是它必须要实现的方法
				public boolean onCreateActionMode(ActionMode mode, Menu menu) {
					mode.getMenuInflater().inflate(R.menu.list_context_menu,
							menu);
					return true;
				}

				public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
					return false;
				}

				public boolean onActionItemClicked(ActionMode mode,
						MenuItem item) {
					switch (item.getItemId()) {
					case R.id.context_menu_item_delete:

						final ArrayList<Integer> selectedDevices = new ArrayList<Integer>();
						// 存储被点中的设备
						for (int i = mMyAdapter.getCount() - 1; i >= 0; i--) {
							if (getListView().isItemChecked(i)) {
								selectedDevices.add(i); // 添加要刪除设备的编号
							}
						}
						new AlertDialog.Builder(getActivity()).setTitle("系统提示")
								.setIcon(android.R.drawable.ic_dialog_alert)
								.setMessage("确认删除关于选中设备的所有信息(包括服务器端)？")
								.setPositiveButton("确定", new OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										deleteDevice(selectedDevices);
										// 删除设备
									}
								}).setNegativeButton("取消", null).show();
						mode.finish();
						mMyAdapter.notifyDataSetChanged(); // 更新listView的视图
						return true;
					default:
						return false;
					}
				}

				public void onDestroyActionMode(android.view.ActionMode mode) {

				}
			});
		}
		return view;
	}

	/**
	 * 删除和此设备相关的所有信息
	 */
	protected void deleteDevice(final ArrayList<Integer> devices) {

		// 在服务器上删除设备
		new AsyncTask<Void, Void, Boolean>() {
			@Override
			protected void onPreExecute() {
				mProgressBar.setVisibility(View.VISIBLE);// 设置为可见
			};

			@Override
			protected Boolean doInBackground(Void... params) {
				// 发送删除请求到服务器
				boolean isSuccess = true;
				String urlString = "http://mandmlee.nat123.net:40192/findHimm/DeleteDevices?";
				for (int i = 0; i < devices.size(); i++) {
					int pos = (int) devices.get(i);
					if (i == 0) {
						urlString += "device_id="
								+ mDevices.get(pos).getDeviceId();
					} else {
						urlString += "&device_id="
								+ mDevices.get(pos).getDeviceId();
					}
				}
				Log.i(TAG, "删除请求url: " + urlString);
				try {
					URL url = new URL(urlString);
					HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();
					conn.addRequestProperty("Cookie", mCookie);
					conn.setDoInput(true);
					conn.connect();
					if (HttpURLConnection.HTTP_OK != conn.getResponseCode()) { // 当连接不成功时
						Log.i(TAG, "连接服务器失败");
						isSuccess = false;
					} else {
						InputStream in = conn.getInputStream();
						BufferedReader reader = new BufferedReader(
								new InputStreamReader(in));
						// 读取返回的值
						String resultString = reader.readLine();
						if (resultString.equals("fail")) {
							isSuccess = false;
						}
					}
				} catch (MalformedURLException e) {
					isSuccess = false;
					e.printStackTrace();
				} catch (IOException e) {
					isSuccess = false;
					e.printStackTrace();
				}
				return isSuccess;
			}

			@Override
			protected void onPostExecute(Boolean result) {
				if (result == true) { // 如果服务器上的设备删除成功
					// 删除本地设备信息
					for (int i = 0; i < devices.size(); i++) {
						int pos = (int) devices.get(i);
						// 在数据库中删除设备的相关信息
						mDmg.deleteDevice(mDevices.get(pos).getDeviceId());
						// 删除图片
						if (mDevices.get(pos).getDevicePhoto() != null) {
							File file = mDevices.get(pos).getPhotoFile();
							file.delete();
						}
						mDevices.remove(pos);
					}
					mProgressBar.setVisibility(View.INVISIBLE);// 设置为不可见
					Toast.makeText(getActivity(), "删除成功", Toast.LENGTH_SHORT)
							.show();
				} else {
					mProgressBar.setVisibility(View.INVISIBLE);// 设置为不可见
					Toast.makeText(getActivity(), "删除失败", Toast.LENGTH_SHORT)
							.show();
				}
				// 更新当前Fragment
				mMyAdapter.notifyDataSetChanged();
				// 更新ShowTrackFragment
				ShowTrackFragment fragment = (ShowTrackFragment) getActivity()
						.getSupportFragmentManager().findFragmentByTag(
								"android:switcher:" + R.id.pager + ":0");
				fragment.updateFragment();
			};

		}.execute();
	}

	@SuppressWarnings("rawtypes")
	private class MyAdapter extends ArrayAdapter {
		private static final String TAG = "MyAdapter";

		@SuppressWarnings("unchecked")
		public MyAdapter(ArrayList<Device> devices) {
			super(getActivity(), 0, devices);
		}

		@SuppressLint("InflateParams")
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = getActivity().getLayoutInflater().inflate(
						R.layout.listview_item_show_device, null);
			}

			// 初始化ListView各组件
			ImageView imageView = (ImageView) convertView
					.findViewById(R.id.showDevice_item_imageView);
			TextView textId = (TextView) convertView
					.findViewById(R.id.showDevice_item_textId);
			TextView textName = (TextView) convertView
					.findViewById(R.id.showDevice_item_textName);

			String id = mDevices.get(position).getDeviceId();
			String name = mDevices.get(position).getDeviceName();
			String photo = mDevices.get(position).getDevicePhoto();

			textId.setText(id);
			if (name != null) { // 如果有名字
				textName.setText(name);
			}

			if (photo != null) { // 如果有对应的图片则显示图片
				File photoFile = mDevices.get(position).getPhotoFile();

				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inJustDecodeBounds = true; // 不返回bitmap对象，分配像素，但是可以允许调用者查询位图
				BitmapFactory.decodeFile(photoFile.getAbsolutePath(), options);
				options.inSampleSize = 4; // 图为原图宽长的1/4
				options.inJustDecodeBounds = false;
				Bitmap bm = BitmapFactory.decodeFile(
						photoFile.getAbsolutePath(), options);

				Bitmap rbm = PictureUtils.toRoundBitmap(bm); // 将图片转换成圆形
				imageView.setImageBitmap(rbm); // 设置图片

			} else {
				imageView.setImageResource(R.drawable.icon_default);
			}
			return convertView;
		}

	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		// 启动到更新设备信息的activity中
		Intent intent = new Intent(getActivity(), UpdateDeviceInfo.class);
		intent.putExtra(EXTRA_KEY_DEVICE_ID, mDevices.get(position)
				.getDeviceId());
		startActivityForResult(intent, REQUES_CODE);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// 更新当前页面
		if (mDmg == null) {
			mDmg = new DBManager(getActivity());
		}
		mDevices = mDmg.getDevices();
		mMyAdapter.notifyDataSetChanged();
		// 更新ShowTrackFragment
		ShowTrackFragment fragment = (ShowTrackFragment) getActivity()
				.getSupportFragmentManager().findFragmentByTag(
						"android:switcher:" + R.id.pager + ":0");
		fragment.updateFragment();
	}

	/**
	 * 创建一个上下文菜单，每次长按View(已注册)都会弹出菜单
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		Log.i(TAG, "onCreateContextMenu");
		getActivity().getMenuInflater().inflate(R.menu.list_context_menu, menu);
	}

	/**
	 * 上下文菜单点击事件
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		Log.i(TAG, "onContextItemSelected");
		// MenuItem有一个资源ID可以用于识别选中的菜单项
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		// 取出被点击的item对象的Device对象
		int position = info.position;
		final ArrayList<Integer> selectedDevices = new ArrayList<Integer>();
		selectedDevices.add(position);

		switch (item.getItemId()) {
		case R.id.context_menu_item_delete:
			new AlertDialog.Builder(getActivity()).setTitle("系统提示")
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setMessage("确认删除关于选中设备的所有信息(包括服务器端)？")
					.setPositiveButton("确定", new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							deleteDevice(selectedDevices);
							// 删除设备
						}
					}).setNegativeButton("取消", null).show();
			mMyAdapter.notifyDataSetChanged(); // 更新listView的视图
			return true;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onDestroy() {
		Log.i(TAG, "ShowDevicesFragment onDestroy");
		mDmg.destroyDB();
		super.onDestroy();
	}
}
