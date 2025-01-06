package com.demeth.massaudioplayer.backend.adapters

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore

import com.demeth.massaudioplayer.backend.models.adapters.Content
import com.demeth.massaudioplayer.backend.models.adapters.DatabaseContentProvider
import com.demeth.massaudioplayer.backend.models.objects.Audio
import com.demeth.massaudioplayer.backend.models.objects.AudioType
import com.demeth.massaudioplayer.backend.models.objects.Metadata

fun removeExtension(name: String): String = name.substring(0,name.lastIndexOf("."))

class LocalFileDatabaseProvider : DatabaseContentProvider {
    companion object {
        private val projection = arrayOf(
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.VOLUME_NAME,
            MediaStore.Audio.Media._ID
        )
    }


    private var audioCursor: Cursor?=null
    private var hasContent=false

    override fun open(context: Context) {
        this.audioCursor = context.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                MediaStore.Audio.Media.DISPLAY_NAME + " ASC")

        hasContent = audioCursor!=null && audioCursor!!.moveToFirst()
    }

    override fun close() {
        hasContent=false
        audioCursor?.close()
    }

    override fun next(): Content? {
        if(!hasContent)
            return null

        var content : Content? = null

        audioCursor?.apply {
            val nameIndex: Int = getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)
            val volumeIndex: Int = getColumnIndex(MediaStore.Audio.Media.VOLUME_NAME)
            val idIndex: Int = getColumnIndex(MediaStore.Audio.Media._ID)
            val fileName = getString(nameIndex)
            val displayName = removeExtension(fileName)

            //get uri and name for AudioFile struct creation
            val uri = MediaStore.Audio.Media.getContentUri(getString(volumeIndex),getLong(idIndex))

            val audio = Audio(displayName,fileName, AudioType.LOCAL)
            val metadata = Metadata.FileAudioMetadata(uri)

            hasContent=moveToNext()

            content = Content(audio,metadata)
        }
        return content
    }

    override fun hasNext(): Boolean = hasContent
}
