package com.myweather.app.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;
import org.w3c.dom.Text;

import com.myweather.app.R;
import com.myweather.app.activity.SettingActivity;
import com.myweather.app.db.MyWeatherDB;
import com.myweather.app.model.City;
import com.myweather.app.model.County;
import com.myweather.app.model.Province;
import com.myweather.app.service.AutoUpdateService;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Utility {
	/**
	 * 解析服务器返回的JSON数据，并将解析出的数据存储到本地。
	 */
	public static void handleWeatherResponse(Context context, String response) {
		try {
			JSONObject jsonObject = new JSONObject(response);
			JSONObject weatherInfo = jsonObject.getJSONObject("weatherinfo");
			String cityName = weatherInfo.getString("city");
			String weatherCode = weatherInfo.getString("cityid");
			String temp1 = weatherInfo.getString("temp1");
			String temp2 = weatherInfo.getString("temp2");
			String weatherDesp = weatherInfo.getString("weather");
			String publishTime = weatherInfo.getString("ptime");
			saveWeatherInfo(context, cityName, weatherCode, temp1, temp2,
					weatherDesp, publishTime);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static Map<String,Object> loadRefershSetting(Context context){
		Map<String, Object> map = new HashMap<String, Object>();
		SharedPreferences spfs = context.getSharedPreferences("refershSetting", context.MODE_PRIVATE);
		String tempRefersh = spfs.getString("isRefersh", null);
		int tempTime = spfs.getInt("time", 0);
		if(tempRefersh==null && tempTime==0){
			Editor editor = spfs.edit();
			editor.putString("isRefersh", "open");
			editor.putInt("time", (8 * 60 * 60 * 1000));
			editor.commit();
			map.put("isRefersh", "open");
			map.put("time", (8 * 60 * 60 * 1000));
		}else{
			map.put("isRefersh", tempRefersh);
			map.put("time", tempTime);
		}
		return map;
	}
	public static void RefershSetting(Context context,String isOpen,int time){
		boolean rs = false;
		SharedPreferences spfs = context.getSharedPreferences("refershSetting", context.MODE_PRIVATE);
		Editor editor = spfs.edit();
		if(isOpen!=null){
			editor.remove("isRefersh");
			editor.putString("isRefersh",isOpen);
			
		}
		if(time>0){
			editor.remove("time");
			editor.putInt("time", time);
		}
		editor.commit();
	}
	/**
	 * 将服务器返回的所有天气信息存储到SharedPreferences文件中。
	 */
	public static void saveWeatherInfo(Context context, String cityName,
			String weatherCode, String temp1, String temp2, String weatherDesp,
			String publishTime) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月d日",Locale.CHINA);
		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
		//标示 已查询并保存过天气,当打开软件时直接进入天气详情界面
		editor.putBoolean("city_selected", true);
		
		editor.putString("city_name", cityName);
		editor.putString("weather_code", weatherCode);
		editor.putString("temp1", temp1);
		editor.putString("temp2", temp2);
		editor.putString("weather_desp", weatherDesp);
		editor.putString("publish_time", publishTime);
		editor.putString("current_date", sdf.format(new Date()));
		editor.commit();
	}
	/**
	 * 开启后台更新服务
	 * @param context
	 */
	public static void startService(Context context){
		Map<String, Object> map = Utility.loadRefershSetting(context);
		if(map.get("isRefersh").toString().equals("open")){
			if(Integer.parseInt(map.get("time").toString())>0){
				AutoUpdateService.anHour = Integer.parseInt(map.get("time").toString());
			}
			Intent intent = new Intent(context, AutoUpdateService.class);
			context.startService(intent);
		}
	}
	/**
	 * 获得设置频率的dialog
	 * @param context 
	 * @param map loadRefersh方法获得的map
	 * @return
	 */
	public static AlertDialog getSetTimeDialog(final Context context,Map<String, Object> map){
		
		LayoutInflater inflater = ((SettingActivity)context).getLayoutInflater();
		View layout = inflater.inflate(R.layout.dialog_layout, null);
		final EditText etTime = (EditText) layout.findViewById(R.id.et_time);
		final AlertDialog dialog = new AlertDialog.Builder(context).create();
		dialog.setIcon(R.drawable.ic_launcher);
		dialog.setTitle("频率设置");
		dialog.setCancelable(false);
		dialog.setView(layout);
		dialog.setButton(DialogInterface.BUTTON_POSITIVE, "确认",new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				try {
					int time = Integer.parseInt(etTime.getText().toString());
					time = time  * 60 * 60 * 1000;
					if(time>0){
						Utility.RefershSetting(context, null, time);//更改频率
						//重启服务
						Intent intent = new Intent(context,AutoUpdateService.class);
						context.stopService(intent);
						Utility.startService(context);
					}
				} catch (Exception e) {
					// TODO: handle exception
					Toast.makeText(context, "值应为数字", Toast.LENGTH_SHORT).show();
				}
			}
		});
		dialog.setButton(DialogInterface.BUTTON_NEUTRAL, "取消",new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				
			}
		});
		return dialog;
	}
	/**
	 * 保存省份信息 解析和处理服务器返回的省级数据
	 */
	public synchronized static boolean handleProvincesResponse(
			MyWeatherDB myWeatherDB, String response) {
		if (!TextUtils.isEmpty(response)) {
			String[] allProvinces = response.split(",");
			if (allProvinces != null && allProvinces.length > 0) {
				for (String p : allProvinces) {
					String[] array = p.split("\\|");
					Province province = new Province();
					province.setProvinceCode(array[0]);
					province.setProvinceName(array[1]);
					// 将从服务器解析出的省份信息保存到数据库中
					myWeatherDB.saveProvince(province);
				}
				return true;
			}
		}
		return false;
	}

	/**
	 * 解析和处理服务器返回的市级数据
	 */
	public static boolean handleCitiesResponse(MyWeatherDB myWeatherDB,
			String response, int provinceId) {
		if (!TextUtils.isEmpty(response)) {
			String[] allProvinces = response.split(",");
			if (allProvinces != null && allProvinces.length > 0) {
				for (String p : allProvinces) {
					String[] array = p.split("\\|");
					City city = new City();
					city.setCityCode(array[0]);
					city.setCityName(array[1]);
					city.setProvinceId(provinceId);
					// 将从服务器解析出的省份信息保存到数据库中
					myWeatherDB.saveCity(city);
				}
				return true;
			}
		}
		return false;
	}

	/**
	 * 解析和处理服务器返回的县级数据
	 */
	public static boolean handleCountiesResponse(MyWeatherDB myWeatherDB,
			String response, int cityId) {
		if (!TextUtils.isEmpty(response)) {
			String[] allCounties = response.split(",");
			if (allCounties != null && allCounties.length > 0) {
				for (String c : allCounties) {
					String[] array = c.split("\\|");
					County county = new County();
					county.setCountyCode(array[0]);
					county.setCountyName(array[1]);
					county.setCityId(cityId);
					// 将解析出来的数据存储到County表
					myWeatherDB.saveCounty(county);
				}
				return true;
			}
		}
		return false;
	}

}
