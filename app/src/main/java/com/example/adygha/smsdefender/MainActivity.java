package com.example.adygha.smsdefender;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.ListPopupWindowCompat;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public final String TAG = "SMSDefender log";
    public static ListView listView;

    final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    IntentFilter filter = new IntentFilter(SMS_RECEIVED);
    BroadcastReceiver receiver;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS}, 2);

        receiver = new SmsReceiver();

        Log.d(TAG, "Register smsReceiver");
        registerReceiver(receiver, new IntentFilter(SMS_RECEIVED));

        listView = (ListView)findViewById(R.id.listView);

        updateListView(this);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        Log.d(TAG, "Unregister smsReceiver");
    }

    public void onClickButtonScan(View view) {
        int count = 0, malignant = 0;
        Uri inboxURI = Uri.parse("content://sms/");
        Cursor cur = getContentResolver().query(inboxURI, new String[]{"_id", "body", "address"}, null, null, null);
        DatabaseHandler handler = new DatabaseHandler(this);
        if(cur.moveToFirst()){
            do {
                count++;
                ////Changed to 0 to get Message id instead of Thread id :
                String smsBody = cur.getString(1);
                String address = cur.getString(2);
                if((smsBody.toLowerCase().contains("ув.клиент") ||
                        smsBody.toLowerCase().contains("банк") ||
                        smsBody.toLowerCase().contains("банковск") ||
                        smsBody.toLowerCase().contains("карт") ||
                        smsBody.toLowerCase().contains("списано") ||
                        smsBody.toLowerCase().contains("руб.")) && address.length() > 4){
                    String msgID = cur.getString(0);
                    getContentResolver().delete(Uri.parse("content://sms/" + msgID), null, null);

                    handler.addSMS(address, smsBody); // adding sms at BD

                    Log.d(TAG, "address: " + address  + " id: " + cur.getString(0));
                    Log.d(TAG, "smsBody: "  + smsBody);
                    malignant++;
                }
            }while(cur.moveToNext());
        }

        cur.close();
        Toast.makeText(this, "Устройство успешно отсканировано. Проверено "
                + count + " записей. Удалено " + malignant, Toast.LENGTH_LONG).show();
    }

    public void onClickButton2(View view) {
        Uri inboxURI = Uri.parse("content://sms/");
        Cursor cur = getContentResolver().query(inboxURI, new String[]{"_id", "body", "address"}, null, null, null);
        String[] from = {"address", "body"};
        int[] to = {R.id.number_entry, R.id.smsBody_entry};
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.number_and_smsbody, cur, from, to, 0);
        listView.setAdapter(adapter);
    }

    public void onClickButton3(View view) {
        try {
            DatabaseHandler handler = new DatabaseHandler(this);
            handler.addSMS("testing Address", "testing smsbody");
            handler.close();
            Log.d("MYTAG", "add sms");
        }catch(android.database.sqlite.SQLiteException e){
            Log.e("MY_EXCEPTION", "DB is empty");
    }
    }

    public void onClickButton4(View view) {
        DatabaseHandler handler = new DatabaseHandler(this);
        Cursor cursor = handler.getCursor();
        try{if(cursor.moveToFirst()){
            do {
                Log.d("MYTAG", "address: " + cursor.getString(cursor.getColumnIndex("address")) + " sms: " + cursor.getString(cursor.getColumnIndex("smsBody")));
            }while(cursor.moveToNext());
            cursor.close();
        }
        }catch (android.database.sqlite.SQLiteException e){
            Log.e("MY_EXCEPTION", "DB is empty");
        }
    }

    public void onClickButton5(View view) {
        DatabaseHandler handler = new DatabaseHandler(this);
        handler.deleteDB(this);
    }

    public void updateListView(Context context){
        DatabaseHandler handler = new DatabaseHandler(context);
        Cursor cursor = handler.getCursor();
        String[] from = {"address", "smsBody"};
        int[] to = {R.id.number_entry, R.id.smsBody_entry};
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(context, R.layout.number_and_smsbody, cursor, from, to, 0);
        listView.setAdapter(adapter);
    }

    public  void onClickReloadList(View view) {
        /*DatabaseHandler handler = new DatabaseHandler(this);
        Cursor cursor = handler.getCursor();
        String[] from = {"address", "smsBody"};
        int[] to = {R.id.number_entry, R.id.smsBody_entry};
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.number_and_smsbody, cursor, from, to, 0);
        listView.setAdapter(adapter);*/

        updateListView(this);
        Log.d("MYLOG", "setListView");
    }


}
