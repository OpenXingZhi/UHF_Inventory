package com.example.uhf_inventory;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.media.AudioManager;
import android.media.SoundPool;

import com.tencent.mmkv.MMKV;
import com.uhf.base.UHFManager;


/**
 * author CYD
 * date 2018/11/19
 */
public class MyApp extends Application {

    public static byte[] UHF = {0x01, 0x02, 0x03};
    private UHFManager uhfMangerImpl;
    private static MyApp myApp;
    private SoundPool soundPool;
    private int soundID;
    //是否启动盘点声音
    // Whether to activate the inventory sound
    public static boolean ifOpenSound = false;
    //应用是否处于弹框状态
    // Is the application in a pop-up box
    //  public static AlertDialog showAtd = null;
    public static int currentInvtDataType = -1;
    public static boolean ifSupportR2000Fun = true;
    public static boolean if5100Module = false;

//    public static UHFModuleType currentUHFModule = UHFModuleType.UM_MODULE;

    @Override
    public void onCreate() {
        super.onCreate();
        myApp = this;
        MMKV.initialize(this);
        // 默认true开启日志调试，false关闭
        // Default true, true to enable logging debugging, false to disable
        soundPool = new SoundPool(10, AudioManager.STREAM_SYSTEM, 5);
        soundID = soundPool.load(this, R.raw.beep, 1);
    }

    public static MyApp getMyApp() {
        return myApp;
    }

    public void setUhfMangerImpl(UHFManager uhfMangerImpl) {
        this.uhfMangerImpl = uhfMangerImpl;
    }

    public UHFManager getUhfMangerImpl() {
        return uhfMangerImpl;
    }


    //播放滴滴滴的声音
    // Play the "di di di" sound
    public void playSound() {
        soundPool.play(soundID, 1, 1, 0, 1, 1);
    }

    /**
     * 判断当前应用是否是debug状态
     * Judge whether the current application is in debug state
     *
     * @return true当前为debug版本的apk，false不是debug版本
     * True is currently the APK of the debug version, and false is not the debug version
     */
    private boolean isApkInDebug() {
        try {
            ApplicationInfo info = getApplicationInfo();
            return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception e) {
            return false;
        }
    }
}
