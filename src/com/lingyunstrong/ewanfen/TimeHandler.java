package com.lingyunstrong.ewanfen;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class TimeHandler extends Handler
{
	private OnClickListener lastlistener,firstListener;//轮询的第一次状态和最后一次状态的回调函数
	private ImageView img;
	private Activity act;
	private String requestUrl;
	private BasicThread thread;
	
	public TimeHandler(Activity act,ImageView img,OnClickListener firstlistener,OnClickListener lastlistener,String requesturl)
	{
		this.act=act;
		this.img=img;
		this.firstListener=firstlistener;
		this.lastlistener=lastlistener;
		this.requestUrl=requesturl;
	}
	
	public void handleMessage(Message msg)
	{
		super.handleMessage(msg);
		int step=msg.getData().getInt("step");
		switch (step) {
		case 3:{
			img.setImageResource(R.drawable.paysucc);
			firstListener.onClick(null);
			break;
		}
		case 2:img.setImageResource(R.drawable.making);break;
		case 1:img.setImageResource(R.drawable.waitingfetch);break;
		case 0:{
			img.setImageResource(R.drawable.happyeat);
			lastlistener.onClick(null);
		break;
		}
		default:
			break;
		}
		
	}
	public void start()
	{
		thread = new BasicThread(requestUrl);
		thread.handler = this;
		new Thread(thread).start();
	}
}

class BasicThread implements Runnable
{
	public boolean flag = true;
	public TimeHandler handler;
	private int step=3;
	private String requestUrl;
	
	public BasicThread(String requestUrl){
		this.requestUrl=requestUrl;
	}
	public void run()
	{
		while(flag)
		{
			if(step==0)
				flag=false;
			//此处为状态轮询
			//模拟支付，制作，传送过程
			Bundle b = new Bundle();
			b.putInt("step", step);
			Message msg = new Message();
			msg.setData(b);
			handler.sendMessage(msg);
			try 
			{
				step--;
				Thread.sleep(2000);
			} catch (InterruptedException e) {}
			if( !flag )
				break;
		}
	}
}
