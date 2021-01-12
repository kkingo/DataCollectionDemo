package site.bdsc.localization.myapplication.tools;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.List;

public class WiFiScanHelper {
	private WifiManager localWifiManager;
	//提供Wifi管理的各种主要API，主要包含wifi的扫描、建立连接、配置信息等
	private List<WifiConfiguration> wifiConfigList;
	//WIFIConfiguration描述WIFI的链接信息，包括SSID、SSID隐藏、password等的设置
	private WifiInfo wifiConnectedInfo;
	//已经建立好网络链接的信息

	private Context mContext;

	public WiFiScanHelper(Context context){
		mContext = context;
		localWifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
	}

	/**
	 * 检查WIFI状态
	 * @return
	 */
	public int WifiCheckState(){
		return localWifiManager.getWifiState();
	}

	/**
	 * 开启WIFI
	 */
	public void WifiOpen(){
		if(!localWifiManager.isWifiEnabled()){
			localWifiManager.setWifiEnabled(true);
		}
	}

	/**
	 * 扫描wifi
	 */
	public boolean WifiStartScan(){
		return localWifiManager.startScan();
	}
	/**
	 * 得到Scan结果
	 * @return
	 */
	public List<ScanResult> getScanResults(){
		return localWifiManager.getScanResults();//得到扫描结果
	}

	/**
	 * 得到Wifi配置好的信息
	 */
	public void getConfiguration(){
		wifiConfigList = localWifiManager.getConfiguredNetworks();//得到配置好的网络信息
		for(int i =0;i<wifiConfigList.size();i++){
			Log.i("getConfiguration",wifiConfigList.get(i).SSID);
			Log.i("getConfiguration", String.valueOf(wifiConfigList.get(i).networkId));
		}
	}

	//得到建立连接的信息
	public void getConnectedInfo(){
		wifiConnectedInfo = localWifiManager.getConnectionInfo();
	}

	//得到连接的MAC地址
	public String getConnectedMacAddr(){
		return (wifiConnectedInfo == null)? "NULL":wifiConnectedInfo.getMacAddress();
	}

}