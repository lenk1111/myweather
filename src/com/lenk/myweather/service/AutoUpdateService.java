package com.lenk.myweather.service;

import com.lenk.myweather.receiver.AutoUpdateReceiver;
import com.lenk.myweather.util.HttpCallbackListener;
import com.lenk.myweather.util.HttpUtil;
import com.lenk.myweather.util.Utility;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class AutoUpdateService extends Service {
	public static int anHour = 8 * 60 * 60 * 1000; 
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Log.d("Log", "��������");
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				updateWeatherInfo();
			}
		}).start();
		//��ʱִ��
		AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		//int anHour = 3 * 1000; 
		long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
		Intent i = new Intent(this,AutoUpdateReceiver.class);
		PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
		manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
		
		return super.onStartCommand(intent, flags, startId);
	}
	/**
	 * ����������Ϣ��
	 */
	private void updateWeatherInfo() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String weatherCode = prefs.getString("weather_code", "");
		final String address = "http://m.weather.com.cn/data/"
				+ weatherCode + ".html";
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {
				// TODO Auto-generated method stub
				Utility.handleWeatherResponse(AutoUpdateService.this, response,null);
				Log.d("Log","��̨�Ѹ���");
			}
			
			@Override
			public void onError(Exception e) {
				// TODO Auto-generated method stub
				e.printStackTrace();
			}
		});
	}
}
