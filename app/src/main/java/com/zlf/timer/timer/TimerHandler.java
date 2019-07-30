package com.zlf.timer.timer;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

import static com.zlf.timer.timer.TimerService.ACTION_NAME;

public class TimerHandler extends Handler {
    private WeakReference<Context> mContext;
    public TimerHandler(Context context) {
        mContext = new WeakReference<>(context);
    }

    @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    final int count = msg.arg1;
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (mContext.get() != null) {
                                final Intent intent = new Intent(ACTION_NAME);
                                intent.putExtra("count", count + 1);
                                mContext.get().sendBroadcast(intent);

                            }

                            Message msg = obtainMessage();
                            msg.arg1 = count + 1;
                            msg.what = 0;
                            sendMessage(msg);
                        }
                    }, 1000);
                    break;
                case 1:
                    removeMessages(1);
                    break;
                default:
                    break;
            }
        }

}
