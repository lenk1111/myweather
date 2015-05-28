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
	 * 省/市/县名称集合
	 */
	private List<String> dataList = new ArrayList<String>();
	
	/**
	 * 省列表
	 */
	private List<Province> provinceList;
	/**
	 * 市列表
	 */
	private List<City> cityList;
	/**
	 * 省列表
	 */
	private List<County> countyList;
	/**
	 * 选中的省份
	 */
	private Province selectedProvince;
	/**
	 * 选中的城市
	 */
	private City selectedCity;
	/**
	 * 选中的区县
	 */
	private County selectedCounty;
	/**
	 * 当前选中的级别
	 */
	private int currentLevel;
	/**
	 * 是否从weatherActivity中跳转过来
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
						SpotManager.ORIENTATION_PORTRAIT);//竖屏显示
			}
		}).start();
		
		isFromWeatherActivity = getIntent().getBooleanExtra("from_weather_activity", false);
		SharedPreferences prefs = getSharedPreferences("data",
				MODE_PRIVATE);
		//-------------------city_selected有疑问----------------
		// 已经选择了城市且不是从WeatherActivity跳转过来，才会直接跳转到WeatherActivity
		Log.i("Log", "city_selected:"+prefs.getBoolean("city_selected", false));
		Log.i("Log", "isFromWeatherActivity:"+isFromWeatherActivity);
		//-------------有米广告
		
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
		queryProvince();//#1 第一次运行,查询信息
	}
	/**
	* 查询所有的省,优先从数据库查询,如果没有查询到再去服务器上查询.
	*/
	public void queryProvince(){
		provinceList = myWeatherDB.loadProvince();
		if(provinceList.size() > 0){
			dataList.clear();//#6 添加省份名称信息
			for (Province p : provinceList) {
				dataList.add(p.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			listview.setSelection(0);
			titleText.setText("中国");
			currentLevel = LEVEL_PROVINCE;//#7 完全显示省份信息
			
		}else{
			queryFromServer(null,"province");//#2 第一次运行无数据,向服务器查询省份信息
		}
	}
	
	/**
	* 查询选中省内所有的市,优先从数据库查询,如果没有查询到再去服务器上查询.
	*/
	private void queryCities() {
		Log.i("Log", "开始查询城市");
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
	* 查询选中市内所有的县,优先从数据库查询,如果没有查询到再去服务器上查询.
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
	* 根据传入的代号和类型从服务器上查询省市县数据.
	*/
	private void queryFromServer(final String code, final String type) {
		String address;
		if(!TextUtils.isEmpty(code)){
			address = "http://www.weather.com.cn/data/list3/city" + code + ".xml";
		}else{
			address = "http://www.weather.com.cn/data/list3/city.xml";//#3 查询所有省份信息 
		}
		showProgressDialog();
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {
				// TODO Auto-generated method stub
				boolean result = false;
				if("province".equals(type)){
					result = Utility.handleProvincesResponse(myWeatherDB, response);//#4 第一次运行,处理请求结果,向数据库中保存省份信息
				}else if("city".equals(type)){
					result = Utility.handleCitiesResponse(myWeatherDB, response, selectedProvince.getId());
					
				}else if("county".equals(type)){
					result = Utility.handleCountiesResponse(myWeatherDB, response, selectedCity.getId());
					
				}
				if(result){
					// 通过runOnUiThread()方法回到主线程处理逻辑
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							closeProgressDialog();
							if("province".equals(type)){
								queryProvince();//#5  第一次运行显示省份信息
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
				// 通过runOnUiThread()方法回到主线程处理逻辑
				runOnUiThread(new Runnable() {
					public void run() {
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "加载失败,请检查网络连接",Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
		
	}
	/**
	* 显示进度对话框
	*/
	private void showProgressDialog() {
		if(progressDialog==null){
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("正在加载...");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}
	/**
	* 关闭进度对话框
	*/
	private void closeProgressDialog() {
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
	}
	/**
	* 捕获Back按键,根据当前的级别来判断,此时应该返回市列表、省列表、还是直接退出.
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
