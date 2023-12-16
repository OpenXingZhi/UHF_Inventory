package com.example.uhf_inventory;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.widget.TabHost;

import java.util.Locale;

public class ConfigFrequency extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_frequency);
        String[] mainLayTittle = { "select", "meta"};
        if(Locale.ENGLISH.getLanguage().equals(getCurrLanguage(this))){
            mainLayTittle = new String[]{ "Antanna", "Frequency" };
        }else
        {
            mainLayTittle =new String[]{ "天线", "射频"};
        }
        int[] mainLayRes = { R.id.antanna, R.id.radioFrequency};

        TabHost mainTabhost = (TabHost) findViewById(R.id.tab_frequency);
        mainTabhost.setup();
        for (int i = 0; i < mainLayRes.length; i++)
        {
            TabHost.TabSpec myTab = mainTabhost.newTabSpec("tab" + i);
            myTab.setIndicator(mainLayTittle[i]);
            myTab.setContent(mainLayRes[i]);
            mainTabhost.addTab(myTab);
        }
        mainTabhost.setCurrentTab(0);
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
}