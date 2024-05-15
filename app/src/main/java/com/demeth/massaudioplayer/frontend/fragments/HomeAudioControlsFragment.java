package com.demeth.massaudioplayer.frontend.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.demeth.massaudioplayer.R;
import com.demeth.massaudioplayer.backend.Dependencies;
import com.demeth.massaudioplayer.backend.Shiraori;
import com.demeth.massaudioplayer.backend.models.objects.LoopMode;
import com.demeth.massaudioplayer.frontend.HomeViewModel;
import com.demeth.massaudioplayer.frontend.service.AudioService;
import com.demeth.massaudioplayer.frontend.service.AudioServiceBoundable;

public class HomeAudioControlsFragment extends Fragment {

    private HomeViewModel viewModel;
    private ImageButton random_button,loop_button,play_pause_button, next_button, previous_button;
    private TextView title, timestamp_text;

    private SeekBar timestamp_bar;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_audio_controls, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstance){
        super.onViewCreated(view, savedInstance);
        viewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        load_components(view);

        Bundle bun = this.getArguments();
        if(bun==null || !bun.containsKey("audio_service")){
            return;
        }
        Dependencies dep = ((AudioService.ServiceBinder) bun.getBinder("audio_service")).getService((AudioServiceBoundable) requireActivity()).getDependencies();

        setup_random(dep);
        setup_loop(dep);
        setup_pause_play(dep);
        setup_next_button(dep);
        setup_previous_button(dep);
        setup_timestamp(dep);

        viewModel.getCurrentAudioUI().observe(requireActivity(), audio -> {
            if(audio==null) {
                title.setText("");
            }else{
                title.setText(audio.display_name);
            }
        });
    }

    private void load_components(View view){
        random_button = view.findViewById(R.id.random_button);
        loop_button = view.findViewById(R.id.loop_button);
        play_pause_button = view.findViewById(R.id.play_button);
        next_button = view.findViewById(R.id.next_button);
        title = view.findViewById(R.id.audio_title);
        previous_button = view.findViewById(R.id.previous_button);
        timestamp_text = view.findViewById(R.id.audio_text_timer);
        timestamp_bar = view.findViewById(R.id.time_progression_bar);
    }

    private void setup_random(Dependencies dep){
        viewModel.getRandomModeUI().observe(getViewLifecycleOwner(), aBoolean -> {
            Log.d("Whatever", "value changed to "+aBoolean);
            if(aBoolean){
                random_button.setImageResource(R.drawable.random_enabled);
            }else{
                random_button.setImageResource(R.drawable.random_none);
            }
        });
        random_button.setOnClickListener(v -> {
            Log.d("Whatever", "Random button pressed");
            if(viewModel.getRandomModeUI().getValue()!=null){
                Log.d("Whatever", "got current value: "+viewModel.getRandomModeUI().getValue());
                Shiraori.setRandomModeEnabled(!viewModel.getRandomModeUI().getValue(),dep);
            }
        });
    }

    private void setup_loop(Dependencies dep){
        viewModel.getLoopModeUI().observe(getViewLifecycleOwner(), loopMode -> {
            switch(loopMode){
                case ALL:
                    loop_button.setImageResource(R.drawable.loop_all);
                    break;
                case NONE:
                    loop_button.setImageResource(R.drawable.loop_none);
                    break;
                case SINGLE:
                    loop_button.setImageResource(R.drawable.loop_one);
                    break;
            }
        });

        loop_button.setOnClickListener(v -> {
            switch (Shiraori.getLoopMode(dep)){
                case SINGLE:
                    Shiraori.setLoopMode(LoopMode.NONE,dep);
                    break;
                case NONE:
                    Shiraori.setLoopMode(LoopMode.ALL,dep);
                    break;
                case ALL:
                    Shiraori.setLoopMode(LoopMode.SINGLE,dep);
                    break;
            }
        });
    }

    private void setup_pause_play(Dependencies dep){
        viewModel.getPlayPauseStateUI().observe(requireActivity(),aBoolean -> {
            if(aBoolean){
                play_pause_button.setImageResource(android.R.drawable.ic_media_pause);
            }else{
                play_pause_button.setImageResource(android.R.drawable.ic_media_play);
            }
        });

        play_pause_button.setOnClickListener(v -> {
            Shiraori.pauseAudio(dep);
        });
    }

    private void setup_next_button(Dependencies dep){
        next_button.setOnClickListener(v -> {
            viewModel.setCurrentAudioUI(null); // reset current audio on the UI because we have no guaranty that the next audio will be loaded
            Shiraori.skipToNextAudio(dep);
        });
    }

    private void setup_previous_button(Dependencies dep){
        previous_button.setOnClickListener(v -> {
            Shiraori.skipToPreviousAudio(dep);
        });
    }

    private void setup_timestamp(Dependencies dependencies){
        viewModel.getAudioTimestamp().observe(requireActivity(),ts -> {
            if(ts==null) return;
            int current = (int) (ts.getDuration()*ts.getProgress());
            timestamp_text.setText(
                    timestamp_text.getContext()
                            .getString(
                                    R.string.timestamp,
                                    (current/60000),
                                    (current/1000)%60,ts.getDuration()/60000,
                                    (ts.getDuration()/1000)%60));
            timestamp_bar.setProgress((int)(timestamp_bar.getMax()*ts.getProgress()));
        });

        timestamp_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                double ratio=(double)seekBar.getProgress()/seekBar.getMax();
                Shiraori.setTimestamp(ratio, dependencies);
            }
        });
    }
}