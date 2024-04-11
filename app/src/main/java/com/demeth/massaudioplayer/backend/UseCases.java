package com.demeth.massaudioplayer.backend;

import android.content.Context;

import com.demeth.massaudioplayer.backend.models.adapters.EventManager;
import com.demeth.massaudioplayer.backend.models.objects.Audio;
import com.demeth.massaudioplayer.backend.models.objects.Playlist;

import java.util.Collection;

public class UseCases {

    public static Dependencies openDependencies(Context context){
        return Dependencies.inject_dependencies(context);
    }
    public static void setHandler(String id, EventManager.EventHandler handler, Dependencies dependencies){
        dependencies.event_manager.registerHandler(id,handler);
    }

    public static void reloadDatabase(Context context,Dependencies dependencies){
        dependencies.database.reload(context);
    }

    public static Collection<Audio> getDatabaseEntries(Dependencies dependencies){
        return dependencies.database.getEntries();
    }

    public static void playAudio(Audio audio, Dependencies dependencies){
        dependencies.audio_provider.add_to_queue(audio);
        dependencies.audio_provider.move_to_next();

        dependencies.audio_manager.play();
    }
}
