package com.zlf.timer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.zlf.timer.event.TimerOperEvent;
import com.zlf.timer.event.TimerTicEvent;
import com.zlf.timer.timer.TimerService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private TextView textView;
    private Intent mIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.start).setOnClickListener(this);
        findViewById(R.id.stop).setOnClickListener(this);
        findViewById(R.id.pause).setOnClickListener(this);
        ((TextView) findViewById(R.id.desc)).setText("最多可计时：" + getTime(Integer.MAX_VALUE));
        textView = findViewById(R.id.text);

        SharedPreferences sp = getSharedPreferences("data", Context.MODE_PRIVATE);
        textView.setText(getTime(sp.getInt("count", 0)));
        textView.append("");

        mIntent = new Intent(this, TimerService.class);

        EventBus.getDefault().register(this);

        String flag = sp.getString("flag", "");
        if (!"".equals(flag) && flag.equals("start")) {
            mIntent.putExtra("count", sp.getInt("count", 0));
            mIntent.putExtra("flag", "start");
            startService(mIntent);
        }

    }

    private String getTime(int count) {
        int hour = count / 3600;
        int min = (count - hour * 3600) / 60;
        int second = count % 60;
        return String.format(Locale.CHINA,"%d时 %02d分 %02d秒", hour, min, second);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }

    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onClick(View v) {
        SharedPreferences sp = getSharedPreferences("data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        switch (v.getId()) {
            case R.id.start:

                mIntent.putExtra("count", sp.getInt("count", 0));
                mIntent.putExtra("flag", "start");
                startService(mIntent);
                editor.putString("flag", "start");
                break;

            case R.id.stop:

                stopService(mIntent);
                editor.putString("flag", "stop");
                break;

            case R.id.pause:
                mIntent.putExtra("flag", "pause");
                startService(mIntent);
                editor.putString("flag", "pause");
                break;
        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(TimerTicEvent event) {
        textView.setText(String.valueOf(event.count));
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences sp = getSharedPreferences("data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("count", Integer.decode(textView.getText().toString()));
        editor.commit();
    }
}
