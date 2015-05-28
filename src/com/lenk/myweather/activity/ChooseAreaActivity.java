package com.lenk.myweather.activity;

import java.util.ArrayList;
import java.util.List;

import net.youmi.android.AdManager;
import net.youmi.android.spot.SpotManager;

import com.lenk.myweather.R;
import com.lenk.myweather.db.MyWeatherDB;
import com.lenk.myweather.model.City;
import com.lenk.myweather.model.County;
import com.lenk.myweather.model.Province;
import com.lenk.myweather.service.AutoUpdateService;
import com.lenk.myweather.util.HttpCallbackListener;
import com.lenk.myweather.util.HttpUtil;
import com.lenk.myweather.util.Utility;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseAreaActivity extends Activity {
	public static final int LEVEL_PROVINCE = 0;
	public static final int LEVEL_CITY = 1;
	public static final int LEVEL_COUNTY = 2;
	
	private ProgressDialog progressDialog;
	private TextView titleText;
	private TextView levelText;
	private ListView listview;
	private ArrayAdapter<String> adapter;
	private MyWeatherDB myWeatherDB;
	/**
	 * ʡ/��/�����Ƽ���
	 */
	private List<String> dataList = new ArrayList<String>();
	
	/**
	 * ʡ�б�
	 */
	private List<Province> provinceList;
	/**
	 * ���б�
	 */
	private List<City> cityList;
	/**
	 * ʡ�б�
	 */
	private List<County> countyList;
	/**
	 * ѡ�е�ʡ��
	 */
	private Province selectedProvince;
	/**
	 * ѡ�еĳ���
	 */
	private City selectedCity;
	/**
	 * ѡ�е�����
	 */
	private County selectedCounty;
	/**
	 * ��ǰѡ�еļ���
	 */
	private int currentLevel;
	/**
	 * �Ƿ��weatherActivity����ת����
	 */
	private boolean isFromWeatherActivity;
	String appId = "6b2b30b94be72ae4";
	String appSecret = "f02a1c93c1a0f673";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Intent i = new Intent(ChooseAreaActivity.this, AutoUpdateService.class);
				startService(i); 
				AdManager.getInstance(ChooseAreaActivity.this).init(appId,appSecret, false);
				SpotManager.getInstance(ChooseAreaActivity.this).loadSpotAds();
				SpotManager.getInstance(ChooseAreaActivity.this).setSpotOrientation(
						SpotManager.ORIENTATION_PORTRAIT);//������ʾ
			}
		}).start();
		
		isFromWeatherActivity = getIntent().getBooleanExtra("from_weather_activity", false);
		SharedPreferences prefs = getSharedPreferences("data",
				MODE_PRIVATE);
		//-------------------city_selected������----------------
		// �Ѿ�ѡ���˳����Ҳ��Ǵ�WeatherActivity��ת�������Ż�ֱ����ת��WeatherActivity
		Log.i("Log", "city_selected:"+prefs.getBoolean("city_selected", false));
		Log.i("Log", "isFromWeatherActivity:"+isFromWeatherActivity);
		//-------------���׹��
		
		if(prefs.getBoolean("city_selected", false) && !isFromWeatherActivity){
			Intent intent = new Intent(this,WeatherActivity.class);
			startActivity(intent);
			finish();
			return;
		}
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_choose_area);
		listview = (ListView) findViewById(R.id.list_view);
		titleText = (TextView) findViewById(R.id.title_text);
		levelText = (TextView) findViewById(R.id.level_text);
		adapter = new ArrayAdapter<String>(this, R.layout.choose_area_item,dataList);
		listview.setAdapter(adapter);
		myWeatherDB  = myWeatherDB.getInstance(this);
		levelText.setText("3");
		listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				if(currentLevel == LEVEL_PROVINCE){
					levelText.setText("2");
					selectedProvince = provinceList.get(position);
					queryCities();
				}else if(currentLevel == LEVEL_CITY){
					levelText.setText("1");
					selectedCity = cityList.get(position);
					queryCounties();
				}else if(currentLevel == LEVEL_COUNTY){
					String countyCode = countyList.get(position).getCountyCode();
					Intent intent = new Intent(ChooseAreaActivity.this,WeatherActivity.class);
					intent.putExtra("county_code", countyCode);
					startActivity(intent);
					finish();
				}
			}
		});
		queryProvince();//#1 ��һ������,��ѯ��Ϣ
	}
	/**
	* ��ѯ���е�ʡ,���ȴ����ݿ��ѯ,���û�в�ѯ����ȥ�������ϲ�ѯ.
	*/
	public void queryProvince(){
		provinceList = myWeatherDB.loadProvince();
		if(provinceList.size() > 0){
			dataList.clear();//#6 ���ʡ��������Ϣ
			for (Province p : provinceList) {
				dataList.add(p.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			listview.setSelection(0);
			titleText.setText("�й�");
			currentLevel = LEVEL_PROVINCE;//#7 ��ȫ��ʾʡ����Ϣ
			
		}else{
			queryFromServer(null,"province");//#2 ��һ������������,���������ѯʡ����Ϣ
		}
	}
	
	/**
	* ��ѯѡ��ʡ�����е���,���ȴ����ݿ��ѯ,���û�в�ѯ����ȥ�������ϲ�ѯ.
	*/
	private void queryCities() {
		Log.i("Log", "��ʼ��ѯ����");
		cityList = myWeatherDB.loadCity(selectedProvince.getId());
		if(cityList.size()>0){
			dataList.clear();
			for (City c : cityList) {
				dataList.add(c.getCityName());
			}
			adapter.notifyDataSetChanged();
			listview.setSelection(0);
			titleText.setText(selectedProvince.getProvinceName());
			currentLevel = LEVEL_CITY;
		}else{
			queryFromServer(selectedProvince.getProvinceCode(), "city");
		}
		
	}
	
	/**
	* ��ѯѡ���������е���,���ȴ����ݿ��ѯ,���û�в�ѯ����ȥ�������ϲ�ѯ.
	*/
	private void queryCounties() {
		countyList = myWeatherDB.loadCounty(selectedCity.getId());
		if(countyList.size()>0){
			dataList.clear();
			for (County c : countyList) {
				dataList.add(c.getCountyName());
			}
			adapter.notifyDataSetChanged();
			listview.setSelection(0);
			titleText.setText(selectedCity.getCityName());
			currentLevel = LEVEL_COUNTY;
		}else{
			queryFromServer(selectedCity.getCityCode(), "county");
		}
	}
	
	/**
	* ���ݴ���Ĵ��ź����ʹӷ������ϲ�ѯʡ��������.
	*/
	private void queryFromServer(final String code, final String type) {
		String address;
		if(!TextUtils.isEmpty(code)){
			address = "http://www.weather.com.cn/data/list3/city" + code + ".xml";
		}else{
			address = "http://www.weather.com.cn/data/list3/city.xml";//#3 ��ѯ����ʡ����Ϣ 
		}
		showProgressDialog();
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {
				// TODO Auto-generated method stub
				boolean result = false;
				if("province".equals(type)){
					result = Utility.handleProvincesResponse(myWeatherDB, response);//#4 ��һ������,����������,�����ݿ��б���ʡ����Ϣ
				}else if("city".equals(type)){
					result = Utility.handleCitiesResponse(myWeatherDB, response, selectedProvince.getId());
					
				}else if("county".equals(type)){
					result = Utility.handleCountiesResponse(myWeatherDB, response, selectedCity.getId());
					
				}
				if(result){
					// ͨ��runOnUiThread()�����ص����̴߳����߼�
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							closeProgressDialog();
							if("province".equals(type)){
								queryProvince();//#5  ��һ��������ʾʡ����Ϣ
							}else if("city".equals(type)){
								queryCities();
							}else if("county".equals(type)){
								queryCounties();
							}
						}
					});
				}
			}
			
			@Override
			public void onError(Exception e) {
				// TODO Auto-generated method stub
				// ͨ��runOnUiThread()�����ص����̴߳����߼�
				runOnUiThread(new Runnable() {
					public void run() {
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "����ʧ��,������������",Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
		
	}
	/**
	* ��ʾ���ȶԻ���
	*/
	private void showProgressDialog() {
		if(progressDialog==null){
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("���ڼ���...");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}
	/**
	* �رս��ȶԻ���
	*/
	private void closeProgressDialog() {
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
	}
	/**
	* ����Back����,���ݵ�ǰ�ļ������ж�,��ʱӦ�÷������б�ʡ�б�����ֱ���˳�.
	*/
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if(currentLevel == LEVEL_COUNTY){
			queryCities();
			levelText.setText("2");
		}else if(currentLevel == LEVEL_CITY){
			queryProvince();
			levelText.setText("3");
		}else{
			if(isFromWeatherActivity){
				Intent intent = new Intent(this,WeatherActivity.class);
				startActivity(intent);
			}
			finish();
		}
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.choose_area, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		
		return super.onOptionsItemSelected(item);
	}
}
