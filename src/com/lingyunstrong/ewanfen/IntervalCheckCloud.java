package com.lingyunstrong.ewanfen;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

public class IntervalCheckCloud extends Handler{

	protected Activity act;
	protected String settingurl;
	protected ResHttp reshttp;
	protected HttpcheckSetting conn;
	public IntervalCheckCloud(Activity act,String settingurl,ResHttp reshttp)
	{
		this.act=act;
		this.settingurl=settingurl;
		this.reshttp=reshttp;
		conn=new HttpcheckSetting(this.settingurl, this);
	}
	@Override
	public void handleMessage(Message msg) {
		// TODO Auto-generated method stub
		super.handleMessage(msg);
		String res= (String)msg.obj;
		if(res.equals("internetfailed"))
		{
			Toast toast = Toast.makeText(act.getApplicationContext(), "网络未连接，检查更新失败", Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			conn.setdelaytime(PublicStatic.delaytime*1000);
			run();
		}
		else
		{
			try {
				JSONObject jsonObject = new JSONObject(res);
				this.settingurl=jsonObject.getString("cloudurl")+"&mac="+PublicStatic.getLocalMacAddress();
				conn.seturl(this.settingurl);
				int checkdelay=Integer.parseInt(jsonObject.getString("checkupdatedelay"));
				conn.setdelaytime(checkdelay*1000);
				int installversion=Integer.parseInt(jsonObject.getString("versioncode"));
				//没有新版本更新
				if(installversion<=PublicStatic.getVerCode(act))
				{
					run();
				}
				Log.e("checksetting", "checked");
			}
			catch (Exception e) {
				// TODO: handle exception
			}
			finally{
				reshttp.resStr(res);
			}
		}
	}
	public void run()
	{
		new Thread(conn).start();
	}
}
class HttpcheckSetting implements Runnable{
	private String URL;
	private Handler handler;
	private int delaytime=0;
	public HttpcheckSetting(String URL,Handler handler)
	{
		this.URL=URL;
		this.handler=handler;
	}
	public void setdelaytime(int num)
	{
		delaytime=num;
	}
	public void seturl(String url)
	{
		this.URL=url;
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		String urlString = URL;
		//Log.e("settingurl", URL);
		HttpGet httpRequest = new HttpGet(urlString);
		String res = "internetfailed";
		try
		{
			Thread.sleep(delaytime);
			HttpParams params = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(params, 3000);
			HttpConnectionParams.setSoTimeout(params, 3000);
			HttpResponse response = new DefaultHttpClient(params).execute(httpRequest);
			if (response.getStatusLine().getStatusCode() == 200)
				res = EntityUtils.toString(response.getEntity());
		}
			catch(Exception e){
		}
		Message msg = new Message();
		msg.obj = res;
		handler.sendMessage(msg);
	}
}
