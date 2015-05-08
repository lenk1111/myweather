package com.myweather.app.util;

import com.myweather.app.db.MyWeatherDB;
import com.myweather.app.model.City;
import com.myweather.app.model.County;
import com.myweather.app.model.Province;

import android.text.TextUtils;

public class Utility {
	/**
	* �����ʹ������������ص�ʡ������
	*/
	public synchronized static boolean handleProvincesResponse(MyWeatherDB
			myWeatherDB, String response) {
		if(!TextUtils.isEmpty(response)){
			String[] allProvinces = response.split(",");
			if(allProvinces !=null && allProvinces.length>0){
				for (String p: allProvinces) {
					String[] array = p.split("\\|");
					Province province = new Province();
					province.setProvinceCode(array[0]);
					province.setProvinceName(array[1]);
					//���ӷ�������������ʡ����Ϣ���浽���ݿ���
					myWeatherDB.saveProvince(province);
				}
				return true;
			}
		}
		return false;
	}
	
	public static boolean handleCitiesResponse(MyWeatherDB	myWeatherDB,String response, int provinceId) {
		if(!TextUtils.isEmpty(response)){
			String[] allProvinces = response.split(",");
			if(allProvinces !=null && allProvinces.length>0){
				for (String p: allProvinces) {
					String[] array = p.split("\\|");
					City city = new City();
					city.setCityCode(array[0]);
					city.setCityName(array[1]);
					city.setProvinceId(provinceId);
					//���ӷ�������������ʡ����Ϣ���浽���ݿ���
					myWeatherDB.saveCity(city);
				}
				return true;
			}
		}
		return false;
	}
	
	/**
	* �����ʹ������������ص��ؼ�����
	*/
	public static boolean handleCountiesResponse(MyWeatherDB myWeatherDB,String response, int cityId) {
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