package com.demeth.massaudioplayer.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/**
 * detect earphone unplug event and pause audio on audio service to prevent embarrassment
 */
public class AudioDeviceBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        int iii;
        if (Intent.ACTION_HEADSET_PLUG.equals(action)) {
            iii = intent.getIntExtra("state", -1);
            if (iii == 0) {
                Log.d("AudioDeviceBroadcastReceiver","audio device unplugged");
                Intent audio_service_intent = new Intent(context, AudioService.class);
                audio_service_intent.setAction(ServiceAction.DEVICE_UNPLUGGED);
                context.startService(audio_service_intent);
            }
        }
    }
}
