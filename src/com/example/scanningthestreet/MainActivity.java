package com.example.scanningthestreet;


import java.io.File;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.example.logic.ImgFileListActivity;
import com.example.scaningthestreet.util.LocationUtil;
import com.example.scaningthestreet.util.MyLocation;
import com.example.scanningthestreet.struct.AddressDetail;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnGetGeoCoderResultListener, OnClickListener{

	private EditText mEditTextShi;
	private EditText mEditTextXian;
	private EditText mEditTextJieDao;
	private EditText mEditTextMingChen;
	private EditText mEditTextMiaoShu;
	
   
	private TextView mTextViewJinDu;
	private TextView mTextViewWeiDu;
	private TextView mTextViewCoordinateStatus;
    
	private Button mButtonBaoChun;
	private Button mButtonQuXiao;
	
	private ImageButton mImageButtonPaiZhao;

	private LocationUtil locationUtil;
	private MyLocation location = null;
	private MyLocation BaiDuLocation = new MyLocation();
	private GeoCoder mSearch = null; // 搜索模块，也可去掉地图模块独立使用
	
	private AddressDetail mAddressDetail;
	public static final int HANDLE_COORDINATE = 1000;//获取坐标
	public static final int HANDLE_ADDRESS = 1001;//获取地址
	
	public static final int RESULT_PHOTO = 10000;
	private static final int SELECT_PICTURE = 10001;
	
	public static final String SHOW_PICTURE = "com.refeved.monitor.adapter.broadcast.SHOW_PICTURE";
	// 自定义的弹出框类
	private SelectPicPopupWindow menuWindow;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mEditTextShi = (EditText) findViewById(R.id.EditTextShi);
		mEditTextXian =(EditText) findViewById(R.id.EditTextXian);
		mEditTextJieDao = (EditText) findViewById(R.id.EditTextJieDao);
		mEditTextMingChen = (EditText) findViewById(R.id.EditTextMingChen);
		mEditTextMiaoShu = (EditText) findViewById(R.id.EditTextMiaoShu);
		mTextViewJinDu = (TextView) findViewById(R.id.TextViewJinDu);
		mTextViewWeiDu = (TextView) findViewById(R.id.TextViewWeiDu);
		mTextViewCoordinateStatus = (TextView) findViewById(R.id.TextViewCoordinateStatus);
		mButtonBaoChun = (Button) findViewById(R.id.ButtonBaoChun);
		mButtonQuXiao = (Button) findViewById(R.id.ButtonQuXiao);
		mImageButtonPaiZhao = (ImageButton) findViewById(R.id.ImageButtonPaiZhao);
		mImageButtonPaiZhao.setOnClickListener(this);
		// 初始化搜索模块，注册事件监听
		mSearch = GeoCoder.newInstance();
		mSearch.setOnGetGeoCodeResultListener(this);
		locationUtil = new LocationUtil(this);
		if(locationUtil.isEnbled() == true) {
			locationUtil.exec();
			location = locationUtil.getMyLocation();
			if(location.getLatitude() == 0.0 && location.getLongitude() == 0.0){
				mTextViewCoordinateStatus.setText("未能获取位置信息");
			}else{
				mTextViewJinDu.setText(""+location.getLongitude());
				mTextViewWeiDu.setText(""+location.getLatitude());
				LatLng ptCenter = new LatLng(location.getLatitude(), location.getLongitude());
				// 反Geo搜索
				mSearch.reverseGeoCode(new ReverseGeoCodeOption()
						.location(ptCenter));	
			}
		}else{
			mTextViewCoordinateStatus.setText("未能获取位置信息");
		}
		IntentFilter filter = new IntentFilter();
		filter.addAction(SHOW_PICTURE);
		registerReceiver(receiver, filter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onGetGeoCodeResult(GeoCodeResult result) {
		// TODO Auto-generated method stub
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			Toast.makeText(MainActivity.this, "抱歉，未能找到结果", Toast.LENGTH_LONG)
					.show();
		}
		String strInfo = String.format("纬度:%f 经度:%f",
				result.getLocation().latitude, result.getLocation().longitude);
		if(BaiDuLocation != null){
			BaiDuLocation.setLatitude(result.getLocation().latitude);
			BaiDuLocation.setLongitude(result.getLocation().longitude);
			Message message = new Message();
			message.what = HANDLE_COORDINATE;
			mHandler.sendMessage(message);
		}

	}

	@Override
	public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			Toast.makeText(MainActivity.this, "抱歉，未能找到结果", Toast.LENGTH_LONG)
					.show();
		}
		Message message = new Message();
		message.what = HANDLE_ADDRESS;
		message.obj = result.getAddress();
		mAddressDetail = new AddressDetail();
		mAddressDetail.city = result.getAddressDetail().city;
		mAddressDetail.district = result.getAddressDetail().district;
		mAddressDetail.province = result.getAddressDetail().province;
		mAddressDetail.street = result.getAddressDetail().street;
		mAddressDetail.streetNumber = result.getAddressDetail().streetNumber;
		mHandler.sendMessage(message);
	}
	@SuppressLint("HandlerLeak")
	public Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HANDLE_ADDRESS:
				if(msg.obj != null){
					if(msg.obj.equals("")){
						mTextViewCoordinateStatus.setText("未能获取位置信息");	
					}else{
						mTextViewCoordinateStatus.setText((String) msg.obj);	
						
						mEditTextShi.setText(mAddressDetail.city);
						mEditTextXian.setText(mAddressDetail.district);;
						mEditTextJieDao.setText(mAddressDetail.street+mAddressDetail.streetNumber);;
					}
				}else{
					mTextViewCoordinateStatus.setText("未能获取位置信息");
				}
				break;
			case HANDLE_COORDINATE:
				mTextViewJinDu.setText(""+BaiDuLocation.getLongitude());
				mTextViewWeiDu.setText(""+BaiDuLocation.getLatitude());
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		// 实例化SelectPicPopupWindow
		menuWindow = new SelectPicPopupWindow(MainActivity.this, itemsOnClick);
		// 显示窗口
		menuWindow.showAtLocation(MainActivity.this
				.findViewById(R.id.LinearLayoutMainActivity), Gravity.BOTTOM
				| Gravity.CENTER_HORIZONTAL, 0, 0); // 设置layout在PopupWindow中显示的位置
	}
	// 为弹出窗口实现监听类
	private OnClickListener itemsOnClick = new OnClickListener() {

		public void onClick(View v) {
			menuWindow.dismiss();
			switch (v.getId()) {
			case R.id.btn_take_photo:
				try {
					Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					File out = new File(Environment.getExternalStorageDirectory(),"camera.jpg");

					Uri uri = Uri.fromFile(out);

					intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

					startActivityForResult(intent, RESULT_PHOTO);

				} catch (Exception e) {
					e.printStackTrace();
				}

				break;
			case R.id.btn_pick_photo:
				showPictureContent();
				break;
			default:
				break;
			}
		}
	};
	private void showPictureContent() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(this, ImgFileListActivity.class);
		startActivity(intent);
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == SELECT_PICTURE) {
			if (resultCode == RESULT_OK) {
				if (data != null) {
				}
			}

		} else if (requestCode == RESULT_PHOTO) {
			String pathString = Environment.getExternalStorageDirectory().toString() + "/camera.jpg";
		}
	}
	public BroadcastReceiver receiver = new BroadcastReceiver() {
		@SuppressLint("SimpleDateFormat")
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (intent.getAction().equals(SHOW_PICTURE)) {
				Bundle bundle = intent.getExtras();
				if (bundle.getStringArrayList("files") != null) {
				}
			}
		}
	};
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		unregisterReceiver(receiver);
		super.onDestroy();
	}

}
