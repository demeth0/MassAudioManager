package com.demeth.massaudioplayer.frontend.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.demeth.massaudioplayer.R;
import com.demeth.massaudioplayer.backend.Dependencies;
import com.demeth.massaudioplayer.backend.Shiraori;
import com.demeth.massaudioplayer.backend.models.objects.Audio;
import com.demeth.massaudioplayer.frontend.HomeViewModel;
import com.demeth.massaudioplayer.frontend.service.AudioService;
import com.demeth.massaudioplayer.frontend.service.AudioServiceBoundable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class AllAudioListFragment extends Fragment {

    private class MyAdapter extends BaseAdapter {
        List<Audio> items;
        public MyAdapter(Collection<Audio> data){
            items = new ArrayList<>(data);
        }

        // override other abstract methods here

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int i) {
            return items.get(i);
        }

        @Override
        public long getItemId(int i) {
            return items.get(i).hashCode();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup container) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_view_dummy_layout, container, false);
            }

            ((TextView) convertView.findViewById(R.id.list_view_item))
                    .setText(((Audio)getItem(position)).display_name);
            return convertView;
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_all_audio_list, container, false);
    }

    private HomeViewModel viewModel;
    private ListView audio_list;
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstance) {
        super.onViewCreated(view, savedInstance);
        viewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        load_components(view);

        Bundle bun = this.getArguments();
        if (bun == null || !bun.containsKey("service")) {
            return;
        }
        Dependencies dep = ((AudioService.ServiceBinder) bun.getBinder("service")).getService((AudioServiceBoundable) requireActivity()).getDependencies();
        audio_list.setAdapter(new MyAdapter(Shiraori.getDatabaseEntries(dep)));
        audio_list.setOnItemClickListener((adapterView, _view, i, l) -> {
            Audio audio = (Audio) adapterView.getItemAtPosition(i);
            Shiraori.playAudio(audio, dep);
        });

        viewModel.getSearchQuery().observe(requireActivity(),s -> {
            audio_list.setAdapter(new MyAdapter(Shiraori.getDatabaseEntries(dep).stream().filter(a->a.display_name.toLowerCase().contains(s.toLowerCase())).collect(Collectors.toCollection(ArrayList::new))));
        });
    }

    private void load_components(View view){
        audio_list = view.findViewById(R.id.list_view);
    }
}