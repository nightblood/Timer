package com.zlf.timer.timer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.zlf.timer.MainActivity;
import com.zlf.timer.R;
import com.zlf.timer.event.TimerTicEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


public class TimerService extends Service {
    public static final String ACTION_NAME = "com.zlf.timer.timer.timerservice.ACTION";
    private static final String TAG = TimerService.class.getSimpleName();
    private NotificationCompat.Builder mBuilder;
    private int notificationId = 1 ;
    private int mCount = 0;
    private String mFlag = "";
    private Handler mHandler;
    private Runnable run;
    private NotificationManager notificationManager;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: ");
        super.onCreate();
        EventBus.getDefault().register(this);
        initNotification();
        mHandler = new Handler();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String flag = intent.getStringExtra("flag");
        Log.d(TAG, "onStartCommand: " + mFlag + " " + flag);
        if (flag.equals("pause")) {
            // 当TimerService 都没启动过，就接收到了pause 时，不改变当前 mFlag状态。
            // 当TimerService 处于计时或暂停状态时，可以改变 mFlag值。
            if (!mFlag.equals(""))
                mFlag = flag;
        } else if ("".equals(mFlag) || mFlag.equals("pause")) {
            // 当TimerService 处于第一次启动时，或者处于暂停状态时，启动计时线程。
            // 第一次启动时读取初始值，处于暂停状态无需初始值。记录开始时间
            if (!mFlag.equals("pause")) {
                mCount = intent.getIntExtra("count", 0);
                SharedPreferences sp = getSharedPreferences("data", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putFloat("start_time", System.currentTimeMillis());
                editor.commit();
            }
            if ("start".equals(flag)) {
                run = new Runnable() {
                    @Override
                    public void run() {
                        mCount += 1;
                        Log.d(TAG, "run: " + getTime(mCount));
                        EventBus.getDefault().post(new TimerTicEvent(mCount));
                        if (mFlag.equals("pause")) {
                            Log.d(TAG, "run: " + mCount);
                            saveCount();
                        } else {
                            mHandler.postDelayed(this, 1000);
                        }
                    }
                };
                mHandler.post(run);
            }
            mFlag = flag;
        }
        Log.d(TAG, "onStartCommand:..... " + mFlag);
        return super.onStartCommand(intent, flags, startId);
    }

    private String getTime(int count) {
        int hour = count / 3600;
        int min = (count - hour * 3600) / 60;
        int second = count % 60;
        return String.format("%d时 %02d分 %02d秒", hour, min, second);
    }

    private void saveCount() {
        Log.d(TAG, "saveCount: " + mCount);
        SharedPreferences sp = getSharedPreferences("data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("count", mCount);
        editor.apply();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        saveCount();

        if (mFlag.equals("start")) {
            mHandler.removeCallbacks(run);
            mHandler = null;
        }
    }

    private void initNotification() {
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder = new NotificationCompat.Builder(this, "default")
                .setContentTitle("睡觉中。。。")
                .setContentText("this is content text")
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)//点击事件
                .setPriority(Notification.PRIORITY_MAX);//重要而紧急的通知，通知用户这个事件是时间上紧迫的或者需要立即处理的。

        Notification notification = mBuilder.build();
        notification.flags = Notification.FLAG_NO_CLEAR;//设置通知点击或滑动时不被清除

        startForeground(notificationId, notification);
        mBuilder.setContentText("adsddfas");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(TimerTicEvent event) {
        Log.d(TAG, "onMessageEvent: " + event.count);
        mBuilder.setContentText(getTime(event.count));
        Notification notification = mBuilder.build();
        notificationManager.notify(1, notification);
    }
}
