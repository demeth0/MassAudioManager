package com.demeth.massaudioplayer.backend.models.adapters;

import android.content.Context;

import com.demeth.massaudioplayer.backend.models.objects.Audio;
import com.demeth.massaudioplayer.backend.models.objects.Metadata;

public interface DatabaseContentProvider {
    final class Content{
        public final Audio audio;
        public final Metadata metadata;

        public Content(Audio audio){
            this.audio=audio;
            this.metadata=null;
        }
        public Content(Audio audio, Metadata metadata){
            this.audio=audio;
            this.metadata=metadata;
        }
    }
    Content next();
    boolean hasNext();

    void open(Context context);
    void close();
}
