package com.example.uhf_inventory;

import static realid.rfidlib.EmshConstant.EmshBatteryPowerMode.EMSH_PWR_MODE_BATTERY_ERROR;
import static realid.rfidlib.EmshConstant.EmshBatteryPowerMode.EMSH_PWR_MODE_CHG_FULL;
import static realid.rfidlib.EmshConstant.EmshBatteryPowerMode.EMSH_PWR_MODE_CHG_GENERAL;
import static realid.rfidlib.EmshConstant.EmshBatteryPowerMode.EMSH_PWR_MODE_CHG_QUICK;
import static realid.rfidlib.EmshConstant.EmshBatteryPowerMode.EMSH_PWR_MODE_DSG_UHF;
import static realid.rfidlib.EmshConstant.EmshBatteryPowerMode.EMSH_PWR_MODE_STANDBY;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import com.rfid.api.ADReaderInterface;
import com.rfid.api.BluetoothCfg;
import com.rfid.api.GFunction;
import com.rfid.api.ISO18000p6CInterface;
import com.rfid.def.ApiErrDefinition;
import com.rfid.def.RfidDef;
import com.uhf.base.UHFManager;
import com.uhf.base.UHFModuleType;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TabHost.TabSpec;

import realid.rfidlib.EmshConstant;

public class MainActivity extends Activity implements OnClickListener
{
	private VoicePlayer voicePlayer=null;
	private TabHost myTabhost = null;
	private int[] layRes = { R.id.tab_reader,R.id.tab_inventory,R.id.tab_frequency};
	private String[] layTittle = { "RFID", "INVENTORY","Frequency"};
	private byte[] antannaDefault=null;
	private boolean isExit;
	private boolean powerOn=true;

	private boolean ifPowerOn;
	private Timer mTimer = null;
	private TimerTask mTimerTask = null;
	private Thread m_initConfig=null;

	private RadioGroup rbtn_radioGroup=null;
	private Spinner sn_region=null;
	private Spinner sn_frequencyCount=null;
	private Spinner sn_customFrequency=null;
	private EditText ed_minFrequency=null;
	private EditText ed_maxFrequency=null;
	private EditText edit_frequency1=null;
	private EditText edit_frequency2=null;
	private EditText edit_frequency3=null;
	private EditText edit_frequency4=null;
	private EditText edit_frequency5=null;
	private EditText edit_frequency6=null;
	private Spinner sn_readPower=null;
	private Spinner sn_writePower=null;
	private Spinner sn_commType = null;// Connector
	private Spinner sn_devName = null;// Device type
	private EditText ed_ipAddr = null;// IP
	private EditText ed_port = null;// Port
	private Spinner sn_bluetooth = null;// bluetooth
	private Spinner sn_comName = null;// com
	private Spinner sn_comBaud = null;
	private Spinner sn_comFrame = null;
	private Button btn_antannaConfirm=null;
	private Button btn_antannaDefault=null;
	private Button btn_frequencyConfirm=null;
	private Button btn_frequencyDefault=null;
	private Button btn_connect = null;// connect tag
	private Button btn_disconnect = null;// disconnect
	private Button btn_getDevInfo = null;// get device information
	private Button btn_startInventory = null;
	private Button btn_stopInventory = null;
	private Button btn_paraInventory = null;
	private Button btn_clearInventoryList = null;
	private TextView tv_inventoryInfo = null;
	static ADReaderInterface m_reader = new ADReaderInterface();
	static PARAMETERS invenParams = new PARAMETERS();
	private Thread m_inventoryThread = null;
	private final static int INVENTORY_MSG = 1;
	private final static int INVENTORY_FAIL_MSG = 2;
	private final static int THREAD_END = 3;

	private ListView list_inventory_record = null;// inventory list
	private List<InventoryReport> inventoryList = new ArrayList<InventoryReport>();
	private InventoryAdapter inventoryAdapter = null;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		myTabhost = (TabHost) findViewById(R.id.tabhost);

		String[] tabFrequency = { "Write", "Lock" };
		if(Locale.ENGLISH.getLanguage().equals(getCurrLanguage(this))){
			layTittle = new String[]{ "RFID", "INVENTORY","Frequency"};
			tabFrequency = new String[]{ "Antanna", "Frequency" };
		}else
		{
			layTittle =new String[]{ "射频识别", "盘点标签","射频配置"};
			tabFrequency = new String[]{ "天线", "射频" };
		}
		myTabhost.setup();
		for (int i = 0; i < layRes.length; i++)
		{
			TabSpec myTab = myTabhost.newTabSpec("tab" + i);
			myTab.setIndicator(layTittle[i]);
			myTab.setContent(layRes[i]);
			myTabhost.addTab(myTab);
		}
		myTabhost.setCurrentTab(0);


		int[] embeddedLayRes = { R.id.antanna,
				R.id.radioFrequency };
		TabHost embeddedHost = (TabHost) findViewById(R.id.tab_frequency);
		embeddedHost.setup();
		for (int i = 0; i < embeddedLayRes.length; i++)
		{
			TabSpec myTab = embeddedHost.newTabSpec("tab" + i);
			myTab.setIndicator(tabFrequency[i]);
			myTab.setContent(embeddedLayRes[i]);
			embeddedHost.addTab(myTab);
		}
		embeddedHost.setCurrentTab(0);

		// Inventory list tittle
		ViewGroup InventorytableTitle = (ViewGroup) findViewById(R.id.inventorylist_title);
		InventorytableTitle.setBackgroundColor(Color.rgb(255, 100, 10));

		rbtn_radioGroup=findViewById(R.id.radioGroup);
		sn_region=findViewById(R.id.spRegion);
		sn_frequencyCount=findViewById((R.id.spFrequencyCount));
		sn_customFrequency=findViewById(R.id.spCustomFrequency);
		ed_maxFrequency=findViewById((R.id.etxtMaxFrequency));
		ed_minFrequency=findViewById((R.id.etxtMinFrequency));
		edit_frequency1=findViewById(R.id.etxtFrequency1);
		edit_frequency2=findViewById(R.id.etxtFrequency2);
		edit_frequency3=findViewById(R.id.etxtFrequency3);
		edit_frequency4=findViewById(R.id.etxtFrequency4);
		edit_frequency5=findViewById(R.id.etxtFrequency5);
		edit_frequency6=findViewById(R.id.etxtFrequency6);
		sn_readPower=findViewById(R.id.spReadPower);
		sn_writePower=findViewById(R.id.spWritePower);
		sn_commType = (Spinner) findViewById(R.id.sn_commType);
		sn_devName = (Spinner) findViewById(R.id.sn_devType);
		ed_ipAddr = (EditText) findViewById(R.id.ed_ipAddr);
		ed_port = (EditText) findViewById(R.id.ed_port);
		sn_bluetooth = (Spinner) findViewById(R.id.sn_blueName);
		sn_comName = (Spinner) findViewById(R.id.sn_comName);
		sn_comBaud = (Spinner) findViewById(R.id.sn_comBaud);
		sn_comFrame = (Spinner) findViewById(R.id.sn_comFrame);
		btn_antannaConfirm=findViewById(R.id.btnAntannaConfirm);
		btn_antannaDefault=findViewById(R.id.btnAntannaDefault);
		btn_frequencyConfirm=findViewById(R.id.btnFrequencyConfirm);
		btn_frequencyDefault=findViewById(R.id.btnFrequencyDefault);
		btn_connect = (Button) findViewById(R.id.btn_connect);
		btn_disconnect = (Button) findViewById(R.id.btn_disconnect);
		btn_getDevInfo = (Button) findViewById(R.id.btn_infor);
		btn_startInventory = (Button) findViewById(R.id.btn_startInventory);
		btn_stopInventory = (Button) findViewById(R.id.btn_stopInventory);
		btn_paraInventory = (Button) findViewById(R.id.btn_paraInventory);
		btn_clearInventoryList = (Button) findViewById(R.id.btn_clearInventoryList);
		list_inventory_record = (ListView) findViewById(R.id.list_inventory_record);
		tv_inventoryInfo = (TextView) findViewById(R.id.tv_inventoryInfo);

		sn_commType.setSelection(1);
		sn_commType.setOnItemSelectedListener(new OnItemSelectedListener()
		{

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id)
			{
				CommTypeChange();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent)
			{
			}
		});

		btn_connect.setOnClickListener(this);
		btn_disconnect.setOnClickListener(this);
		btn_getDevInfo.setOnClickListener(this);
		btn_startInventory.setOnClickListener(this);
		btn_paraInventory.setOnClickListener(this);
		btn_stopInventory.setOnClickListener(this);
		btn_clearInventoryList.setOnClickListener(this);
		btn_antannaDefault.setOnClickListener(this);
		btn_antannaConfirm.setOnClickListener(this);
		btn_frequencyDefault.setOnClickListener(this);
		btn_frequencyConfirm.setOnClickListener(this);

		ArrayList<CharSequence> m_bluetoolNameList = null;
		ArrayAdapter<CharSequence> m_adaBluetoolName = null;
		m_bluetoolNameList = new ArrayList<CharSequence>();
		ArrayList<BluetoothCfg> m_blueList = ADReaderInterface
				.GetPairBluetooth();
		if (m_blueList != null)
		{
			for (BluetoothCfg bluetoolCfg : m_blueList)
			{
				m_bluetoolNameList.add(bluetoolCfg.GetName());
			}
		}

		m_adaBluetoolName = new ArrayAdapter<CharSequence>(this,
				android.R.layout.simple_spinner_dropdown_item,
				m_bluetoolNameList);
		sn_bluetooth.setAdapter(m_adaBluetoolName);

		// Get the Serial port
		ArrayList<CharSequence> m_comNameList = new ArrayList<CharSequence>();
		String m_comList[] = ADReaderInterface.GetSerialPortPath();
		for (String s : m_comList)
		{
			m_comNameList.add(s);
		}
		ArrayAdapter<CharSequence> m_adaComName = new ArrayAdapter<CharSequence>(
				this, android.R.layout.simple_spinner_dropdown_item,
				m_comNameList);
		if(m_adaComName.getCount()<=0)
		{

			m_adaComName.add("/dev/ttyS1");
		    m_adaComName.add("/dev/ttyS0");
		    m_adaComName.add("/dev/ttyS2");
		}

		sn_comName.setAdapter(m_adaComName);

		ArrayList<CharSequence> antennaList = new ArrayList<CharSequence>();
		for (int i = 1; i <= 4; i++)
		{
			antennaList.add(i + "");
		}
		
		inventoryAdapter = new InventoryAdapter(this, inventoryList);
		list_inventory_record.setAdapter(inventoryAdapter);
		sn_devName.setSelection(1);
		sn_comBaud.setSelection(2);
		sn_comFrame.setSelection(2);
		LoadActivityByHistory();
		AllControlVisible(false);
	}

	public static String getCurrLanguage(Context context) {
		Locale locale = null;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			locale = context.getResources().getConfiguration().getLocales().get(0);

		} else {
			locale = context.getResources().getConfiguration().locale;

		}

		return locale.getLanguage();

	}


	/*@Override
	public boolean onTouchEvent(android.view.MotionEvent event) {
		switch(event.getAction()){
			case MotionEvent.ACTION_DOWN:{ //手指下来的时候,取消之前绑定的Runnable
				mHandler.removeCallbacks(runnable);
				break;
			}
			case MotionEvent.ACTION_UP:{ //手指离开屏幕，发送延迟消息 ，5秒后执行
				mHandler.sendEmptyMessageDelayed(0, 1000 * 5);
				break;
			}
		}
		return super.onTouchEvent(event);
	};

	private Runnable runnable = new Runnable() {
		@Override
		public void run() {
			//用户5秒没操作了
			recyleResoure();
			btn_connect.setEnabled(true);
			btn_disconnect.setEnabled(false);
			btn_getDevInfo.setEnabled(false);
		}
	};*/

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d("close app","close");

		super.onActivityResult(requestCode, resultCode, data);
	}

	//退出当前Activity时被调用,调用之后Activity就结束了
	/*protected void onDestroy()
	{
		super.onDestroy();
		if (m_reader.isReaderOpen())
		{
			stopInventory();
			m_reader.RDR_Close();
		}
		//super.onDestroy();
		if(m_initConfig!=null){
			if(m_initConfig.isAlive()){
				m_initConfig.destroy();
			}

		}
	}*/

	private static volatile long lastTime = 0;
	@Override
	public void onBackPressed() {
		long currentTime = SystemClock.currentThreadTimeMillis();
		if (lastTime != 0 && currentTime - lastTime < 500) {
			recyleResoure();
		}
		lastTime = currentTime;
	}

	//当子页面调用finish()方法，则onPause()方法被触发
	//当用户按下Home键、息屏，该方法会被触发
	//Activity被覆盖到下面或者锁屏时被调用
	//@Override
	protected void onPause() {

		super.onPause();
		if(powerOn){
			if (m_reader.isReaderOpen())
			{
				stopInventory();
				m_reader.RDR_Close();
			}
			//super.onPause();
			recyleResoure();
			AllControlVisible(false);
			btn_connect.setEnabled(true);
			btn_disconnect.setEnabled(false);
			btn_getDevInfo.setEnabled(false);
		}

	}

	private  long mLastActionTime; // 上一次操作时间
	// 每当用户接触了屏幕，都会执行此方法
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		mLastActionTime = System.currentTimeMillis();
		return super.dispatchTouchEvent(ev);
	}

	//手柄按钮控制RFID线程读取与停止
	// Handle button controls RFID thread reading and stopping
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_F8 || keyCode == KeyEvent.KEYCODE_F4 || keyCode == KeyEvent.KEYCODE_BUTTON_4) {

			if(m_reader.isReaderOpen() && m_inventoryThread==null)
			{
				myTabhost.setCurrentTab(1);
				startInventory();
			}

		}
		if(keyCode==KeyEvent.KEYCODE_BACK){
			exitByDoubleClick();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_F8 || keyCode == KeyEvent.KEYCODE_F4 || keyCode == KeyEvent.KEYCODE_BUTTON_4) {

			if(m_reader.isReaderOpen() && m_inventoryThread!=null){
				myTabhost.setCurrentTab(1);
				stopInventory();
			}
		}
		return super.onKeyUp(keyCode, event);
	}

	private void exitByDoubleClick() {
		Timer tExit=null;
		if(!isExit){
			isExit=true;
			tExit=new Timer();
			tExit.schedule(new TimerTask() {
				@Override
				public void run() {
					isExit=false;//取消退出
				}
			},2000);// 如果2秒钟内没有按下返回键，则启动定时器取消掉刚才执行的任务
		}else{
			m_reader.RDR_Close();
			recyleResoure();
			btn_connect.setEnabled(true);
			btn_disconnect.setEnabled(false);
			btn_getDevInfo.setEnabled(false);
			finish();
			System.exit(0);
		}
	}

	private void CommTypeChange()
	{
		RelativeLayout grouDev=findViewById(R.id.group_dev);
		LinearLayout bluetoothView = (LinearLayout) findViewById(R.id.group_bluetooth);
		RelativeLayout netView = (RelativeLayout) findViewById(R.id.group_net);
		RelativeLayout comView = (RelativeLayout) findViewById(R.id.group_com);
		int index=sn_commType.getSelectedItemPosition();
		//sn_commType.setSelection(0);
		//sn_devName.setSelection(1);
		//sn_comBaud.setSelection(2);
		//sn_comFrame.setSelection(2);
		switch (sn_commType.getSelectedItemPosition())
		{
		case 0:
			bluetoothView.setVisibility(View.VISIBLE);//蓝牙
			netView.setVisibility(View.GONE);
			comView.setVisibility(View.GONE);
			break;
		case 1:
			//grouDev.setVisibility(View.GONE);
			bluetoothView.setVisibility(View.GONE);
			netView.setVisibility(View.GONE);
			comView.setVisibility(View.VISIBLE);
			break;
		case 2:
			bluetoothView.setVisibility(View.GONE);
			netView.setVisibility(View.VISIBLE);
			comView.setVisibility(View.GONE);
			break;
		case 3:
			bluetoothView.setVisibility(View.GONE);
			netView.setVisibility(View.GONE);
			comView.setVisibility(View.GONE);
			break;
		case 4:
			bluetoothView.setVisibility(View.GONE);
			netView.setVisibility(View.GONE);
			comView.setVisibility(View.VISIBLE);
			break;
		default:
			bluetoothView.setVisibility(View.GONE);
			netView.setVisibility(View.GONE);
			comView.setVisibility(View.GONE);
			break;
		}
	}

	private void OpenDevice()
	{
		String conStr = "";
		String devName = "";
		devName = sn_devName.getSelectedItem().toString();
		if(devName.equals("(U)AH2212")){
			devName="UM200";
		}
		int mIdx = sn_commType.getSelectedItemPosition();
		if (mIdx == 0)
		{
			if (sn_bluetooth.getAdapter().isEmpty())
			{
				Toast.makeText(this, "Please specify the bluetooth.",
						Toast.LENGTH_LONG).show();
				return;
			}
			String bluetoolName = sn_bluetooth.getSelectedItem().toString();
			if (bluetoolName == "")
			{
				Toast.makeText(this, "The bluetooth is null", Toast.LENGTH_LONG)
						.show();
				return;
			}
			conStr = String.format("RDType=%s;CommType=BLUETOOTH;Name=%s",
					devName, bluetoolName);
		}
		else if (mIdx == 1)// ����
		{
			//String comLst[]=m_reader.GetSerialPortPath();

			if (sn_comName.getAdapter().isEmpty())
			{
				Toast.makeText(this, "Please specify a serial port.",
						Toast.LENGTH_LONG).show();
				//return;
			}

			conStr = String
					.format("RDType=%s;CommType=COM;ComPath=%s;Baund=%s;Frame=%s;Addr=255",
							devName, sn_comName.getSelectedItem().toString(),
							sn_comBaud.getSelectedItem().toString(),
							sn_comFrame.getSelectedItem().toString());
		}
		else if (mIdx == 2)// (commTypeStr.equals(getString(R.string.tx_type_net)))//
							// ����
		{
			String sRemoteIp = ed_ipAddr.getText().toString();
			String sRemotePort = ed_port.getText().toString();
			conStr = String.format(
					"RDType=%s;CommType=NET;RemoteIp=%s;RemotePort=%s",
					devName, sRemoteIp, sRemotePort);
		}
		else if (mIdx == 3)// (commTypeStr.equals("USB"))
		{
			// ע�⣺ʹ��USB��ʽʱ��������Ҫö������USB�豸
			// Note: Before using USB, you must enumerate all USB devices first.
			int usbCnt = ADReaderInterface.EnumerateUsb(this);
			if (usbCnt <= 0)
			{
				Toast.makeText(this, "No USB device was found.",
						Toast.LENGTH_SHORT).show();
				return;
			}

			if (!ADReaderInterface.HasUsbPermission(""))
			{
				Toast.makeText(this,
						"No permission of operating the USB device.",
						Toast.LENGTH_SHORT).show();
				ADReaderInterface.RequestUsbPermission("");
				return;
			}

			conStr = String.format("RDType=%s;CommType=USB;Description=",
					devName);
		}
		else if (mIdx == 4)// (commTypeStr.equals(getString(R.string.tx_type_usb_com)))
		{
			// Attention: Only support Z-TEK
			// ע�⣺Ŀ¼ֻ֧��Z-TEK�ͺŵ�USBת������
			int mUsbCnt = ADReaderInterface.EnumerateZTEK(this, 0x0403, 0x6001);
			if (mUsbCnt <= 0)
			{
				Toast.makeText(this,
						"No permission of operating the USB device.",
						Toast.LENGTH_SHORT).show();
				return;
			}
			conStr = String
					.format("RDType=%s;CommType=Z-TEK;port=1;Baund=%s;Frame=%s;Addr=255",
							devName, sn_comBaud.getSelectedItem().toString(),
							sn_comFrame.getSelectedItem().toString());

		}
		else
		{
			return;
		}
		if (m_reader.RDR_Open(conStr) == ApiErrDefinition.NO_ERROR)
		{
			// ///////////////////////////////////////////////////
			Toast.makeText(this, "打开设备成功.",
					Toast.LENGTH_SHORT).show();
			SaveActivity();
			AllControlVisible(true);
		}
		else
		{
			Toast.makeText(this, "打开失败，请重试.",
					Toast.LENGTH_SHORT).show();
			recyleResoure();
		}
	}

	private void AllControlVisible(boolean isConnect)
	{
		sn_devName.setEnabled(!isConnect);
		sn_commType.setEnabled(!isConnect);
		sn_bluetooth.setEnabled(!isConnect);
		ed_ipAddr.setEnabled(!isConnect);
		ed_port.setEnabled(!isConnect);
		sn_comName.setEnabled(!isConnect);
		sn_comBaud.setEnabled(!isConnect);
		sn_comFrame.setEnabled(!isConnect);
		btn_connect.setEnabled(!isConnect);
		btn_disconnect.setEnabled(isConnect);
		btn_getDevInfo.setEnabled(isConnect);
		btn_startInventory.setEnabled(isConnect);
		btn_stopInventory.setEnabled(false);
		btn_clearInventoryList.setEnabled(isConnect);
	}

	private void CloseDevice()
	{
		stopInventory();
		m_reader.RDR_Close();
		sn_devName.setEnabled(true);
		sn_commType.setEnabled(true);
		sn_bluetooth.setEnabled(true);
		ed_ipAddr.setEnabled(true);
		ed_port.setEnabled(true);
		sn_comName.setEnabled(true);
		sn_comBaud.setEnabled(true);
		sn_comFrame.setEnabled(true);
		btn_connect.setEnabled(true);
		btn_disconnect.setEnabled(false);
		btn_getDevInfo.setEnabled(false);
		AllControlVisible(false);
	}

	@SuppressLint("WorldReadableFiles")
	private void saveHistory(String sKey, String val)
	{
		@SuppressWarnings("deprecation")
		SharedPreferences preferences = this.getSharedPreferences(sKey,
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(sKey, val);
		editor.commit();
	}

	@SuppressLint("WorldReadableFiles")
	@SuppressWarnings("deprecation")
	private void saveHistory(String sKey, int val)
	{
		SharedPreferences preferences = this.getSharedPreferences(sKey,
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putInt(sKey, val);
		editor.commit();
	}

	private int GetHistoryInt(String sKey)
	{
		@SuppressWarnings("deprecation")
		@SuppressLint("WorldReadableFiles")
		SharedPreferences preferences = this.getSharedPreferences(sKey,
				Context.MODE_PRIVATE);
		return preferences.getInt(sKey, -1);
	}

	@SuppressLint("WorldReadableFiles")
	private void saveHistory(String sKey, long val)
	{
		@SuppressWarnings("deprecation")
		SharedPreferences preferences = this.getSharedPreferences(sKey,
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putLong(sKey, val);
		editor.commit();
	}

	@SuppressLint("WorldReadableFiles")
	private void saveHistory(String sKey, boolean val)
	{
		@SuppressWarnings("deprecation")
		SharedPreferences preferences = this.getSharedPreferences(sKey,
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putBoolean(sKey, val);
		editor.commit();
	}

	@SuppressWarnings("unused")
	private boolean GetHistoryBool(String sKey)
	{
		@SuppressWarnings("deprecation")
		@SuppressLint("WorldReadableFiles")
		SharedPreferences preferences = this.getSharedPreferences(sKey,
				Context.MODE_PRIVATE);
		return preferences.getBoolean(sKey, false);
	}

	@SuppressWarnings("unused")
	private long GetHistoryLong(String sKey)
	{
		@SuppressWarnings("deprecation")
		@SuppressLint("WorldReadableFiles")
		SharedPreferences preferences = this.getSharedPreferences(sKey,
				Context.MODE_PRIVATE);
		return preferences.getLong(sKey, 0);
	}

	private String GetHistoryString(String sKey)
	{
		@SuppressWarnings("deprecation")
		@SuppressLint("WorldReadableFiles")
		SharedPreferences preferences = this.getSharedPreferences(sKey,
				Context.MODE_PRIVATE);
		return preferences.getString(sKey, "");
	}

	private void SaveActivity()
	{
		int devItem = 0;
		int commItem = 0;
		int blueToolItem = 0;
		String ipStr = ed_ipAddr.getText().toString();
		String portStr = ed_port.getText().toString();
		int comNameItem = 0;
		int comBaudItem = 0;
		int comFrameItem = 0;

		if (!sn_devName.getAdapter().isEmpty())
		{
			devItem = sn_devName.getSelectedItemPosition();
		}
		if (!sn_commType.getAdapter().isEmpty())
		{
			commItem = sn_commType.getSelectedItemPosition();
		}
		if (!sn_bluetooth.getAdapter().isEmpty())
		{
			blueToolItem = sn_bluetooth.getSelectedItemPosition();
		}

		if (!sn_comName.getAdapter().isEmpty())
		{
			comNameItem = sn_comName.getSelectedItemPosition();
		}
		if (!sn_comBaud.getAdapter().isEmpty())
		{
			comBaudItem = sn_comBaud.getSelectedItemPosition();
		}
		if (!sn_comFrame.getAdapter().isEmpty())
		{
			comFrameItem = sn_comFrame.getSelectedItemPosition();
		}

		saveHistory("DEVNAME", devItem);
		saveHistory("COMMTYPE", commItem);
		saveHistory("COMBAUD", comBaudItem);
		saveHistory("COMFRAME", comFrameItem);
		saveHistory("BLUETOOL", blueToolItem);
		saveHistory("COMNAME", comNameItem);
		saveHistory("DEVIPADDR", ipStr);
		saveHistory("DEVPORT", portStr);
	}

	private void LoadActivityByHistory()
	{
		int devItem = GetHistoryInt("DEVNAME");
		if (devItem < sn_devName.getCount()&devItem>=0)
		{
			sn_devName.setSelection(devItem);
		}
		else {
			sn_devName.setSelection(1);
		}
		int commItem = GetHistoryInt("COMMTYPE");
		if (commItem < sn_commType.getCount()&commItem>=0)
		{
			sn_commType.setSelection(commItem);
		}else {
			sn_commType.setSelection(1);
		}

		int blueToolItem = GetHistoryInt("BLUETOOL");
		if (blueToolItem < sn_bluetooth.getCount())
		{
			sn_bluetooth.setSelection(blueToolItem);
		}

		int comNameItem = GetHistoryInt("COMNAME");
		if (comNameItem < sn_comName.getCount())
		{
			sn_comName.setSelection(comNameItem);
		}

		int comBaudItem = GetHistoryInt("COMBAUD");
		if (comBaudItem < sn_comBaud.getCount() && comBaudItem >= 0)
		{
			sn_comBaud.setSelection(comBaudItem);
		}
		else
		{
			sn_comBaud.setSelection(2);
		}

		int comFrameItem = GetHistoryInt("COMFRAME");
		if (comFrameItem < sn_comFrame.getCount() && comFrameItem >= 0)
		{
			sn_comFrame.setSelection(comFrameItem);
		}
		else
		{
			sn_comFrame.setSelection(2);
		}

		String sIp = GetHistoryString("DEVIPADDR");
		if (sIp != "")
		{
			ed_ipAddr.setText(sIp);
		}

		String sPort = GetHistoryString("DEVPORT");
		if (sPort != "")
		{
			ed_port.setText(sPort);
		}
	}

	private void GetInformation()
	{
		int iret = -1;
		StringBuffer buffer = new StringBuffer();
		iret = m_reader.RDR_GetReaderInfor(buffer);
		if (iret == ApiErrDefinition.NO_ERROR)
		{
			new AlertDialog.Builder(this).setTitle("")
					.setMessage(buffer.toString())
					.setPositiveButton("OK", null).show();
		}
		else
		{
			new AlertDialog.Builder(this)
					.setTitle("")
					.setMessage(
							"It is failure to get the information. Error="
									+ iret).setPositiveButton("OK", null)
					.show();
		}
	}

	private void SetAntanna(){
		long readid=sn_readPower.getSelectedItemId();
		long writeid=sn_writePower.getSelectedItemId();
		byte[] buffer=new byte[14];

		if(!m_reader.isReaderOpen()){
			Toast.makeText(this,"请连接到设备",Toast.LENGTH_SHORT).show();
			return;
		}
		int iret=0;
		iret=m_reader.RDR_ReadCfgBlock(8,buffer);

		buffer[2]= (byte) (readid+1);
		buffer[3]=(byte)(writeid+1);
		iret=m_reader.RDR_WriteConfigBlock(8,buffer,buffer.length,0xFF);
		iret=m_reader.RDR_SaveConfigBlock(8);
		Toast.makeText(this,"修改成功!",Toast.LENGTH_SHORT).show();
	}

	private void GetAntanna(){
		if(!m_reader.isReaderOpen()){
			Toast.makeText(this,"Please connect to devices",Toast.LENGTH_SHORT).show();
			return;
		}
		int iret=0;
		byte[] buffer=new byte[14];
		iret=m_reader.RDR_ReadCfgBlock(8,buffer);
		if(iret==ApiErrDefinition.NO_ERROR){
			sn_readPower.setSelection(buffer[2]-1);
			sn_writePower.setSelection(buffer[3]-1);
		}


	}

	private void SetFrequency(){
		if(!m_reader.isReaderOpen()){
			Toast.makeText(this, "Please Connect to devices!", Toast.LENGTH_SHORT).show();
		}
		int iret=0;
		byte[] buffer=new byte[14];
		iret=m_reader.RDR_ReadCfgBlock(2,buffer);
		if(iret==ApiErrDefinition.NO_ERROR){
			long regionValue=sn_region.getSelectedItemId();
			long frequencyCount=sn_frequencyCount.getSelectedItemId();
			long customFrequency=sn_customFrequency.getSelectedItemId();
			int minFrequency=Integer.parseInt(ed_minFrequency.getText().toString());
			int maxFrequency=Integer.parseInt(ed_maxFrequency.getText().toString());


			regionValue=regionValue<16?regionValue:0xFF;
			iret=m_reader.RDR_ReadCfgBlock(2,buffer);
			buffer[6]=(byte)regionValue;//region
			buffer[7]=(byte)((frequencyCount&0x2F)|((customFrequency<<6) & 0xC0));
			buffer[8]=(byte)((minFrequency&0xFF));
			buffer[9]=(byte) ((minFrequency>>8)&0xFF);
			buffer[10]=(byte) (((minFrequency>>16)&0xFF));

			buffer[11]=(byte)((maxFrequency&0xFF));
			buffer[12]=(((byte)((maxFrequency>>8)&0xFF)));
			buffer[13]=(((byte) ((maxFrequency>>16)&0xFF)));
			iret=m_reader.RDR_WriteConfigBlock(2,buffer,buffer.length,0x3FFF);
			iret=m_reader.RDR_SaveConfigBlock(2);


			int frequency1=Integer.parseInt(edit_frequency1.getText().toString());
			int frequency2=Integer.parseInt(edit_frequency2.getText().toString());
			int frequency3=Integer.parseInt(edit_frequency3.getText().toString());
			int frequency4=Integer.parseInt(edit_frequency4.getText().toString());
			int frequency5=Integer.parseInt(edit_frequency5.getText().toString());
			int frequency6=Integer.parseInt(edit_frequency6.getText().toString());
			iret=m_reader.RDR_ReadCfgBlock(5,buffer);
			if(iret==ApiErrDefinition.NO_ERROR){
				buffer[0]=(byte) (frequency1&0xFF);
				buffer[1]=(byte) ((frequency1>>8)&0xFF);
				buffer[2]=(byte) ((frequency1>>16)&0xFF);

				buffer[3]=(byte) (frequency2&0xFF);
				buffer[4]=(byte) ((frequency2>>8)&0xFF);
				buffer[5]=(byte) ((frequency2>>16)&0xFF);

				buffer[6]=(byte) (frequency3&0xFF);
				buffer[7]=(byte) ((frequency3>>8)&0xFF);
				buffer[8]=(byte) ((frequency3>>16)&0xFF);

				buffer[9]=(byte) (frequency4&0xFF);
				buffer[10]=(byte) ((frequency4>>8)&0xFF);
				buffer[11]=(byte) ((frequency4>>16)&0xFF);

				iret=m_reader.RDR_WriteConfigBlock(5,buffer,buffer.length,0x07FF);
				iret=m_reader.RDR_SaveConfigBlock(5);
			}

			iret=m_reader.RDR_ReadCfgBlock(6,buffer);
			if(iret==ApiErrDefinition.NO_ERROR){
				buffer[0]=(byte) (frequency5&0xFF);
				buffer[1]=(byte) ((frequency5>>8)&0xFF);
				buffer[2]=(byte) ((frequency5>>16)&0xFF);

				buffer[3]=(byte) (frequency6&0xFF);
				buffer[4]=(byte) ((frequency6>>8)&0xFF);
				buffer[5]=(byte) ((frequency6>>16)&0xFF);
				iret=m_reader.RDR_WriteConfigBlock(6,buffer,buffer.length,0x3F);
				iret=m_reader.RDR_SaveConfigBlock(6);
				if(iret==ApiErrDefinition.NO_ERROR){
					Toast.makeText(this, getString(R.string.success), Toast.LENGTH_SHORT).show();

				}
			}


		}
	}

	private void ReadFrequency(){
		byte[] buffer=new byte[14];
		int iret=0;
		iret=m_reader.RDR_ReadCfgBlock(2,buffer);
		if(iret==ApiErrDefinition.NO_ERROR){
			sn_region.setSelection(buffer[6]);
			sn_frequencyCount.setSelection(buffer[7]&0x2F);
			sn_customFrequency.setSelection((buffer[7]>>6)&0xFF);
			int bf8=0,bf9=0,bf10=0,bf11=0,bf12=0,bf13=0;

			if(buffer[8]<0){
				bf8=(buffer[8]+256);
			}else{
				bf8=buffer[8];
			}
			if(buffer[9]<0){
				bf9= (buffer[9]+256);
			}
			else
			{
				bf9=buffer[9];
			}
			if(buffer[10]<0){
				bf10= (buffer[10]+256);
			}else{
				bf10=buffer[10];
			}

			if(buffer[11]<0){
				bf11= (buffer[11]+256);
			}else{
				bf11=buffer[11];
			}
			if(buffer[12]<0){
				bf12= (buffer[12]+256);
			}else{
				bf12=buffer[12];
			}
			if(buffer[13]<0){
				bf13= (buffer[13]+256);
			}else{
				bf13=buffer[13];
			}
			//Toast.makeText(this, buffer[12], Toast.LENGTH_SHORT).show();
			ed_minFrequency.setText((bf8|(bf9<<8)|(bf10<<16))+"");
			ed_maxFrequency.setText((bf11|(bf12<<8)|(bf13<<16))+"");
		}

		iret=m_reader.RDR_ReadCfgBlock(5,buffer);
		int f1=0,f2=0,f3=0,f4=0,f5=0,f6=0,f7=0,f8=0,f9=0,f10=0,f11=0,f0=0;
		if(iret==ApiErrDefinition.NO_ERROR){
			if(buffer[0]<0){
				f0=(buffer[0]+256);
			}else{
				f0=buffer[0];
			}
			if(buffer[1]<0){
				f1= (buffer[1]+256);
			}
			else
			{
				f1=buffer[1];
			}
			if(buffer[2]<0){
				f2= (buffer[2]+256);
			}else{
				f2=buffer[2];
			}

			if(buffer[3]<0){
				f3= (buffer[3]+256);
			}else{
				f3=buffer[3];
			}
			if(buffer[4]<0){
				f4= (buffer[4]+256);
			}else{
				f4=buffer[4];
			}
			if(buffer[5]<0){
				f5= (buffer[5]+256);
			}else{
				f5=buffer[5];
			}

			if(buffer[6]<0){
				f6=(buffer[6]+256);
			}else{
				f6=buffer[6];
			}
			if(buffer[7]<0){
				f7= (buffer[7]+256);
			}
			else
			{
				f7=buffer[7];
			}
			if(buffer[8]<0){
				f8= (buffer[8]+256);
			}else{
				f8=buffer[8];
			}

			if(buffer[9]<0){
				f9= (buffer[9]+256);
			}else{
				f9=buffer[9];
			}
			if(buffer[10]<0){
				f10= (buffer[10]+256);
			}else{
				f10=buffer[10];
			}
			if(buffer[11]<0){
				f11= (buffer[11]+256);
			}else{
				f11=buffer[11];
			}
			edit_frequency1.setText((f0|(f1<<8)|(f2<<16))+"");
			edit_frequency2.setText((f3|(f4<<8)|(f5<<16))+"");
			edit_frequency3.setText((f6|(f7<<8)|(f8<<16))+"");
			edit_frequency4.setText((f9|(f10<<8)|(f11<<16))+"");
		}
		iret=m_reader.RDR_ReadCfgBlock(6,buffer);
		if(iret==ApiErrDefinition.NO_ERROR){
			if(buffer[0]<0){
				f0=(buffer[0]+256);
			}else{
				f0=buffer[0];
			}
			if(buffer[1]<0){
				f1= (buffer[1]+256);
			}
			else
			{
				f1=buffer[1];
			}
			if(buffer[2]<0){
				f2= (buffer[2]+256);
			}else{
				f2=buffer[2];
			}

			if(buffer[3]<0){
				f3= (buffer[3]+256);
			}else{
				f3=buffer[3];
			}
			if(buffer[4]<0){
				f4= (buffer[4]+256);
			}else{
				f4=buffer[4];
			}
			if(buffer[5]<0){
				f5= (buffer[5]+256);
			}else{
				f5=buffer[5];
			}
			edit_frequency5.setText((f0|(f1<<8)|(f2<<16))+"");
			edit_frequency6.setText((f3|(f4<<8)|(f5<<16))+"");
		}
	}

	private void SetInventoryParas()
	{
		powerOn=false;
		startActivity(new Intent(this, InventorySetActivity.class));
	}

	//private int inventoryTagNum = 0;
	//private int inventoryLoopNum = 0;
	private Handler mHandler = new MyHandler(this);

	private static class MyHandler extends Handler
	{
		private final WeakReference<MainActivity> mActivity;

		public MyHandler(MainActivity activity)
		{
			mActivity = new WeakReference<MainActivity>(activity);
		}

		@SuppressWarnings({ "unused", "unchecked" })
		public void handleMessage(Message msg)
		{
			MainActivity pt = mActivity.get();
			if (pt == null)
			{
				return;
			}
			switch (msg.what)
			{
			case INVENTORY_MSG:// �̵㵽��ǩ
				//pt.inventoryList.clear();
				if(pt.rbtn_radioGroup.getCheckedRadioButtonId()==R.id.rdbtnUnsave){
					pt.inventoryList.clear();
				}
				Vector<TagInformation>tagList = (Vector<TagInformation>) msg.obj;
				if(!tagList.isEmpty())
				{
					//读取到标签，可以播放音效
					pt.voicePlayer.Play();
				}
				for (TagInformation tag : tagList)
				{
					char epcBitsLen = 0;
					byte epcData[] = null;
					byte readData[] = null;
					long metaFlags = 0;
					int idx = 0;
					long timestamp = 0;
					long frequency = 0;
					byte rssi = 0;
					int readCnt = 0;
					metaFlags = tag.metaFlags;
					if (tag.metaFlags == 0)
					{
						metaFlags |= RfidDef.ISO18000p6C_META_BIT_MASK_EPC;
					}
					if ((metaFlags & RfidDef.ISO18000p6C_META_BIT_MASK_EPC) != 0)
					{
						if (tag.dataLen < 2)
						{
							break;
						}
						int b0 = tag.tagData[idx];
						int b1 = tag.tagData[idx + 1];
						if (b0 < 0)
						{
							b0 += 256;
						}
						if (b1 < 0)
						{
							b1 += 256;
						}
						epcBitsLen = (char) (b0 | b1 << 8);
						idx += 2;
						int epcBytes = ((epcBitsLen + 7) / 8);
						if ((tag.dataLen - idx) < epcBytes)
						{
							break;
						}
						epcData = new byte[epcBytes];
						for (int i = 0; i < epcBytes; i++)
						{
							epcData[i] = tag.tagData[idx + i];
						}
						idx += epcBytes;
					}

					if ((metaFlags & RfidDef.ISO18000P6C_META_BIT_MASK_TIMESTAMP) != 0)
					{
						if ((tag.dataLen - idx) < 4)
						{
							break;
						}
						timestamp = (long) (tag.tagData[idx]
								| (tag.tagData[idx + 1] << 8 & 0xff00)
								| (tag.tagData[idx + 2] << 16 & 0xff0000) | (tag.tagData[idx + 3] << 24 & 0xff000000));
						idx += 4;
					}

					if ((metaFlags & RfidDef.ISO18000P6C_META_BIT_MASK_FREQUENCY) > 0)
					{
						if ((tag.dataLen - idx) < 4)
						{
							break;
						}
						frequency = (long) (tag.tagData[idx]
								| (tag.tagData[idx + 1] << 8 & 0xff00)
								| (tag.tagData[idx + 2] << 16 & 0xff0000) | (tag.tagData[idx + 3] << 24 & 0xff000000));
						idx += 4;
					}

					if ((metaFlags & RfidDef.ISO18000p6C_META_BIT_MASK_RSSI) > 0)
					{
						if ((tag.dataLen - idx) < 1)
						{
							break;
						}
						rssi = tag.tagData[idx];
						idx += 1;
					}
					if ((metaFlags & RfidDef.ISO18000P6C_META_BIT_MASK_READCOUNT) > 0)
					{
						if ((tag.dataLen - idx) < 1)
						{
							break;
						}
						readCnt = tag.tagData[idx];
						if (readCnt < 0)
						{
							readCnt += 256;
						}
						idx += 1;
					}
					if ((metaFlags & RfidDef.ISO18000P6C_META_BIT_MASK_TAGDATA) > 0)
					{
						if (tag.dataLen > idx)
						{
							readData = new byte[tag.dataLen - idx];
						}
						else
						{
							break;
						}
						for (int i = idx; i < tag.dataLen; i++)
						{
							readData[i - idx] = tag.tagData[i];
						}
					}
					String strEPC = "";
					String strReadData = "";
					if (epcData != null)
					{
						strEPC = GFunction.encodeHexStr(epcData);
					}
					if (readData != null)
					{
						strReadData = GFunction.encodeHexStr(readData);
					}

					boolean b_find = false;
					for (int j = 0; j < pt.inventoryList.size(); j++)
					{
						InventoryReport mReport = pt.inventoryList.get(j);
						if (mReport.getEpcStr().equals(strEPC))
						{
							mReport.setFindCnt(mReport.getFindCnt() + 1);
							b_find = true;
							break;
						}

					}

					if (!b_find)
					{
						pt.inventoryList.add(new InventoryReport(strEPC,
								strReadData, 1));
						pt.tv_inventoryInfo.setText(String.format(
								"Tag count:%d  Loop:%dms", pt.inventoryList.size(),
								msg.arg1));
					}
				}
			
				pt.inventoryAdapter.notifyDataSetChanged();
				break;
			case INVENTORY_FAIL_MSG:
				break;
			case THREAD_END:// �߳̽���
				break;
			default:
				break;
			}
		}
	}

	private boolean b_inventoryThreadRun = false;

	private class InventoryThrd implements Runnable
	{
		public void run()
		{
			b_inventoryThreadRun = true;

			Object InvenParamSpecList = ADReaderInterface
					.RDR_CreateInvenParamSpecList();
			if (InvenParamSpecList != null)
			{
				ADReaderInterface.RDR_SetInvenStopTrigger(InvenParamSpecList,
						(byte) RfidDef.INVEN_STOP_TRIGGER_TYPE_TIMEOUT,
						invenParams.m_timeout, 0);
				Object AIPIso18000p6c = ISO18000p6CInterface
						.ISO18000p6C_CreateInvenParam(InvenParamSpecList,
								(byte) 0, (byte) 0, RfidDef.ISO18000p6C_S0,
								RfidDef.ISO18000p6C_TARGET_A,
								RfidDef.ISO18000p6C_Dynamic_Q);
				if (AIPIso18000p6c != null)
				{
					if (invenParams.m_sel.m_enable)
					{
						byte[] maskBits = new byte[invenParams.m_sel.m_maskBits
								.size()];
						for (int i = 0; i < maskBits.length; i++)
						{
							maskBits[i] = invenParams.m_sel.m_maskBits.get(i);
						}
						ISO18000p6CInterface.ISO18000p6C_SetInvenSelectParam(
								AIPIso18000p6c, invenParams.m_sel.m_target,
								invenParams.m_sel.m_action,
								invenParams.m_sel.m_memBank,
								invenParams.m_sel.m_pointer, maskBits,
								invenParams.m_sel.m_maskBitsLength, (byte) 0);

					}
					// set inventory read parameters
					if (invenParams.m_read.m_enable)
					{
						ISO18000p6CInterface.ISO18000p6C_SetInvenReadParam(
								AIPIso18000p6c, invenParams.m_read.m_memBank,
								invenParams.m_read.m_wordPtr,
								(byte) invenParams.m_read.m_wordCnt);
					}

					// Add Embedded commands
					if (invenParams.m_write.m_enable)
					{
						byte[] writeDatas = new byte[invenParams.m_write.m_datas
								.size()];
						for (int i = 0; i < writeDatas.length; i++)
						{
							writeDatas[i] = invenParams.m_write.m_datas.get(i);
						}

						ISO18000p6CInterface.ISO18000p6C_CreateTAWrite(
								AIPIso18000p6c, invenParams.m_write.m_memBank,
								invenParams.m_write.m_wordPtr,
								invenParams.m_write.m_wordCnt, writeDatas,
								(long) writeDatas.length);
					}

					if (invenParams.m_lock.m_enable)
					{
						char mask = 0, action = 0;
						mask = action = 0;
						if (invenParams.m_lock.m_userMemSelected)
						{
							mask |= 0x03;
							action |= (char) (invenParams.m_lock.m_userMem);
						}
						if (invenParams.m_lock.m_TIDMemSelected)
						{
							mask |= (0x03 << 2);
							action |= (char) (invenParams.m_lock.m_TIDMem << 2);
						}
						if (invenParams.m_lock.m_EPCMemSelected)
						{
							mask |= (0x03 << 4);
							action |= (char) (invenParams.m_lock.m_EPCMem << 4);
						}
						if (invenParams.m_lock.m_accessPwdSelected)
						{
							mask |= (0x03 << 6);
							action |= (char) (invenParams.m_lock.m_accessPwd << 6);
						}
						if (invenParams.m_lock.m_killPwdSelected)
						{
							mask |= (0x03 << 8);
							action |= (char) (invenParams.m_lock.m_killPwd << 8);
						}

						ISO18000p6CInterface.ISO18000p6C_CreateTALock(
								AIPIso18000p6c, mask, action);
					}
					// set meta flags
					if (invenParams.m_metaFlags.m_enable)
					{
						long metaFlags = 0;
						if (invenParams.m_metaFlags.m_EPC)
						{
							metaFlags |= RfidDef.ISO18000p6C_META_BIT_MASK_EPC;
						}
						if (invenParams.m_metaFlags.m_timestamp)
						{
							metaFlags |= RfidDef.ISO18000P6C_META_BIT_MASK_TIMESTAMP;
						}
						if (invenParams.m_metaFlags.m_frequency)
						{
							metaFlags |= RfidDef.ISO18000P6C_META_BIT_MASK_FREQUENCY;
						}
						if (invenParams.m_metaFlags.m_RSSI)
						{
							metaFlags |= RfidDef.ISO18000p6C_META_BIT_MASK_RSSI;
						}
						if (invenParams.m_metaFlags.m_readCnt)
						{
							metaFlags |= RfidDef.ISO18000P6C_META_BIT_MASK_READCOUNT;
						}
						if (invenParams.m_metaFlags.m_tagData)
						{
							metaFlags |= RfidDef.ISO18000P6C_META_BIT_MASK_TAGDATA;
						}
						ISO18000p6CInterface.ISO18000p6C_SetInvenMetaDataFlags(
								AIPIso18000p6c, metaFlags);
					}
					// set access password
					if (invenParams.m_read.m_enable
							|| invenParams.m_write.m_enable
							|| invenParams.m_lock.m_enable)
					{
						ISO18000p6CInterface
								.ISO18000p6C_SetInvenAccessPassword(
										AIPIso18000p6c, invenParams.m_accessPwd);
					}
				}

			}
			int loopCnt = 0;
			byte AIType = RfidDef.AI_TYPE_NEW;
			while (b_inventoryThreadRun)
			{
				Vector<TagInformation>tagList = new Vector<TagInformation>();
				int iret = m_reader.RDR_TagInventory(AIType, null, 0,
						InvenParamSpecList);
				loopCnt++;
				if (iret == ApiErrDefinition.NO_ERROR || iret == -21)
				{
					Object TagDataReport = m_reader
							.RDR_GetTagDataReport(RfidDef.RFID_SEEK_FIRST);
					while (TagDataReport != null)
					{
						long aip_id[] = new long[1];
						long tag_id[] = new long[1];
						long ant_id[] = new long[1];
						byte tagData[] = new byte[256];
						long nSize[] = new long[1];
						long metaFlags[] = new long[1];
						nSize[0] = tagData.length;

						iret = ISO18000p6CInterface.ISO18000p6C_ParseTagReport(
								TagDataReport, aip_id, tag_id, ant_id,
								metaFlags, tagData, nSize);
						if (iret == ApiErrDefinition.NO_ERROR)
						{
							String writeOper = "";
							String lockOper = "";
							if (invenParams.m_write.m_enable)
							{
								iret = ISO18000p6CInterface
										.ISO18000p6C_CheckTAWriteResult(TagDataReport);
								if (iret != 0)
								{
									writeOper = "fail";
								}
								else
								{
									writeOper = "success";

								}
							}
							if (invenParams.m_lock.m_enable)
							{
								iret = ISO18000p6CInterface
										.ISO18000p6C_CheckTALockResult(TagDataReport);
								if (iret != 0)
								{
									lockOper = "fail";
								}
								else
								{
									lockOper = "success";
								}
							}

							TagInformation tag = new TagInformation();
							tag.aip_id = aip_id[0];
							tag.tag_id = tag_id[0];
							tag.ant_id = ant_id[0];
							tag.metaFlags = metaFlags[0];
							System.arraycopy(tagData, 0, tag.tagData, 0,
									(int) nSize[0]);
							tag.dataLen = (int) nSize[0];
							tag.writeOper = writeOper;
							tag.lockOper = lockOper;
							
							tagList.add(tag);

							TagDataReport = m_reader
									.RDR_GetTagDataReport(RfidDef.RFID_SEEK_NEXT); // next
						}
					}

				}
		
				Message msg = mHandler.obtainMessage();
				msg.what = INVENTORY_MSG;
				msg.obj = tagList;
				msg.arg1 = loopCnt;
				mHandler.sendMessage(msg);
			}
			b_inventoryThreadRun = false;
			m_reader.RDR_ResetCommuImmeTimeout();
			mHandler.sendEmptyMessage(THREAD_END);// �̵����
		}
	};

	public class TagInformation
	{
		public long aip_id = 0;// The air protocol ID
		public long tag_id = 0;// The tag type ID
		public long ant_id = 0;// The antenna ID
		public long metaFlags = 0;// The meta flags
		public byte[] tagData = new byte[256];// The tag's data
		public int dataLen = 0;// The data length
		public String writeOper = "";// The result of writing the tag.
		public String lockOper = "";// The result of locking the tag.
	}

	private void startInventory()
	{
		stopInventory();
		clearInventoryList();

		voicePlayer=VoicePlayer.GetInst(this);

		m_inventoryThread = new Thread(new InventoryThrd());
		btn_startInventory.setEnabled(false);
		btn_stopInventory.setEnabled(true);
		btn_clearInventoryList.setEnabled(false);
		btn_paraInventory.setEnabled(false);
		m_inventoryThread.start();
	}

	private void stopInventory()
	{
		if (m_inventoryThread != null && m_inventoryThread.isAlive())
		{
			b_inventoryThreadRun = false;
			m_reader.RDR_SetCommuImmeTimeout();
			try
			{
				m_inventoryThread.join();
			}
			catch (InterruptedException e)
			{
			}
			m_inventoryThread = null;
		}
		btn_startInventory.setEnabled(true);
		btn_stopInventory.setEnabled(false);
		btn_clearInventoryList.setEnabled(true);
		btn_paraInventory.setEnabled(true);
	}

	public static class InventoryReport
	{
		private String epcStr;
		private String tagData;
		private long findCnt = 0;

		public InventoryReport()
		{
			super();
		}

		public InventoryReport(String epc, String data, long cnt)
		{
			super();
			this.setEpcStr(epc);
			this.setTagDataStr(data);
			this.setFindCnt(cnt);
		}

		public String getEpcStr()
		{
			return epcStr;
		}

		public void setEpcStr(String epc)
		{
			this.epcStr = epc;
		}

		public String getTagDataStr()
		{
			return tagData;
		}

		public void setTagDataStr(String data)
		{
			tagData = data;
		}

		public long getFindCnt()
		{
			return findCnt;
		}

		public void setFindCnt(long findCnt)
		{
			this.findCnt = findCnt;
		}
	}

	static public class InventoryAdapter extends BaseAdapter
	{
		private List<InventoryReport> list;
		private LayoutInflater inflater;

		public InventoryAdapter(Context context, List<InventoryReport> list)
		{
			this.list = list;
			inflater = LayoutInflater.from(context);
		}

		public int getCount()
		{
			return list.size();
		}

		public Object getItem(int position)
		{
			return list.get(position);
		}

		public long getItemId(int position)
		{
			return position;
		}

		@SuppressLint("ResourceType")
		public View getView(int position, View convertView, ViewGroup parent)
		{
			InventoryReport inventoryReport = (InventoryReport) this.getItem(position);
			ViewHolder viewHolder;
			if (convertView == null)
			{
				viewHolder = new ViewHolder();
				convertView = inflater
						.inflate(R.xml.inventorylist_tittle, null);
				viewHolder.mTextEpc = (TextView) convertView
						.findViewById(R.id.tv_inventoryEpc);
				viewHolder.mTextTagData = (TextView) convertView
						.findViewById(R.id.tv_inventoryTagData);
				viewHolder.mTextFindCnt = (TextView) convertView
						.findViewById(R.id.tv_inventoryCnt);
				convertView.setTag(viewHolder);
			}
			else
			{
				viewHolder = (ViewHolder) convertView.getTag();
			}

			long mCnt = inventoryReport.getFindCnt();
			String strCnt = mCnt > 0 ? (mCnt + "") : "1";
			viewHolder.mTextEpc.setText(inventoryReport.getEpcStr());
			viewHolder.mTextTagData.setText(inventoryReport.getTagDataStr());
			viewHolder.mTextFindCnt.setText(strCnt);

			return convertView;
		}

		private class ViewHolder
		{
			public TextView mTextEpc;
			public TextView mTextTagData;
			public TextView mTextFindCnt;
		}
	}


	private void clearInventoryList()
	{
		inventoryList.clear();
		tv_inventoryInfo.setText("Tag count:0  Loop:0");
		inventoryAdapter.notifyDataSetChanged();
	}

	@SuppressWarnings("unused")
	private String handle_tag_report(long metaFlags, byte[] tagData, long datlen)
	{
		long epcBitsLen = 0;
		int idx = 0;
		byte epc[] = null;
		byte readData[] = null;
		long timestamp;
		long frequency;
		byte rssi;
		byte readCnt;

		timestamp = 0;
		frequency = 0;
		rssi = 0;
		readCnt = 0;
		if (metaFlags == 0)
			metaFlags |= RfidDef.ISO18000p6C_META_BIT_MASK_EPC;
		if ((metaFlags & RfidDef.ISO18000p6C_META_BIT_MASK_EPC) > 0)
		{
			if (datlen < 2)
			{
				// error data size
				return "";
			}
			int b0 = tagData[idx];
			int b1 = tagData[idx + 1];
			if (b0 < 0)
			{
				b0 += 256;
			}
			if (b1 < 0)
			{
				b1 += 256;
			}
			epcBitsLen = (b0 | (b1 << 8)) & 0xffff;
			idx += 2;
			int epcBytes = (int) ((epcBitsLen + 7) / 8);
			if (((int) (datlen - idx)) < epcBytes)
			{
				// error data size
				return "";
			}
			epc = new byte[epcBytes];
			System.arraycopy(tagData, idx, epc, 0, epcBytes);
			// for (i = 0; i < epcBytes; i++) epc.Add(tagData[idx + i]);
			idx += epcBytes;
		}
		if ((metaFlags & RfidDef.ISO18000P6C_META_BIT_MASK_TIMESTAMP) > 0)
		{
			if ((datlen - idx) < 4)
			{
				// error data size
				return "";
			}
			timestamp = (tagData[idx] | (tagData[idx + 1] << 8 & 0xff00)
					| (tagData[idx + 2] << 16 & 0xff0000) | (tagData[idx + 3] << 24 & 0xff000000));
			idx += 4;
		}
		if ((metaFlags & RfidDef.ISO18000P6C_META_BIT_MASK_FREQUENCY) > 0)
		{
			if ((datlen - idx) < 4)
			{
				// error data size
				return "";
			}
			frequency = (tagData[idx] | (tagData[idx + 1] << 8 & 0xff00)
					| (tagData[idx + 2] << 16 & 0xff0000) | (tagData[idx + 3] << 24 & 0xff000000));
			idx += 4;
		}
		if ((metaFlags & RfidDef.ISO18000p6C_META_BIT_MASK_RSSI) > 0)
		{
			if ((datlen - idx) < 1)
			{
				// error data size
				return "";
			}
			rssi = tagData[idx];
			idx += 1;
		}
		if ((metaFlags & RfidDef.ISO18000P6C_META_BIT_MASK_READCOUNT) > 0)
		{
			if ((datlen - idx) < 1)
			{
				// error data size
				return "";
			}
			readCnt = tagData[idx];
			idx += 1;
		}
		if ((metaFlags & RfidDef.ISO18000P6C_META_BIT_MASK_TAGDATA) > 0)
		{
			if (datlen <= idx)
			{
				return "";
			}
			readData = new byte[(int) (datlen - idx)];
			System.arraycopy(tagData, idx, readData, 0, readData.length);
			// for (i = idx; i < (int)datlen; i++) readData.Add(tagData[i]);
		}

		if (epc != null)
		{
			return GFunction.encodeHexStr(epc);
		}
		else
		{
			return "";
		}
	}


	//下电，回收停止线程，退出应用
	// Power down, recycle stop threads, exit application
	private void recyleResoure() {
		//这里强制停止盘点，无论是否使用
		// Forced cessation of inventory, whether used or not

		if(MyApp.getMyApp().getUhfMangerImpl()!=null){
			MyApp.getMyApp().getUhfMangerImpl().powerOff();
		}

		if (mEmshStatusReceiver != null) {
			unregisterReceiver(mEmshStatusReceiver);
			mEmshStatusReceiver = null;
		}
		if (mTimer != null || mTimerTask != null) {
			mTimerTask.cancel();
			mTimer.cancel();
			mTimerTask = null;
			mTimer = null;
		}
		//rfidThread.destoryThread();
		//MyApp.getMyApp().getUhfMangerImpl().changeConfig(false);
		//System.exit(0);
	}
	//获取串口号
	private void getSerialPortNo() {
		File mFile = new File("/dev");
		File nFile = new File("/dev/ttyS0");
		Toast.makeText(this, "getSerialPortNo: " + mFile.getName() + " nFile.exists = " + nFile.exists() + " nFile = " + nFile.isFile(), Toast.LENGTH_SHORT).show();
		Log.e("TAG", "getSerialPortNo: " + mFile.getName() + " nFile.exists = " + nFile.exists() + " nFile = " + nFile.isFile());
/*        if (mFile.exists()) {
            File allFile[] = mFile.listFiles();
            for (int i = 0; i < allFile.length; i++) {
                if (allFile[i] != null) {
                    String childFileName = allFile[i].getName();
                    if (childFileName.startsWith("tty")) {
                        Log.e("TAG", "allFile[" + i + "] = " + childFileName);
                    }
                }
            }
        }*/
	}

	private void init(){
		UHFModuleType mType = UHFModuleType.UM_MODULE;
		MyApp.getMyApp().setUhfMangerImpl(UHFManager.getUHFImplSigleInstance(mType));
		UHFManager myapp=MyApp.getMyApp().getUhfMangerImpl();
		m_initConfig=new Thread(new SerialPortConfig());
		m_initConfig.start();
	}
	private class SerialPortConfig implements Runnable {
		public void run() {
			// 初始化开启把枪和串口配置(50把枪设备)
			// Initialisation of the device and serial port configuration （only 50 equipment）
			if(MyApp.getMyApp()!=null){
				if (MyApp.getMyApp().getUhfMangerImpl().getDeviceInfo().isIfHaveTrigger()) {
					ifPowerOn = MyApp.getMyApp().getUhfMangerImpl().powerOn();
					MyApp.getMyApp().getUhfMangerImpl().changeConfig(true);
					//monitorEmsh();
				} else {
					ifPowerOn = MyApp.getMyApp().getUhfMangerImpl().powerOn();
				}
				//getModuleInfo();
			}

		}
	}
	//定时监听把枪状态
	// Listening for device status at regular intervals
	private void monitorEmsh() {
		mEmshStatusReceiver = new EmshStatusBroadcastReceiver();
		IntentFilter intentFilter = new IntentFilter(EmshConstant.Action.INTENT_EMSH_BROADCAST);
		registerReceiver(mEmshStatusReceiver, intentFilter);

		mTimer = new Timer();
		mTimerTask = new TimerTask() {
			@Override
			public void run() {
				Intent intent = new Intent(EmshConstant.Action.INTENT_EMSH_REQUEST);
				intent.putExtra(EmshConstant.IntentExtra.EXTRA_COMMAND, EmshConstant.Command.CMD_REFRESH_EMSH_STATUS);
				sendBroadcast(intent);
			}
		};
		mTimer.schedule(mTimerTask, 0, 1000);
	}

	private EmshStatusBroadcastReceiver mEmshStatusReceiver;

	private int oldStatue = -1;

	public class EmshStatusBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			if (EmshConstant.Action.INTENT_EMSH_BROADCAST.equalsIgnoreCase(intent.getAction())) {

				int sessionStatus = intent.getIntExtra("SessionStatus", 0);
				int batteryPowerMode = intent.getIntExtra("BatteryPowerMode", -1);
				//  MLog.e("sessionStatus = " + sessionStatus + "  batteryPowerMode  = " + batteryPowerMode);
				Log.e("BroadcastReceiver","batteryPowerMode:"+batteryPowerMode);
				// 把枪电池当前状态
				// Current status of battery
				if ((sessionStatus & EmshConstant.EmshSessionStatus.EMSH_STATUS_POWER_STATUS) != 0) {
					//相同状态不处理
					// Same status does not process
					if (batteryPowerMode == oldStatue) {
						return;
					}
					oldStatue = batteryPowerMode;

					switch (batteryPowerMode) {
						case EMSH_PWR_MODE_STANDBY:
							if (!ifPowerOn) {
								MyApp.getMyApp().getUhfMangerImpl().powerOn();
								//getModuleInfo();
							}
							ifPowerOn = false;
							break;
						case EMSH_PWR_MODE_DSG_UHF:

							break;
						case EMSH_PWR_MODE_CHG_GENERAL:
						case EMSH_PWR_MODE_CHG_QUICK:

							break;
						case EMSH_PWR_MODE_CHG_FULL:

							break;
						default:
							break;
					}
				} else {
					oldStatue = EMSH_PWR_MODE_BATTERY_ERROR;
				}
			}
		}
	}

	private void getModuleInfo() {
		//给时间(根据机型配置性能判断，一般2.5S足以)让串口和模块初始化
		//Give time (according to the configuration performance of the model, 2.5S is generally sufficient) for the serial port and module to initialize
		SystemClock.sleep(2500);
		if (UHFModuleType.UM_MODULE == UHFManager.getType()) {
			//初始化判断UM系列的UHF模块类型
			//Initialize and judge the UHF module type of UM series
            /*String ver = MyApp.getMyApp().getUhfMangerImpl().hardwareVerGet();
            if (!TextUtils.isEmpty(ver)) {
                //判断是否支持UM7模块功能
                //Determine whether the UM 7 module function is supported
                char moduleType = ver.charAt(0);
                MyApp.ifSupportR2000Fun = moduleType == '7' || moduleType == '4' || moduleType == '5';
            }*/
		} else if (UHFModuleType.SLR_MODULE == UHFManager.getType()) {
			String type = MyApp.getMyApp().getUhfMangerImpl().getUHFModuleType();
			if (!TextUtils.isEmpty(type)) {
				if (type.contains("5100")) {
					MyApp.if5100Module = true;
				}
			}
		}
	}

	public void onClick(View v)
	{
		switch (v.getId())
		{
		case R.id.btn_connect:
			synchronized (this) {
			/*	while (!ifPowerOn){
					init();
				}*/
				init();

			}
			synchronized (this) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException exception) {
					exception.printStackTrace();
				}
				OpenDevice();
			}
		/*	init();

			OpenDevice();*/
			break;
		case R.id.btn_disconnect:
			recyleResoure();
			CloseDevice();
			break;
		case R.id.btn_infor:
			GetInformation();
			break;
		case R.id.btn_startInventory:
			startInventory();
			break;
		case R.id.btn_stopInventory:
			stopInventory();
			break;
		case R.id.btn_paraInventory:
			SetInventoryParas();
			break;
		case R.id.btn_clearInventoryList:
			clearInventoryList();
			break;
		case R.id.btnAntannaConfirm:
			SetAntanna();
			break;
		case R.id.btnFrequencyConfirm:
			SetFrequency();
			break;
		case R.id.btnAntannaDefault:
			GetAntanna();
			break;
		case R.id.btnFrequencyDefault:
			ReadFrequency();
			break;
		default:
			break;
		}
	}
}
