package com.demeth.massaudioplayer.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.demeth.massaudioplayer.service.notification.NotificationBuilder;

import java.util.HashSet;
import java.util.Set;

/**
 * abstraction of AudioService that setup all connection stop and start related operations
 */
public abstract class AbstractAudioService  extends Service{
    /**
     * define a simple audio binder that return the service and get  the client informations
     * @param <T> the service to return
     */
    protected abstract class AbstractAudioBinder<T extends AbstractAudioService> extends Binder {
        protected abstract T _getService();

        /**
         * @param client the client that is binding to the service
         * @return the current service instance
         */
        public T getService(BoundableActivity client){
            clients.add(client);
            return _getService();
        }
    }

    /**
     * all the clients connected to the service
     */
    protected Set<BoundableActivity> clients;



    /**
     * @return the IBinder object needed by the activity
     */
    protected abstract AbstractAudioBinder<? extends AbstractAudioService> getBinder();

    /**
     * set to false then true when the service finished creating and called onBind or onStartCommand
     */
    protected boolean service_started=false;

    /**
     * service handler used to load heavy components
     */
    protected AudioServiceHandler handler;

    protected NotificationBuilder notification_builder;



    /**
     * executed at the creation of the service in any case
     */
    @Override
    public void onCreate() {
        super.onCreate();
        if(!service_started){
            Log.d("service","service onCreate");
            /*create a thread for the service to run*/
            HandlerThread thread = new HandlerThread("massaudioplayer.AudioServiceHandlerThread", HandlerThread.NORM_PRIORITY);
            thread.start();

            handler = new AudioServiceHandler(thread.getLooper(),this);

            notification_builder = new NotificationBuilder(this);

            /*create the list of all the clients*/
            clients = new HashSet<>();

            firstInitialization();
        }
    }

    public abstract void firstInitialization();

    /**
     * on débranche tout les clients connecté a ce service pour s'assurer qu'il s'arrete comme il faut
     */
    protected void disconnectClients(){
        clients.forEach(BoundableActivity::onServiceDisconnected);
    }

    void closeService(){
        disconnectClients();
    }

    /**
     * called when a client bind to this service
     * @param intent the intent of the client
     * @return a binder to this service
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        /*the service is running in bound mode*/
        Log.d("service","received onBind request");
        if(!service_started){
            startServiceInForeground();
        }

        return getBinder();
    }

    protected void startServiceInForeground(){
        Intent serviceIntent = new Intent(this,getClass());
        serviceIntent.setAction(ServiceAction.START_SERVICE);
        startForegroundService(serviceIntent);
    }

    /**
     * called when a service is started and unbound
     * @param intent the intent of the client who started the service
     * @param flags the flags
     * @param startId the id of the intent
     * @return the behavior of the service (keep running ...)
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        /*the service started in independant mode*/
        Log.d("service","received intent");
        if(!service_started){
            //go to foreground
            startForeground(NotificationBuilder.NOTIFICATION_ID, notification_builder.getNotification());
            service_started=true;
        }
        if(intent!=null){
            switch(intent.getAction()){
                case ServiceAction.START_SERVICE:
                    Log.d("service","service started with START_SERVICE action");
                    break;
                case ServiceAction.END_SERVICE:
                    Log.d("service","stop service request received");
                    stopForeground(true);
                    closeService();
                    stopSelf();
                    // code template
                    // Message msg = handler.obtainMessage();
                    // msg.arg1 = ServiceAction.HANDLER_STOP_SERVICE;
                    // handler.sendMessage(msg);
                    return START_NOT_STICKY;
                    //break;
            }
        }

        return START_STICKY;
    }

    /**
     * met a jour la notification de status du service
     */
    public abstract void updateNotification(boolean paused);
}
