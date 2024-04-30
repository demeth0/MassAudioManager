package com.demeth.massaudioplayer.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.demeth.massaudioplayer.R;
import com.demeth.massaudioplayer.backend.AlbumLoader;
import com.demeth.massaudioplayer.backend.models.objects.Audio;
import com.demeth.massaudioplayer.backend.models.objects.AudioType;
import com.demeth.massaudioplayer.backend.models.objects.LoopMode;
import com.demeth.massaudioplayer.service.AudioService;
import com.demeth.massaudioplayer.ui.utils.OnSwipeTouchDetector;

import java.util.Collections;

public class ControllerActivity extends ServiceBoundActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);


    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onServiceConnection() {
        AudioService service = binder.getService(this);
        TextView title = findViewById(R.id.controller_controller_music_title);
        ImageView album = findViewById(R.id.controller_album_imageview);
        TextView timer = findViewById(R.id.controller_controller_music_timer);

        SeekBar bar = findViewById(R.id.controller_song_progression_bar);
        ImageButton pause = findViewById(R.id.controller_control_play_button);
        ImageButton loopbtn = findViewById(R.id.controller_control_loop_button);
        ImageButton randbtn = findViewById(R.id.controller_control_random_button);
        ImageButton nextbtn = findViewById(R.id.controller_control_next_button);
        ImageButton previousbtn = findViewById(R.id.controller_control_previous_button);

        ImageButton clear_queue = findViewById(R.id.controller_clear_queue);
        ImageButton like = findViewById(R.id.controller_like_button);

        //to set title and album
        diffusionViewModel.getEntry().observe(this,audio -> {
            title.setText(audio.display_name);
            AlbumLoader.getAlbumImage(this,audio,512,album::setImageBitmap);
        });

        album.setOnTouchListener(new OnSwipeTouchDetector(this) {
            @Override
            public void onSwipeRight() {
                service.play_previous_audio();
            }

            @Override
            public void onSwipeLeft() {
                service.play_next_audio();
            }
        });

        /*to set time*/
        diffusionViewModel.getTimestamp().observe(this,timestamp -> {
            timer.setText(timer.getContext().getString(R.string.timestamp,(timestamp.current/60000),(timestamp.current/1000)%60,timestamp.duration/60000, (timestamp.duration/1000)%60));
            float ratio=(float)timestamp.current/timestamp.duration;
            bar.setProgress((int)(bar.getMax()*ratio));
        });
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int i, boolean b) {}
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {
                double ratio=(double)seekBar.getProgress()/seekBar.getMax();
                //no inspection constant conditions
                service.set_audio_progress(ratio);
            }
        });

        diffusionViewModel.getLoopMode().observe(this,loopMode -> {
            Log.d("ControllerActivity","loop mode :"+loopMode);
            switch(loopMode){
                case ALL:
                    loopbtn.setImageResource(R.drawable.loop_all);
                    break;
                case NONE:
                    loopbtn.setImageResource(R.drawable.loop_none);
                    break;
                case SINGLE:
                    loopbtn.setImageResource(R.drawable.loop_one);
                    break;
            }
        });

        loopbtn.setOnClickListener(view1 -> {
            switch (service.get_loop_mode()){
                case SINGLE:
                    service.set_loop_mode(LoopMode.NONE);
                    break;
                case NONE:
                    service.set_loop_mode(LoopMode.ALL);
                    break;
                case ALL:
                    service.set_loop_mode(LoopMode.SINGLE);
                    break;
            }
        });

        diffusionViewModel.getRandomMode().observe(this,aBoolean -> {
            if(aBoolean){
                randbtn.setImageResource(R.drawable.random_enabled);
            }else{
                randbtn.setImageResource(R.drawable.random_none);
            }
        });

        randbtn.setOnClickListener(view1 -> {
            service.set_shuffle_mode(!service.get_shuffle_mode());
        });


        diffusionViewModel.getPaused().observe(this,aBoolean -> {
            if(aBoolean){
                pause.setImageResource(android.R.drawable.ic_media_play);
            }else{
                pause.setImageResource(android.R.drawable.ic_media_pause);
            }
        });

        pause.setOnClickListener(view1 -> {
            if(service.is_audio_paused())
                service.play();
            else
                service.pause();
        });

        nextbtn.setOnClickListener(o -> service.play_next_audio());
        previousbtn.setOnClickListener(o -> service.play_previous_audio());

        clear_queue.setOnClickListener(view -> {
            diffusionViewModel.setEntry(new Audio("","", AudioType.LOCAL)); //TODO EMPTY undefined
            service.set_playlist(Collections.emptyList());
        });

        //PlaylistManager playlist_manager = service.getPlaylistManager();

        diffusionViewModel.getEntry().observe(this,identifiedEntry -> {
            /*if(playlist_manager.get("liked").contains(identifiedEntry)){
                like.setImageResource(R.drawable.like_enabled);
            }else{
                like.setImageResource(R.drawable.like);
            }*/ //TODO playlist
        });
        View.OnClickListener liked_listener =view -> {
            Audio audio = diffusionViewModel.getEntry().getValue();
            /*if(audio!=null){
                Playlist p = playlist_manager.get("liked");
                if(p.contains(audio)){
                    p.remove(audio);
                    like.setImageResource(R.drawable.like);
                }else{
                    p.add(audio);
                    like.setImageResource(R.drawable.like_enabled);
                }
            }*///TODO playlist
        };
        liked_listener.onClick(like);
        like.setOnClickListener(liked_listener);
    }
}
