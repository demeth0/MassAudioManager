package com.demeth.massaudioplayer.backend;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Size;
import android.view.View;

import androidx.annotation.NonNull;

import com.demeth.massaudioplayer.R;
import com.demeth.massaudioplayer.backend.models.objects.Audio;
import com.demeth.massaudioplayer.backend.models.objects.AudioType;
import com.demeth.massaudioplayer.backend.models.objects.Metadata;
import com.demeth.massaudioplayer.service.AudioService;

import java.io.IOException;

/**
 * Manage asynchronous loading of album image.
 */
public class AlbumLoader {
    private static AudioService initiator;

    /**
     * provider main thread that will do the heavy loading of ressources and more
     */
    public static class ProviderThread extends Thread{
        /**
         * the handler of the thread
         */
        protected Handler handler;

        /**
         * create the looper and initialise the handler
         */
        @Override
        public void run() {
            Looper.prepare();
            handler = new Handler(Looper.myLooper()){
                @Override
                public void handleMessage(@NonNull Message msg) {
                    super.handleMessage(msg);
                }
            };
            Looper.loop();
        }

        /**
         * @param r the runnable to execute in the provider thread
         */
        public void post(Runnable r){
            handler.post(r);
        }

        /**
         * same as post but with a delay
         * @param r the runnable to execute
         * @param delayMillis the time to wait in ms
         */
        @SuppressWarnings("unused")
        public void postDelayed(Runnable r, int delayMillis){
            handler.postDelayed(r,delayMillis);
        }
    }

    /**
     * lorsque l'album a fini d'étre chargé ce callback est appelé
     */
    @FunctionalInterface
    public interface OnAlbumQueryFinished{
        void onFinish(@NonNull Bitmap result);
    }

    /**
     * thread du provider pour execution parallele
     */
    private static ProviderThread thread = null;
    private static Bitmap default_bitmap;

    /**
     * Cette fonction retourne l'image de l'album de la track en paramètre si l'image n'existe pas alors la bitmap retournée serra la bitmap de l'image par défaut pour les albums
     * multi-thread version
     * @param entry la track dont on veut l'image d'album
     */
    public static void getAlbumImage(View v, Audio entry, int size, OnAlbumQueryFinished callback) {
        thread.post(()->{
            if(entry!=null && entry.type.equals(AudioType.LOCAL) && size>0) {
                Size s = new Size(size, size);
                try {
                    final Bitmap res = v.getContext().getContentResolver().loadThumbnail(((Metadata.FileAudioMetadata)initiator.getDatabase().getMetadata(entry)).getUri(), s, null);
                    v.post(() -> callback.onFinish(res));
                } catch (IOException e) {
                    v.post(() -> callback.onFinish(default_bitmap));
                }
            }else{
                v.post(() -> callback.onFinish(default_bitmap));
            }
        });
    }

    /**
     * Cette fonction retourne l'image de l'album de la track en paramètre si l'image n'existe pas alors la bitmap retournée serra la bitmap de l'image par défaut pour les albums
     * non multi-threaded version
     * @param context the context
     * @param entry the track from
     * @param size the size
     * @param callback the callback
     */
    public static void getAlbumImage(Context context, Audio entry, int size, OnAlbumQueryFinished callback) {
        Bitmap res=default_bitmap;
        if(entry!=null && entry.type.equals(AudioType.LOCAL) && size>0) {
            Size s = new Size(size, size);
            try {
                res = context.getContentResolver().loadThumbnail(((Metadata.FileAudioMetadata)initiator.getDatabase().getMetadata(entry)).getUri(), s, null);
            } catch (IOException ignored) {}
        }
        callback.onFinish(res);
    }

    /**
     * @return the default cover mage
     */
    public static Bitmap getDefaultCover(){
        return default_bitmap;
    }



    /**
     * create the content provider context and start the threads
     * @param appContext the context of the app
     */
    public static void open(Context appContext, AudioService _initiator){
        if(thread==null){
            default_bitmap = BitmapFactory.decodeResource(appContext.getResources(), R.drawable.no_album);
            //super important sinon après close le thread est consumé et ne peux plus etre démarrer (jusqu'a redémarrage de l'appareil probablement)
            thread = new ProviderThread();
            thread.start();
        }
        initiator=_initiator;
    }

    /**
     * stop the thread and close the IO streams
     */
    public static void close(AudioService _initiator){
        //TODO need to test if pointers are conserved
        if(_initiator==initiator){
            thread.interrupt();
            thread = null;
        }
    }
}
