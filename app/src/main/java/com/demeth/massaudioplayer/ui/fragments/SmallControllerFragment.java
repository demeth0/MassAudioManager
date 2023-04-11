package com.demeth.massaudioplayer.ui.fragments;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.demeth.massaudioplayer.R;
import com.demeth.massaudioplayer.audio_player.AudioPlayer;
import com.demeth.massaudioplayer.database.AlbumLoader;
import com.demeth.massaudioplayer.service.AudioService;
import com.demeth.massaudioplayer.service.BoundableActivity;
import com.demeth.massaudioplayer.ui.ControllerActivity;
import com.demeth.massaudioplayer.ui.MainActivity;
import com.demeth.massaudioplayer.ui.SquareImageButton;
import com.demeth.massaudioplayer.ui.viewmodel.DiffusionViewModel;
import com.demeth.massaudioplayer.ui.viewmodel.ListViewModel;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class SmallControllerFragment extends Fragment {

    public SmallControllerFragment() {
        // Required empty public constructor
    }

    private DiffusionViewModel diffusionViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bun = this.getArguments();
        Log.d("AudioEntryDisplayer",bun==null?"null":bun.toString());
        if(bun!=null && bun.containsKey("service")) {
            AudioService service = ((AudioService.AudioBinder) bun.getBinder("service")).getService((BoundableActivity) requireActivity());
            diffusionViewModel = new ViewModelProvider(service).get(DiffusionViewModel.class);
        }
        //timer.setText(timer.getContext().getString(R.string.timestamp,(cur/60000),(cur/1000)%60,duration/60000, (duration/1000)%60));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_small_controller, container, false);

        Bundle bun = this.getArguments();
        if(diffusionViewModel!=null && bun!=null && bun.containsKey("service")){

            AudioService service = ((AudioService.AudioBinder) bun.getBinder("service")).getService((BoundableActivity) requireActivity());

            TextView title = view.findViewById(R.id.small_controller_music_title);
            SquareImageButton album = view.findViewById(R.id.small_controller_album_imageview);

            TextView timer = view.findViewById(R.id.small_controller_music_timer);

            SeekBar bar = view.findViewById(R.id.small_controller_progression_bar);

            //to set title and album
            diffusionViewModel.getEntry().observe(getViewLifecycleOwner(),identifiedEntry -> {
                title.setText(identifiedEntry.getName());
                //album.setImageBitmap();
                AlbumLoader.getAlbumImage(view,identifiedEntry,Math.max(album.getWidth(),128),album::setImageBitmap);
            });

            ImageButton loopbtn = view.findViewById(R.id.main_control_loop_button);
            diffusionViewModel.getLoopMode().observe(getViewLifecycleOwner(),loopMode -> {
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
                switch (service.getPlayer().getLoopMode()){
                    case SINGLE:
                        service.getPlayer().setLoop(AudioPlayer.LoopMode.NONE);
                        break;
                    case NONE:
                        service.getPlayer().setLoop(AudioPlayer.LoopMode.ALL);
                        break;
                    case ALL:
                        service.getPlayer().setLoop(AudioPlayer.LoopMode.SINGLE);
                        break;
                }
            });

            ImageButton randbtn = view.findViewById(R.id.main_control_random_button);

            diffusionViewModel.getRandomMode().observe(getViewLifecycleOwner(),aBoolean -> {
                if(aBoolean){
                    randbtn.setImageResource(R.drawable.random_enabled);
                }else{
                    randbtn.setImageResource(R.drawable.random_none);
                }
            });

            randbtn.setOnClickListener(view1 -> {
                service.getPlayer().setRandom(!service.getPlayer().isRandom());
            });

            ImageButton pause = view.findViewById(R.id.main_control_play_button);

            diffusionViewModel.getPaused().observe(getViewLifecycleOwner(),aBoolean -> {
                if(aBoolean){
                    pause.setImageResource(android.R.drawable.ic_media_play);
                }else{
                    pause.setImageResource(android.R.drawable.ic_media_pause);
                }
            });

            pause.setOnClickListener(view1 -> {
                if(service.getPlayer().getState().equals(AudioPlayer.State.PAUSED))
                    service.getPlayer().play();
                else
                    service.getPlayer().pause();
            });

            ImageButton next = view.findViewById(R.id.main_control_next_button);
            next.setOnClickListener(o -> service.getPlayer().next());
            ImageButton prev = view.findViewById(R.id.main_control_previous_button);
            prev.setOnClickListener(o -> service.getPlayer().previous());

            /*to set time*/
            diffusionViewModel.getTimestamp().observe(getViewLifecycleOwner(),timestamp -> {
                timer.setText(timer.getContext().getString(R.string.timestamp,(timestamp.current/60000),(timestamp.current/1000)%60,timestamp.duration/60000, (timestamp.duration/1000)%60));
                //TODO set seek bar
                float ratio=(float)timestamp.current/timestamp.duration;
                bar.setProgress((int)(bar.getMax()*ratio));
            });
            bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override public void onProgressChanged(SeekBar seekBar, int i, boolean b) {}
                @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override public void onStopTrackingTouch(SeekBar seekBar) {
                    float ratio=(float)seekBar.getProgress()/seekBar.getMax();
                    //noinspection ConstantConditions
                    service.getPlayer().seekTo((int) (diffusionViewModel.getTimestamp().getValue().duration*ratio));
                }
            });

            view.setOnClickListener(view1 -> {
                Intent intent = new Intent(getContext(), ControllerActivity.class);
                startActivity(intent);
            });
        }

        return view;
    }
}