package com.example.adygha.smsdefender;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import static com.example.adygha.smsdefender.R.id.listView;

/**
 * Created by adygha on 27.04.2017.
 */

public class SmsReceiver extends BroadcastReceiver {
    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    public final String TAG = "SMSDefender log";
    String toastText = "Новое сообщение";
    String originatingAddress = "";
    String smsBody = "";


    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(SMS_RECEIVED)){
            Bundle bundle = intent.getExtras();

            if(bundle != null){
                Object[] pdus = (Object[]) bundle.get("pdus");
                SmsMessage[] messages = new SmsMessage[pdus.length];
                for (int i = 0; i < pdus.length; i++) {
                    messages[i] = SmsMessage.createFromPdu((byte[])pdus[i], bundle.getString("format"));
                    originatingAddress = messages[i].getOriginatingAddress();
                    smsBody = messages[i].getMessageBody();
                    if(originatingAddress.length() > 4 && (smsBody.toLowerCase().contains("ув.клиент") ||
                            smsBody.toLowerCase().contains("банк") ||
                            smsBody.toLowerCase().contains("банковск") ||
                            smsBody.toLowerCase().contains("карт") ||
                            smsBody.toLowerCase().contains("списано") ||
                            smsBody.toLowerCase().contains("руб.") ||
                            smsBody.toLowerCase().contains("баланс"))) {
                        abortBroadcast();
                        Log.d(TAG, "aborted broadcast");
                        toastText = "Нежелательное содержимое! Сообщение удалено.";

                        DatabaseHandler handler = new DatabaseHandler(context);
                        handler.addSMS(originatingAddress, smsBody);


                        Uri inboxURI = Uri.parse("content://sms/");
//                        ContentResolver cr = context.getContentResolver();
                        Cursor cur = context.getContentResolver().query(inboxURI, new String[]{"_id"}, null, null, null);
                        cur.moveToFirst();
                        String MsgId = cur.getString(0);
                        do {
                            ////Changed to 0 to get Message id instead of Thread id :
                            context.getContentResolver().delete(Uri.parse("content://sms/" + MsgId), null, null);
                            Log.d(TAG, "deleted smsId: " + cur.getString(0));
                        } while (cur.moveToNext());
                        cur.close();
                        updateListView(context);
                    }
                }
                Toast.makeText(context, toastText, Toast.LENGTH_LONG).show();

            }
        }
    }
    public void updateListView(Context context){
        DatabaseHandler handler = new DatabaseHandler(context);
        Cursor cursor = handler.getCursor();
        String[] from = {"address", "smsBody"};
        int[] to = {R.id.number_entry, R.id.smsBody_entry};
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(context, R.layout.number_and_smsbody, cursor, from, to, 0);
        MainActivity.listView.setAdapter(adapter);
    }
}
