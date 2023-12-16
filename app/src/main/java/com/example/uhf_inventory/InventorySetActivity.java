package com.example.uhf_inventory;

import java.util.ArrayList;
import java.util.Locale;

import com.rfid.api.GFunction;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.Toast;

public class InventorySetActivity extends Activity implements OnClickListener
{
	private Spinner sp_selection_mask_bit_point = null;
	private Spinner sp_selection_mask_bit_length = null;
	private CheckBox chk_enable_selection = null;
	private Spinner sp_selection_target = null;
	private Spinner sp_selection_action = null;
	private Spinner sp_selection_memory_bank = null;
	private EditText ed_selection_mask_bit = null;
	private CheckBox chk_enable_meta_flag = null;
	private CheckBox chk_meta_select_epc = null;
	private CheckBox chk_meta_select_antenna_id = null;
	private CheckBox chk_meta_select_time_stamp = null;
	private CheckBox chk_meta_select_frequency = null;
	private CheckBox chk_meta_select_rssi = null;
	private CheckBox chk_meta_select_read_count = null;
	private CheckBox chk_meta_select_tag_data = null;
	private CheckBox chk_enable_inventory_read = null;
	private Spinner sp_inv_memory_bank = null;
	private Spinner sp_inv_word_point = null;
	private Spinner sp_inv_word_count = null;
	private CheckBox chk_enable_embedded_write = null;
	private Spinner sp_embedded_write_memory = null;
	private Spinner sp_embedded_write_word_point = null;
	private Spinner sp_embedded_write_word_count = null;
	private EditText ed_embedded_write_datas = null;
	private CheckBox chk_enable_embedded_lock = null;
	private CheckBox chk_enable_embedded_lock_user_mem = null;
	private Spinner sp_embedded_lock_user_mem = null;
	private CheckBox chk_enable_embedded_lock_tid_mem = null;
	private Spinner sp_embedded_lock_tid_mem = null;
	private CheckBox chk_enable_embedded_lock_epc_mem = null;
	private Spinner sp_embedded_lock_epc_mem = null;
	private CheckBox chk_enable_embedded_lock_access_pwb = null;
	private Spinner sp_embedded_lock_access_pwb = null;
	private CheckBox chk_enable_embedded_lock_kill_pwb = null;
	private Spinner sp_embedded_lock_kill_pwb = null;
	private EditText ed_access_pwb_datas = null;
	private EditText ed_timeout_value = null;

	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_inventory_set);
		String[] mainLayTittle = { "select", "meta", "inventory read",
				"embedded", "access pwb", "timeout" };
		if(Locale.ENGLISH.getLanguage().equals(getCurrLanguage(this))){
			mainLayTittle = new String[]{ "select", "meta", "inventory read",
					"embedded", "access pwb", "timeout" };
		}else
		{
			mainLayTittle =new String[]{ "选择参数", "meta","盘点读参数","嵌入命令","访问密码","超时"};
		}
		int[] mainLayRes = { R.id.tab_selection, R.id.tab_meta_flags,
				R.id.tab_inventory_read, R.id.tab_embedded_command,
				R.id.tab_access_pwb, R.id.tab_timeout };

		TabHost mainTabhost = (TabHost) findViewById(R.id.tabInventoryHost);
		mainTabhost.setup();
		for (int i = 0; i < mainLayRes.length; i++)
		{
			TabSpec myTab = mainTabhost.newTabSpec("tab" + i);
			myTab.setIndicator(mainLayTittle[i]);
			myTab.setContent(mainLayRes[i]);
			mainTabhost.addTab(myTab);
		}
		mainTabhost.setCurrentTab(0);

		String[] embeddedLayTittle = { "Write", "Lock" };
		int[] embeddedLayRes = { R.id.tab_embedded_write,
				R.id.tab_embedded_lock };
		TabHost embeddedHost = (TabHost) findViewById(R.id.tab_embedded_command);
		embeddedHost.setup();
		for (int i = 0; i < embeddedLayRes.length; i++)
		{
			TabSpec myTab = embeddedHost.newTabSpec("tab" + i);
			myTab.setIndicator(embeddedLayTittle[i]);
			myTab.setContent(embeddedLayRes[i]);
			embeddedHost.addTab(myTab);
		}
		embeddedHost.setCurrentTab(0);

		chk_enable_selection = (CheckBox) findViewById(R.id.chk_enable_selection);
		sp_selection_target = (Spinner) findViewById(R.id.sp_selection_target);
		sp_selection_action = (Spinner) findViewById(R.id.sp_selection_action);
		sp_selection_memory_bank = (Spinner) findViewById(R.id.sp_selection_memory_bank);
		sp_selection_mask_bit_point = (Spinner) findViewById(R.id.sp_selection_mask_bit_point);
		sp_selection_mask_bit_length = (Spinner) findViewById(R.id.sp_selection_mask_bit_length);
		ed_selection_mask_bit = (EditText) findViewById(R.id.ed_selection_mask_bit);
		chk_enable_meta_flag = (CheckBox) findViewById(R.id.chk_enable_meta_flag);
		chk_meta_select_epc = (CheckBox) findViewById(R.id.chk_meta_select_epc);
		chk_meta_select_antenna_id = (CheckBox) findViewById(R.id.chk_meta_select_antenna_id);
		chk_meta_select_time_stamp = (CheckBox) findViewById(R.id.chk_meta_select_time_stamp);
		chk_meta_select_frequency = (CheckBox) findViewById(R.id.chk_meta_select_frequency);
		chk_meta_select_rssi = (CheckBox) findViewById(R.id.chk_meta_select_rssi);
		chk_meta_select_read_count = (CheckBox) findViewById(R.id.chk_meta_select_read_count);
		chk_meta_select_tag_data = (CheckBox) findViewById(R.id.chk_meta_select_tag_data);

		chk_enable_inventory_read = (CheckBox) findViewById(R.id.chk_enable_inventory_read);
		sp_inv_memory_bank = (Spinner) findViewById(R.id.sp_inv_memory_bank);
		sp_inv_word_point = (Spinner) findViewById(R.id.sp_inv_word_point);
		sp_inv_word_count = (Spinner) findViewById(R.id.sp_inv_word_count);

		chk_enable_embedded_write = (CheckBox) findViewById(R.id.chk_enable_embedded_write);
		sp_embedded_write_memory = (Spinner) findViewById(R.id.sp_embedded_write_memory);
		sp_embedded_write_word_point = (Spinner) findViewById(R.id.sp_embedded_write_word_point);
		sp_embedded_write_word_count = (Spinner) findViewById(R.id.sp_embedded_write_word_count);
		ed_embedded_write_datas = (EditText) findViewById(R.id.ed_embedded_write_datas);

		chk_enable_embedded_lock = (CheckBox) findViewById(R.id.chk_enable_embedded_lock);
		chk_enable_embedded_lock_user_mem = (CheckBox) findViewById(R.id.chk_enable_embedded_lock_user_mem);
		sp_embedded_lock_user_mem = (Spinner) findViewById(R.id.sp_embedded_lock_user_mem);
		chk_enable_embedded_lock_tid_mem = (CheckBox) findViewById(R.id.chk_enable_embedded_lock_tid_mem);
		sp_embedded_lock_tid_mem = (Spinner) findViewById(R.id.sp_embedded_lock_tid_mem);
		chk_enable_embedded_lock_epc_mem = (CheckBox) findViewById(R.id.chk_enable_embedded_lock_epc_mem);
		sp_embedded_lock_epc_mem = (Spinner) findViewById(R.id.sp_embedded_lock_epc_mem);
		chk_enable_embedded_lock_access_pwb = (CheckBox) findViewById(R.id.chk_enable_embedded_lock_access_pwb);
		sp_embedded_lock_access_pwb = (Spinner) findViewById(R.id.sp_embedded_lock_access_pwb);
		chk_enable_embedded_lock_kill_pwb = (CheckBox) findViewById(R.id.chk_enable_embedded_lock_kill_pwb);
		sp_embedded_lock_kill_pwb = (Spinner) findViewById(R.id.sp_embedded_lock_kill_pwb);
		ed_access_pwb_datas = (EditText) findViewById(R.id.ed_access_pwb_datas);
		ed_timeout_value = (EditText) findViewById(R.id.ed_timeout_value);

		Button btn_set_cancel = (Button) findViewById(R.id.btn_set_cancel);
		Button btn_set_ok = (Button) findViewById(R.id.btn_set_ok);
		btn_set_cancel.setOnClickListener(this);
		btn_set_ok.setOnClickListener(this);

		ArrayList<CharSequence> m_maskBitList = new ArrayList<>();
		for (int j = 0; j <= 255; j++)
		{
			m_maskBitList.add(j + "");
		}
		sp_selection_mask_bit_point.setAdapter(new ArrayAdapter<>(
				this, android.R.layout.simple_spinner_dropdown_item,
				m_maskBitList));
		sp_selection_mask_bit_length.setAdapter(new ArrayAdapter<>(
				this, android.R.layout.simple_spinner_dropdown_item,
				m_maskBitList));
		sp_inv_word_point.setAdapter(new ArrayAdapter<>(this,
				android.R.layout.simple_spinner_dropdown_item, m_maskBitList));
		sp_inv_word_count.setAdapter(new ArrayAdapter<>(this,
				android.R.layout.simple_spinner_dropdown_item, m_maskBitList));
		sp_embedded_write_word_point.setAdapter(new ArrayAdapter<>(
				this, android.R.layout.simple_spinner_dropdown_item,
				m_maskBitList));
		sp_embedded_write_word_count.setAdapter(new ArrayAdapter<>(
				this, android.R.layout.simple_spinner_dropdown_item,
				m_maskBitList));

		sp_selection_mask_bit_point.setSelection(32);

		InitParameters();

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
	@SuppressLint("SetTextI18n")
	private void InitParameters()
	{
		StringBuilder selectMaskBitData = new StringBuilder();
		int selectionMaskBitLen = MainActivity.invenParams.m_sel.m_maskBitsLength;
		if (selectionMaskBitLen < 0)
		{
			selectionMaskBitLen += 255;
		}
		chk_enable_selection
				.setChecked(MainActivity.invenParams.m_sel.m_enable);
		sp_selection_target
				.setSelection(MainActivity.invenParams.m_sel.m_target);
		sp_selection_action
				.setSelection(MainActivity.invenParams.m_sel.m_action);
		sp_selection_memory_bank
				.setSelection(MainActivity.invenParams.m_sel.m_memBank);
		sp_selection_mask_bit_point
				.setSelection((int) MainActivity.invenParams.m_sel.m_pointer);
		sp_selection_mask_bit_length.setSelection(selectionMaskBitLen);
		for (int i = 0; i < MainActivity.invenParams.m_sel.m_maskBits.size(); i++)
		{
			byte[] data = new byte[] { MainActivity.invenParams.m_sel.m_maskBits
					.get(i) };
			selectMaskBitData.append(GFunction.encodeHexStr(data));
		}
		ed_selection_mask_bit.setText(selectMaskBitData.toString());

		chk_enable_meta_flag
				.setChecked(MainActivity.invenParams.m_metaFlags.m_enable);
		chk_meta_select_epc
				.setChecked(MainActivity.invenParams.m_metaFlags.m_EPC);
		chk_meta_select_frequency
				.setChecked(MainActivity.invenParams.m_metaFlags.m_frequency);
		chk_meta_select_read_count
				.setChecked(MainActivity.invenParams.m_metaFlags.m_readCnt);
		chk_meta_select_rssi
				.setChecked(MainActivity.invenParams.m_metaFlags.m_RSSI);
		chk_meta_select_tag_data
				.setChecked(MainActivity.invenParams.m_metaFlags.m_tagData);
		chk_meta_select_time_stamp
				.setChecked(MainActivity.invenParams.m_metaFlags.m_timestamp);
		chk_meta_select_antenna_id
				.setChecked(MainActivity.invenParams.m_metaFlags.m_antennaID);

		chk_enable_inventory_read
				.setChecked(MainActivity.invenParams.m_read.m_enable);
		sp_inv_memory_bank
				.setSelection(MainActivity.invenParams.m_read.m_memBank);
		sp_inv_word_point
				.setSelection((int) MainActivity.invenParams.m_read.m_wordPtr);
		sp_inv_word_count
				.setSelection((int) MainActivity.invenParams.m_read.m_wordCnt);

		StringBuilder embeddedWriteData = new StringBuilder();
		chk_enable_embedded_write
				.setChecked(MainActivity.invenParams.m_write.m_enable);
		sp_embedded_write_memory
				.setSelection(MainActivity.invenParams.m_write.m_memBank);
		sp_embedded_write_word_point
				.setSelection((int) MainActivity.invenParams.m_write.m_wordPtr);
		sp_embedded_write_word_count
				.setSelection((int) MainActivity.invenParams.m_write.m_wordCnt);
		for (int i = 0; i < MainActivity.invenParams.m_write.m_datas.size(); i++)
		{
			byte[] data = new byte[] { MainActivity.invenParams.m_write.m_datas
					.get(i) };
			embeddedWriteData.append(GFunction.encodeHexStr(data));
		}
		ed_embedded_write_datas.setText(embeddedWriteData.toString());

		byte[] accessPwb = new byte[4];
		accessPwb[0] = (byte) (MainActivity.invenParams.m_accessPwd & 0xff);
		accessPwb[1] = (byte) ((MainActivity.invenParams.m_accessPwd >> 8) & 0xff);
		accessPwb[2] = (byte) ((MainActivity.invenParams.m_accessPwd >> 16) & 0xff);
		accessPwb[3] = (byte) ((MainActivity.invenParams.m_accessPwd >> 24) & 0xff);
		ed_access_pwb_datas.setText(GFunction.encodeHexStr(accessPwb));

		ed_timeout_value.setText(MainActivity.invenParams.m_timeout + "");

		chk_enable_embedded_lock
				.setChecked(MainActivity.invenParams.m_lock.m_enable);
		chk_enable_embedded_lock_user_mem
				.setChecked(MainActivity.invenParams.m_lock.m_userMemSelected);
		chk_enable_embedded_lock_tid_mem
				.setChecked(MainActivity.invenParams.m_lock.m_TIDMemSelected);
		chk_enable_embedded_lock_epc_mem
				.setChecked(MainActivity.invenParams.m_lock.m_EPCMemSelected);
		chk_enable_embedded_lock_access_pwb
				.setChecked(MainActivity.invenParams.m_lock.m_accessPwdSelected);
		chk_enable_embedded_lock_kill_pwb
				.setChecked(MainActivity.invenParams.m_lock.m_killPwdSelected);
		sp_embedded_lock_user_mem
				.setSelection((int) MainActivity.invenParams.m_lock.m_userMem);
		sp_embedded_lock_tid_mem
				.setSelection((int) MainActivity.invenParams.m_lock.m_TIDMem);
		sp_embedded_lock_epc_mem
				.setSelection((int) MainActivity.invenParams.m_lock.m_EPCMem);
		sp_embedded_lock_access_pwb
				.setSelection((int) MainActivity.invenParams.m_lock.m_accessPwd);
		sp_embedded_lock_kill_pwb
				.setSelection((int) MainActivity.invenParams.m_lock.m_killPwd);
	}

	private void SetInventoryParameters()
	{
		MainActivity.invenParams.m_sel.m_enable = chk_enable_selection
				.isChecked();
		MainActivity.invenParams.m_sel.m_target = (byte) sp_selection_target
				.getSelectedItemPosition();
		MainActivity.invenParams.m_sel.m_action = (byte) sp_selection_action
				.getSelectedItemPosition();
		MainActivity.invenParams.m_sel.m_memBank = (byte) sp_selection_memory_bank
				.getSelectedItemPosition();
		MainActivity.invenParams.m_sel.m_pointer = sp_selection_mask_bit_point
				.getSelectedItemPosition();
		MainActivity.invenParams.m_sel.m_maskBitsLength = (byte) sp_selection_mask_bit_length
				.getSelectedItemPosition();
		String selectMaskBitData = ed_selection_mask_bit.getText().toString();
		byte[] selectMaskBit = GFunction.decodeHex(selectMaskBitData);
		if ((selectMaskBit == null || selectMaskBit.length != MainActivity.invenParams.m_sel.m_maskBitsLength/8)
				&& MainActivity.invenParams.m_sel.m_maskBitsLength != 0)
		{
			Toast.makeText(this, "Data error."+selectMaskBit.length+"maskBitLen"+MainActivity.invenParams.m_sel.m_maskBitsLength, Toast.LENGTH_LONG).show();
			return;
		}
		MainActivity.invenParams.m_sel.m_maskBits.clear();
		if (selectMaskBit!=null)
		{
			for (byte b : selectMaskBit) {
				MainActivity.invenParams.m_sel.m_maskBits.add(b);
			}
		}
		

		MainActivity.invenParams.m_metaFlags.m_enable = chk_enable_meta_flag
				.isChecked();
		MainActivity.invenParams.m_metaFlags.m_EPC = chk_meta_select_epc
				.isChecked();
		MainActivity.invenParams.m_metaFlags.m_frequency = chk_meta_select_frequency
				.isChecked();
		MainActivity.invenParams.m_metaFlags.m_readCnt = chk_meta_select_read_count
				.isChecked();
		MainActivity.invenParams.m_metaFlags.m_RSSI = chk_meta_select_rssi
				.isChecked();
		MainActivity.invenParams.m_metaFlags.m_tagData = chk_meta_select_tag_data
				.isChecked();
		MainActivity.invenParams.m_metaFlags.m_timestamp = chk_meta_select_time_stamp
				.isChecked();
		MainActivity.invenParams.m_metaFlags.m_antennaID = chk_meta_select_antenna_id
				.isChecked();
		MainActivity.invenParams.m_read.m_enable = chk_enable_inventory_read
				.isChecked();
		MainActivity.invenParams.m_read.m_memBank = (byte) sp_inv_memory_bank
				.getSelectedItemPosition();
		MainActivity.invenParams.m_read.m_wordPtr = sp_inv_word_point
				.getSelectedItemPosition();
		MainActivity.invenParams.m_read.m_wordCnt = sp_inv_word_count
				.getSelectedItemPosition();

		MainActivity.invenParams.m_write.m_enable = chk_enable_embedded_write
				.isChecked();
		MainActivity.invenParams.m_write.m_memBank = (byte) sp_embedded_write_memory
				.getSelectedItemPosition();
		MainActivity.invenParams.m_write.m_wordPtr = sp_embedded_write_word_point
				.getSelectedItemPosition();
		MainActivity.invenParams.m_write.m_wordCnt = sp_embedded_write_word_count
				.getSelectedItemPosition();

		String embeddedWriteData = ed_embedded_write_datas.getText().toString();
		byte[] embeddedWriteBy = GFunction.decodeHex(embeddedWriteData);
		if ((embeddedWriteBy == null || embeddedWriteBy.length != MainActivity.invenParams.m_write.m_wordCnt*2)
				&& MainActivity.invenParams.m_write.m_wordCnt != 0)
		{
			Toast.makeText(this, "Data error.", Toast.LENGTH_LONG).show();
			return;
		}

		MainActivity.invenParams.m_write.m_datas.clear();
		if (embeddedWriteBy!=null)
		{
			for (byte b : embeddedWriteBy) {
				MainActivity.invenParams.m_write.m_datas.add(b);
			}
		}
		
		MainActivity.invenParams.m_accessPwd = Long.parseLong(
				ed_access_pwb_datas.getText().toString(), 16);

		MainActivity.invenParams.m_timeout = Long.parseLong(ed_timeout_value
				.getText().toString(), 10);

		MainActivity.invenParams.m_lock.m_enable = chk_enable_embedded_lock
				.isChecked();

		MainActivity.invenParams.m_lock.m_userMemSelected = chk_enable_embedded_lock_user_mem
				.isChecked();

		MainActivity.invenParams.m_lock.m_TIDMemSelected = chk_enable_embedded_lock_tid_mem
				.isChecked();

		MainActivity.invenParams.m_lock.m_EPCMemSelected = chk_enable_embedded_lock_epc_mem
				.isChecked();

		MainActivity.invenParams.m_lock.m_accessPwdSelected = chk_enable_embedded_lock_access_pwb
				.isChecked();

		MainActivity.invenParams.m_lock.m_killPwdSelected = chk_enable_embedded_lock_kill_pwb
				.isChecked();

		MainActivity.invenParams.m_lock.m_userMem = sp_embedded_lock_user_mem
				.getSelectedItemPosition();

		MainActivity.invenParams.m_lock.m_TIDMem = sp_embedded_lock_tid_mem
				.getSelectedItemPosition();

		MainActivity.invenParams.m_lock.m_EPCMem = sp_embedded_lock_epc_mem
				.getSelectedItemPosition();

		MainActivity.invenParams.m_lock.m_accessPwd = sp_embedded_lock_access_pwb
				.getSelectedItemPosition();

		MainActivity.invenParams.m_lock.m_killPwd = sp_embedded_lock_kill_pwb
				.getSelectedItemPosition();

	}

	@SuppressLint("NonConstantResourceId")
	public void onClick(View v)
	{
		switch (v.getId())
		{
		case R.id.btn_set_cancel:
			finish();
			break;
		case R.id.btn_set_ok:
			SetInventoryParameters();
			finish();
			break;
		default:
			break;
		}
	}
}
