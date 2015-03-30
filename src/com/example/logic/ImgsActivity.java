package com.example.logic;

import java.util.ArrayList;

import com.example.logic.ImgsAdapter.OnItemClickClass;
import com.example.scanningthestreet.MainActivity;
import com.example.scanningthestreet.R;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class ImgsActivity extends Activity {

	Bundle bundle;
	FileTraversal fileTraversal;
	GridView imgGridView;
	ImgsAdapter imgsAdapter;
	LinearLayout select_layout;
	Util util;
	RelativeLayout relativeLayout2;
	Button choise_button;
	ArrayList<String> filelist;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.photogrally);
		
		imgGridView=(GridView) findViewById(R.id.gridView1);
		bundle= getIntent().getExtras();
		fileTraversal=bundle.getParcelable("data");
		imgsAdapter=new ImgsAdapter(this, fileTraversal.filecontent,onItemClickClass);
		imgGridView.setAdapter(imgsAdapter);
		filelist=new ArrayList<String>();
//		imgGridView.setOnItemClickListener(this);
		util=new Util(this);
	}
	
	class BottomImgIcon implements OnItemClickListener{
		
		int index;
		public BottomImgIcon(int index) {
			this.index=index;
		}
		
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			
		}
	}
	
	
	ImgCallBack imgCallBack=new ImgCallBack() {
		@Override
		public void resultImgCall(ImageView imageView, Bitmap bitmap) {
			imageView.setImageBitmap(bitmap);
		}
	};
	
	ImgsAdapter.OnItemClickClass onItemClickClass=new OnItemClickClass() {
		@Override
		public void OnItemClick(View v, int Position) {
			String filapath=fileTraversal.filecontent.get(Position);
			Log.i("img", "img choise position->"+Position);
			filelist.add(filapath);
			sendfiles();
		}
	};
	
	public void tobreak(View view){
		Intent intent = new Intent();
		intent.setClass(ImgsActivity.this, ImgFileListActivity.class);
		startActivity(intent);
		this.finish();
	}
	
	/**
	 * FIXME
	 * 亲只需要在这个方法把选中的文档目录已list的形式传过去即可
	 * @param view
	 */
	public void sendfiles(){
		Intent intent = new Intent(MainActivity.SHOW_PICTURE);
		Bundle bundle=new Bundle();
		bundle.putStringArrayList("files", filelist);
		intent.putExtras(bundle);
		sendBroadcast(intent);
		this.finish();
	}
}
