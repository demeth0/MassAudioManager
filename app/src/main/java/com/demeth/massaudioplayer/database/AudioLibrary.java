package com.demeth.massaudioplayer.database;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.demeth.massaudioplayer.database.data_structure.LocalEntry;

import java.util.Collection;
import java.util.HashMap;
import java.util.stream.Collectors;

public class AudioLibrary {
    private static final String[] projection = {
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.VOLUME_NAME,
            MediaStore.Audio.Media._ID
    };

    public AudioLibrary(Context context){
        loadMusics(context);
    }

    private final HashMap<Integer, IdentifiedEntry> library = new HashMap<>();

    public IdentifiedEntry get(@NonNull String title, @NonNull DataType type){
        return library.values().stream().parallel().filter(e -> e.getName().equals(title) && e.getType().equals(type)).findFirst().orElseGet(() -> null);
    }
    public IdentifiedEntry get(int id){
        return library.get(id);
    }

    public Collection<? extends IdentifiedEntry> getAll(){
        return library.values().stream().sorted().collect(Collectors.toList());
    }

    public Collection<? extends IdentifiedEntry> get(Collection<Integer> match){
        return match.stream().map(library::get).collect(Collectors.toList());
    }

    public @Nullable IdentifiedEntry get(@NonNull String title){
        return library.values().stream().parallel().filter(e -> e.getName().equals(title)).findFirst().orElseGet(() -> null);
    }

    /**
     * load all musics for the player, if connected can load spotify musics ...
     */
    public void loadMusics(Context cont){
        loadLocalMusics(cont);
    }
    /**
     * load from storage musics
     **/
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

                String name = removeExtension(audioCursor.getString(name_index));
                Uri uri = MediaStore.Audio.Media.getContentUri(audioCursor.getString(volume_index),audioCursor.getLong(id_index));
                LocalEntry track = new LocalEntry(name,uri);
                library.put(track.hashCode(),track);
            } while (audioCursor.moveToNext());
        }
        if(audioCursor!=null) audioCursor.close(); //don't forget to close the cursor ♥
    }

    /**
     * supprime les .mp3 a la fin des nom de fichiers
     * @param name le fichier
     * @return le nom sans l'extension
     */
    private static String removeExtension(String name){
        return name.substring(0,name.lastIndexOf("."));
    }
}
