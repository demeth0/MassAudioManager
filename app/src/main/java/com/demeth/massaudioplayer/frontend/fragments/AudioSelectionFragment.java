package com.demeth.massaudioplayer.frontend.fragments;

import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.lifecycle.ViewModelProvider;

import com.demeth.massaudioplayer.R;
import com.demeth.massaudioplayer.backend.Dependencies;
import com.demeth.massaudioplayer.backend.Shiraori;
import com.demeth.massaudioplayer.backend.models.objects.Audio;
import com.demeth.massaudioplayer.backend.models.objects.EventCodeMap;
import com.demeth.massaudioplayer.frontend.HomeViewModel;
import com.demeth.massaudioplayer.frontend.service.AudioService;
import com.demeth.massaudioplayer.frontend.service.AudioServiceBoundable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AudioSelectionFragment extends Fragment {

    private HomeViewModel viewModel;
    private RadioGroup categories_list;

    //keep an instance of the bundle to pass the backend core to all child fragments.
    private Bundle fragment_view_model_bundle;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_audio_selection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstance) {
        super.onViewCreated(view, savedInstance);
        viewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        load_components(view);

        Bundle bun = this.getArguments();
        if (bun == null || !bun.containsKey("audio_service")) {
            return;
        }
        Dependencies dep = ((AudioService.ServiceBinder) bun.getBinder("audio_service")).getService((AudioServiceBoundable) requireActivity()).getDependencies();

        loadFragments(bun.getBinder("audio_service"));
        setCategoryButtons();

        viewModel.getAudioSelectionCategory().observe(requireActivity(),s -> {
            //TODO switch category by loading new fragment
        });
    }

    private void loadFragments(IBinder binder){
        fragment_view_model_bundle = new Bundle();
        fragment_view_model_bundle.putBinder("audio_service",binder);

        getParentFragmentManager().beginTransaction().replace(R.id.selection_list_fragment_container, AllAudioListFragment.class,fragment_view_model_bundle).setReorderingAllowed(true).commit();
    }

    private void setCategoryButtons(){
        categories_list.setOnCheckedChangeListener((radioGroup, i) -> {
            RadioButton rb = radioGroup.findViewById(i);
            Log.d("whatever", "clicked category "+rb.toString());
            viewModel.setAudioSelectionCategory(rb.getText().toString());
        });

        RadioButton pistes, playlist, queue;
        pistes = addCategory("PISTES");
        playlist = addCategory("PLAYLIST");
        queue = addCategory("QUEUE");
        String c = viewModel.getAudioSelectionCategory().getValue();
        if(c!=null)
            switch(c){
                case "PLAYLIST":
                    playlist.setChecked(true);
                    break;
                case "PISTES":
                    pistes.setChecked(true);
                    break;
                case "QUEUE":
                    queue.setChecked(true);
                    break;
            }
    }

    private RadioButton addCategory(String str){
        RadioButton but = new RadioButton(requireContext());
        RadioGroup.LayoutParams param = new RadioGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        int spacing = (int)getResources().getDimension(R.dimen.category_button_spacing);
        but.setPadding(spacing,0,spacing,0);
        but.setLayoutParams(param);

        but.setButtonDrawable(null);
        but.setBackground(AppCompatResources.getDrawable(requireContext(),R.drawable.radiobutton_selector));
        but.setText(str);
        but.setTextColor(requireContext().getColor(R.color.white));
        categories_list.addView(but);
        return but;
    }

    private void load_components(View view){
        categories_list = view.findViewById(R.id.categories_radio_group);
    }
}