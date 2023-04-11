package com.demeth.massaudioplayer.service;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

/**
 * gère les messages envoyer a la partie fonctionement indépendante du service
 */
public class AudioServiceHandler extends Handler {
    private final AbstractAudioService service;

    /**
     * @param loop main looper of the service
     * @param service the service bound to this handler
     */
    public AudioServiceHandler(Looper loop, AbstractAudioService service){
        super(loop);
        this.service = service;
    }

    /**
     * receive a message of type int which may contain the values STOP_SERVICE and will try to disconnect all the client to the service
     *
     * @param msg the message to process
     */
    @Override
    public void handleMessage(@NonNull Message msg) {
        if(msg.arg1==ServiceAction.HANDLER_STOP_SERVICE){
            Log.d("service handler","stopping service looper requesting clients connection closure");

            /*disconnect from clients*/
            //service.stopForeground(true);
            //service.closeService();
            //service.stopSelf();


            Log.d("service handler","service successfully closed");
        }
    }
}
