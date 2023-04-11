package com.demeth.massaudioplayer.service.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.demeth.massaudioplayer.service.AudioService;

public class ServiceBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent audio_service_intent = new Intent(context, AudioService.class);
        audio_service_intent.setAction(intent.getAction());
        context.startService(audio_service_intent);
    }
}
