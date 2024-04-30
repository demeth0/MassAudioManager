package com.demeth.massaudioplayer.backend;

import android.content.Context;

import com.demeth.massaudioplayer.backend.models.adapters.EventManager;
import com.demeth.massaudioplayer.backend.models.objects.Audio;

import java.util.Collection;

public class Shiraori {

    public static Dependencies openDependencies(Context context){
        return Dependencies.inject_dependencies(context);
    }
    public static void setHandler(String id, EventManager.EventHandler handler, Dependencies dependencies){
        dependencies.event_manager.registerHandler(id,handler);
    }

    public static void unsetHandler(String id, Dependencies dependencies){
        dependencies.event_manager.removeHandler(id);
    }

    public static void reloadDatabase(Context context,Dependencies dependencies){
        dependencies.database.reload(context);
    }

    public static Collection<Audio> getDatabaseEntries(Dependencies dependencies){
        return dependencies.database.getEntries();
    }

    public static boolean isRandomModeEnabled(Dependencies dependencies){
        return dependencies.audio_provider.get_random();
    }

    public static void setRandomModeEnabled(boolean value, Dependencies dependencies){
        dependencies.audio_provider.set_random(value);
    }

    public static void playAudio(Audio audio, Dependencies dependencies){
        dependencies.audio_provider.add_to_queue(audio);
        dependencies.audio_provider.move_to_next();

        dependencies.audio_manager.play();
    }
}
