package com.lingyunstrong.ewanfen;

import android.os.Handler;
import android.os.Message;
import android.view.View.OnClickListener;

public class TimeDelay  extends Handler
{
	static abstract public class OnTimeElapsedListener
	{
		abstract void timeElapsedListener();
	}
	private OnClickListener listener;
	public TimeDelay(OnClickListener ls)
	{
		listener=ls;
	}
	@Override
	public void handleMessage(Message msg)
	{
		super.handleMessage(msg);
		String res=(String)msg.obj;
		if(res.equals("TimeOver"))
			listener.onClick(null);
		
	}
	public void start(int ms)
	{
		DelayThread thread = new DelayThread(ms,this);
		new Thread(thread).start();
	}
}

class DelayThread implements Runnable
{
	
	public Handler handler;
	public int ms;
	
	public DelayThread(int ms,Handler h)
	{
		this.ms=ms;
		this.handler=h;
	}
	public void run()
	{
		
		try 
		{
			Thread.sleep(ms);
		} catch (InterruptedException e) {}		
		Message msg= new Message();
		msg.obj="TimeOver";
		handler.sendMessage(msg);
	}
}
