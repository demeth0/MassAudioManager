package com.demeth.massaudioplayer.frontend.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.demeth.massaudioplayer.backend.Dependencies;
import com.demeth.massaudioplayer.backend.Shiraori;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

/**
 * Foreground service that will host the playback and audio management module. Can be used from notification, activities and Broadcast.
 */
public class AudioService extends Service {
    public class ServiceBinder extends Binder {
        /**
         * @param client the client that is binding to the service
         * @return the current service instance
         */
        public AudioService getService(AudioServiceBoundable client){
            clients.add(client);
            return AudioService.this;
        }
    }
    private boolean service_started = false;
    private Set<AudioServiceBoundable> clients = new HashSet<>();
    private ServiceBinder service_binder=new ServiceBinder();
    private NotificationBuilder notification_builder;

    private Dependencies dependencies=null;

    public final static String ACTION_START_NOTIFICATION = "service start notification";

    /**
     * Create the service, this should be called by an activity binding and creating the service so we then start it in foreground mode.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("[abc] AudioService","service onCreate call");

        startShiraori();

        Intent serviceIntent = new Intent(this,getClass());
        serviceIntent.setAction(ACTION_START_NOTIFICATION);
        startForegroundService(serviceIntent);
    }

    private void startShiraori(){
        if(dependencies==null)
            dependencies = Shiraori.openDependencies(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("[abc] AudioService","service onBind call");

        return service_binder;
    }

    /**
     * Is not called by onBind directly. In case the action is ACTION_START_NOTIFICATION the service will open a notification to prevent the
     * service to be closed by the system when the activity close (Foreground Service). Also load dependencies for Audio playback and management.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("[abc] AudioService","service onStartCommand call");
        if(intent==null)
            return START_STICKY;
        if(Objects.equals(intent.getAction(), ACTION_START_NOTIFICATION)){
            /*Should call startForeground() 5seconds after starting the service.
            This call create the foreground service notification required by all foreground services*/

            startShiraori();

            notification_builder = new NotificationBuilder(this);
            startForeground(NotificationBuilder.NOTIFICATION_ID,notification_builder.getNotification());
        }

        return START_STICKY;
    }

    /**
     *
     * @return The playback configuration and resources to control the music.
     */
    public Dependencies getDependencies(){
        return dependencies;
    }
}
