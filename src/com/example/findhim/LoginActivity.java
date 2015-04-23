package com.example.findhim;

import java.io.File;
import java.util.ArrayList;

import com.example.findhim.LoginManager.ICallBack;
import com.example.model.Device;
import com.example.model.User;
import com.example.utils.NetworkState;
import com.example.utils.Utils;
import com.scott.db.DBManager;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity implements OnClickListener,
		OnItemClickListener, OnDismissListener {
	protected static final String TAG = "LoginActivity";
	protected static final String EXTRA_KEY_COOKIE = "cookie";
	private LinearLayout mLoginLinearLayout; // 登录内容的容器
	private LinearLayout mUserIdLinearLayout; // 将下拉弹出窗口在此容器下方显示
	private Animation mTranslate; // 位移动画
	private Dialog mLoginingDlg; // 显示正在登录的Dialog
	private EditText mIdEditText; // 登录ID编辑框
	private EditText mPwdEditText; // 登录密码编辑框
	private ImageView mMoreUser; // 下拉图标
	private TextView mForgetPwd; // 忘记密码控件
	private Button mLoginButton; // 登录按钮
	private ImageView mLoginMoreUserView; // 弹出下拉弹出窗的按钮
	private String mIdString;
	private String mPwdString;
	private ArrayList<User> mUsers; // 用户列表
	private ListView mUserIdListView; // 下拉弹出窗显示的ListView对象
	private MyAapter mAdapter; // ListView的监听器
	private PopupWindow mPop; // 下拉弹出窗
	private DBManager mDmg; // 管理数据库对象
	private ArrayList<String> mDevicesId; // 设备id集合

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		initView();
		setListener();
		mLoginLinearLayout.startAnimation(mTranslate); // Y轴水平移动

		mDmg = new DBManager(this); // 实例化
		/* 获取已经保存好的用户密码 */
		mUsers = Utils.getUserList(LoginActivity.this);

		if (mUsers.size() > 0) {
			/* 将列表中的第一个user显示在编辑框 */
			mIdEditText.setText(mUsers.get(0).getId());
			mPwdEditText.setText(mUsers.get(0).getPwd());
		}

		LinearLayout parent = (LinearLayout) getLayoutInflater().inflate(
				R.layout.userifo_listview, null);
		mUserIdListView = (ListView) parent.findViewById(android.R.id.list);
		parent.removeView(mUserIdListView);
		mUserIdListView.setOnItemClickListener(this); // 设置点击事
		mAdapter = new MyAapter(mUsers);
		mUserIdListView.setAdapter(mAdapter);

	}

	/* ListView的适配器 */
	class MyAapter extends ArrayAdapter<User> {

		public MyAapter(ArrayList<User> users) {
			super(LoginActivity.this, 0, users);
		}

		public View getView(final int position, View convertView,
				ViewGroup parent) {
			if (convertView == null) {
				convertView = getLayoutInflater().inflate(
						R.layout.userifo_listview_item, null);
			}

			TextView userIdText = (TextView) convertView
					.findViewById(R.id.listview_userid);
			userIdText.setText(getItem(position).getId());

			ImageView deleteUser = (ImageView) convertView
					.findViewById(R.id.login_delete_user);
			deleteUser.setOnClickListener(new OnClickListener() {
				// 点击删除deleteUser时,在mUsers中删除选中的元素
				@Override
				public void onClick(View v) {

					if (getItem(position).getId().equals(mIdString)) {
						// 如果要删除的用户Id和Id编辑框当前值相等，则清空
						mIdString = "";
						mPwdString = "";
						mIdEditText.setText(mIdString);
						mPwdEditText.setText(mPwdString);
					}
					mUsers.remove(getItem(position));
					mAdapter.notifyDataSetChanged(); // 更新ListView
				}
			});
			return convertView;
		}

	}

	private void setListener() {
		mIdEditText.addTextChangedListener(new TextWatcher() {

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				mIdString = s.toString();
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void afterTextChanged(Editable s) {
			}
		});
		mPwdEditText.addTextChangedListener(new TextWatcher() {

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				mPwdString = s.toString();
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void afterTextChanged(Editable s) {
			}
		});
		mForgetPwd.setOnClickListener(this);
		mLoginButton.setOnClickListener(this);
		mLoginMoreUserView.setOnClickListener(this);
	}

	private void initView() {
		mIdEditText = (EditText) findViewById(R.id.login_edtId);
		mPwdEditText = (EditText) findViewById(R.id.login_edtPwd);
		mMoreUser = (ImageView) findViewById(R.id.login_more_user);
		mLoginButton = (Button) findViewById(R.id.login_btnLogin);
		mForgetPwd = (TextView) findViewById(R.id.login_txtForgotPwd);
		mLoginMoreUserView = (ImageView) findViewById(R.id.login_more_user);
		mLoginLinearLayout = (LinearLayout) findViewById(R.id.login_linearLayout);
		mUserIdLinearLayout = (LinearLayout) findViewById(R.id.userId_LinearLayout);
		mTranslate = AnimationUtils.loadAnimation(this, R.anim.my_translate); // 初始化动画对象
		initLoginingDlg();
	}

	public void initPop() {
		int width = mUserIdLinearLayout.getWidth() - 4;
		int height = LayoutParams.WRAP_CONTENT;
		mPop = new PopupWindow(mUserIdListView, width, height, true);
		mPop.setOnDismissListener(this);// 设置弹出窗口消失时监听器

		// 注意要加这句代码，点击弹出窗口其它区域才会让窗口消失
		mPop.setBackgroundDrawable(new ColorDrawable(0xffffffff));

	}

	/* 初始化正在登录对话框 */
	private void initLoginingDlg() {

		mLoginingDlg = new Dialog(this, R.style.loginingDlg);
		mLoginingDlg.setContentView(R.layout.logining_dlg);

		Window window = mLoginingDlg.getWindow();
		WindowManager.LayoutParams params = window.getAttributes();
		// 获取和mLoginingDlg关联的当前窗口的属性，从而设置它在屏幕中显示的位置

		// 获取屏幕的高宽
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int cxScreen = dm.widthPixels;
		int cyScreen = dm.heightPixels;

		int height = (int) getResources().getDimension(
				R.dimen.loginingdlg_height);// 高42dp
		int lrMargin = (int) getResources().getDimension(
				R.dimen.loginingdlg_lr_margin); // 左右边沿10dp
		int topMargin = (int) getResources().getDimension(
				R.dimen.loginingdlg_top_margin); // 上沿20dp

		params.y = (-(cyScreen - height) / 2) + topMargin; // -199
		/* 对话框默认位置在屏幕中心,所以x,y表示此控件到"屏幕中心"的偏移量 */

		params.width = cxScreen;
		params.height = height;
		// width,height表示mLoginingDlg的实际大小

		mLoginingDlg.setCanceledOnTouchOutside(true); // 设置点击Dialog外部任意区域关闭Dialog
	}

	/* 显示正在登录对话框 */
	private void showLoginingDlg() {
		if (mLoginingDlg != null)
			mLoginingDlg.show();
	}

	/* 关闭正在登录对话框 */
	private void closeLoginingDlg() {
		if (mLoginingDlg != null && mLoginingDlg.isShowing())
			mLoginingDlg.dismiss();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.login_btnLogin:
			if (!NetworkState.isNetworkConnected(getApplication())) { // 如果设备未连接网络
				Toast.makeText(LoginActivity.this, "未连接网络", Toast.LENGTH_SHORT)
						.show();
			} else {
				// 启动登录
			//	Log.i(TAG, mIdString + "  " + mPwdString);
				if (mIdString == null || mIdString.equals("")) { // 账号为空时
					Toast.makeText(LoginActivity.this, "请输入账号",
							Toast.LENGTH_SHORT).show();
				} else if (mPwdString == null || mPwdString.equals("")) {// 密码为空时
					Toast.makeText(LoginActivity.this, "请输入密码",
							Toast.LENGTH_SHORT).show();
				} else {// 账号和密码都不为空时
					final LoginManager loginManager = new LoginManager();
					loginManager.login(mIdString, mPwdString, new ICallBack() {
						@Override
						public void onSuccess() {
							closeLoginingDlg(); // 关闭对话框
							// 更新用户登录信息列表
							boolean mIsSave = true;
							try {
							//	Log.i(TAG, "保存用户列表");
								for (User user : mUsers) { // 判断本地文档是否有此ID用户
									if (user.getId().equals(mIdString)) {
										mIsSave = false;
										break;
									}
								}
								if (mIsSave) { // 将新用户加入users
									User user = new User(mIdString, mPwdString);
									mUsers.add(user);
								}

							} catch (Exception e) {
								e.printStackTrace();
							}

							String cookieString = loginManager.getCookie();
							mDevicesId = loginManager.getDevicesId(); // 获取的设备id集合
							updateDB(); // 更新数据库记录，如果有新的设备入手，则在表中添加它的记录
							Intent intent = new Intent(LoginActivity.this,
									MainActivity.class);
							intent.putExtra(EXTRA_KEY_COOKIE, cookieString);
						//	Log.i(TAG, cookieString);
							// 启动到应用主页面去
							startActivity(intent);
							finish();
						}

						@Override
						public void onFailed(String error) {
							closeLoginingDlg();
							Toast.makeText(LoginActivity.this, error,
									Toast.LENGTH_LONG).show();
						}

						@Override
						public void onSetLoginUI() {
							showLoginingDlg();
						}

					});
				}
			}
			break;
		case R.id.login_more_user: // 当点击下拉栏
			if (mPop == null) {
				initPop();
			}
			if (!mPop.isShowing() && mUsers.size() > 0) {
				// Log.i(TAG, "切换为角向上图标");
				mMoreUser.setImageResource(R.drawable.login_more_down); // 切换图标
				mPop.showAsDropDown(mUserIdLinearLayout, 2, 1); // 显示弹出窗口
			}
			break;
		case R.id.login_txtForgotPwd: // 点击忘记密码
			Dialog dialog = new Dialog(this);
			dialog.setContentView(R.layout.dialog_forget_pwd);
			;
			dialog.setTitle("忘记密码");
			dialog.show();
		default:
			break;
		}

	}

	private void updateDB() {
		// 更新数据库表
		for (String deviceId : mDevicesId) {
			if (!mDmg.isRecordExist(deviceId)) { // 如果此设备记录不存在
				mDmg.add(deviceId); // 添加此条记录
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		mIdEditText.setText(mUsers.get(position).getId());
		mPwdEditText.setText(mUsers.get(position).getPwd());
		mPop.dismiss();
	}

	/* PopupWindow对象dismiss时的事件 */
	@Override
	public void onDismiss() {
		// Log.i(TAG, "切换为角向下图标");
		mMoreUser.setImageResource(R.drawable.login_more_up);
	}

	/* 退出此Activity时保存users */
	@Override
	public void onPause() {
		super.onPause();
		try {
			Utils.saveUserList(LoginActivity.this, mUsers);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "LoginActivity onDestroy");
		mDmg.destroyDB();
	}

}
