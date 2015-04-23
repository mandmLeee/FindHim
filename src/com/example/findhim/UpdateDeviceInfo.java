package com.example.findhim;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.Toast;

import com.example.model.Device;
import com.example.utils.PictureUtils;
import com.scott.db.DBManager;

public class UpdateDeviceInfo extends Activity implements OnClickListener,
		DialogInterface.OnClickListener {

	private static final String TAG = "UpdateDeviceInfo";
	private static final int REQUES_TAKE_PHOTO = 0;
	private static final int RESULT_LOAD_IMAGE = 1;
	private ImageView mImageView; // 设备头像
	private TextView mTextView; // 设备id
	private PopupWindow mPop; // 弹出选择更换头像的窗
	private View mPopContent; // 在mPop中显示的内容
	private View mPart; // mPop在此View下方显示
	private View mUpdateNameRelativeLayout; // 更改名称一栏设置
	private TextView mNameTextView; // 显示对应设备名称的控件
	private View mUpdateTimeRelativeLayout;
	private TextView mTimeTextView;
	private View mUpdateIntroRelativeLayout;
	private TextView mIntroTextView;

	private Button mTakePhotoButton;
	private Button mSelectPhotoButton;
	private Button mCancelButton;
	private Device mDevice;
	private String mDeviceId;
	private DBManager mDmg;
	private EditText mEditText;
	private Date mDate;
	private Date date;

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@SuppressLint({ "InflateParams", "NewApi" })
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_update_device_info);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setDisplayShowHomeEnabled(false);
		getActionBar().setTitle("设置");
		mDeviceId = getIntent().getStringExtra(
				ShowDevicesFragment.EXTRA_KEY_DEVICE_ID);
		mDmg = new DBManager(this);
		mDevice = mDmg.getDevice(mDeviceId); // 实例化当前设备

		String id = mDevice.getDeviceId();
		String name = mDevice.getDeviceName();
		String photo = mDevice.getDevicePhoto();
		String intro = mDevice.getDeviceIntro();
		mDate = mDevice.getDeviceTime();

		mImageView = (ImageView) findViewById(R.id.updateImageView);
		mTextView = (TextView) findViewById(R.id.updateTextView);

		mTextView.setText(id);
		if (photo != null) { // 如果有对应的图片则显示图片
			File photoFile = mDevice.getPhotoFile();
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true; // 不返回bitmap对象，分配像素，但是可以允许调用者查询位图
			BitmapFactory.decodeFile(photoFile.getAbsolutePath(), options);
			options.inSampleSize = 4; // 图为原图宽长的1/4
			options.inJustDecodeBounds = false;
			Bitmap bm = BitmapFactory.decodeFile(photoFile.getAbsolutePath(),
					options);
			Bitmap rbm = PictureUtils.toRoundBitmap(bm); // 将图片转换成圆形
			mImageView.setImageBitmap(rbm); // 设置图片
		} else {
			mImageView.setImageResource(R.drawable.icon_default);
		}
		mPopContent = getLayoutInflater().inflate(R.layout.item_popupwindows,
				null); // 实例化内容对象
		initPopContent();
		mPart = findViewById(R.id.part);
		mImageView.setOnClickListener(this);

		// 设置信息初始化
		mUpdateNameRelativeLayout = findViewById(R.id.update_name_RelativeLayout);
		mUpdateNameRelativeLayout.setOnClickListener(this);
		mNameTextView = (TextView) findViewById(R.id.update_name_textView);
		mNameTextView.setText(name);
		// 绑定时间
		mUpdateTimeRelativeLayout = findViewById(R.id.update_time_RelativeLayout);
		mUpdateTimeRelativeLayout.setOnClickListener(this);
		mTimeTextView = (TextView) findViewById(R.id.update_time_textView);
		// 转换格式
		String dateString = DateFormat.format("EEEE,MMM dd,yyyy", mDate)
				.toString();
		mTimeTextView.setText(dateString);
		// 简介
		mUpdateIntroRelativeLayout = findViewById(R.id.update_intro_RelativeLayout);
		mUpdateIntroRelativeLayout.setOnClickListener(this);
		mIntroTextView = (TextView) findViewById(R.id.update_intro_textView);
		mIntroTextView.setText(intro);

	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		String nameString = mEditText.getText().toString();
		if (!nameString.equals(mDevice.getDeviceName())) { // 如果数据更改了
			if (mDmg == null)
				mDmg = new DBManager(UpdateDeviceInfo.this);
			mDmg.updateName(mDeviceId, nameString);
			mDevice = mDmg.getDevice(mDeviceId);
			if (nameString != "") {
				mNameTextView.setText(nameString);
			}
		}
	}

	/**
	 * 初始化PopContent里面的控件
	 */
	private void initPopContent() {
		mTakePhotoButton = (Button) mPopContent
				.findViewById(R.id.item_popupwindows_camera);
		mSelectPhotoButton = (Button) mPopContent
				.findViewById(R.id.item_popupwindows_Photo);
		mCancelButton = (Button) mPopContent
				.findViewById(R.id.item_popupwindows_cancel);
		mTakePhotoButton.setOnClickListener(this);
		mSelectPhotoButton.setOnClickListener(this);
		mCancelButton.setOnClickListener(this);
	}

	/**
	 * 初始化mPop
	 */
	public void initPop() {
		int width = mPart.getWidth();
		int height = LayoutParams.WRAP_CONTENT;
		mPop = new PopupWindow(mPopContent, width, height, true);

		// 注意要加这句代码，点击弹出窗口其它区域才会让窗口消失
		mPop.setBackgroundDrawable(new ColorDrawable(0xffffffff));
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.updateImageView:
			Log.i(TAG, "updateImage");
			// 设置图片
			if (mPop == null) {
				initPop();
			}
			if (!mPop.isShowing()) {
				mPop.showAsDropDown(mPart, 0, 0); // 显示弹出窗口
			}
			break;
		case R.id.item_popupwindows_camera:
			// 跳到CameraActivity中
			Intent intent = new Intent(this, CameraActivity.class);
			intent.putExtra(ShowDevicesFragment.EXTRA_KEY_DEVICE_ID,
					mDevice.getDeviceId());
			startActivityForResult(intent, REQUES_TAKE_PHOTO);
			mPop.dismiss();
			break;
		case R.id.item_popupwindows_Photo:
			// 显式Intent
			Intent i = new Intent(
					Intent.ACTION_PICK,
					android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			startActivityForResult(i, RESULT_LOAD_IMAGE);
			mPop.dismiss();
			break;
		case R.id.item_popupwindows_cancel:
			// 取消显示
			mPop.dismiss();
			break;
		case R.id.update_name_RelativeLayout:
			// 弹出窗口修改对应设备名称
			mEditText = new EditText(this);
			new AlertDialog.Builder(this).setView(mEditText)
					.setPositiveButton("更新", this)
					.setNegativeButton("取消", null).show();
			break;
		case R.id.update_time_RelativeLayout:
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(mDate);
			int year = calendar.get(Calendar.YEAR);
			int month = calendar.get(Calendar.MONTH);
			int day = calendar.get(Calendar.DAY_OF_MONTH);
			date = new Date(mDate.getTime());
			View view = getLayoutInflater().inflate(R.layout.dialog_date, null);
			DatePicker datePicker = (DatePicker) view
					.findViewById(R.id.dialog_date);
			datePicker.init(year, month, day, new OnDateChangedListener() {
				// 当日期发生改变时，将其保存在Arguments中，那样当横屏时，数据也不会发生改变
				public void onDateChanged(DatePicker view, int year, int month,
						int day) {
					date = new GregorianCalendar(year, month, day).getTime();
				}
			});
			new AlertDialog.Builder(this)
					.setTitle("绑定时间")
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {
									mDate.setTime(date.getTime());
									// 更新数据库
									mDmg.updateTime(mDeviceId, mDate);
									// 更新UI
									String dateString = DateFormat.format(
											"EEEE,MMM dd,yyyy", mDate)
											.toString();
									mTimeTextView.setText(dateString);
								}

							}).setView(view).show();
			Log.v(TAG, "date was clicked!");
			break;
		case R.id.update_intro_RelativeLayout:
			final EditText editText = new EditText(this);
			new AlertDialog.Builder(this)
					.setView(editText)
					.setPositiveButton("更新",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									String introString = editText.getText()
											.toString();
									if (!introString.equals(mDevice
											.getDeviceIntro())) { // 如果数据更改了
										if (mDmg == null)
											mDmg = new DBManager(
													UpdateDeviceInfo.this);
										mDmg.updateIntro(mDeviceId, introString);
										mDevice = mDmg.getDevice(mDeviceId);
										if (introString != "") {
											mIntroTextView.setText(introString);
										}
									}
								}
							}).setNegativeButton("取消", null).show();
			break;
		default:
			break;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			if (NavUtils.getParentActivityName(this) != null) {
				NavUtils.navigateUpFromSameTask(this);
			}
			finish();
			return true;

		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == RESULT_LOAD_IMAGE && data != null) {

			Uri uri = data.getData(); // 被选择图片的uri
			String[] proj = { MediaStore.Images.Media.DATA };
			@SuppressWarnings("deprecation")
			Cursor actualimagecursor = managedQuery(uri, proj, null, null, null);
			int actual_image_column_index = actualimagecursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			actualimagecursor.moveToFirst();
			String img_path = actualimagecursor
					.getString(actual_image_column_index);
			if (mDmg == null) {
				mDmg = new DBManager(this);
			}
			Log.v(TAG, "img_path:" + img_path);
			mDmg.updatePhoto(mDeviceId, img_path);

		}

		// 更新当前页面
		if (mDmg == null) {
			mDmg = new DBManager(this);
		}
		mDevice = mDmg.getDevice(mDeviceId); // 实例化当前设备
		String photo = mDevice.getDevicePhoto();
		if (photo != null) { // 如果有对应的图片则显示图片
			File photoFile = mDevice.getPhotoFile();
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true; // 不返回bitmap对象，分配像素，但是可以允许调用者查询位图
			BitmapFactory.decodeFile(photoFile.getAbsolutePath(), options);
			options.inSampleSize = 4; // 图为原图宽长的1/4
			options.inJustDecodeBounds = false;
			Bitmap bm = BitmapFactory.decodeFile(photoFile.getAbsolutePath(),
					options);
			Bitmap rbm = PictureUtils.toRoundBitmap(bm); // 将图片转换成圆形
			mImageView.setImageBitmap(rbm); // 设置图片
		}
	}

	@Override
	public void onDestroy() {
		mDmg.destroyDB();
		super.onDestroy();
	}

}
