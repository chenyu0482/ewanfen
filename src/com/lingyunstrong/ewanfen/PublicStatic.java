package com.lingyunstrong.ewanfen;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;
import android.widget.Toast;

public class PublicStatic {
	//默认二维码请求地址
	public static String qrRequesturl="http://61.187.51.109/demo/ewanfen/qrtemp.html?act=qrcode";//"http://t.qqm.td.lingyunstrong.com/plugin.php?id=ewf&mod=get_qrcode";
	//默认云端配置文件地址
	public static String settingurl="http://61.187.51.109/demo/ewanfen/setting.html?act=set";
	//网络不通时重复请求的时间间隔,单位秒
	public static int delaytime=1;
	//获取版本号
	public static int getVerCode(Context context) {
        PackageManager pm = context.getPackageManager();//context为当前Activity上下文 
		PackageInfo pi;
		int version=0;
		try {
			pi = pm.getPackageInfo(context.getPackageName(), 0);
			version = pi.versionCode;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return version;
    } 
	//获取本地mac地址
	public static String getLocalMacAddress() {  
        String Mac=null;  
        try{  
            //wifi mac
            String path="sys/class/net/wlan0/address";  
            if((new File(path)).exists())  
            {  
                FileInputStream fis = new FileInputStream(path);  
                byte[] buffer = new byte[8192];  
                int byteCount = fis.read(buffer);  
                if(byteCount>0)  
                {  
                    Mac = new String(buffer, 0, byteCount, "utf-8");  
                }  
            }  
            Log.v("wifi**mac11**", ""+Mac);  
            if(Mac==null||Mac.length()==0)  
            {  
            	//eth0 mac
                path="sys/class/net/eth0/address";  
                FileInputStream fis_name = new FileInputStream(path);  
                byte[] buffer_name = new byte[8192];  
                int byteCount_name = fis_name.read(buffer_name);  
                if(byteCount_name>0)  
                {  
                    Mac = new String(buffer_name, 0, byteCount_name, "utf-8");  
                }  
            }  
            Log.v("eth0**mac11**", ""+Mac);   
            if(Mac.length()==0||Mac==null){  
                return "Get Macadr error";  
            }  
        }catch(Exception io){  
            Log.v("daming.zou**exception*", ""+io.toString());  
        }  
        Log.v("Mac", Mac);  
        return Mac.trim();    
    }
	/*
	 * 根据包名启动一个app
	 */
	public static void startAPP(Context context,String appPackageName){
	    try{
	        Intent intent = context.getPackageManager().getLaunchIntentForPackage(appPackageName);
	        context.startActivity(intent);
	    }catch(Exception e){
	        Toast.makeText(context, "没有安装", Toast.LENGTH_LONG).show();
	    }
	}
	//检查packageName包是否在已安装目录中
	public static boolean isAppInstalled(Context context, String packageName) {  
        final PackageManager packageManager = context.getPackageManager();  
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);  
        List<String> pName = new ArrayList<String>();  
        if (pinfo != null) {  
            for (int i = 0; i < pinfo.size(); i++) {  
                String pn = pinfo.get(i).packageName;  
                pName.add(pn);  
            }  
        }  
        return pName.contains(packageName);  
    }  
	/** 
	 * 请求ROOT权限后执行命令（最好开启一个线程） 
	 * @param cmd   (pm install -r *.apk) 
	 * @param packagestartcmd (am start -n 包名/包名.mainactivity)
	 * @return 
	 */  
	public static boolean runRootCommand(String cmd ,String packagestartcmd) {   
	       Process process = null;   
	       DataOutputStream os = null;   
	    BufferedReader br = null;  
	    StringBuilder sb = null;  
	           try {   
	           process = Runtime.getRuntime().exec("su");   
	           os = new DataOutputStream(process.getOutputStream());   
	           os.writeBytes(cmd+"\n"); 
	           os.writeBytes(packagestartcmd+"\n");
	           os.writeBytes("exit\n");   
	        br = new BufferedReader(new InputStreamReader(process.getInputStream()));  
	          
	        sb = new StringBuilder();  
	        String temp=null;  
	        while((temp = br.readLine())!=null){  
	            sb.append(temp+"\n");  
	            if("Success".equalsIgnoreCase(temp)){  
	                Log.e("update_thread","----------"+sb.toString());  
	                return true;   
	            }  
	        }  
	           process.waitFor();   
	           } catch (Exception e) {   
	        	   Log.e("update_thread","异常："+e.getMessage());   
	           }   
	           finally {   
	               try {   
	                   if (os != null) {   
	                    os.flush();   
	                       os.close();   
	                   }   
	                   if(br!=null){  
	                    br.close();  
	                   }  
	                   process.destroy();   
	               } catch (Exception e) {   
	                return false;   
	               }   
	           }   
	           return false;   
	   }
}
