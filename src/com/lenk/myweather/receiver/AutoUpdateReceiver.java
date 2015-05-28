package com.lenk.myweather.receiver;

import com.lenk.myweather.service.AutoUpdateService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AutoUpdateReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent arg1) {
		// TODO Auto-generated method stub
		Intent intent = new Intent(context,AutoUpdateService.class);
		context.startService(intent);
	}

}
