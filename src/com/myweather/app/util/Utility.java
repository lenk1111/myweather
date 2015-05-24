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
	 * �������������ص�JSON���ݣ����������������ݴ洢�����ء�
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
	 * �����������ص�����������Ϣ�洢��SharedPreferences�ļ��С�
	 */
	public static void saveWeatherInfo(Context context, String cityName,
			String weatherCode, String temp1, String temp2, String weatherDesp,
			String publishTime) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy��M��d��",Locale.CHINA);
		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
		//��ʾ �Ѳ�ѯ�����������,�������ʱֱ�ӽ��������������
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
	 * ������̨���·���
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
	 * �������Ƶ�ʵ�dialog
	 * @param context 
	 * @param map loadRefersh������õ�map
	 * @return
	 */
	public static AlertDialog getSetTimeDialog(final Context context,Map<String, Object> map){
		
		LayoutInflater inflater = ((SettingActivity)context).getLayoutInflater();
		View layout = inflater.inflate(R.layout.dialog_layout, null);
		final EditText etTime = (EditText) layout.findViewById(R.id.et_time);
		final AlertDialog dialog = new AlertDialog.Builder(context).create();
		dialog.setIcon(R.drawable.ic_launcher);
		dialog.setTitle("Ƶ������");
		dialog.setCancelable(false);
		dialog.setView(layout);
		dialog.setButton(DialogInterface.BUTTON_POSITIVE, "ȷ��",new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				try {
					int time = Integer.parseInt(etTime.getText().toString());
					time = time  * 60 * 60 * 1000;
					if(time>0){
						Utility.RefershSetting(context, null, time);//����Ƶ��
						//��������
						Intent intent = new Intent(context,AutoUpdateService.class);
						context.stopService(intent);
						Utility.startService(context);
					}
				} catch (Exception e) {
					// TODO: handle exception
					Toast.makeText(context, "ֵӦΪ����", Toast.LENGTH_SHORT).show();
				}
			}
		});
		dialog.setButton(DialogInterface.BUTTON_NEUTRAL, "ȡ��",new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				
			}
		});
		return dialog;
	}
	/**
	 * ����ʡ����Ϣ �����ʹ�����������ص�ʡ������
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
					// ���ӷ�������������ʡ����Ϣ���浽���ݿ���
					myWeatherDB.saveProvince(province);
				}
				return true;
			}
		}
		return false;
	}

	/**
	 * �����ʹ�����������ص��м�����
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
					// ���ӷ�������������ʡ����Ϣ���浽���ݿ���
					myWeatherDB.saveCity(city);
				}
				return true;
			}
		}
		return false;
	}

	/**
	 * �����ʹ�����������ص��ؼ�����
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
					// ���������������ݴ洢��County��
					myWeatherDB.saveCounty(county);
				}
				return true;
			}
		}
		return false;
	}

}
