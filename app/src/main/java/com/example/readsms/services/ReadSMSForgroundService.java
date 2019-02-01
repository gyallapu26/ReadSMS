package com.example.readsms.services;

import android.app.*;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsMessage;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import com.example.readsms.MainActivity;
import com.example.readsms.R;
import static com.example.readsms.application.App.CHANNEL_ID;

public class ReadSMSForgroundService extends Service {


    BroadcastReceiver callExplicitReceiver;

    @Override
    public void onCreate() {
        Log.d("sos", "oncreate called");

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.setPriority(2147483647);
        intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        this.callExplicitReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle extras = intent.getExtras();

                String strMessage = "";

                if ( extras != null )
                {
                    Object[] smsextras = (Object[]) extras.get( "pdus" );

                    for ( int i = 0; i < smsextras.length; i++ )
                    {
                        SmsMessage smsmsg = SmsMessage.createFromPdu((byte[])smsextras[i]);

                        String strMsgBody = smsmsg.getMessageBody().toString();
                        String strMsgSrc = smsmsg.getOriginatingAddress();

                        strMessage += "SMS from " + strMsgSrc + " : " + strMsgBody;

                        Log.i("sos", strMessage);
                        showNotification( context, strMsgSrc, strMsgBody) ;
                    }

                }
            }

            private void showNotification(Context ctx, String strMsgSrc, String strMsgBody) {


                 NotificationManager mNotificationManager;

                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(ctx.getApplicationContext(), "notify_001");
                Intent ii = new Intent(ctx.getApplicationContext(), MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(ctx, 0, ii, 0);

                NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
                //bigText.bigText(strMsgSrc);
                bigText.setBigContentTitle(strMsgSrc);
                bigText.setSummaryText(strMsgBody);

                mBuilder.setContentIntent(pendingIntent);
                mBuilder.setSmallIcon(R.mipmap.ic_launcher_round);
                mBuilder.setContentTitle(strMsgSrc);
                mBuilder.setContentText(strMsgBody);
                mBuilder.setPriority(Notification.PRIORITY_MAX);
                mBuilder.setStyle(bigText);

                mNotificationManager =
                        (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    String channelId = "";
                    NotificationChannel channel = new NotificationChannel(channelId,
                            "id",
                            NotificationManager.IMPORTANCE_DEFAULT);
                    mNotificationManager.createNotificationChannel(channel);
                    mBuilder.setChannelId(channelId);
                }

                mNotificationManager.notify(0, mBuilder.build());
            }
        };
        registerReceiver(callExplicitReceiver, intentFilter);
        super.onCreate();
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d("sos", "onStartCommand called");


        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Read sms service")
                .setContentText("running.......")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);

        //do heavy work on a background thread
        //stopSelf();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("sos", "ondestroy called");
        unregisterReceiver(callExplicitReceiver);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static class ExampleJobService extends JobService {
        private static final String TAG = "ExampleJobService";
        private boolean jobCancelled = false;

        @Override
        public boolean onStartJob(JobParameters params) {
            Log.d(TAG, "Job started");
            doBackgroundWork(params);

            return true;
        }

        private void doBackgroundWork(final JobParameters params) {

            Log.d("sos", "job doBackGroundWOrk" + params);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < 10; i++) {
                        Log.d("sos", "run: " + i);
                        if (jobCancelled) {
                            return;
                        }

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    Log.d("sos", "Job finished");
                    jobFinished(params, false);
                }
            }).start();
        }

        @Override
        public boolean onStopJob(JobParameters params) {
            Log.d(TAG, "Job cancelled before completion");
            jobCancelled = true;
            return true;
        }
    }
}