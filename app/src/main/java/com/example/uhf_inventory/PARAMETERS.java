package com.example.uhf_inventory;

import java.util.Vector;


public class PARAMETERS
{
	public SELECTION m_sel;
	public META_FLAGS m_metaFlags;
	public long m_accessPwd;
	public INVEN_READ m_read;
	public EMBEDDED_WRITE m_write;
	public long m_timeout;
	public EMBEDDED_Lock m_lock;

	public PARAMETERS()
	{
		m_sel = new SELECTION();
		m_metaFlags = new META_FLAGS();
		m_read = new INVEN_READ();
		m_write = new EMBEDDED_WRITE();
		m_lock = new EMBEDDED_Lock();
		m_timeout = 1000;
	}

	public class SELECTION
	{
		public boolean m_enable = false;
		public byte m_target = 0x04;
		public byte m_action = 0x00;
		public byte m_memBank = 0x01;
		public long m_pointer = 0x20;
		public byte m_maskBitsLength = 0;
		public Vector<Byte> m_maskBits = new Vector<Byte>();
	}

	public class META_FLAGS
	{
		public boolean m_enable = false;
		public boolean m_EPC = true;
		public boolean m_antennaID = false;
		public boolean m_timestamp = false;
		public boolean m_frequency = false;
		public boolean m_RSSI = false;
		public boolean m_readCnt = false;
		public boolean m_tagData = false;
	}

	public class INVEN_READ
	{
		public boolean m_enable = false;
		public byte m_memBank;
		public long m_wordPtr;
		public long m_wordCnt;

		public INVEN_READ()
		{
			m_enable = false;
		}
	}

	public class EMBEDDED_WRITE
	{
		public boolean m_enable;
		public byte m_memBank;
		public long m_wordPtr;
		public long m_wordCnt;
		public Vector<Byte> m_datas = new Vector<Byte>();

		public EMBEDDED_WRITE()
		{
			m_enable = false;
			m_wordPtr = 2;
			m_memBank = 01;
		}
	}

	public class EMBEDDED_Lock
	{
		public boolean m_enable;
		public boolean m_userMemSelected;
		public boolean m_TIDMemSelected;
		public boolean m_EPCMemSelected;
		public boolean m_accessPwdSelected;
		public boolean m_killPwdSelected;
		public long m_userMem;
		public long m_TIDMem;
		public long m_EPCMem;
		public long m_accessPwd;
		public long m_killPwd;

		public EMBEDDED_Lock()
		{
			m_enable = false;
			m_userMemSelected = false;
			m_TIDMemSelected = false;
			m_EPCMemSelected = false;
			m_accessPwdSelected = false;
			m_killPwdSelected = false;
			m_userMem = 0;
			m_TIDMem = 0;
			m_EPCMem = 0;
			m_accessPwd = 0;
			m_killPwd = 0;
		}
	}
}
