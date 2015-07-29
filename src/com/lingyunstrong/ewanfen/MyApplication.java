package com.lingyunstrong.ewanfen;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import android.app.Application;
import android.util.Log;

public class MyApplication extends Application{
	
	private RequestQueue mRequestQueue;
	private static MyApplication sInstance;
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		sInstance=this;
		if(mRequestQueue==null)
		{
			mRequestQueue = Volley.newRequestQueue(getApplicationContext());  
			Log.i("requestQueue", "create");
		}
	}
	public static synchronized MyApplication getInstance(){
		return sInstance;
	}
	public RequestQueue getRequestQueue() {
	    return mRequestQueue;  
	} 
}
