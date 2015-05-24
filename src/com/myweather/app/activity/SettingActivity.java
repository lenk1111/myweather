package com.myweather.app.activity;

import java.util.Map;

import com.myweather.app.R;
import com.myweather.app.R.layout;
import com.myweather.app.R.menu;
import com.myweather.app.service.AutoUpdateService;
import com.myweather.app.util.Utility;

import android.os.Bundle;
import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;
import android.widget.ToggleButton;

public class SettingActivity extends Activity {
	ToggleButton refershToggle;
	Button timeSetBtn;
	Button backBtn;
	Map<String, Object> map ;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_setting);
		refershToggle = (ToggleButton) findViewById(R.id.refersh_toggle);
		timeSetBtn = (Button) findViewById(R.id.time_set_button);
		backBtn = (Button) findViewById(R.id.back_button);
		
		map = Utility.loadRefershSetting(this);
		if(map.get("isRefersh").toString().equals("open")){
			refershToggle.setChecked(true);
		}else if(map.get("isRefersh").toString().equals("close")){
			refershToggle.setChecked(false);
		}
		
		//------------设置频率 dialog加载layout并设置初始值---------------------------
		/*LayoutInflater inflater = getLayoutInflater();
		View layout = inflater.inflate(R.layout.dialog_layout, null);
		final EditText etTime = (EditText) layout.findViewById(R.id.et_time);
		try {
			int time = Integer.parseInt(map.get("time").toString());
			if(time>0){
				time = time / 60 /60 /1000;
				etTime.setText(time);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		final AlertDialog dialog = new AlertDialog.Builder(SettingActivity.this).create();
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
					Utility.RefershSetting(SettingActivity.this, null, time);
				} catch (Exception e) {
					// TODO: handle exception
					Toast.makeText(SettingActivity.this, "值应为数字", Toast.LENGTH_SHORT).show();
				}
			}
		});
		dialog.setButton(DialogInterface.BUTTON_NEUTRAL, "取消",new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				
			}
		});*/
		//-------------------设置dilog结束----------------------------------
		backBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		timeSetBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				 AlertDialog dialog = Utility.getSetTimeDialog(SettingActivity.this, map);
						dialog.show();
			}
		});
		refershToggle.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				// TODO Auto-generated method stub
				if(arg1){//开启定时查询天气服务
					Intent intent = new Intent(SettingActivity.this,AutoUpdateService.class);
					startService(intent);
					Utility.RefershSetting(SettingActivity.this, "open",AutoUpdateService.anHour);
					Toast.makeText(SettingActivity.this, "已开启自动更新", Toast.LENGTH_SHORT).show();
				}else{
					Intent intent = new Intent(SettingActivity.this,AutoUpdateService.class);
					stopService(intent);
					Utility.RefershSetting(SettingActivity.this, "close",0);
					Toast.makeText(SettingActivity.this, "已关闭自动更新", Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.setting, menu);
		return true;
	}

}
