package com.demeth.massaudioplayer.backend.adapters;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.demeth.massaudioplayer.backend.models.adapters.Database;
import com.demeth.massaudioplayer.backend.models.adapters.DatabaseContentProvider;
import com.demeth.massaudioplayer.backend.models.objects.Audio;
import com.demeth.massaudioplayer.backend.models.objects.AudioType;
import com.demeth.massaudioplayer.backend.models.objects.Metadata;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class HashMapDatabase implements Database {
    public static class DuplicateEntriesException extends Exception{
        public DuplicateEntriesException(){
            super("Entry already added to the collection, duplicate ?");
        }
    }


    // Database content
    HashMap<String, Metadata.FileAudioMetadata> file_audio_metadata = new HashMap<>();
    HashMap<String, Audio> audio_entries = new HashMap<>();

    private final DatabaseContentProvider[] providers;

    public HashMapDatabase(Context context,DatabaseContentProvider ...content_providers){
        this.providers = content_providers;
        reload(context);
    }

    private void clear(){
        audio_entries.clear();
        file_audio_metadata.clear();
    }

    private void save_metadata(Audio audio,Metadata metadata){
        switch(audio.type){
            case LOCAL:
                file_audio_metadata.put(audio.path,(Metadata.FileAudioMetadata) metadata);
                break;
        }
    }

    private void load_provider(DatabaseContentProvider provider){
        Audio audio;
        Metadata metadata;
        while(provider.hasNext()){
            DatabaseContentProvider.Content content = provider.next();
            audio = content.audio;
            metadata = content.metadata;
            if(audio_entries.containsKey(audio.path))
                throw new RuntimeException(new DuplicateEntriesException());

            audio_entries.put(audio.path,audio);
            save_metadata(audio,metadata);
        }
    }

    @Override
    public void reload(Context context) {
        clear();
        for(DatabaseContentProvider provider : this.providers){
            provider.open(context);
            load_provider(provider);
            provider.close();
        }
    }


    /************************************/

    @Override
    public Metadata getMetadata(Audio audio) {
        if(audio.type.equals(AudioType.LOCAL))
            return file_audio_metadata.get(audio.path);
        return null;
    }

    @Override
    public Collection<Audio> getEntries() {
        return audio_entries.values();
    }

    @Override
    public Collection<Audio> getEntries(Predicate<Audio> filter) {
        return audio_entries.values().stream().filter(filter).collect(Collectors.toList());
    }


}
