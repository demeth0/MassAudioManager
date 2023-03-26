package com.demeth.massaudioplayer.ui.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.demeth.massaudioplayer.R;
import com.demeth.massaudioplayer.database.AlbumLoader;
import com.demeth.massaudioplayer.databinding.FragmentAudioEntryDisplayerBinding;
import com.demeth.massaudioplayer.ui.MainActivity;
import com.demeth.massaudioplayer.ui.SquareImageButton;
import com.demeth.massaudioplayer.ui.viewmodel.DiffusionViewModel;

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
        diffusionViewModel = new ViewModelProvider(requireActivity()).get(DiffusionViewModel.class);
        //timer.setText(timer.getContext().getString(R.string.timestamp,(cur/60000),(cur/1000)%60,duration/60000, (duration/1000)%60));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_small_controller, container, false);

        TextView title = view.findViewById(R.id.small_controller_music_title);
        SquareImageButton album = view.findViewById(R.id.small_controller_album_imageview);

        TextView timer = view.findViewById(R.id.small_controller_music_timer);

        //to set title and album
        diffusionViewModel.getEntry().observe(getViewLifecycleOwner(),identifiedEntry -> {
            title.setText(identifiedEntry.getName());
            //album.setImageBitmap();
            AlbumLoader.getAlbumImage(view,identifiedEntry,album.getWidth(),album::setImageBitmap);
        });

        /*to set time*/
        diffusionViewModel.getTimestamp().observe(getViewLifecycleOwner(),timestamp -> {
            timer.setText(timer.getContext().getString(R.string.timestamp,(timestamp.current/60000),(timestamp.current/1000)%60,timestamp.duration/60000, (timestamp.duration/1000)%60));
        });

        return view;
    }
}