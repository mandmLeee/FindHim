package com.example.findhim;

import java.io.File;
import java.util.ArrayList;

import com.example.model.Device;
import com.example.utils.PictureUtils;
import com.scott.db.DBManager;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class ShowTrackFragment extends Fragment implements OnItemClickListener {
	public static final String EXTRA_KEY_DEVICEID = "device_id";
	private static final String TAG = "ShowTrackFragment";
	private String mCookie;
	private GridView mGridView; // 显示所有设备的GridView控件
	private DBManager mDmg; // 数据库管理对象
	private ArrayList<Device> mDevices; // 所有的设备对象
	private GridAdapter mAdapter; // 设配器

	@SuppressWarnings("unchecked")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Log.i(TAG, "ShowTrackFragment onCreate");
		// 获取LoginActivity发送过来的Cookie
		mCookie = getActivity().getIntent().getStringExtra(
				LoginActivity.EXTRA_KEY_COOKIE);
		mDmg = new DBManager(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.i(TAG, "oneCreateView ShowTrackFragment");
		View view = inflater.inflate(R.layout.fragment_show_track, null);
		mGridView = (GridView) view.findViewById(R.id.gridView_show_track); // 实例化
		mDevices = mDmg.getDevices(); // 从数据库中实例化设备对象集合
		mAdapter = new GridAdapter(mDevices);
		mGridView.setAdapter(mAdapter); // 设置设配器
		mGridView.setOnItemClickListener(this); // 设置监听器
		return view;
	}

	/**
	 * 更新此Fragment页面
	 */
	public void updateFragment() {
		Log.i(TAG, "updateFragment");
		mDevices = mDmg.getDevices();// 是否是mDevices数量的影响？
		mAdapter = new GridAdapter(mDevices);
		mGridView.setAdapter(mAdapter); // 设置设配器
	}

	private class GridAdapter extends ArrayAdapter<Device> {

		public GridAdapter(ArrayList<Device> devices) {
			super(getActivity(), 0, devices); // 0表示item使用自定义布局
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (position >= mDevices.size()) {
				return null;
			}
			if (convertView == null) {
				// item使用自定义布局
				convertView = getActivity().getLayoutInflater().inflate(
						R.layout.gridview_item_show_track, null);
			}
			ImageView imageView = (ImageView) convertView
					.findViewById(R.id.showTrack_item_imageView);
			TextView textView = (TextView) convertView
					.findViewById(R.id.showTrack_item_textView);
			String id = mDevices.get(position).getDeviceId();
			String name = mDevices.get(position).getDeviceName();
			String photo = mDevices.get(position).getDevicePhoto();

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
			if (name != null) { // 如果名称不为空显示名字，否则显示id
				textView.setText(name);
			} else {
				textView.setText(id);
			}
			return convertView;
		}

	}

	/**
	 * GridView item的监听器
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Intent intent = new Intent(getActivity(), ShowMapActivity.class);
		// 传递Cookie和要显示的设备id到ShowMapActivity
		intent.putExtra(LoginActivity.EXTRA_KEY_COOKIE, mCookie);
		intent.putExtra(this.EXTRA_KEY_DEVICEID, mDevices.get(position)
				.getDeviceId());
		startActivity(intent);
	}

	@Override
	public void onDestroy() {
		Log.i(TAG, "ShowTrackFragment onDestroy");
		mDmg.destroyDB();
		super.onDestroy();
	}
}
