package com.lenk.myweather.activity;

import java.util.Map;

import net.youmi.android.banner.AdSize;
import net.youmi.android.banner.AdView;

import com.lenk.myweather.R;
import com.lenk.myweather.model.Constants;
import com.lenk.myweather.service.AutoUpdateService;
import com.lenk.myweather.util.AccessTokenKeeper;
import com.lenk.myweather.util.HttpCallbackListener;
import com.lenk.myweather.util.HttpUtil;
import com.lenk.myweather.util.Utility;
import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.api.share.BaseResponse;
import com.sina.weibo.sdk.api.share.IWeiboHandler;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.SendMultiMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.constant.WBConstants;
import com.sina.weibo.sdk.exception.WeiboException;

import android.R.integer;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract.Contacts;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class WeatherActivity extends Activity implements OnClickListener,
		IWeiboHandler.Response {
	private LinearLayout weatherInfoLayout;
	// ��������
	private TextView cityNameText;
	// ����ʱ��
	private TextView publishText;
	// ��ʾ����1
	private TextView temp1Text;
	private Button btnShared;
	private Button btnSetting;
	// ��������
	private TextView weatherDespText;
	// ��ʾ����2
	private TextView temp2Text;
	private TextView temp3Text;
	private TextView temp4Text;
	// ��ǰ����
	private TextView currentDateText;
	//���� 
	private TextView wind1Text;
	//����ָ��
	private TextView indexCoText;
	//����ָ��
	private TextView indexdText;
	//�����̶�
	private TextView index_ag;
	
	private Button switchCity;

	private Button refreshWeather;

	private AuthInfo mAuthInfo;

	Oauth2AccessToken mAccessToken;

	SsoHandler mSsoHandler;
	
	

	/** ΢��΢������ӿ�ʵ�� */
	private IWeiboShareAPI mWeiboShareAPI = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weather_layout);
		init();
		// �� chooseAreaActivity�������ؼ�����
		String countyCode = getIntent().getStringExtra("county_code");
		if (!TextUtils.isEmpty(countyCode)) {
			// ���ؼ�����ʱ��ѯ����
			publishText.setText("ͬ����...");
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			cityNameText.setVisibility(View.INVISIBLE);
			queryWeatherCode(countyCode);
		} else {
			// û���ؼ�����/�Ѿ�ѡ������ʱֱ����ʾ��������
			showWeather();
		}
		switchCity.setOnClickListener(this);
		refreshWeather.setOnClickListener(this);
		if (isWiFiActive()) {
			// ʵ���������
			AdView adView = new AdView(this, AdSize.FIT_SCREEN);
			// ��ȡҪǶ�������Ĳ���
			LinearLayout adLayout = (LinearLayout) findViewById(R.id.adLayout);
			// ����������뵽������
			adLayout.addView(adView);
		}

		// ΢������
		mAuthInfo = new AuthInfo(this, Constants.APP_KEY,
				Constants.REDIRECT_URL, Constants.SCOPE);
		// ����΢������ӿ�ʵ��
		mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(this, Constants.APP_KEY);
		// ע�������Ӧ�õ�΢���ͻ����У�ע��ɹ����Ӧ�ý���ʾ��΢����Ӧ���б��С�
		// ���ø��������ɷ���Ȩ����Ҫ�������룬������鿴 Demo ��ʾ
		// NOTE���������ǰע�ᣬ�������ʼ����ʱ�����Ӧ�ó����ʼ��ʱ������ע��
		mWeiboShareAPI.registerApp();
		// �� Activity �����³�ʼ��ʱ���� Activity ���ں�̨ʱ�����ܻ������ڴ治�㱻ɱ���ˣ���
		// ��Ҫ���� {@link IWeiboShareAPI#handleWeiboResponse} ������΢���ͻ��˷��ص����ݡ�
		// ִ�гɹ������� true�������� {@link IWeiboHandler.Response#onResponse}��
		// ʧ�ܷ��� false�������������ص�
		if (savedInstanceState != null) {
			mWeiboShareAPI.handleWeiboResponse(getIntent(), this);
		}

		Map<String, Object> map = Utility.loadRefershSetting(this);
		 if (map.get("isRefersh").toString().equals("close")){
			Intent intent = new Intent(this,AutoUpdateService.class);
			stopService(intent);
		}
		
	}
	public void init(){
		// ��ʼ�����
				weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
				cityNameText = (TextView) findViewById(R.id.city_name);
				publishText = (TextView) findViewById(R.id.publish_text);
				weatherDespText = (TextView) findViewById(R.id.weather_desp);
				btnSetting = (Button) findViewById(R.id.btn_setting);
				btnShared = (Button) findViewById(R.id.btn_shared);
				temp1Text = (TextView) findViewById(R.id.temp1);
				temp2Text = (TextView) findViewById(R.id.temp2);
				temp3Text = (TextView) findViewById(R.id.temp3);
				temp4Text = (TextView) findViewById(R.id.temp4);
				wind1Text = (TextView) findViewById(R.id.wind1);
				indexCoText = (TextView) findViewById(R.id.index_co);
				indexdText = (TextView) findViewById(R.id.index_d);
				index_ag = (TextView) findViewById(R.id.index_ag);
				currentDateText = (TextView) findViewById(R.id.current_date);
				switchCity = (Button) findViewById(R.id.switch_city);
				refreshWeather = (Button) findViewById(R.id.refresh_weather);
				btnSetting.setOnClickListener(this);
				btnShared.setOnClickListener(this);
	}
	public boolean isWiFiActive() {
		ConnectivityManager connectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity != null) {
			NetworkInfo[] infos = connectivity.getAllNetworkInfo();
			if (infos != null) {
				for (NetworkInfo ni : infos) {
					if (ni.getTypeName().equals("WIFI") && ni.isConnected()) {
						return true;
					}
				}
			}
		}
		return false;
	}

	class AuthListener implements WeiboAuthListener {
		@Override
		public void onComplete(Bundle values) {
			mAccessToken = Oauth2AccessToken.parseAccessToken(values); // ��
																		// Bundle
																		// �н���
																		// Token
			if (mAccessToken.isSessionValid()) {
				AccessTokenKeeper.writeAccessToken(WeatherActivity.this,
						mAccessToken); // ����Token
			} else {
				// ����ע���Ӧ�ó���ǩ������ȷʱ���ͻ��յ�����Code����ȷ��ǩ����ȷ
				String code = values.getString("code", "");
				Toast.makeText(WeatherActivity.this, "ע���Ӧ�ó���ǩ������ȷ" + code,
						Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		public void onCancel() {
			// TODO Auto-generated method stub

		}

		@Override
		public void onWeiboException(WeiboException arg0) {
			// TODO Auto-generated method stub

		}
	}

	/**
	 * ���ú�Ż� �ص�onResponse����
	 * 
	 * @see {@link Activity#onNewIntent}
	 */
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		// �ӵ�ǰӦ�û���΢�������з���󣬷��ص���ǰӦ��ʱ����Ҫ�ڴ˴����øú���
		// ������΢���ͻ��˷��ص����ݣ�ִ�гɹ������� true��������
		// {@link IWeiboHandler.Response#onResponse}��ʧ�ܷ��� false�������������ص�
		mWeiboShareAPI.handleWeiboResponse(intent, this);
	}

	/**
	 * ����΢�ͻ��˲���������ݡ� ��΢���ͻ��˻���ǰӦ�ò����з���ʱ���÷��������á�
	 * 
	 * @param baseRequest
	 *            ΢���������ݶ���
	 * @see {@link IWeiboShareAPI#handleWeiboRequest}
	 */
	@Override
	public void onResponse(BaseResponse baseResp) {
		// TODO Auto-generated method stub
		switch (baseResp.errCode) {
		case WBConstants.ErrorCode.ERR_OK:
			//Toast.makeText(this, "����ɹ�", Toast.LENGTH_LONG).show();
			break;
		case WBConstants.ErrorCode.ERR_CANCEL:// �˳�ʱ����
			Toast.makeText(this, "ȡ������", Toast.LENGTH_LONG).show();
			break;
		case WBConstants.ErrorCode.ERR_FAIL:
			Toast.makeText(this, "����ʧ��" + "Error Message: " + baseResp.errMsg,
					Toast.LENGTH_LONG).show();
			break;
		}
	}

	/**
	 * �����ı���Ϣ����
	 * 
	 * @return �ı���Ϣ����
	 */
	private TextObject getTextObj() {
		SharedPreferences prefs = getSharedPreferences("data",
				MODE_PRIVATE);;
		
		TextObject textObject = new TextObject();
		String temp1 = temp1Text.getText().toString();
		String temp2 = temp2Text.getText().toString();
		String desp = weatherDespText.getText().toString();
		if (desp != null && desp.length() > 0) {
			// ���������
			textObject.text = cityNameText.getText().toString() + "����:  " + desp
				+"   "	+ temp1 +"\n��������:  " + prefs.getString("weather2", "")+"  "+prefs.getString("temp2", "") + "\n--���� �ᰲ����";

		} else {
			Toast.makeText(this, "δ��ȡ������,����ͬ��", Toast.LENGTH_SHORT).show();
			return null;
		}
		return textObject;
	}

	private void queryWeatherCode(String countyCode) {
		String address = "http://www.weather.com.cn/data/list3/city"
				+ countyCode + ".xml";
		
		queryFromServer(address, "countyCode",null);
	}

	/**
	 * ��ѯ�������Ŷ�Ӧ������
	 * 
	 * @param weatherCode
	 */
	private void queryWeatherInfo(String weatherCode) {
		final String address = "http://m.weather.com.cn/data/"
				+ weatherCode + ".html";
		queryFromServer(address, "weatherCode",weatherCode);
	}

	private void queryFromServer(final String address, final String type,final String weatherCode) {
		
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {

			@Override
			public void onFinish(final String response) {
				// TODO Auto-generated method stub
				if ("countyCode".equals(type)) {
					if (!TextUtils.isEmpty(response)) {//�������ش����ѯ����Ӧ����������
						// �ӷ��������ص������н�������������
						String[] array = response.split("\\|");
						if (array != null && array.length == 2) {
							String weatherCode = array[1];
							
							queryWeatherInfo(weatherCode);
						}
					}
				} else if ("weatherCode".equals(type)) {
					// ������������ص�������Ϣ
					Utility.handleWeatherResponse(WeatherActivity.this,
							response,weatherCode);
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							showWeather();
						}
					});
				}
			}
				@Override
				public void onError(Exception e) {
					// TODO Auto-generated method stub
					runOnUiThread(new Runnable() {
	
						@Override
						public void run() {
							// TODO Auto-generated method stub
							publishText.setText("ͬ��ʧ��,������������");
						}
					});
				}
		});
	}

	/**
	 * ��sharedPreferences�ļ��ж�ȡ�洢��������Ϣ,����ʾ
	 */
	private void showWeather() {
		SharedPreferences prefs = getSharedPreferences("data",
				MODE_PRIVATE);
		cityNameText.setText(prefs.getString("city_name", ""));
		temp1Text.setText(prefs.getString("temp1", "δ��ȡ������"));
		weatherDespText.setText(prefs.getString("weather1", "δ��ȡ����������"));
		publishText.setText("����" + prefs.getString("publish_time", "") + "ʱ����");
		currentDateText.setText(prefs.getString("current_date", "")+" "+prefs.getString("week", ""));
		wind1Text.setText("����: "+prefs.getString("wind1", ""));
		indexCoText.setText("����ָ��: "+prefs.getString("index_co", ""));
		indexdText.setText("����ָ��: "+prefs.getString("index_d", ""));
		index_ag.setText("�����̶�: "+prefs.getString("index_ag", ""));
		temp2Text.setText("��������:\t\t"+prefs.getString("temp2", "")+"\t\t"+prefs.getString("weather2", ""));;
		temp3Text.setText("��������:\t\t"+prefs.getString("temp3", "")+"\t\t"+prefs.getString("weather3", ""));;
		temp4Text.setText("���������:\t\t"+prefs.getString("temp4", "")+"\t\t"+prefs.getString("weather4", ""));;
		weatherInfoLayout.setVisibility(View.VISIBLE);
		cityNameText.setVisibility(View.VISIBLE);
		
		
		
		 
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (mSsoHandler != null) {
			mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.weather, menu);
		return true;
	}
	public void weioBoShared(){
		mSsoHandler = new SsoHandler(WeatherActivity.this, mAuthInfo);
		mSsoHandler.authorize(new AuthListener());
		// 1. ��ʼ��΢���ķ�����Ϣ
		WeiboMultiMessage weiboMessage = new WeiboMultiMessage();
		if (getTextObj() != null) {
			weiboMessage.textObject = getTextObj();
			// 2. ��ʼ���ӵ�������΢������Ϣ����
			SendMultiMessageToWeiboRequest request = new SendMultiMessageToWeiboRequest();
			// ��transactionΨһ��ʶһ������
			request.transaction = String.valueOf(System.currentTimeMillis());
			request.multiMessage = weiboMessage;
			AuthInfo authInfo = new AuthInfo(this, Constants.APP_KEY,
					Constants.REDIRECT_URL, Constants.SCOPE);
			// ����uid��xml�л�ȡ token ����Ѿ���¼�򱣴�token �����ٴε�¼ ������
			Oauth2AccessToken accessToken = AccessTokenKeeper
					.readAccessToken(getApplicationContext());
			String token = "";
			if (accessToken != null) {
				token = accessToken.getToken();
			}

			mWeiboShareAPI.sendRequest(WeatherActivity.this, request, authInfo,
					token, new WeiboAuthListener() {

						@Override
						public void onWeiboException(WeiboException arg0) {
							Toast.makeText(WeatherActivity.this, "΢���������",
									Toast.LENGTH_SHORT).show();
						}

						@Override
						public void onComplete(Bundle bundle) {
							// TODO Auto-generated method stub
							Oauth2AccessToken newToken = Oauth2AccessToken
									.parseAccessToken(bundle);
							AccessTokenKeeper.writeAccessToken(
									getApplicationContext(), newToken);
							// Toast.makeText(getApplicationContext(),
							// "onAuthorizeComplete token = " +
							// newToken.getToken(), 0).show();
						}

						@Override
						public void onCancel() {
							Toast.makeText(WeatherActivity.this, "�˳�΢������",
									Toast.LENGTH_SHORT).show();
						}
					});
		} 
		
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.switch_city:
			Intent intent = new Intent(this, ChooseAreaActivity.class);
			intent.putExtra("from_weather_activity", true);
			startActivity(intent);
			finish();
			break;

		case R.id.refresh_weather:
			publishText.setText("ͬ����...");
			SharedPreferences prefs = getSharedPreferences("data",
					MODE_PRIVATE);
			String weatherCode = prefs.getString("weather_code", "");
			if (!TextUtils.isEmpty(weatherCode)) {
				queryWeatherInfo(weatherCode);
			}else{
				publishText.setText("ͬ����������");
			}
			break;
		case R.id.btn_setting:
			Intent i = new Intent(this, SettingActivity.class);
			startActivity(i);
			break;
		case R.id.btn_shared:
			weioBoShared();
			break;
		default:
			break;
		}

	}
	private long touchTime = 0;
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode == KeyEvent.KEYCODE_BACK){
			if(System.currentTimeMillis()-touchTime>1500){
				Toast.makeText(WeatherActivity.this, "�ٰ�һ���˳�", Toast.LENGTH_SHORT).show();
				touchTime = System.currentTimeMillis(); 
			}else{
				finish();
			}
			
			return true;
		}else{
			return super.onKeyDown(keyCode, event);
		}
	}

}
