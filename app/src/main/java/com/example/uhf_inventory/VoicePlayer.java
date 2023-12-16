package com.example.uhf_inventory;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.widget.Toast;

public class VoicePlayer {
    private static SoundPool soundPool=null;
    private static int soundID=0;
    private static VoicePlayer mInst=null;
    private float audioCurrentVolume=0;
    private final Context m_ctx;
    public static VoicePlayer GetInst(Context ctx)
    {
        if(mInst==null){
            mInst=new VoicePlayer(ctx);
        }
        soundPool=new SoundPool(10, AudioManager.STREAM_MUSIC,5);
        soundID=soundPool.load(ctx, R.raw.msg,1);
        return mInst;
    }
    private VoicePlayer(Context ctx){
        m_ctx=ctx;
    }

    @Override
    protected void finalize() throws Throwable {
        if(soundPool!=null){
            soundPool.release();
        }
        super.finalize();
    }

    public void Play(){
        AudioManager am=(AudioManager) m_ctx.getSystemService(Context.AUDIO_SERVICE);
        audioCurrentVolume=am.getStreamVolume(AudioManager.STREAM_MUSIC);
        //region ---Plan A---
        //plan A 直接进行播放
        // 缺点是有些设备会杀掉soundID,导致播放失败
        // 优点是播放声音不会影响盘点速度
        soundPool.play(soundID,audioCurrentVolume,audioCurrentVolume,0,0,1);
       // Toast.makeText(m_ctx,soundID+"",Toast.LENGTH_SHORT).show();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //endregion
        //region ---Plan B---通过线程播放
        //Plan B 监听播放 每次播放创建soundID
      /*  //缺点是:播放声音会影响盘点速度
        //优点是：防止内存自动释放掉soundID ，不会播放失败
       // Toast.makeText(m_ctx,soundID+"",Toast.LENGTH_SHORT).show();
        soundID= soundPool.load(m_ctx, R.raw.msg,1);
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int i, int i1) {

                    //Toast.makeText(m_ctx,i1+"",Toast.LENGTH_SHORT).show();

                soundPool.play(soundID,audioCurrentVolume,audioCurrentVolume,0,0,1);
                try {
                    Thread.sleep(80);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                 soundPool.unload(soundID);
                //soundPool.release();

            }
        });*/
        //endregion
    }
}
