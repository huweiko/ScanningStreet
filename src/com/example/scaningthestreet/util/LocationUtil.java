package com.example.scaningthestreet.util;

import java.util.List;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

public class LocationUtil {
	private Context mContext;

	private LocationManager locationManager;
	private String provider;
	private String provider_gps_network;
	private Location location = null;
	private MyLocation myLocation = null;

	public LocationUtil(Context mContext) {
		super();
		this.mContext = mContext;
		locationManager = (LocationManager) mContext
				.getSystemService(Context.LOCATION_SERVICE);
		myLocation = new MyLocation();
	}

	public boolean isEnbled() {
		if (hasGPSDevice(mContext)) {
			// 如果GPS关，则打�?
			if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) == false)
				OnOffGps();
			provider_gps_network = LocationManager.GPS_PROVIDER;
			return true;
		} else {
			Toast.makeText(mContext, "no GPS", Toast.LENGTH_SHORT).show();
			if (isNetworkAvailable(mContext) == false) {
				Toast.makeText(mContext, "no network", Toast.LENGTH_SHORT)
						.show();
				return false;
			} else {
				provider_gps_network = LocationManager.NETWORK_PROVIDER;
				return true;
			}
		}
	}

	public void exec() {
		// 通过GPS获取当前坐标
		// 获取经纬�?
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			// 设置服务商的信息
			Criteria criteria = new Criteria();
			// 提供服务的精度标�?
			criteria.setAccuracy(Criteria.ACCURACY_FINE);
			// 不需要高度信�?
			criteria.setAltitudeRequired(false);
			// 不需要方位信�?
			criteria.setBearingRequired(false);
			// 不允许产生费�?
			criteria.setCostAllowed(false);
			// 消�?�电力为�?
			criteria.setPowerRequirement(Criteria.POWER_LOW);
			// 取得�?匹配的criteria
			provider = locationManager.getBestProvider(criteria, true);

			location = locationManager.getLastKnownLocation(provider);
			if (location != null) {
				setMyLocation();
			} else {
				if (provider_gps_network == LocationManager.GPS_PROVIDER)
					GpsForArea();
				else if (provider_gps_network == LocationManager.NETWORK_PROVIDER)
					NetWorkForArea();
			}
		} else {// 通过网络获取当前坐标
			NetWorkForArea();
		}
	}

	// 判断是否有可用网�?
	public boolean isNetworkAvailable(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm == null) {
		} else { // 如果仅仅是用来判断网络连�? �?�?�?�?�?�? //则可以使�?
					// cm.getActiveNetworkInfo().isAvailable();
			NetworkInfo[] info = cm.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		}
		return false;
	}

	// 判断是否有GPS设备
	public boolean hasGPSDevice(Context context) {
		final LocationManager mgr = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);
		if (mgr == null)
			return false;
		final List<String> providers = mgr.getAllProviders();
		if (providers == null)
			return false;
		return providers.contains(LocationManager.GPS_PROVIDER);
	}

	// �?关GPS
	public void OnOffGps() {
		Intent gpsIntent = new Intent();
		gpsIntent.setClassName("com.android.settings",
				"com.android.settings.widget.SettingsAppWidgetProvider");
		gpsIntent.addCategory("android.intent.category.ALTERNATIVE");
		gpsIntent.setData(Uri.parse("custom:3"));
		try {
			PendingIntent.getBroadcast(mContext, 0, gpsIntent, 0).send();
		} catch (CanceledException e) {
			e.printStackTrace();
		}
	}

	// 通过网络获取当前位置坐标
	public void NetWorkForArea() {
		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);
		location = locationManager
				.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		if (location != null) {
			setMyLocation();
		}
	}

	// 通过GPS获取当前位置坐标
	public void GpsForArea() {
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				1000, 0, locationListener);
		location = locationManager
				.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (location != null) {
			setMyLocation();
		}
	}

	private final LocationListener locationListener = new LocationListener() {
		public void onLocationChanged(Location location) { // 当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触�?
			if (location != null) {
				setMyLocation();
			}
		}

		public void onProviderDisabled(String provider) {
			// Provider被disable时触发此函数，比如GPS被关�?
		}

		public void onProviderEnabled(String provider) {
			// Provider被enable时触发此函数，比如GPS被打�?
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
			// Provider的转态在可用、暂时不可用和无服务三个状�?�直接切换时触发此函�?
		}
	};

	private void setMyLocation() {
		myLocation.setLatitude(location.getLatitude());
		myLocation.setLongitude(location.getLongitude());
	}

	public MyLocation getMyLocation() {
		return myLocation;
	}

}
