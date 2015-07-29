package com.lingyunstrong.ewanfen;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Setting extends Activity{
	private EditText editshopsit,editcloudurl,editlocalhost,editadvurl,editmenubackimg,macadr,editqrrequest;
	private Context context;
	private SharedPreferences sharedPreferences;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		context=this;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settinglayout);
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		getActionBar().setTitle("点餐屏设置");
		editshopsit=(EditText)findViewById(R.id.shopsit);
		editcloudurl=(EditText)findViewById(R.id.cloudurl);
		editlocalhost=(EditText)findViewById(R.id.localhost);
		editadvurl=(EditText)findViewById(R.id.advurl);
		editmenubackimg=(EditText)findViewById(R.id.menubackimg);
		macadr=(EditText)findViewById(R.id.macadr);
		editqrrequest=(EditText)findViewById(R.id.qrrequest);
		//
		editshopsit.setEnabled(false);
		editlocalhost.setEnabled(false);
		editadvurl.setEnabled(false);
		editmenubackimg.setEnabled(false);
		macadr.setEnabled(false);
		editqrrequest.setEnabled(false);
		//
		Bundle bundle=getIntent().getExtras();
		if(bundle!=null)
			getbundleInfo(bundle);
		else
		{
			finish();
			return;
		}
		//
		Button bnt=(Button)findViewById(R.id.button1);
		bnt.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(checkall())
				{
					saveSettings();
					//Toast.makeText(context, "保存成功", Toast.LENGTH_LONG).show();
					Intent intent=new Intent();
					intent.setClass(context, MainActivity.class);
					startActivity(intent);
					finish();
				}
				else 
				{
					Toast.makeText(context, "输入不符合规则", Toast.LENGTH_LONG).show();
				}
			}
		});
	}
	public void getbundleInfo(Bundle bundle)
	{
		if(bundle!=null)
		{
			editshopsit.setText(bundle.getString("shopsit"));
			editcloudurl.setText(bundle.getString("cloudurl"));
			editlocalhost.setText(bundle.getString("localhost"));
			editadvurl.setText(bundle.getString("advurl"));
			editmenubackimg.setText(bundle.getString("menubackimg"));
			editqrrequest.setText(bundle.getString("qrrequest"));
			macadr.setText(PublicStatic.getLocalMacAddress());
		}
	}
	/*
	 * 保存云端配置文件的地址
	 */
	private void saveSettings()
	{
		SharedPreferences.Editor editor = sharedPreferences.edit();
		//editor.putString("shopsit",editshopsit.getText().toString());
		editor.putString("cloudurl",editcloudurl.getText().toString());
		//editor.putString("localhost",editlocalhost.getText().toString());
		//editor.putString("advurl",editadvurl.getText().toString());
		//editor.putString("menubackimg",editmenubackimg.getText().toString());
		//editor.putString("qrrequest", editqrrequest.getText().toString());
		editor.commit();
	}
	private boolean checkall()
	{
		boolean flag=true;
		//if(editshopsit.getText().toString().trim().equals(""))
		//	flag=false;
		boolean ft=checkurl(editcloudurl.getText().toString().trim());//&&checkurl(editlocalhost.getText().toString().trim())&&checkurl(editadvurl.getText().toString().trim())&&checkurl(editmenubackimg.getText().toString().trim());
		if(!ft)
			flag=false;
		return flag;
	}
	/*
	 * url地址的正则表达式验证
	 */
	private boolean checkurl(String url)
	{
		String ruler="[a-zA-z]+://[^\\s]*";
		return url.matches(ruler);
	}
}
