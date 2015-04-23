package com.example.findhim;

import com.astuetz.PagerSlidingTabStrip;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;

/**
 * 使用ViewPager存放多个Fragment,实现左右滑动切换Fragment
 */
public class MainActivity extends FragmentActivity {
	protected static final String TAG = "MainActivity";
	private PagerSlidingTabStrip mTabs; // 滑动Tabs
	private ViewPager mViewPager; // 存放Fragment的容器
	private ShowTrackFragment mShowTrackFragment; // 操作轨迹
	private ShowDevicesFragment mShowDevicesFragment; // 操作所有的设备信息
	private DisplayMetrics mDm; // 当前屏幕的密度
	private MyPagerAdapter mAdapter; // 设配器

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mDm = getResources().getDisplayMetrics(); // 获取当前屏幕的密度

		mTabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mAdapter = new MyPagerAdapter(getSupportFragmentManager());
		mViewPager.setAdapter(mAdapter); // 给ViewPager设配器
		mTabs.setViewPager(mViewPager); // 将tabs和viewPager联系起来
		setTabsValue(); // 初始化tabs属性
	}

	/**
	 * 对PagerSlidingTabStrip的各项属性进行赋值。
	 */
	private void setTabsValue() {
		// 设置Tab是自动填充满屏幕的
		mTabs.setShouldExpand(true);
		// 设置Tab的分割线是透明的
		mTabs.setDividerColor(Color.TRANSPARENT);
		// 设置Tab底部线的高度
		mTabs.setUnderlineHeight((int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 1, mDm));
		// 设置Tab Indicator的高度
		mTabs.setIndicatorHeight((int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 4, mDm));
		// 设置Tab标题文字的大小
		mTabs.setTextSize((int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_SP, 16, mDm));
		// 设置Tab 滑动条的颜色
		mTabs.setIndicatorColor(Color.parseColor("#45c01a"));
		// 设置选中Tab文字的颜色 (这是我自定义的一个方法)
		mTabs.setSelectedTextColor(Color.parseColor("#45c01a"));
		// 取消点击Tab时的背景色
		mTabs.setTabBackground(0);
	}

	private class MyPagerAdapter extends FragmentPagerAdapter {

		private static final String TAG = "MyPagerAdapter";
		private final String[] titles = { "轨迹", "设备" };

		public MyPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return titles[position];
		}

		@Override
		public Fragment getItem(int position) {
			switch (position) {
			case 0: // 显示操作轨迹页面
				if (mShowTrackFragment == null) {
					mShowTrackFragment = new ShowTrackFragment();
				}
				return mShowTrackFragment;
			case 1: // 显示操作设备页面
				if (mShowDevicesFragment == null) {
					mShowDevicesFragment = new ShowDevicesFragment();
				}
				return mShowDevicesFragment;

			default:
				return null;
			}
		}

		@Override
		public int getCount() {
			return titles.length;
		}

	}

	/* 监听设备物理键，做出反应 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		/*
		 * keyCode: 被按下的键值即键盘码 event: 按键事件的对象，其中包括触发事件的详细信息。如事件发生时间等。
		 */
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			new AlertDialog.Builder(MainActivity.this).setTitle("系统提示")
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setMessage("确认退出程序？")
					.setPositiveButton("确定", new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) { //
							finish();
						}
					}).setNegativeButton("取消", null).show();
		}
		return super.onKeyDown(keyCode, event);
		// false表示未处理此事件，它应该继续传播 相当于 return super.
		// true表示处理完此事件，不会继续传播
	}

}
