package com.demeth.massaudioplayer.service.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import com.demeth.massaudioplayer.R;
import com.demeth.massaudioplayer.database.AlbumLoader;
import com.demeth.massaudioplayer.database.IdentifiedEntry;
import com.demeth.massaudioplayer.service.ServiceAction;
import com.demeth.massaudioplayer.ui.MainActivity;

/**
 * classe qui s'occupe de la création et de la génération de la notification pour la partie foreground du service
 */
public class NotificationBuilder {
    /**
     * string used for intent to transmit to service an audio control command
     */
    public static final String COMMAND_FLAG="audio service interaction message";

    public static final String ACTION_NOTIFICATION="audio service interaction action";

    public static final String CHANNEL_ID = "massaudioplayer notification channel id";
    public static final int NOTIFICATION_ID=88;

    /**
     * flags for the notification : cancel current notification and unchanging over time
     */
    private final int flags = PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE;

    /**
     * referenceto the service bound to this notification
     */
    private final Service service;

    /**
     * notification manager used to create notification builder and edit current notification
     */
    private NotificationManager manager;

    /**
     * the inflated notification view
     */
    private RemoteViews notificationView;

    /**
     * the builder that instanciate a notification from the view
     */
    private NotificationCompat.Builder notification_builder;

    /** to change the pause button texture */
    private int pauseButtonResource = android.R.drawable.ic_media_play;

    public NotificationBuilder(Service service){
        this.service = service;
        createNotificationBuilder();
    }
    private void createNotificationBuilder(){
        /*get the notification manager from the app context*/
        manager = (NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE);

        /*inflate notification view*/
        notificationView = new RemoteViews(service.getPackageName(), R.layout.notification_layout);

        /*init pendig intent*/
        PendingIntent next  = createPendingIntent(0, ServiceAction.NEXT_AUDIO),
                pause = createPendingIntent(1,ServiceAction.PAUSE_AUDIO),
                prev  = createPendingIntent(2,ServiceAction.PREV_AUDIO),
                end   = createPendingIntent(3,ServiceAction.END_SERVICE);

        /*load pending intents on the notification*/
        notificationView.setOnClickPendingIntent(R.id.notification_close,end);
        notificationView.setOnClickPendingIntent(R.id.notification_prev,prev);
        notificationView.setOnClickPendingIntent(R.id.notification_next,next);
        notificationView.setOnClickPendingIntent(R.id.notification_pause,pause);

        /*TODO set the image ressource for the pause button ???*/
        notificationView.setImageViewResource(R.id.notification_pause,pauseButtonResource);

        /*make the notification builder*/
        notification_builder = new NotificationCompat.Builder(service, CHANNEL_ID); //TODO channel id
        notification_builder.setContent(notificationView)
                .setSmallIcon(android.R.drawable.ic_media_play)
                /*to set notification at the top*/
                .setPriority(NotificationCompat.PRIORITY_MAX);

        /*when clicking on the notification open the app main activity*/
        Intent start_activity = new Intent(service, MainActivity.class);
        PendingIntent start_activity_pending_intent = PendingIntent.getActivity(service,4,start_activity, flags);
        notification_builder.setContentIntent(start_activity_pending_intent);
    }

    /**
     * créer les intent liées a des actions de la notification
     * @param requestCode code unique par action
     * @param extra la commande a exécuter
     * @return la pending intent associée
     */
    private PendingIntent createPendingIntent(int requestCode,String extra){
        Intent intent = new Intent(service, ServiceBroadcast.class);
        //action set in broadcast receiver
        intent.setAction(extra);
        return PendingIntent.getBroadcast(service,requestCode,intent,flags);
    }

    /**
     * prépare le builder pour affichage de la notification (appel dynamique)
     * /@param file la track qui défini l'affichage de la notification
     */
    private void prepareBuilder(IdentifiedEntry tr){ //TODO add update to notification
        createNotificationBuilder();
        String title="";
        if(tr != null){
            title = tr.getName();
        }

        //TODO album images
        //si ya qq chose a afficher sinon on fait rien
        AlbumLoader.getAlbumImage(service,tr,48,(res)->notificationView.setImageViewBitmap(R.id.notification_album_image,res));

        notificationView.setTextViewText(R.id.notification_title,title);

        notification_builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
    }

    /**
     * construit et retourne la notification
     * /@param file le fichier de base de la construction
     * @return la notification créer
     */
    public Notification getNotification(){
        prepareBuilder(null);
        return notification_builder.build();
    }

    /**
     * met a jour la notification avec la nouvelle track
     * /@param file la nouvelle donnée a utiliser pour construire la notification
     */
    public void updateNotification(IdentifiedEntry tr){
        prepareBuilder(tr);
        manager.notify(NOTIFICATION_ID, notification_builder.build());
    }

    /**
     * met a jour les boutons de la notification lors de mises en pause
     * @param paused le nouvel état du bouton pause de la notification
     */
    public void setPauseButtonPaused(boolean paused){
        if(!paused){
            pauseButtonResource=android.R.drawable.ic_media_pause;
        }else{
            pauseButtonResource= android.R.drawable.ic_media_play;
        }
    }

    /**
     * create the notification channel for the audio player
     */
    public static void createNotificationChannel(Context context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        CharSequence name = context.getString(R.string.channel_name);
        String description = context.getString(R.string.channel_description);
        int importance = NotificationManager.IMPORTANCE_HIGH; //want to be oin top but no sound
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.enableVibration(false);
        channel.setDescription(description);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        channel.setSound(null,null);

        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }
}
