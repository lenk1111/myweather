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
	// 城市名称
	private TextView cityNameText;
	// 发布时间
	private TextView publishText;
	// 显示气温1
	private TextView temp1Text;
	private Button btnShared;
	private Button btnSetting;
	// 天气描述
	private TextView weatherDespText;
	// 显示气温2
	private TextView temp2Text;
	private TextView temp3Text;
	private TextView temp4Text;
	// 当前日期
	private TextView currentDateText;
	//风速 
	private TextView wind1Text;
	//舒适指数
	private TextView indexCoText;
	//穿衣指数
	private TextView indexdText;
	//过敏程度
	private TextView index_ag;
	
	private Button switchCity;

	private Button refreshWeather;

	private AuthInfo mAuthInfo;

	Oauth2AccessToken mAccessToken;

	SsoHandler mSsoHandler;
	
	

	/** 微博微博分享接口实例 */
	private IWeiboShareAPI mWeiboShareAPI = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weather_layout);
		init();
		// 从 chooseAreaActivity传来的县级代码
		String countyCode = getIntent().getStringExtra("county_code");
		if (!TextUtils.isEmpty(countyCode)) {
			// 有县级代码时查询天气
			publishText.setText("同步中...");
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			cityNameText.setVisibility(View.INVISIBLE);
			queryWeatherCode(countyCode);
		} else {
			// 没有县级代码/已经选过县区时直接显示本地天气
			showWeather();
		}
		switchCity.setOnClickListener(this);
		refreshWeather.setOnClickListener(this);
		if (isWiFiActive()) {
			// 实例化广告条
			AdView adView = new AdView(this, AdSize.FIT_SCREEN);
			// 获取要嵌入广告条的布局
			LinearLayout adLayout = (LinearLayout) findViewById(R.id.adLayout);
			// 将广告条加入到布局中
			adLayout.addView(adView);
		}

		// 微博分享
		mAuthInfo = new AuthInfo(this, Constants.APP_KEY,
				Constants.REDIRECT_URL, Constants.SCOPE);
		// 创建微博分享接口实例
		mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(this, Constants.APP_KEY);
		// 注册第三方应用到微博客户端中，注册成功后该应用将显示在微博的应用列表中。
		// 但该附件栏集成分享权限需要合作申请，详情请查看 Demo 提示
		// NOTE：请务必提前注册，即界面初始化的时候或是应用程序初始化时，进行注册
		mWeiboShareAPI.registerApp();
		// 当 Activity 被重新初始化时（该 Activity 处于后台时，可能会由于内存不足被杀掉了），
		// 需要调用 {@link IWeiboShareAPI#handleWeiboResponse} 来接收微博客户端返回的数据。
		// 执行成功，返回 true，并调用 {@link IWeiboHandler.Response#onResponse}；
		// 失败返回 false，不调用上述回调
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
		// 初始化组件
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
			mAccessToken = Oauth2AccessToken.parseAccessToken(values); // 从
																		// Bundle
																		// 中解析
																		// Token
			if (mAccessToken.isSessionValid()) {
				AccessTokenKeeper.writeAccessToken(WeatherActivity.this,
						mAccessToken); // 保存Token
			} else {
				// 当您注册的应用程序签名不正确时，就会收到错误Code，请确保签名正确
				String code = values.getString("code", "");
				Toast.makeText(WeatherActivity.this, "注册的应用程序签名不正确" + code,
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
	 * 调用后才会 回调onResponse方法
	 * 
	 * @see {@link Activity#onNewIntent}
	 */
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		// 从当前应用唤起微博并进行分享后，返回到当前应用时，需要在此处调用该函数
		// 来接收微博客户端返回的数据；执行成功，返回 true，并调用
		// {@link IWeiboHandler.Response#onResponse}；失败返回 false，不调用上述回调
		mWeiboShareAPI.handleWeiboResponse(intent, this);
	}

	/**
	 * 接收微客户端博请求的数据。 当微博客户端唤起当前应用并进行分享时，该方法被调用。
	 * 
	 * @param baseRequest
	 *            微博请求数据对象
	 * @see {@link IWeiboShareAPI#handleWeiboRequest}
	 */
	@Override
	public void onResponse(BaseResponse baseResp) {
		// TODO Auto-generated method stub
		switch (baseResp.errCode) {
		case WBConstants.ErrorCode.ERR_OK:
			//Toast.makeText(this, "分享成功", Toast.LENGTH_LONG).show();
			break;
		case WBConstants.ErrorCode.ERR_CANCEL:// 退出时进入
			Toast.makeText(this, "取消分享", Toast.LENGTH_LONG).show();
			break;
		case WBConstants.ErrorCode.ERR_FAIL:
			Toast.makeText(this, "分享失败" + "Error Message: " + baseResp.errMsg,
					Toast.LENGTH_LONG).show();
			break;
		}
	}

	/**
	 * 创建文本消息对象。
	 * 
	 * @return 文本消息对象。
	 */
	private TextObject getTextObj() {
		SharedPreferences prefs = getSharedPreferences("data",
				MODE_PRIVATE);;
		
		TextObject textObject = new TextObject();
		String temp1 = temp1Text.getText().toString();
		String temp2 = temp2Text.getText().toString();
		String desp = weatherDespText.getText().toString();
		if (desp != null && desp.length() > 0) {
			// 分享的内容
			textObject.text = cityNameText.getText().toString() + "今日:  " + desp
				+"   "	+ temp1 +"\n明日天气:  " + prefs.getString("weather2", "")+"  "+prefs.getString("temp2", "") + "\n--来自 酷安天气";

		} else {
			Toast.makeText(this, "未获取到天气,请先同步", Toast.LENGTH_SHORT).show();
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
	 * 查询天气代号对应的天气
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
					if (!TextUtils.isEmpty(response)) {//根据区县代码查询出对应的天气代码
						// 从服务器返回的数据中解析出天气代码
						String[] array = response.split("\\|");
						if (array != null && array.length == 2) {
							String weatherCode = array[1];
							
							queryWeatherInfo(weatherCode);
						}
					}
				} else if ("weatherCode".equals(type)) {
					// 处理服务器返回的天气信息
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
							publishText.setText("同步失败,请检查网络连接");
						}
					});
				}
		});
	}

	/**
	 * 从sharedPreferences文件中读取存储的天气信息,并显示
	 */
	private void showWeather() {
		SharedPreferences prefs = getSharedPreferences("data",
				MODE_PRIVATE);
		cityNameText.setText(prefs.getString("city_name", ""));
		temp1Text.setText(prefs.getString("temp1", "未获取到天气"));
		weatherDespText.setText(prefs.getString("weather1", "未获取到天气描述"));
		publishText.setText("今天" + prefs.getString("publish_time", "") + "时发布");
		currentDateText.setText(prefs.getString("current_date", "")+" "+prefs.getString("week", ""));
		wind1Text.setText("风速: "+prefs.getString("wind1", ""));
		indexCoText.setText("舒适指数: "+prefs.getString("index_co", ""));
		indexdText.setText("穿衣指数: "+prefs.getString("index_d", ""));
		index_ag.setText("过敏程度: "+prefs.getString("index_ag", ""));
		temp2Text.setText("明天天气:\t\t"+prefs.getString("temp2", "")+"\t\t"+prefs.getString("weather2", ""));;
		temp3Text.setText("后天天气:\t\t"+prefs.getString("temp3", "")+"\t\t"+prefs.getString("weather3", ""));;
		temp4Text.setText("大后天天气:\t\t"+prefs.getString("temp4", "")+"\t\t"+prefs.getString("weather4", ""));;
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
		// 1. 初始化微博的分享消息
		WeiboMultiMessage weiboMessage = new WeiboMultiMessage();
		if (getTextObj() != null) {
			weiboMessage.textObject = getTextObj();
			// 2. 初始化从第三方到微博的消息请求
			SendMultiMessageToWeiboRequest request = new SendMultiMessageToWeiboRequest();
			// 用transaction唯一标识一个请求
			request.transaction = String.valueOf(System.currentTimeMillis());
			request.multiMessage = weiboMessage;
			AuthInfo authInfo = new AuthInfo(this, Constants.APP_KEY,
					Constants.REDIRECT_URL, Constants.SCOPE);
			// 根据uid从xml中获取 token 如果已经登录则保存token 不需再次登录 有问题
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
							Toast.makeText(WeatherActivity.this, "微博分享出错",
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
							Toast.makeText(WeatherActivity.this, "退出微博分享",
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
			publishText.setText("同步中...");
			SharedPreferences prefs = getSharedPreferences("data",
					MODE_PRIVATE);
			String weatherCode = prefs.getString("weather_code", "");
			if (!TextUtils.isEmpty(weatherCode)) {
				queryWeatherInfo(weatherCode);
			}else{
				publishText.setText("同步出现问题");
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
				Toast.makeText(WeatherActivity.this, "再按一次退出", Toast.LENGTH_SHORT).show();
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
