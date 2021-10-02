package dk.stacktrace.messagingforwarder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsMessage;
import android.util.Log;
import android.util.Pair;

import java.net.MalformedURLException;
import java.net.URL;

public class IncomingMessageReceiver extends BroadcastReceiver {
    private static final String TAG = IncomingMessageReceiver.class.getName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Handling message for forwarding");
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (!preferences.contains("phone_number")) {
            Log.w(TAG, "Phone number to forward message to is not set. Will not forward any messages");
            return;
        }
        /*if (!preferences.contains("target_URL")) {
            Log.w(TAG, "URL to forward to not set. Will not forward any messages");
            return;
        }*/
        String phone_number = preferences.getString("phone_number", "");
        String[] phone_numbers = phone_number.split(",");
        /*URL target_url;
        try {
            target_url = new URL(preferences.getString("target_URL", ""));
        } catch (MalformedURLException e) {
            Log.w(TAG, "Unable to parse URL: " + e.getMessage());
            return;
        }*/
        SmsMessage[] messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
        for (SmsMessage message : messages) {
            //if (PhoneNumberUtils.compare(message.getDisplayOriginatingAddress(), phone_number)) {
                String msg = message.getDisplayMessageBody();
                String messageFrom = getSenderName(message.getDisplayOriginatingAddress(),context);
                for (String number : phone_numbers) {
                    Log.i(TAG, "Starting forwarding of message from " + number);
                    new Thread(new ForwardMessage(number, msg, messageFrom)).start();
                }
            //}
        }
    }

    private String getSenderName(final String phoneNumber,final Context context) {
        if (phoneNumber.matches(".*[a-zA-Z]+.*"))
            return phoneNumber;
        String callerName = phoneNumber;
        long contactId = Long.MIN_VALUE;
        final Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));

        String[] projection = null;

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M)
            projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup.CONTACT_ID};
        else
            projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, BaseColumns._ID};

        final Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                callerName = cursor.getString(0);
                contactId = cursor.getLong(1);
            }
            cursor.close();
        }

        return callerName;
    }
}
