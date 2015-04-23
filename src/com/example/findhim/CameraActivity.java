package com.example.findhim;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import com.example.model.Device;
import com.scott.db.DBManager;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

public class CameraActivity extends Activity {

	protected static final String TAG = "CameraActivity";
	private SurfaceView mSurfaceView;
	private Camera mCamera;
	private View mProgressContainer;
	private OrientationEventListener mOrEventListener; // 设备方向监听器
	private Boolean mIsLandscape; // 是否横屏
	private Device mDevice;
	private DBManager mDmg;

	// 图像数据还未处理完成时的回调函数
	private Camera.ShutterCallback mShutterCallback = new Camera.ShutterCallback() {

		public void onShutter() {
			// 是进度条可见
			mProgressContainer.setVisibility(View.VISIBLE);
		}
	};
	// JPEG版本图像可用时的回调函数
	private Camera.PictureCallback mJpegCallback = new Camera.PictureCallback() {

		public void onPictureTaken(byte[] data, Camera camera) {
			// 将数据保存在本地，结束此Activity
			String fileName = mDevice.getDeviceId() + ".jpg";
			FileOutputStream out = null;
			Bitmap oldBitmap = null;
			Bitmap newBitmap = null;
			boolean success = true;
			boolean landscape = false;
			try {
				File rootFile = new File(getFilesDir(), "/images");
				if (!rootFile.exists()) {
					Log.v(TAG, "当前目录不存在，创建它");

					if (rootFile.mkdirs()) { // 创建多级目录
						Log.v(TAG, "创建成功");
					}
				}
				File photoFile = new File(rootFile, fileName);
				out = new FileOutputStream(photoFile);

				if (!mIsLandscape) { // 如果竖屏拍照，旋转图片后保存
					landscape = true;
					oldBitmap = BitmapFactory.decodeByteArray(data, 0,
							data.length);
					Matrix matrix = new Matrix();
					matrix.setRotate(90); // 图片旋转90度
					newBitmap = Bitmap.createBitmap(oldBitmap, 0, 0,
							oldBitmap.getWidth(), oldBitmap.getHeight(),
							matrix, true);
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					newBitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos);
					byte[] newData = baos.toByteArray();
					out.write(newData);
				} else {
					Log.i(TAG, "直接保存图片");
					out.write(data); // 保存图片
				}
			} catch (FileNotFoundException e) {
				success = false;
				e.printStackTrace();

			} catch (IOException e) {
				success = false;
				e.printStackTrace();
			} finally {
				if (landscape) { // 如果是竖屏拍的，则清除使用图片资源
					if (!oldBitmap.isRecycled()) {
						oldBitmap.recycle();
						oldBitmap = null;
					}
					if (!newBitmap.isRecycled()) {
						newBitmap.recycle();
						newBitmap = null;
					}
				}
				try {
					if (out != null)
						out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (success) { // 如果图片保存成功
				File rootFile = new File(getFilesDir(), "/images");
				File photoFile = new File(rootFile, mDevice.getDeviceId()
						+ ".jpg");
				mDmg.updatePhoto(mDevice.getDeviceId(),
						photoFile.getAbsolutePath());
			}
			finish();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		startOrientationChangeListener();
		setContentView(R.layout.activity_camera);
		String deviceId = getIntent().getStringExtra(
				ShowDevicesFragment.EXTRA_KEY_DEVICE_ID);
		mDmg = new DBManager(this);
		mDevice = mDmg.getDevice(deviceId); // 实例化当前设备

		mProgressContainer = findViewById(R.id.crime_camera_progressContainer);
		// 初始化进度条不可见
		mProgressContainer.setVisibility(View.INVISIBLE);
		Button takePictureButton = (Button) findViewById(R.id.crime_camera_takePictureButton);

		takePictureButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// 捕捉相机图像
				mCamera.takePicture(mShutterCallback, null, mJpegCallback);
			}
		});
		mSurfaceView = (SurfaceView) findViewById(R.id.crime_camera_surfaceView);
		SurfaceHolder holder = mSurfaceView.getHolder(); // SurfaceView的控制器
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); // 设置缓存
		// 当Surface对象销毁后，必须保证没有任何内容要在Surface的缓冲区绘制
		// 所以，需要实现SurfaceHolder.Callback接口
		holder.addCallback(new SurfaceHolder.Callback() {

			public void surfaceDestroyed(SurfaceHolder holder) {
				if (mCamera != null) {
					mCamera.stopPreview();// 在Surface上移除mCamera
				}
			}

			public void surfaceCreated(SurfaceHolder holder) {

				try {
					if (mCamera != null)
						mCamera.setPreviewDisplay(holder);
					// 连接mCamera和Surface
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			public void surfaceChanged(SurfaceHolder holder, int format,
					int width, int height) {
				// 初始化mCamera
				// 当Surface首次在屏幕上显示的时候调用此方法（包括surface的尺寸大小发生改变时）
				Parameters parameters = mCamera.getParameters();
				// 获取mCamera的参数对象
				Size s = getBestSupportedSize(parameters
						.getSupportedPreviewSizes());
				// 设置预览图片尺寸
				parameters.setPreviewSize(s.width, s.height);
				// 设置捕捉图片尺寸
				s = getBestSupportedSize(parameters.getSupportedPictureSizes());
				parameters.setPictureSize(s.width, s.height);
				mCamera.setParameters(parameters);

				try {
					mCamera.startPreview();
				} catch (Exception e) {
					if (mCamera != null) {
						mCamera.release();
						mCamera = null;
					}
				}
			}

			private Size getBestSupportedSize(List<Size> sizes) {
				// 取能适用的最大的SIZE
				Size bestSize = sizes.get(0);
				int largestArea = sizes.get(0).height * sizes.get(0).width;
				for (Size s : sizes) {
					int area = s.width * s.height;
					if (area > largestArea) {
						largestArea = area;
						bestSize = s;
					}
				}
				return bestSize;
			}
		});
	}

	private final void startOrientationChangeListener() { // 设备方向监听
		mOrEventListener = new OrientationEventListener(this) {
			@Override
			public void onOrientationChanged(int rotation) {
				if (((rotation >= 0) && (rotation <= 45)) || (rotation >= 315)
						|| ((rotation >= 135) && (rotation <= 225))) {// portrait
					mIsLandscape = false;
					Log.i(TAG, "竖屏");
				} else if (((rotation > 45) && (rotation < 135))
						|| ((rotation > 225) && (rotation < 315))) {// landscape
					mIsLandscape = true;
					Log.i(TAG, "横屏");
				}
			}
		};
		mOrEventListener.enable();
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	@Override
	public void onResume() {
		super.onResume();
		// 启动Camera
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			mCamera = Camera.open(0);
			// open(i) since GINGERBREAD
			// i=0 表示后置相机

		} else
			mCamera = Camera.open();
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.i(TAG, "onPause 释放相机");
		mOrEventListener.disable();
		// 释放相机
		if (mCamera != null) {
			mCamera.release();
			mCamera = null;
		}
	}

	@Override
	public void onDestroy() {
		mDmg.destroyDB();
		super.onDestroy();
	}

}
