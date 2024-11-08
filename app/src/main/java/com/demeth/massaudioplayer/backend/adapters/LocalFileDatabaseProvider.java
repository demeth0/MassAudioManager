package com.demeth.massaudioplayer.backend.adapters;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.annotation.Nullable;

import com.demeth.massaudioplayer.backend.models.adapters.DatabaseContentProvider;
import com.demeth.massaudioplayer.backend.models.objects.Audio;
import com.demeth.massaudioplayer.backend.models.objects.AudioType;
import com.demeth.massaudioplayer.backend.models.objects.Metadata;

public class LocalFileDatabaseProvider implements DatabaseContentProvider {
    private static final String[] projection = {
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.VOLUME_NAME,
            MediaStore.Audio.Media._ID
    };

    private Cursor audioCursor;

    private boolean has_content=false;

    public LocalFileDatabaseProvider(){

    }

    @Override
    public void open(Context context) {
        this.audioCursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                MediaStore.Audio.Media.DISPLAY_NAME + " ASC");
        if (audioCursor != null && audioCursor.moveToFirst()){
            has_content=true;
        }else{
            has_content=false;
        }
    }

    @Override
    public void close() {
        has_content=false;
        if(this.audioCursor!=null) audioCursor.close();
    }

    @Nullable
    @Override
    public Content next() {
        if(!has_content) return null;

        int name_index,volume_index,id_index;
        //get uri and name for AudioFile struct creation
        name_index = audioCursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME);
        volume_index = audioCursor.getColumnIndex(MediaStore.Audio.Media.VOLUME_NAME);
        id_index= audioCursor.getColumnIndex(MediaStore.Audio.Media._ID);
        String file_name = audioCursor.getString(name_index);
        String display_name = removeExtension(file_name);
        Uri uri = MediaStore.Audio.Media.getContentUri(audioCursor.getString(volume_index),audioCursor.getLong(id_index));

        Audio audio = new Audio(display_name,file_name, AudioType.LOCAL);
        Metadata.FileAudioMetadata metadata = new Metadata.FileAudioMetadata(uri);

        has_content=audioCursor.moveToNext();
        return new Content(audio,metadata);
    }

    @Override
    public boolean hasNext() {
        return has_content;
    }

    private static String removeExtension(String name){
        return name.substring(0,name.lastIndexOf("."));
    }
}
