package com.demeth.massaudioplayer.frontend.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.demeth.massaudioplayer.backend.Dependencies
import com.demeth.massaudioplayer.backend.Shiraori
import java.util.Objects

/**
 * Foreground service that will host the playback and audio management module. Can be used from notification, activities and Broadcast.
 */
class AudioService : Service() {
    inner class ServiceBinder : Binder() {
        /**
         * @param client the client that is binding to the service
         * @return the current service instance
         */
        fun getService(client: AudioServiceBoundable): AudioService{
            clients.add(client)
            return this@AudioService
        }
    }
    private var serviceStarted = false
    private val clients = HashSet<AudioServiceBoundable>()
    private val serviceBinder= ServiceBinder()
    private lateinit var notificationBuilder: NotificationBuilder

    private var dependencies: Dependencies? = null



    companion object {
        const val ACTION_START_NOTIFICATION = "service start notification"
    }

    /**
     * Create the service, this should be called by an activity binding and creating the service so we then start it in foreground mode.
     */
    override fun onCreate() {
        super.onCreate()
        Log.d("[abc] AudioService","service onCreate call")

        startShiraori()

        val serviceIntent = Intent(this,javaClass)
        serviceIntent.setAction(ACTION_START_NOTIFICATION)
        startForegroundService(serviceIntent)
    }

    private fun startShiraori(){
        if(dependencies==null)
            dependencies = Shiraori.openDependencies(this)
    }

    override fun onBind(intent: Intent): IBinder {
        Log.d("[abc] AudioService","service onBind call")

        return serviceBinder
    }

    /**
     * Is not called by onBind directly. In case the action is ACTION_START_NOTIFICATION the service will open a notification to prevent the
     * service to be closed by the system when the activity close (Foreground Service). Also load dependencies for Audio playback and management.
     */
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d("[abc] AudioService","service onStartCommand call")
        if(Objects.equals(intent.action, ACTION_START_NOTIFICATION)){
            /*Should call startForeground() 5seconds after starting the service.
            This call create the foreground service notification required by all foreground services*/

            startShiraori()

            notificationBuilder = NotificationBuilder(this)
            startForeground(NotificationBuilder.NOTIFICATION_ID,notificationBuilder.getNotification())
        }

        return START_STICKY
    }

    /**
     *
     * @return The playback configuration and resources to control the music.
     */
    fun getDependencies(): Dependencies{
        return dependencies!!
    }
}
