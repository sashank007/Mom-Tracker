package com.example.myapplication.Receivers;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.telephony.SmsMessage;
import android.widget.Toast;

import com.example.myapplication.Data.Expense;
import com.example.myapplication.Services.NotificationIntentService;

public class SmsReceiver extends BroadcastReceiver {

    //interface
    private static SmsListener mListener;

    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "myapp:wakelock2");
        wl.acquire();
        Bundle data  = intent.getExtras();
        Object[] pdus = (Object[]) data.get("pdus");

        for(int i=0;i<pdus.length;i++){
            SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdus[i]);
            String dollarAmount="0";
            String sender = smsMessage.getDisplayOriginatingAddress();
            String body = smsMessage.getMessageBody().toLowerCase();
            //Check the sender to filter messages which we require to read
            System.out.println("smsMessage:"  + smsMessage.getMessageBody());
            if (body.contains("debit") || body.contains("debited"))
            {
                String[] words = body.split(" ");
                for(int j = 0 ;j <words.length;j++)
                {
                    if(words[j].startsWith("$"))
                    {
                        dollarAmount = words[j];
                    }
                }
                String messageBody = smsMessage.getMessageBody().toLowerCase();
                Toast.makeText(context , messageBody, Toast.LENGTH_LONG).show();
                Intent intent1 = new Intent(context, NotificationIntentService.class );
                intent1.putExtra("amount",dollarAmount);

                context.startService(intent1);
            }
        }
        wl.release();

    }

    public static void bindListener(SmsListener listener) {
        mListener = listener;
    }
}
