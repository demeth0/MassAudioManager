package com.demeth.massaudioplayer.backend.adapters;

import static org.junit.Assert.*;

import android.content.Context;
import android.net.Uri;

import com.demeth.massaudioplayer.backend.models.adapters.DatabaseContentProvider;
import com.demeth.massaudioplayer.backend.models.objects.Audio;
import com.demeth.massaudioplayer.backend.models.objects.AudioType;
import com.demeth.massaudioplayer.backend.models.objects.Metadata;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collection;

/** @noinspection ConstantValue*/
public class HashMapDatabaseTest {
    final static int SIZE = 10;
    final static ArrayList<Audio> audios=new ArrayList<>();
    static{
        for(int i=0;i<SIZE;i++){
            audios.add(new Audio("audio "+i,"audio "+i,AudioType.LOCAL));
        }
    }
    final Context DUMMY_CONTEXT = null;
    class StubFileMetadata extends Metadata.FileAudioMetadata {
        int j;
        public StubFileMetadata(int j){
            super();
            this.j=j;
        }
    }
    class StubDatabaseContentProvider implements DatabaseContentProvider{
        int i=0;
        @Override
        public Content next() {

            if(this.i>=SIZE) return null;

            Content c= new Content(audios.get(this.i),new StubFileMetadata(this.i));
            this.i++;
            return c;
        }

        @Override
        public boolean hasNext() {
            if(this.i>=SIZE)
                return false;
            return true;
        }

        @Override
        public void open(Context context) {

        }

        @Override
        public void close() {
            i=SIZE;
        }
    }

    StubDatabaseContentProvider provider;
    HashMapDatabase database;
    @Before
    public void setup() {
        provider = new StubDatabaseContentProvider();
        database = new HashMapDatabase(DUMMY_CONTEXT,provider);
    }

    @Test
    public void test_get_data(){
        Collection<Audio> data = database.getEntries();
        for(Audio a : audios){
            assertTrue(data.contains(a));
        }
    }

    @Test
    public void test_get_metadata(){
        for(int i=0;i<SIZE;i++){
            assertTrue(((StubFileMetadata)database.getMetadata(audios.get(i))).j==i);
        }
    }

    @Test
    public void test_get_filtered(){
        Collection<Audio> data = database.getEntries(a->a.path.contains("3"));
        assertEquals(audios.get(3),data.toArray()[0]);
    }

    @Test
    public void test_reload(){
        provider.i=5;
        Collection<Audio> data = database.getEntries();
        for(Audio a : audios){
            assertTrue(data.contains(a));
        }
        database.reload(DUMMY_CONTEXT);
        data = database.getEntries();
        for(int i=0;i<5;i++){
            assertFalse(data.contains(audios.get(i)));
        }
        for(int i=5;i<SIZE;i++){
            assertTrue(data.contains(audios.get(i)));
        }
    }

    @Test
    public void test_get_metadata_fail(){
        assertNull(database.getMetadata(new Audio("aaa","bbb",AudioType.SPOTIFY)));
        assertNull(database.getMetadata(new Audio("aaa","bbb",AudioType.LOCAL)));
    }
}