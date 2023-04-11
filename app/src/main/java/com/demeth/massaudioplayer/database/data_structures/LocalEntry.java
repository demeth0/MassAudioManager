package com.demeth.massaudioplayer.database.data_structures;

import android.net.Uri;
import android.provider.ContactsContract;

import com.demeth.massaudioplayer.database.DataType;
import com.demeth.massaudioplayer.database.IdProvider;
import com.demeth.massaudioplayer.database.IdentifiedEntry;
import com.demeth.massaudioplayer.database.Typed;

public class LocalEntry extends IdentifiedEntry {
    protected final Uri path;
    public LocalEntry(String name,Uri path) {
        super(name, path,IdProvider.provideId());
        this.path=path;
    }

    public Uri getPath() {
        return path;
    }

    @Override
    public DataType getType() {
        return DataType.LOCAL;
    }
}
