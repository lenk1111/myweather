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
	 * �������������ص�JSON���ݣ����������������ݴ洢�����ء�
	 * @param context
	 * @param response ���������ص�����
	 * @param weatherCode ����ѯ������ʱ,Ҫ�������������
	 */
	public static void handleWeatherResponse(Context context, String response,String weatherCode) {
		try {
			JSONObject jsonObject = new JSONObject(response);
			JSONObject weatherInfo = jsonObject.getJSONObject("weatherinfo");
			String cityName = weatherInfo.getString("city");
			String week = weatherInfo.getString("week");
			String publishTime = weatherInfo.getString("fchh");
			// �¶�temp1~6 ����,����,����,����,����,����,��һ
			String temp1 = weatherInfo.getString("temp1");
			String temp2 = weatherInfo.getString("temp2");
			String temp3 = weatherInfo.getString("temp3");
			String temp4 = weatherInfo.getString("temp4");
			String temp5 = weatherInfo.getString("temp5");
			String temp6 = weatherInfo.getString("temp6");
			// ����
			String weather1 = weatherInfo.getString("weather1");
			String weather2 = weatherInfo.getString("weather2");
			String weather3 = weatherInfo.getString("weather3");
			String weather4 = weatherInfo.getString("weather4");
			// ��������
			String wind1 = weatherInfo.getString("wind1");
			String wind2 = weatherInfo.getString("wind2");
			String wind3 = weatherInfo.getString("wind3");
			String wind4 = weatherInfo.getString("wind4");
			// ���촩��ָ��
			String index_d = weatherInfo.getString("index_d");
			// 48Сʱ����ָ��
			String index48_d = weatherInfo.getString("index48_d");
			// ������ǿ��
			String index_uv = weatherInfo.getString("index_uv");
			// 48Сʱ������ǿ��
			String index48_uv = weatherInfo.getString("index48_uv");
			// ϴ��
			String index_xc = weatherInfo.getString("index_xc");
			// ���ʶ�
			String index_co = weatherInfo.getString("index_co");
			// ����
			String index_cl = weatherInfo.getString("index_cl");
			// ��ɹ
			String index_ls = weatherInfo.getString("index_ls");
			// ����
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
	 * �����������ص�����������Ϣ�洢��SharedPreferences�ļ��С�
	 */
	public static void saveWeatherInfo(Context context, String cityName,
			String week, String publishTime, String temp1, String temp2,
			String temp3, String weather1, String weather2, String weather3,
			String wind1, String wind2, String wind3, String index_d,
			String index_uv, String index_xc, String index_co, String index_cl,
			String index_ag, String index_ls,String weatherCode,String temp4,String weather4) {
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy��M��d��", Locale.CHINA);
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
		// ��ʾ �Ѳ�ѯ�����������,�������ʱֱ�ӽ��������������
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
	 * �����Ƿ�����̨����ͺ�̨����Ƶ�ʵ���Ϣ
	 * @param context
	 * @return
	 */
	public static Map<String, Object> loadRefershSetting(Context context) {
		Map<String, Object> map = new HashMap<String, Object>();
		SharedPreferences spfs = context.getSharedPreferences("data",
				context.MODE_PRIVATE);
		String tempRefersh = spfs.getString("isRefersh", null);
		int tempTime = spfs.getInt("time", 0);
		if (tempRefersh == null && tempTime == 0) {//���û�б����,������ΪĬ��Ƶ��
			Editor editor = spfs.edit();
			editor.putString("isRefersh", "open");
			editor.putInt("time", (8 * 60 * 60 * 1000));
			editor.commit();
			map.put("isRefersh", "open");
			map.put("time", (8 * 60 * 60 * 1000));
		} else {//����Ѿ������������
			map.put("isRefersh", tempRefersh);
			map.put("time", tempTime);
		}
		return map;
	}
	/**
	 * �����Ƿ�����̨�������̨���µ�Ƶ��
	 * @param context
	 * @param isOpen �Ƿ�����̨����
	 * @param time ��̨����Ƶ��
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
	 * ������̨���·���
	 * 
	 * @param context
	 */
	public static void startService(Context context) {
		Map<String, Object> map = Utility.loadRefershSetting(context);
		if (map.get("isRefersh").toString().equals("open")) {//���������Ϣxml�д洢��service��ϢΪopen��������̨����
			if (Integer.parseInt(map.get("time").toString()) > 0) {
				AutoUpdateService.anHour = Integer.parseInt(map.get("time")
						.toString());
			}
			Intent intent = new Intent(context, AutoUpdateService.class);
			context.startService(intent);
		}
	}

	/**
	 * �������Ƶ�ʵ�dialog
	 * 
	 * @param context
	 * @param map
	 *            loadRefersh������õ�map
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
		dialog.setTitle("Ƶ������");
		dialog.setCancelable(false);
		dialog.setView(layout);
		dialog.setButton(DialogInterface.BUTTON_POSITIVE, "ȷ��",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						try {
							int time = Integer.parseInt(etTime.getText()
									.toString());
							time = time * 60 * 60 * 1000;
							if (time > 0) {
								Utility.RefershSetting(context, null, time);// ����Ƶ��
								// ��������
								Intent intent = new Intent(context,
										AutoUpdateService.class);
								context.stopService(intent);
								Utility.startService(context);
								Toast.makeText(context, "���óɹ�", Toast.LENGTH_SHORT)
								.show();
							}
						} catch (Exception e) {
							// TODO: handle exception
							Toast.makeText(context, "ֵӦΪ����", Toast.LENGTH_SHORT)
									.show();
						}
					}
				});
		dialog.setButton(DialogInterface.BUTTON_NEUTRAL, "ȡ��",
				new DialogInterface.OnClickListener() {

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
