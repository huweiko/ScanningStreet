package com.example.scanningthestreet;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
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
import android.util.Log;
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
	private MyLocation myLocation = new MyLocation();
	private MyLocation BaiDuLocation = new MyLocation();
	private GeoCoder mSearch = null; // ����ģ�飬Ҳ��ȥ����ͼģ�����ʹ��
	
	private AddressDetail mAddressDetail;
	public static final int HANDLE_COORDINATE = 1000;//��ȡ����
	public static final int HANDLE_ADDRESS = 1001;//��ȡ��ַ
	public static final int HANDLE_PHONELUJIN = 1002;//��Ƭ·��
	public static final int HANDLE_BAOCUN = 1003;//������Ϣ
	public static final int HANDLE_CANCEL = 1004;//ȡ����Ϣ
	
	public static final int RESULT_PHOTO = 10000;
	private static final int SELECT_PICTURE = 10001;
	
	public static final String SHOW_PICTURE = "com.refeved.monitor.adapter.broadcast.SHOW_PICTURE";
	// �Զ���ĵ�������
	private SelectPicPopupWindow menuWindow;
	private LocationClient mLocClient;
	public MyLocationListenner myListener = new MyLocationListenner();
	//��ǰͼƬ
	private String mPictureName = null;
	//��ǰ��ߵ�ַ
	private String mCurrentAddress = null;
	private void locationInit() {
		// ��λ��ʼ��
		mLocClient = new LocationClient(this);
		mLocClient.registerLocationListener(myListener);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// ��gps
		option.setCoorType("bd09ll"); // ������������
		option.setScanSpan(1000);
		mLocClient.setLocOption(option);
	}
	
	public class MyLocationListenner implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			// map view ���ٺ��ڴ����½��յ�λ��
			if (location != null){
				myLocation.setLatitude(location.getLatitude());
				myLocation.setLongitude(location.getLongitude());
				if(myLocation.getLatitude() == 0.0 && myLocation.getLongitude() == 0.0){
				}else{
					if(BaiDuLocation != null){
						BaiDuLocation.setLatitude(myLocation.getLatitude());
						BaiDuLocation.setLongitude(myLocation.getLongitude());
						Message message = new Message();
						message.what = HANDLE_COORDINATE;
						mHandler.sendMessage(message);
					}
					LatLng ptCenter = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
					// ��Geo����
					mSearch.reverseGeoCode(new ReverseGeoCodeOption()
							.location(ptCenter));	
				}
				Log.d("huwei", "����λ�ø��£�γ�� = " + location.getLatitude()+"������ = "+location.getLongitude());
			}
		}

		public void onReceivePoi(BDLocation poiLocation) {
		}
	}
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
		
		mButtonBaoChun.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Message message = new Message();
				message.what = HANDLE_BAOCUN;
				mHandler.sendMessage(message);
			}
		});
		mButtonQuXiao.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Message message = new Message();
				message.what = HANDLE_CANCEL;
				mHandler.sendMessage(message);
			}
		});
		locationInit();
		// ��ʼ������ģ�飬ע���¼�����
		mSearch = GeoCoder.newInstance();
		mSearch.setOnGetGeoCodeResultListener(this);
		locationUtil = new LocationUtil(this);
		if(!mLocClient.isStarted()){
			mLocClient.start();
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
			Toast.makeText(MainActivity.this, "��Ǹ��δ���ҵ����", Toast.LENGTH_LONG)
					.show();
		}

	}

	@Override
	public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			Toast.makeText(MainActivity.this, "��Ǹ��δ���ҵ����", Toast.LENGTH_LONG)
					.show();
			return;
		}
		mCurrentAddress= result.getAddress();
		
		Message message = new Message();
		message.what = HANDLE_ADDRESS;
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
					mEditTextShi.setText(mAddressDetail.city);
					mEditTextXian.setText(mAddressDetail.district);
					mEditTextJieDao.setText(mAddressDetail.street+mAddressDetail.streetNumber);
				break;
			case HANDLE_COORDINATE:
				mTextViewJinDu.setText(""+BaiDuLocation.getLongitude());
				mTextViewWeiDu.setText(""+BaiDuLocation.getLatitude());
				break;
			case HANDLE_PHONELUJIN:
				if(msg.obj != null){
					if(msg.obj.equals("")){
					}else{
						mTextViewCoordinateStatus.setText((String) msg.obj);	
					}
				}
				break;
			case HANDLE_BAOCUN:
				String mingchen = "���ƣ�"+mEditTextMingChen.getText().toString()+"\n";
				String miaoshu = "������"+mEditTextMiaoShu.getText().toString()+"\n";
				String jingdu = "���ȣ�"+BaiDuLocation.getLongitude()+"\n";
				String weidu = "γ�ȣ�"+BaiDuLocation.getLatitude()+"\n";
				String dizhi = "��ַ��"+mCurrentAddress+"\n";
				String PictureAddress = "";
				if(mCurrentAddress == null){
					PictureAddress = "ͼƬ��ַ����\n**********************\n";
				}else{
					 PictureAddress = "ͼƬ��ַ��"+mPictureName+"\n**********************\n";
				}
				
				writeFile(mingchen+miaoshu+jingdu+weidu+dizhi+PictureAddress);
				Toast.makeText(MainActivity.this, "�ѱ��浽�ֻ�SD����Ŀ¼��ɨ�ּ�¼.txt��", Toast.LENGTH_LONG).show();
				break;
			case HANDLE_CANCEL:
				mTextViewJinDu.setText("");
				mTextViewWeiDu.setText("");
				mEditTextMingChen.setText("");
				mEditTextMiaoShu.setText("");
				mEditTextShi.setText("");
				mEditTextXian.setText("");
				mEditTextJieDao.setText("");
				mTextViewCoordinateStatus.setText("");
				mPictureName = "";
				mCurrentAddress = "";
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};
	public void writeFile(String data){
		
		try {
			File file = new File(Environment.getExternalStorageDirectory().toString(),
					"ɨ�ּ�¼.txt");
			//�ڶ�������������˵�Ƿ���append��ʽ�������
			BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
			bw.write(data);
			bw.flush();
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		// ʵ����SelectPicPopupWindow
		menuWindow = new SelectPicPopupWindow(MainActivity.this, itemsOnClick);
		// ��ʾ����
		menuWindow.showAtLocation(MainActivity.this
				.findViewById(R.id.LinearLayoutMainActivity), Gravity.BOTTOM
				| Gravity.CENTER_HORIZONTAL, 0, 0); // ����layout��PopupWindow����ʾ��λ��
	}
	// Ϊ��������ʵ�ּ�����
	private OnClickListener itemsOnClick = new OnClickListener() {

		public void onClick(View v) {
			menuWindow.dismiss();
			switch (v.getId()) {
			case R.id.btn_take_photo:
				try {
					Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//�������ڸ�ʽ
					mPictureName = "/DCIM/"+df.format(new Date())+".jpg";
					
					File out = new File(Environment.getExternalStorageDirectory(),mPictureName);

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
//����
		} else if (requestCode == RESULT_PHOTO) {
			mPictureName = Environment.getExternalStorageDirectory().toString() + mPictureName;
			Message message = new Message();
			message.what = HANDLE_PHONELUJIN;
			message.obj = mPictureName;
			mHandler.sendMessage(message);
		}
	}
	public BroadcastReceiver receiver = new BroadcastReceiver() {
		@SuppressLint("SimpleDateFormat")
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			//�����ѡȡ
			if (intent.getAction().equals(SHOW_PICTURE)) {
				Bundle bundle = intent.getExtras();
				if (bundle.getStringArrayList("files") != null) {
					mPictureName = bundle.getStringArrayList("files").get(0);
					Message message = new Message();
					message.what = HANDLE_PHONELUJIN;
					message.obj = mPictureName;
					mHandler.sendMessage(message);
				}
			}
		}
	};
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		unregisterReceiver(receiver);
		if(mLocClient.isStarted()){
			mLocClient.stop();
		}
		mLocClient.unRegisterLocationListener(myListener);
		super.onDestroy();
	}

}
