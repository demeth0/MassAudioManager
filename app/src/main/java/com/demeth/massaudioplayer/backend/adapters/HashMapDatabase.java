package com.demeth.massaudioplayer.backend.adapters;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.demeth.massaudioplayer.backend.models.adapters.Database;
import com.demeth.massaudioplayer.backend.models.objects.Audio;
import com.demeth.massaudioplayer.backend.models.objects.AudioType;
import com.demeth.massaudioplayer.backend.models.objects.Metadata;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class HashMapDatabase implements Database {
    public static class DuplicateEntriesException extends Exception{
        public DuplicateEntriesException(){
            super("Entry already added to the collection, duplicate ?");
        }
    }

    public HashMapDatabase(Context context){
        loadLocalMusics(context);
    }

    @Override
    public void reload(Context context) {
        loadLocalMusics(context);
    }

    // Database content
    HashMap<String, Metadata.FileAudioMetadata> file_audio_metadata = new HashMap<>();
    HashMap<String, Audio> audio_entries = new HashMap<>();

    /************************************/

    private static final String[] projection = {
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.VOLUME_NAME,
            MediaStore.Audio.Media._ID
    };

    private static String removeExtension(String name){
        return name.substring(0,name.lastIndexOf("."));
    }

    private void loadLocalMusics(Context context){
        Cursor audioCursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                MediaStore.Audio.Media.DISPLAY_NAME + " ASC");
        if (audioCursor != null && audioCursor.moveToFirst()) {
            int name_index,volume_index,id_index;
            do {
                //get uri and name for AudioFile struct creation
                name_index = audioCursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME);
                volume_index = audioCursor.getColumnIndex(MediaStore.Audio.Media.VOLUME_NAME);
                id_index= audioCursor.getColumnIndex(MediaStore.Audio.Media._ID);
                String file_name = audioCursor.getString(name_index);
                String display_name = removeExtension(file_name);
                Uri uri = MediaStore.Audio.Media.getContentUri(audioCursor.getString(volume_index),audioCursor.getLong(id_index));

                Audio audio = new Audio(display_name,file_name, AudioType.LOCAL);
                Metadata.FileAudioMetadata metadata = new Metadata.FileAudioMetadata(uri);

                if(audio_entries.containsKey(audio.path))
                    throw new RuntimeException(new DuplicateEntriesException());

                audio_entries.put(audio.path,audio);
                file_audio_metadata.put(audio.path,metadata);
            } while (audioCursor.moveToNext());
        }
        if(audioCursor!=null)
            audioCursor.close(); //don't forget to close the cursor
    }
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
