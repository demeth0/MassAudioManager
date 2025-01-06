package com.demeth.massaudioplayer.frontend.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.demeth.massaudioplayer.backend.Dependencies
import com.demeth.massaudioplayer.backend.IShiraori
import com.demeth.massaudioplayer.backend.Shiraori
import com.demeth.massaudioplayer.backend.models.adapters.EventHandler
import com.demeth.massaudioplayer.backend.models.objects.Audio
import com.demeth.massaudioplayer.backend.models.objects.LoopMode
import com.demeth.massaudioplayer.backend.models.objects.Timestamp

/**
 * Foreground service that will host the playback and audio management module. Can be used from notification, activities and Broadcast.
 */
class AudioService : Service() {
    companion object {
        const val ACTION_START_NOTIFICATION = "service start notification"

        fun asInterface(binder : IBinder?): IShiraori?{
            if(binder==null)
                return null
            if(binder is ServiceBinder)
                return binder
            throw IllegalArgumentException("service binder does not implement API")
        }
    }

    inner class ServiceBinder : Binder(), IShiraori {
        /**
         * @param client the client that is binding to the service
         * @return the current service instance
         */
        fun getService(client: AudioServiceBoundable): AudioService{
            clients.add(client)
            return this@AudioService
        }

        override fun setHandler(id: String, handler: EventHandler) = Shiraori.setHandler(id,getDeps(),handler)
        override fun unsetHandler(id: String) = Shiraori.unsetHandler(id, getDeps())
        override fun reloadDatabase(context: Context) = Shiraori.reloadDatabase(context, getDeps())
        override fun getDatabaseEntries(): List<Audio> = Shiraori.getDatabaseEntries(getDeps())
        override fun isRandomModeEnabled(): Boolean = Shiraori.isRandomModeEnabled(getDeps())
        override fun setRandomModeEnabled(value: Boolean) = Shiraori.setRandomModeEnabled(value, getDeps())
        override fun getLoopMode(): LoopMode = Shiraori.getLoopMode(getDeps())
        override fun setLoopMode(loopMode: LoopMode) = Shiraori.setLoopMode(loopMode,getDeps())
        override fun playAudio(audio: Audio) = Shiraori.playAudio(audio, getDeps())
        override fun playInPlaylist(audios: Collection<Audio>) = Shiraori.playInPlaylist(audios, getDeps())
        override fun skipToNextAudio() = Shiraori.skipToNextAudio(getDeps())
        override fun skipToPreviousAudio() = Shiraori.skipToPreviousAudio(getDeps())
        override fun getTimestamp(): Timestamp = Shiraori.getTimestamp(getDeps())
        override fun setTimestamp(timestampProgress: Double) = Shiraori.setTimestamp(timestampProgress, getDeps())
        override fun playAudios(audios: Collection<Audio>) = Shiraori.playAudios(audios, getDeps())
        override fun getCurrentAudio(): Audio? = Shiraori.getCurrentAudio(getDeps())
        override fun isPaused(): Boolean = Shiraori.isPaused(getDeps())
        override fun pauseAudio() = Shiraori.pauseAudio(getDeps())
        override fun viewQueue(): List<Audio> = Shiraori.viewQueue(getDeps())
        override fun viewPlaylist(): List<Audio> = Shiraori.viewPlaylist(getDeps())
    }

    private var serviceStarted = false
    private val clients = HashSet<AudioServiceBoundable>()
    private val serviceBinder= ServiceBinder()
    private lateinit var notificationBuilder: NotificationBuilder

    private var dependencies: Dependencies? = null

    private fun getDeps() : Dependencies {
        return dependencies!!
    }

    /**
     * Create the service, this should be called by an activity binding and creating the service so we then start it in foreground mode.
     */
    override fun onCreate() {
        super.onCreate()
        Log.d("[abc] AudioService","service onCreate call")

        NotificationBuilder.createNotificationChannel(applicationContext)

        startForegroundService(Intent(this,javaClass).apply {
            action = ACTION_START_NOTIFICATION
        })
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
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("[abc] AudioService","service onStartCommand call")
        intent?.apply {
            if(action == ACTION_START_NOTIFICATION && !serviceStarted){
                /*Should call startForeground() 5seconds after starting the service.
                This call create the foreground service notification required by all foreground services*/

                startShiraori()

                notificationBuilder = NotificationBuilder(this@AudioService)

                startForeground(NotificationBuilder.NOTIFICATION_ID,notificationBuilder.getNotification())
                serviceStarted = true
            }
        }

        return START_STICKY
    }

    /**
     * @return The playback configuration and resources to control the music.
     */
    @Deprecated("Replaced with IShiraori interface provided through onBind")
    fun getDependencies(): Dependencies{
        return dependencies!!
    }
}
