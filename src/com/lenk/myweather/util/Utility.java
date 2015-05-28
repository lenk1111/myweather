package com.lenk.myweather.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;
import org.w3c.dom.Text;

import com.lenk.myweather.R;
import com.lenk.myweather.activity.SettingActivity;
import com.lenk.myweather.db.MyWeatherDB;
import com.lenk.myweather.model.City;
import com.lenk.myweather.model.County;
import com.lenk.myweather.model.Province;
import com.lenk.myweather.service.AutoUpdateService;

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
	 * @param context
	 * @param response 服务器返回的数据
	 * @param weatherCode 当查询到天气时,要保存的天气代码
	 */
	public static void handleWeatherResponse(Context context, String response,String weatherCode) {
		try {
			JSONObject jsonObject = new JSONObject(response);
			JSONObject weatherInfo = jsonObject.getJSONObject("weatherinfo");
			String cityName = weatherInfo.getString("city");
			String week = weatherInfo.getString("week");
			String publishTime = weatherInfo.getString("fchh");
			// 温度temp1~6 今天,明天,后天,周五,周六,周日,周一
			String temp1 = weatherInfo.getString("temp1");
			String temp2 = weatherInfo.getString("temp2");
			String temp3 = weatherInfo.getString("temp3");
			String temp4 = weatherInfo.getString("temp4");
			String temp5 = weatherInfo.getString("temp5");
			String temp6 = weatherInfo.getString("temp6");
			// 描述
			String weather1 = weatherInfo.getString("weather1");
			String weather2 = weatherInfo.getString("weather2");
			String weather3 = weatherInfo.getString("weather3");
			String weather4 = weatherInfo.getString("weather4");
			// 风速描述
			String wind1 = weatherInfo.getString("wind1");
			String wind2 = weatherInfo.getString("wind2");
			String wind3 = weatherInfo.getString("wind3");
			String wind4 = weatherInfo.getString("wind4");
			// 今天穿衣指数
			String index_d = weatherInfo.getString("index_d");
			// 48小时穿衣指数
			String index48_d = weatherInfo.getString("index48_d");
			// 紫外线强度
			String index_uv = weatherInfo.getString("index_uv");
			// 48小时紫外线强度
			String index48_uv = weatherInfo.getString("index48_uv");
			// 洗车
			String index_xc = weatherInfo.getString("index_xc");
			// 舒适度
			String index_co = weatherInfo.getString("index_co");
			// 晨练
			String index_cl = weatherInfo.getString("index_cl");
			// 晾晒
			String index_ls = weatherInfo.getString("index_ls");
			// 过敏
			String index_ag = weatherInfo.getString("index_ag");
			
			saveWeatherInfo(context, cityName, week, publishTime, temp1, temp2,
					temp3, weather1, weather2, weather3, wind1, wind2, wind3,
					index_d, index_uv, index_xc, index_co, index_cl, index_ag,
					index_ls,weatherCode,temp4,weather4);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 将服务器返回的所有天气信息存储到SharedPreferences文件中。
	 */
	public static void saveWeatherInfo(Context context, String cityName,
			String week, String publishTime, String temp1, String temp2,
			String temp3, String weather1, String weather2, String weather3,
			String wind1, String wind2, String wind3, String index_d,
			String index_uv, String index_xc, String index_co, String index_cl,
			String index_ag, String index_ls,String weatherCode,String temp4,String weather4) {
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月d日", Locale.CHINA);
		SharedPreferences.Editor editor = context.getSharedPreferences("data",
				context.MODE_PRIVATE).edit();;
		editor.remove("city_selected");
		editor.remove("city_name");
		editor.remove("week");
		editor.remove("temp1");
		editor.remove("temp2");
		editor.remove("temp3");
		editor.remove("temp4");
		editor.remove("wind1");
		editor.remove("wind2");
		editor.remove("wind3");
		editor.remove("weather1");
		editor.remove("weather2");
		editor.remove("weather3");
		editor.remove("weather4");
		editor.remove("wind1");
		editor.remove("wind2");
		editor.remove("wind3");
		editor.remove("index_d");
		editor.remove("index_uv");
		editor.remove("index_xc");
		editor.remove("index_co");
		editor.remove("index_cl");
		editor.remove("index_ag");
		editor.remove("publish_time");
		editor.remove("current_date");
		// 标示 已查询并保存过天气,当打开软件时直接进入天气详情界面
		editor.putBoolean("city_selected", true);
		editor.putString("weather_code", weatherCode);
		
		editor.putString("city_name", cityName);
		editor.putString("week", week);
		editor.putString("temp1", temp1);
		editor.putString("temp2", temp2);
		editor.putString("temp3", temp3);
		editor.putString("temp4", temp4);

		editor.putString("weather1", weather1);
		editor.putString("weather2", weather2);
		editor.putString("weather3", weather3);
		editor.putString("weather4", weather4);
		editor.putString("wind1", wind1);
		editor.putString("wind2", wind2);
		editor.putString("wind3", wind3);
		editor.putString("index_d", index_d);
		editor.putString("index_uv", index_uv);
		editor.putString("index_xc", index_xc);
		editor.putString("index_co", index_co);
		editor.putString("index_cl", index_cl);
		editor.putString("index_ag", index_ag);
		editor.putString("index_ls", index_ls);

		editor.putString("publish_time", publishTime);
		editor.putString("current_date", sdf.format(new Date()));
		editor.commit();
	}
	/**
	 * 加载是否开启后台服务和后台更新频率的信息
	 * @param context
	 * @return
	 */
	public static Map<String, Object> loadRefershSetting(Context context) {
		Map<String, Object> map = new HashMap<String, Object>();
		SharedPreferences spfs = context.getSharedPreferences("data",
				context.MODE_PRIVATE);
		String tempRefersh = spfs.getString("isRefersh", null);
		int tempTime = spfs.getInt("time", 0);
		if (tempRefersh == null && tempTime == 0) {//如果没有保存过,则设置为默认频率
			Editor editor = spfs.edit();
			editor.putString("isRefersh", "open");
			editor.putInt("time", (8 * 60 * 60 * 1000));
			editor.commit();
			map.put("isRefersh", "open");
			map.put("time", (8 * 60 * 60 * 1000));
		} else {//如果已经存在了则加载
			map.put("isRefersh", tempRefersh);
			map.put("time", tempTime);
		}
		return map;
	}
	/**
	 * 保存是否开启后台服务与后台更新的频率
	 * @param context
	 * @param isOpen 是否开启后台服务
	 * @param time 后台更新频率
	 */
	public static void RefershSetting(Context context, String isOpen, int time) {
		boolean rs = false;
		SharedPreferences spfs = context.getSharedPreferences("data",
				context.MODE_PRIVATE);;
		Editor editor = spfs.edit();
		if (isOpen != null) {
			editor.remove("isRefersh");
			editor.putString("isRefersh", isOpen);

		}
		if (time > 0) {
			editor.remove("time");
			editor.putInt("time", time);
		}
		editor.commit();
	}

	/**
	 * 开启后台更新服务
	 * 
	 * @param context
	 */
	public static void startService(Context context) {
		Map<String, Object> map = Utility.loadRefershSetting(context);
		if (map.get("isRefersh").toString().equals("open")) {//如果设置信息xml中存储的service信息为open则启动后台服务
			if (Integer.parseInt(map.get("time").toString()) > 0) {
				AutoUpdateService.anHour = Integer.parseInt(map.get("time")
						.toString());
			}
			Intent intent = new Intent(context, AutoUpdateService.class);
			context.startService(intent);
		}
	}

	/**
	 * 获得设置频率的dialog
	 * 
	 * @param context
	 * @param map
	 *            loadRefersh方法获得的map
	 * @return
	 */
	public static AlertDialog getSetTimeDialog(final Context context,
			Map<String, Object> map) {

		LayoutInflater inflater = ((SettingActivity) context)
				.getLayoutInflater();
		View layout = inflater.inflate(R.layout.dialog_layout, null);
		final EditText etTime = (EditText) layout.findViewById(R.id.et_time);
		final AlertDialog dialog = new AlertDialog.Builder(context).create();
		dialog.setIcon(R.drawable.ic_launcher);
		dialog.setTitle("频率设置");
		dialog.setCancelable(false);
		dialog.setView(layout);
		dialog.setButton(DialogInterface.BUTTON_POSITIVE, "确认",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						try {
							int time = Integer.parseInt(etTime.getText()
									.toString());
							time = time * 60 * 60 * 1000;
							if (time > 0) {
								Utility.RefershSetting(context, null, time);// 更改频率
								// 重启服务
								Intent intent = new Intent(context,
										AutoUpdateService.class);
								context.stopService(intent);
								Utility.startService(context);
								Toast.makeText(context, "设置成功", Toast.LENGTH_SHORT)
								.show();
							}
						} catch (Exception e) {
							// TODO: handle exception
							Toast.makeText(context, "值应为数字", Toast.LENGTH_SHORT)
									.show();
						}
					}
				});
		dialog.setButton(DialogInterface.BUTTON_NEUTRAL, "取消",
				new DialogInterface.OnClickListener() {

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
