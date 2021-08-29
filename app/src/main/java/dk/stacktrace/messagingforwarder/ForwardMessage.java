package dk.stacktrace.messagingforwarder;

import android.os.Build;
import android.telephony.SmsManager;
import android.util.Log;

import androidx.annotation.RequiresApi;

public class ForwardMessage implements Runnable {
    private static final String TAG = HttpPostThread.class.getName();

    public ForwardMessage(final String toMobileNumber, final String message) {
        this.message = message;
        ForwardMessage.toMobileNumber = toMobileNumber;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    @Override
    public void run() {
        if (message.contains("Pack Valid till") || message.contains("Remaining SMS"))
            return;
        SmsManager smsManager = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
            smsManager = SmsManager.getSmsManagerForSubscriptionId(SmsManager.getDefaultSmsSubscriptionId());
        }
        smsManager.sendTextMessage(ForwardMessage.toMobileNumber, null, message, null, null);
    }

    private final String message;
    private static String toMobileNumber;
}

