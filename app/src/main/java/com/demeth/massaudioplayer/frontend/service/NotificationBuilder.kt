package com.demeth.massaudioplayer.frontend.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews

import androidx.core.app.NotificationCompat

import com.demeth.massaudioplayer.R
import com.demeth.massaudioplayer.backend.models.objects.Audio
import com.demeth.massaudioplayer.frontend.HomeActivity


class NotificationBuilder(private val service: AudioService ) {
    companion object{
        const val CHANNEL_ID = "massaudioplayer notification channel id"
        const val NOTIFICATION_ID=88

        /**
         * create the notification channel for the audio player
         */
        fun createNotificationChannel(context: Context) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            val name = context.getString(R.string.channel_name)
            val description = context.getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH //want to be oin top but no sound
            val channel = NotificationChannel(CHANNEL_ID, name, importance)

            channel.enableVibration(false)
            channel.description = description
            channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            channel.setSound(null,null)

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    /**
     * flags for the notification : cancel current notification and unchanging over time
     */
    private val flags = PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE

    /**
     * notification manager used to create notification builder and edit current notification
     */
    private lateinit var manager: NotificationManager

    /**
     * the inflated notification view
     */
    private lateinit var notificationView: RemoteViews

    /**
     * the builder that instantiate a notification from the view
     */
    private lateinit var notificationBuilder: NotificationCompat.Builder

    /** to change the pause button texture */
    private var pauseButtonResource: Int = android.R.drawable.ic_media_play

    init {
        createNotificationBuilder()
    }

    private fun createNotificationBuilder(){
        /*get the notification manager from the app context*/
        manager = service.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        /*inflate notification view*/
        notificationView = RemoteViews(service.packageName, R.layout.notification_dummy_layout)


        /*init pending intent*/
        // PendingIntent next  = createPendingIntent(0, ServiceAction.NEXT_AUDIO),
        //         pause = createPendingIntent(1,ServiceAction.PAUSE_AUDIO),
        //         prev  = createPendingIntent(2,ServiceAction.PREV_AUDIO),
        //         end   = createPendingIntent(3,ServiceAction.END_SERVICE);

        /*load pending intents on the notification*/
        // notificationView.setOnClickPendingIntent(R.id.notification_close,end);
        // notificationView.setOnClickPendingIntent(R.id.notification_prev,prev);
        // notificationView.setOnClickPendingIntent(R.id.notification_next,next);
        // notificationView.setOnClickPendingIntent(R.id.notification_pause,pause);

        /*TODO set the image resource for the pause button ???*/
        //notificationView.setImageViewResource(R.id.notification_pause,pauseButtonResource);

        /*make the notification builder*/
        notificationBuilder = NotificationCompat.Builder(service, CHANNEL_ID)

        notificationBuilder.setCustomContentView(notificationView)
                .setSmallIcon(android.R.drawable.ic_media_play)


        /*when clicking on the notification open the app main activity*/
        val startActivity = Intent(service, HomeActivity::class.java)
        val startActivityPendingIntent = PendingIntent.getActivity(service,4,startActivity, flags)
        notificationBuilder.setContentIntent(startActivityPendingIntent)
    }

    /**
     * Create a pending intent corresponding to a specific command call for the audio service.
     * @param requestCode Unique code referring to the action to execute.
     * @param extra Command to run.
     * @return Inflated pending intent.
     */
    private fun createPendingIntent(requestCode: Int,extra: String): PendingIntent?{
        //TODO add broadcast receiver
        /*Intent intent = new Intent(service, ServiceBroadcast.class);
        //action set in broadcast receiver
        intent.setAction(extra);
        return PendingIntent.getBroadcast(service,requestCode,intent,flags);*/
        return null
    }

    /**
     * Make builder to create the notification.
     * /@param file la track qui défini l'affichage de la notification
     */
    private fun prepareBuilder(tr: Audio?){ //TODO add update to notification
        createNotificationBuilder()
        // String title="aaaa";
        // if(tr != null){
        //     title = tr.display_name;
        // }

        //TODO album images
        //si ya qq chose a afficher sinon on fait rien
        // AlbumLoader.getAlbumImage(service,tr,48,(res)->notificationView.setImageViewBitmap(R.id.notification_album_image,res));

        // notificationView.setTextViewText(R.id.notification_title,title);

        notificationBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
    }

    /**
     * construit et retourne la notification
     * /@param file le fichier de base de la construction
     * @return la notification créer
     */
    fun getNotification(): Notification{
        prepareBuilder(null)
        Log.d("[abc] NotificationBuilder", "create notification !")
        return notificationBuilder.build()
    }

    /**
     * met a jour la notification avec la nouvelle track
     * /@param file la nouvelle donnée a utiliser pour construire la notification
     */
    fun updateNotification(tr: Audio){
        prepareBuilder(tr)
        manager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    /**
     * met a jour les boutons de la notification lors de mises en pause
     * @param paused le nouvel état du bouton pause de la notification
     */
    fun setPauseButtonPaused(paused: Boolean){
        pauseButtonResource = if(!paused){
            android.R.drawable.ic_media_pause
        }else{
            android.R.drawable.ic_media_play
        }
    }


}
