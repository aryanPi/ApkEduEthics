package com.classroom.eduethics.Services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.SmsMessage;

import com.chaos.view.PinView;
import com.google.android.material.textfield.TextInputEditText;

public class OTP_Receiver extends BroadcastReceiver {
    private static PinView editText;

    public void setEditText(PinView editText)
    {
        OTP_Receiver.editText = editText;
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        SmsMessage[] messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);

        for (SmsMessage sms : messages)
        {
            String message = sms.getMessageBody();
            if (message.contains("verification")) {
                editText.setText(message.split(" ")[0]);
            }

        }
    }
}