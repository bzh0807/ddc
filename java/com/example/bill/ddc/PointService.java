package com.example.bill.ddc;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by Bill on 2/11/2017.
 */
public class PointService extends Service {
    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    public int pointsEarned = 0;
    @Override
    public void onCreate(){
        HandlerThread thread = new HandlerThread("TutorialService",
                Process.THREAD_PRIORITY_BACKGROUND);
        // start the new handler thread
        thread.start();

        mServiceLooper = thread.getLooper();
        // start the service using the background handler
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Toast.makeText(this, "onStartCommand", Toast.LENGTH_SHORT).show();

        // call a new service handler. The service ID can be used to identify the service
        Message message = mServiceHandler.obtainMessage();
        message.arg1 = startId;
        mServiceHandler.sendMessage(message);
        return START_STICKY;
    }
    @Override
    public IBinder onBind(Intent intent){
        return null;
    }
    private final class ServiceHandler extends Handler {

        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            try {
                boolean cont = true;
                while(cont) {
                    Thread.sleep(3000);
                    TimeZone tz = TimeZone.getTimeZone("GMT-05:00");
                    Calendar c = Calendar.getInstance(tz);
                    int hr = c.get(Calendar.HOUR_OF_DAY);
                    ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(ACTIVITY_SERVICE);
                    String packageName = activityManager.getRunningTasks(1).get(0).topActivity.getPackageName();
                    //List<ActivityManager.RunningAppProcessInfo> procInfos = activityManager.getRunningAppProcesses();
                    KeyguardManager myKM = (KeyguardManager) getApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);
                    if((9 <= hr && hr < 21)||true){
                        if (myKM.inKeyguardRestrictedInputMode()) {
                            //it is locked
                            pointsEarned += 1;
                        } else {
                            //it is not locked
                            pointsEarned -= 1;
                        }
                        if (packageName.equals("com.example.bill.ddc")) {
                            if (!myKM.inKeyguardRestrictedInputMode()) {
                                pointsEarned += 1;
                            }
                            //Update point value in activity
                            //Set points earned to 0
                            Intent i = new Intent("LOCATION_UPDATED");
                            i.putExtra("newText", pointsEarned);

                            sendBroadcast(i);
                            pointsEarned = 0;
                        }
                    }
                    if (packageName.equals("com.example.bill.ddc")) {
                        //Update point value in activity
                        //Set points earned to 0
                        Intent i = new Intent("LOCATION_UPDATED");
                        i.putExtra("newText", pointsEarned);

                        sendBroadcast(i);
                    }
                    Log.d("a",""+pointsEarned);
                }
            }catch(Exception e){
                stopSelf(msg.arg1);
            }

            // Well calling mServiceHandler.sendMessage(message); from onStartCommand,
            // this method will be called.

            // Add your cpu-blocking activity here
        }
    }
}
