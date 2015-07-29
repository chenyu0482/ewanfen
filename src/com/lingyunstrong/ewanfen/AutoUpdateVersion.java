package com.lingyunstrong.ewanfen;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

public class AutoUpdateVersion extends Handler{

	protected Activity act;
	protected int installversion;//close,open
	protected String installpackage;
	protected String install_file;
	protected String install_startclass;
	protected ProgressDialog pBar;
	protected String updateurl;
	public AutoUpdateVersion(Activity act,String updateurl)
	{
		this.act=act;
		this.updateurl=updateurl;
		this.install_file="ewanfen";
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
			run();
		}
		else
		{
			try {
				JSONObject jsonObject = new JSONObject(res);
				installpackage=jsonObject.getString("versionpackage");
				installversion=Integer.parseInt(jsonObject.getString("versioncode"));
				final String downurl=jsonObject.getString("versionurl");
				install_startclass=jsonObject.getString("versionstartclass");
				//远程版本大于当前版本
				if(installversion>PublicStatic.getVerCode(act))
				{
					this.install_file+=".apk";
					//下载并安装
					pBar = new ProgressDialog(act);  
					pBar.setTitle("正在更新程序");  
					pBar.setMessage("请稍候...");  
					pBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);  
                    //下载
                    downFile(downurl);  
				}
			}
			catch (Exception e) {
				// TODO: handle exception
			}
		}
	}
	void downFile(final String url) {  
		Log.e("url", url);
        pBar.show();  
        new Thread() {  
            public void run() {  
                try {  
                	HttpClient client = new DefaultHttpClient();  
                    HttpGet get = new HttpGet(url);  
                	HttpResponse response = client.execute(get);  
                    HttpEntity entity = response.getEntity();  
                    //long length = entity.getContentLength();  
                    InputStream is = entity.getContent();  
                    FileOutputStream fileOutputStream = null;  
                    if (is != null) {  
                    	String fileurl=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + install_file;
                        File file = new File(fileurl);  
                        if(file.exists())
                        	file.delete();
                        fileOutputStream = new FileOutputStream(file);  
                        byte[] buf = new byte[1024];  
                        int ch = -1;  
                        int count = 0;  
                        while ((ch = is.read(buf)) != -1) {  
                            fileOutputStream.write(buf, 0, ch);  
                            count += ch;  
                            //if (length > 0) {  
                            //}  
                        }  
                    }  
                    fileOutputStream.flush();  
                    if (fileOutputStream != null) {  
                        fileOutputStream.close();  
                    }  
                    //下载完毕
                    down();
                } catch (ClientProtocolException e) {  
                    e.printStackTrace();  
                } catch (IOException e) {  
                    e.printStackTrace();  
                }  
            }  
        }.start();  
    }
	void down() {  
        this.post(new Runnable() {  
            public void run() {  
                pBar.cancel();  
                update();  
            }  
        });  
	}
	void update() {  
		pBar.setTitle("正在安装安装器");
		pBar.show();
		String fileName=install_file;
		String apkName=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + fileName;
		File file=new File(apkName);
		if(file.exists())
		{	
			act.finish();
			Log.e("update_start", "update_start");
			String startcmdString="am start -n "+installpackage+"/"+installpackage+"."+install_startclass;
			Log.e("update", "root = " + PublicStatic.runRootCommand("pm install -r " + apkName,startcmdString)); 
			Log.e("update_over", "update_over");
		}
		pBar.cancel();
    }
	public void run() {
		HttpUpdate conn=new HttpUpdate(this.updateurl, this);
		new Thread(conn).start();
	}
}
class HttpUpdate implements Runnable{
	private String URL;
	private Handler handler;
	public HttpUpdate(String URL,Handler handler)
	{
		this.URL=URL;
		this.handler=handler;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		String urlString = URL;
		HttpGet httpRequest = new HttpGet(urlString);
		String res = "internetfailed";
		try
		{
			Thread.sleep(PublicStatic.delaytime*1000);
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
