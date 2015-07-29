package com.lingyunstrong.ewanfen;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.dwin.navy.serialportapi.SerailPortOpt;

import android.R.bool;
import android.R.integer;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.VideoView;

public class MainActivity extends Activity {
	private static final String TAG="Main";
	private SerailPortOpt serialPort;
	protected OutputStream mOutputStream;
	private InputStream mInputStream;
	private int m_iSerialPort=0;
	private int baudrate=9600;
	private int databits=8;
	private int stopbits=2;
	private int parity='n';
	private LinkedList<byte[]> byteLinkedList = new LinkedList<byte[]>();
	private ReadThread mReadThread;
	private String sitStatus="none";//none 初始状态，free 无人，sit 客人坐下，statusrequest 状态轮询，eating 正在吃粉
	private RelativeLayout layout;
	private VideoView vv;
	private ImageView img,bgimg;
	private Context context;
	private Button setbutton;
	private RequestQueue mRequestQueue;
	private SharedPreferences sharedPreferences;
	private String shopsit,cloudurl,localhost,menubackimg,advurl,qrrequest;
	private String defaultadvurl="android.resource://com.lingyunstrong.ewanfen/"+ R.raw.kaichang;
	private TimeHandler timeHandler;
	private int setBntclickCount=0;
	private int next_seq=0;
	private AutoUpdateVersion httpinstallerupdate;
	private IntervalCheckCloud httpcheckcloud;
	private Activity act;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		context=this;
		act=this;
		requestWindowFeature(Window.FEATURE_NO_TITLE);//隐藏标题
		//getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);//全屏
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		serialPort=new SerailPortOpt();
		mRequestQueue = MyApplication.getInstance().getRequestQueue();
		if(getSetting())//是否有基本配置
		{
			initstatus();
			startReceive();
		}
		//
		layout=(RelativeLayout)findViewById(R.id.screenview);
		bgimg=(ImageView)findViewById(R.id.bgimg);
		img = new ImageView(this);
		setTimehandler();
		img.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//模拟支付
				timeHandler.start();
			}
		});
		vv=new CustomVideoView(this);
		setbutton=new Button(this);
		
		//setimgqr();
		//playadv();
		hideNavigationBar();
	}
	/*
	 状态轮询模块timehandle(activity,img,first,last)
	 img：显示当前状态的imageview控件
	 first:第一个轮询状态的回调,step==3回调
	 last: 最后一个轮询状态的回调,step==0回调
	 状态变化step:3-0
	*/
	private void setTimehandler()
	{
		//用于状态轮询
		timeHandler=new TimeHandler(this, img,new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						//step=3时候的回调
						sitStatus="statusrequest";
					}
				}, new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// 当step＝0时候的回调
						sitStatus="eating";
						//3000ms之后的回调
						new TimeDelay(new View.OnClickListener() {
							
							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub
								playadv();
								Log.i("TimeDelay", "delaysucc");
							}
						}).start(3000);	
					}
				}, localhost);
	}
	/*
	获取云端配置信息 
	*/
	private boolean getSetting()
	{
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);//配置参数
		next_seq=sharedPreferences.getInt("next_seq",0);//请求二维码时的序号参数
		shopsit=sharedPreferences.getString("shopsit", "none");//座位编号
		cloudurl=sharedPreferences.getString("cloudurl", PublicStatic.settingurl);//云端url
		localhost=sharedPreferences.getString("localhost", "none");//本地url
		menubackimg=sharedPreferences.getString("menubackimg", "none");//点餐屏背景图url
		advurl=sharedPreferences.getString("advurl", defaultadvurl);//广告视频url
		qrrequest=sharedPreferences.getString("qrrequest", PublicStatic.qrRequesturl);//二维码请求url
		//advurl=defaultadvurl;//测试的时候只播放本地视频
		//启动同步云端线程，会根据云端配置定时同步，同步原理看IntervalCheckCloud类
		httpcheckcloud=new IntervalCheckCloud(this, cloudurl+"&mac="+PublicStatic.getLocalMacAddress(), new ResHttp() {
				
				@Override
				public void resStr(String res) {
					// TODO Auto-generated method stub
					try {
						JSONObject jsonObject = new JSONObject(res);
						shopsit=jsonObject.getString("sitno");
						cloudurl=jsonObject.getString("cloudurl");
						localhost=jsonObject.getString("localhost");
						menubackimg=jsonObject.getString("menubackimg");
						advurl=jsonObject.getString("advurl");
						qrrequest=jsonObject.getString("qrrequest");
						saveSettings();
						//
						httpinstallerupdate=new AutoUpdateVersion(act, cloudurl+"&mac="+PublicStatic.getLocalMacAddress());
						httpinstallerupdate.run();
					}
					catch (Exception e) {
						// TODO: handle exception
					}
				}
			});
		httpcheckcloud.run();
		return true;
	}
	/*
	 保存云端配置信息到本地
	 */
	private void saveSettings()
	{
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString("shopsit",shopsit);
		editor.putString("cloudurl",cloudurl);
		editor.putString("localhost",localhost);
		editor.putString("advurl",advurl);
		editor.putString("menubackimg",menubackimg);
		editor.putString("qrrequest", qrrequest);
		editor.commit();
	}
	/*
	 移除activity上所有的view
	 */
	private void removemyviews()
	{
		layout.removeView(vv);
		layout.removeView(img);
		layout.removeView(setbutton);
	}
	/*
	 加载点餐背景图片和点餐二维码
	 */
	private void setimgqr()
	{
		String backimgurlString=menubackimg;
		//用Volley加载背景图片,背景图片尺寸比例1022*720
		VolleyLoadPicture vlback = new VolleyLoadPicture(mRequestQueue, bgimg,R.drawable.bg);
		vlback.getmImageLoader().get(backimgurlString, vlback.getOne_listener(),600,423);
		Toast.makeText(context, "加载了点餐背景图", Toast.LENGTH_LONG).show();
		//网络请求支付二维码图片地址
		JsonObjectRequest req = new JsonObjectRequest(qrrequest+"&seat_id="+shopsit+"&cur_seq="+next_seq, null,
			       new Response.Listener<JSONObject>() {
			           @Override
			           public void onResponse(JSONObject response) {
			               try {
			            	next_seq=response.getInt("next_seq");
			            	SharedPreferences.Editor editor = sharedPreferences.edit();
			        		editor.putInt("next_seq",next_seq);
			        		editor.commit();
			            	String qrurl=response.getString("qrcode_url");
			            	Log.e("qrurl", qrurl);
			            	//用Volley加载二维码图片,二维码图片尺寸比例280*280
			                VolleyLoadPicture vlp = new VolleyLoadPicture(mRequestQueue, img,R.drawable.ic_launcher);
			                vlp.getmImageLoader().get(qrurl, vlp.getOne_listener(),280,280);
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
			           }
			       }, new Response.ErrorListener() {
			           @Override
			           public void onErrorResponse(VolleyError error) {
			              VolleyLog.e("Error: ", error.getMessage());
			           }
			       });
		mRequestQueue.add(req);
		//
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);  
		lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);//与父容器的左侧对齐  
		lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);//与父容器的上侧对齐  
		lp.leftMargin=255;  
		lp.topMargin=130;   
		img.setId(1);//设置这个View 的id   
		img.setLayoutParams(lp);//设置布局参数   
		removemyviews();
		layout.addView(img);//RelativeLayout添加子View
		//添加手动设置入口
		RelativeLayout.LayoutParams buttonlp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);  
		buttonlp.addRule(RelativeLayout.ALIGN_LEFT);
		buttonlp.addRule(RelativeLayout.ALIGN_TOP);
		setbutton.setId(3);
		setbutton.setText("Setting");
		setbutton.setAlpha(0.0f);
		setbutton.setLayoutParams(buttonlp);
		setbutton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				setBntclickCount++;
				if(setBntclickCount>5)
					Toast.makeText(context, setBntclickCount+"次", Toast.LENGTH_LONG).show();
				if(setBntclickCount==7)
				{
					new TimeDelay(new View.OnClickListener() {
						
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							if(setBntclickCount==7)
							{
								Intent intent=new Intent();
								intent.putExtra("shopsit", shopsit);
								intent.putExtra("cloudurl", cloudurl);
								intent.putExtra("localhost", localhost);
								intent.putExtra("advurl", advurl);
								intent.putExtra("menubackimg", menubackimg);
								intent.putExtra("qrrequest", qrrequest);
								intent.setClass(context, Setting.class);
								startActivity(intent);
								finish();
							}
						}
					}).start(3000);
				}
			}
		});
		layout.addView(setbutton);
	}
	/*
	 加载播放视频view，并自动循环播放广告视频
	 */
	private void playadv()
	{
		@SuppressWarnings("deprecation")
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT);  
		lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);//与父容器的左侧对齐  
		lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);//与父容器的上侧对齐  
		vv.setId(2);
		vv.setLayoutParams(lp);
		removemyviews();
		layout.addView(vv);
		vv.setVideoURI(Uri.parse(advurl));
	    //video.requestFocus();
		vv.start();
		vv.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			
			@Override
			public void onCompletion(MediaPlayer mp) {
				// TODO Auto-generated method stub
				vv.stopPlayback();
				vv.setVideoURI(Uri.parse(advurl));
				vv.start();
			}
		});
	}
	/*
	 设置屏幕亮度，0:灭屏，1:点亮屏
	 */
	protected void setscreenbright(float light)
	{
		WindowManager.LayoutParams lp = getWindow().getAttributes();  
		lp.screenBrightness=light;
        getWindow().setAttributes(lp);  
	}
	/*
	 将底部的navigationbar隐藏，在android4.1里面效果不是很好
	 */
	public void hideNavigationBar() {
		          int uiFlags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
		              | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
		              | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
		              | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
		              | View.SYSTEM_UI_FLAG_FULLSCREEN; // hide status bar
		  
		          if( android.os.Build.VERSION.SDK_INT >= 19 ){ 
		             uiFlags |= 0x00001000;    //SYSTEM_UI_FLAG_IMMERSIVE_STICKY: hide navigation bars - compatibility: building API level is lower thatn 19, use magic number directly for higher API target level
		         } else {
		             uiFlags |= View.SYSTEM_UI_FLAG_LOW_PROFILE;
		 }
		 getWindow().getDecorView().setSystemUiVisibility(uiFlags);
	}
    /*
     打开串口通信，开启串口数据读取线程
     */
	protected void startReceive()
    {
    	openSerialPort();
    	mReadThread = new ReadThread();
		mReadThread.start();
    }
    /*
     关闭串口通信
     */
	protected void stopReceive()
    {
    	closeSerialPort();
    }
	/*
	 串口通信参数初始化
	 */
	protected void initstatus()
	{
		serialPort.mDevNum = m_iSerialPort;
		serialPort.mSpeed = baudrate;
		serialPort.mDataBits = databits;
		serialPort.mStopBits = stopbits;
		serialPort.mParity = parity;
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		//Log.i(TAG, "==>onDestroy in");
		closeSerialPort();
		serialPort = null;
		super.onDestroy();
		//Log.i(TAG, "==>onDestroy out");
	}
	/*
	 开启串口通信
	 */
	private void openSerialPort() {

		if (serialPort.mFd == null) {
			serialPort.openDev(serialPort.mDevNum);
			Log.i("uart port operate", "Mainactivity.java==>uart open");
			serialPort.setSpeed(serialPort.mFd, serialPort.mSpeed);
			Log.i("uart port operate", "Mainactivity.java==>uart set speed..."+ serialPort.mSpeed);
			serialPort.setParity(serialPort.mFd, serialPort.mDataBits,serialPort.mStopBits, serialPort.mParity);
			Log.i("uart port operate","Mainactivity.java==>uart other params..."+ serialPort.mDataBits + "..."+ serialPort.mStopBits + "..." + serialPort.mParity);
			mInputStream = serialPort.getInputStream();
			mOutputStream = serialPort.getOutputStream();
		}
	}
	/*
	 串口通信线程，循环从串口读数据
	 */
	private class ReadThread extends Thread {
		byte[] buf = new byte[512];

		@Override
		public void run() {
			super.run();
			//Log.i(TAG, "ReadThread==>buffer:" + buf.length);
			while (!isInterrupted()) {
				int size;
				if (mInputStream == null)
					return;
				size = serialPort.readBytes(buf);
				if (size > 0) {
					// new String(buf, 0, size).getBytes()会造成数据错误
					byte[] dest = new byte[size];
					System.arraycopy(buf, 0, dest, 0, size);
					// 使用队列接受数据，解决上版本串口接受
					// 连续大量数据后出现的数据混乱
					//byteLinkedList.offer(new String(buf, 0, size).getBytes());
					byteLinkedList.clear();
					byteLinkedList.offer(dest);
					onDataReceived();
					//Log.i(TAG, "ReadThread==>" + bytesToHexString(dest, size));
				}
			}
		}
	}
	/*
	 * 收到串口数据之后的业务处理部分，在主线程中运行，业务状态的转换核心代码就在此，15:有人，55:无人，06:复位按下，查看log信息可看到具体变化
	 */
	protected void onDataReceived() {
		runOnUiThread(new Runnable() {
			public void run() {
				if (!byteLinkedList.isEmpty()) {
					try {
						byte[] buf = byteLinkedList.poll();
						int size = buf.length;
						//System.out.println("收到的数据===" + new String(buf, 0, size));
						//eTextShowMsg.append(bytesToHexString(buf, size));
						String receiveinfo=bytesToHexString(buf, size);
						if(receiveinfo.trim().equals("55"))
						{
							String nowStatus=sitStatus;
							if(nowStatus.trim().equals("none")||nowStatus.trim().equals("sit"))
							{
								sitStatus="free";
							}
							if(!sitStatus.trim().equals(nowStatus)&&sitStatus.trim().equals("free"))
							{
								if(nowStatus.trim().equals("sit"))
								{
									//释放支付二维码
								}
								removemyviews();
								setscreenbright(0.01f);
								Toast.makeText(MainActivity.this, "此时座位没有人", Toast.LENGTH_LONG).show();
							}
						}
						else if(receiveinfo.trim().equals("15"))
						{
							String nowStatus=sitStatus;
							if(nowStatus.trim().equals("none")||nowStatus.trim().equals("free"))
								sitStatus="sit";
							if(!sitStatus.trim().equals(nowStatus))
							{
								if(sitStatus.trim().equals("sit"))
								{
									setimgqr();
									Toast.makeText(MainActivity.this, "来客人了", Toast.LENGTH_LONG).show();
									//进入轮询状态
								}
								setscreenbright(1.0f);
							}
						}
						else if(receiveinfo.trim().equals("06"))
						{
							String nowstatus=sitStatus;
							if(nowstatus.trim().equals("eating"))
							{
								sitStatus="none";//复位
								Toast.makeText(MainActivity.this, "正在复位", Toast.LENGTH_LONG).show();
							}
						}
						Log.i("eTextShowMsg HEX",sitStatus+"->"+receiveinfo);
					} catch (Exception e) {
						// TODO: handle exception
						Log.i( "serial error!",e.toString());
					}
				}
			}
		});
	}
	/*
	 * 终止串口读取线程，关闭串口
	 */
	private void closeSerialPort() {

		if (mReadThread != null) {
			mReadThread.interrupt();
			mReadThread = null;
		}
		if (serialPort.mFd != null) {
			Log.i("uart port operate", "Mainactivity.java==>uart stop");
			serialPort.closeDev(serialPort.mFd);
			Log.i("uart port operate", "Mainactivity.java==>uart stoped");
		}
	}
	/*
	 * 将从串口收到的byte转成Hex
	 */
	public static String bytesToHexString(byte[] src, int size) {
		String ret = "";
		if (src == null || size <= 0) {
			return null;
		}
		for (int i = 0; i < size; i++) {
			String hex = Integer.toHexString(src[i] & 0xFF);
			//Log.i(TAG, hex);
			if (hex.length() < 2) {
				hex = "0" + hex;
			}
			hex += " ";
			ret += hex;
		}
		return ret.toUpperCase();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
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
