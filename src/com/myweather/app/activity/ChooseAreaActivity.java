package com.myweather.app.activity;

import java.util.ArrayList;
import java.util.List;

import com.myweather.app.R;
import com.myweather.app.R.id;
import com.myweather.app.R.layout;
import com.myweather.app.R.menu;
import com.myweather.app.db.MyWeatherDB;
import com.myweather.app.model.City;
import com.myweather.app.model.County;
import com.myweather.app.model.Province;
import com.myweather.app.util.HttpCallbackListener;
import com.myweather.app.util.HttpUtil;
import com.myweather.app.util.Utility;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
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
	private ListView listview;
	private ArrayAdapter<String> adapter;
	private MyWeatherDB myWeatherDB;
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
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_choose_area);
		listview = (ListView) findViewById(R.id.list_view);
		titleText = (TextView) findViewById(R.id.title_text);
		adapter = new ArrayAdapter<String>(this, R.layout.choose_area_item,dataList);
		listview.setAdapter(adapter);
		myWeatherDB  = myWeatherDB.getInstance(this);
		
		listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				if(currentLevel == LEVEL_PROVINCE){
					Log.i("Log", "����onItemClick");
					selectedProvince  = provinceList.get(position);
					Log.i("Log", "provinceCode:"+selectedProvince.getProvinceCode());
					queryCities();
				}else if (currentLevel == LEVEL_CITY){
					selectedCity = cityList.get(position);
					queryCounties();
				}
			}
			
		});
		queryProvince();
	}
	/**
	* ��ѯ���е�ʡ�����ȴ����ݿ��ѯ�����û�в�ѯ����ȥ�������ϲ�ѯ��
	*/
	public void queryProvince(){
		provinceList = myWeatherDB.loadProvince();
		if(provinceList.size() > 0){
			dataList.clear();
			for (Province p : provinceList) {
				dataList.add(p.getProvinceName());
				
			}
			adapter.notifyDataSetChanged();
			listview.setSelection(0);
			titleText.setText("�й�");
			currentLevel = LEVEL_PROVINCE;
			
		}else{
			queryFromServer(null,"province");
		}
	}
	
	/**
	* ��ѯѡ��ʡ�����е��У����ȴ����ݿ��ѯ�����û�в�ѯ����ȥ�������ϲ�ѯ��
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
	* ��ѯѡ���������е��أ����ȴ����ݿ��ѯ�����û�в�ѯ����ȥ�������ϲ�ѯ��
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
	* ���ݴ���Ĵ��ź����ʹӷ������ϲ�ѯʡ�������ݡ�
	*/
	private void queryFromServer(final String code, final String type) {
		String address;
		if(!TextUtils.isEmpty(code)){
			address =  "http://www.weather.com.cn/data/list3/city" + code +
					".xml";
		}else{
			address = "http://www.weather.com.cn/data/list3/city.xml";
		}
		showProgressDialog();
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {
				// TODO Auto-generated method stub
				Log.i("Log", "Http�������");
				boolean result = false;
				if("province".equals(type)){
					result = Utility.handleProvincesResponse(myWeatherDB, response);
					Log.i("Log", "��Http��Ӧ���ݽ��з�������");
				}else if("city".equals(type)){
					result = Utility.handleCitiesResponse(myWeatherDB, response, selectedProvince.getId());
					
				}else if("county".equals(type)){
					result = Utility.handleCountiesResponse(myWeatherDB, response, selectedCity.getId());
					
				}
				Log.i("Log", "Http��Ӧ���ݽ��з����Ľ��:"+result);
				if(result){
					// ͨ��runOnUiThread()�����ص����̴߳����߼�
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							closeProgressDialog();
							if("province".equals(type)){
								queryProvince();
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
						Toast.makeText(ChooseAreaActivity.this, "����ʧ��",Toast.LENGTH_SHORT).show();
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
	* ����Back���������ݵ�ǰ�ļ������жϣ���ʱӦ�÷������б�ʡ�б�����ֱ���˳���
	*/
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if(currentLevel == LEVEL_COUNTY){
			queryCities();
		}else if(currentLevel == LEVEL_CITY){
			queryProvince();
		}else{
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
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
