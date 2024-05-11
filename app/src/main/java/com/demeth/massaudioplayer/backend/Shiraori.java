package com.demeth.massaudioplayer.backend;

import android.content.Context;

import com.demeth.massaudioplayer.backend.models.adapters.EventManager;
import com.demeth.massaudioplayer.backend.models.objects.Audio;
import com.demeth.massaudioplayer.backend.models.objects.Event;
import com.demeth.massaudioplayer.backend.models.objects.EventCodeMap;
import com.demeth.massaudioplayer.backend.models.objects.LoopMode;
import com.demeth.massaudioplayer.backend.models.objects.Timestamp;

import java.util.Collection;

/**
 * Backend core use cases for audio management.
 */
public class Shiraori {

    /**
     * Load the dependencies for using the functionalities of this library.
     * @param context The application context used to load the various components.
     * @return The dependencies of the library used for every operations.
     */
    public static Dependencies openDependencies(Context context){
        return Dependencies.inject_dependencies(context);
    }

    /**
     * Create an event handler that will react to Shiraori callback events.
     * @param id Unique handler identifier.
     * @param handler Callback to run on event triggered.
     * @param dependencies The backend dependencies.
     */
    public static void setHandler(String id, EventManager.EventHandler handler, Dependencies dependencies){
        dependencies.event_manager.registerHandler(id,handler);
    }

    /**
     * Remove an event handler from the callback system.
     * @param id Unique handler identifier.
     * @param dependencies The backend dependencies.
     */
    public static void unsetHandler(String id, Dependencies dependencies){
        dependencies.event_manager.removeHandler(id);
    }

    public static void reloadDatabase(Context context,Dependencies dependencies){
        dependencies.database.reload(context);
        dependencies.event_manager.trigger(new Event(EventCodeMap.EVENT_DATABASE_RELOADED));
    }

    /**
     * Get an array list of all the loaded audio in the database available for diffusion.
     * @param dependencies The backend dependencies.
     * @returnA collection of all the playable audios.
     */
    public static Collection<Audio> getDatabaseEntries(Dependencies dependencies){
        return dependencies.database.getEntries();
    }

    /**
     * Check if the audio playlist system is currently in random reading mode.
     * @param dependencies The backend dependencies.
     * @return True if the random mode is enabled.
     */
    public static boolean isRandomModeEnabled(Dependencies dependencies){
        return dependencies.audio_provider.get_random();
    }

    /**
     * Set or unset the reading of the playlist audios in random mode.
     * @param value True to enabled random mode, False to disable.
     * @param dependencies The backend dependencies.
     */
    public static void setRandomModeEnabled(boolean value, Dependencies dependencies){
        dependencies.audio_provider.set_random(value);
        dependencies.event_manager.trigger(new Event(EventCodeMap.EVENT_RANDOM_MODE_CHANGED,dependencies.audio_provider.get_random()));
    }

    public static LoopMode getLoopMode(Dependencies dependencies){
        return dependencies.audio_provider.get_loop();
    }

    public static void setLoopMode(LoopMode loop_mode, Dependencies dependencies){
        dependencies.audio_provider.set_loop(loop_mode);
        dependencies.event_manager.trigger(new Event(EventCodeMap.EVENT_LOOP_MODE_CHANGED, loop_mode));
    }

    /**
     * Add an audio to the queue of the audio manager and play it.
     * @param audio Th audio to add to the queue and start instantly.
     * @param dependencies The backend dependencies.
     */
    public static void playAudio(Audio audio, Dependencies dependencies){
        dependencies.audio_provider.add_to_queue(audio);
        dependencies.audio_manager.play_next();
    }

    /**
     * Skip to the next audio in the waiting list and play it.
     * @param dependencies The backend dependencies.
     */
    public static void skipToNextAudio(Dependencies dependencies){
        dependencies.audio_manager.play_next();
    }

    public static void skipToPreviousAudio(Dependencies dependencies){
        dependencies.audio_manager.play_previous();
    }

    public static Timestamp getTimestamp(Dependencies dependencies){
        return dependencies.audio_manager.timestamp();
    }

    public static void setTimestamp(double timestamp_progress, Dependencies dependencies){
        dependencies.audio_manager.setTimestampProgress(timestamp_progress);
    }

    /**
     * Add a list of audio to the queue of the audio manager and play it.
     * @param audios A collection of audio to add to the queue and start immediately.
     * @param dependencies The backend dependencies.
     */
    public static void playAudios(Collection<Audio> audios, Dependencies dependencies){
        dependencies.audio_provider.clear_queue();

        for(Audio a : audios)
            dependencies.audio_provider.add_to_queue(a);
        dependencies.audio_manager.play_next();
    }

    public static Audio getCurrentAudio(Dependencies dependencies){
        return dependencies.audio_provider.get_audio();
    }

    /**
     * This function pause the audio. If the audio is paused or completed, will resume diffusion anyway or start from beginning.
     * @param dep The backend dependencies.
     */
    public static void pauseAudio(Dependencies dep){
        if(dep.audio_manager.isPaused()){
            boolean audio_completed = dep.audio_provider.get_audio()==null;
            // TODO Temporary ? If the audio is null with the current implementation taht mean that the audio is finished and is not paused.
            dep.audio_manager.play();

            // TODO should we really trigger a resume event here ?
            dep.event_manager.trigger(new Event(EventCodeMap.EVENT_AUDIO_RESUME));
        }else{
            dep.audio_manager.pause();
            dep.event_manager.trigger(new Event(EventCodeMap.EVENT_AUDIO_PAUSED));
        }

    }
}
