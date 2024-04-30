package com.demeth.massaudioplayer.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.FragmentContainerView;
import androidx.lifecycle.Observer;

import com.demeth.massaudioplayer.R;
import com.demeth.massaudioplayer.backend.models.objects.Audio;
import com.demeth.massaudioplayer.backend.models.objects.AudioType;
import com.demeth.massaudioplayer.service.AudioService;
import com.demeth.massaudioplayer.ui.fragments.AudioEntryDisplayer;
import com.demeth.massaudioplayer.ui.fragments.SmallControllerFragment;

import java.util.ArrayList;
import java.util.Collections;

@SuppressLint("SetTextI18n")
public class MainActivity extends ServiceBoundActivity {

    public static final SelectionManager selection_manager = new SelectionManager();

    private RadioGroup group=null;
    //private PlaylistManager playlist_manager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onServiceConnection(){
        AudioService service = binder.getService(this);
        //playlist_manager = service.getPlaylistManager();
        //init
        setupCategorySelector();
        setupController();
        setupSearchBar();

        ImageButton play_all = findViewById(R.id.play_all_random_button);
        play_all.setOnClickListener(view -> {
            service.set_shuffle_mode(true);
            AudioEntryDisplayer frag = (AudioEntryDisplayer) getSupportFragmentManager().findFragmentById(R.id.list_displayer_fragment_container);

            assert frag != null;
            service.set_playlist(new ArrayList<>(frag.getDisplayedContent()));
            service.play();
        });

        ImageButton remove_all = findViewById(R.id.main_clear_queue);
        remove_all.setOnClickListener(view -> {
            diffusionViewModel.setEntry(new Audio("","", AudioType.LOCAL)); //TODO define EMPTY
            service.set_playlist(Collections.emptyList());
        });

        ImageButton liked = findViewById(R.id.main_like_button);
        diffusionViewModel.getEntry().observe(this,audio -> {
            /*if(playlist_manager.get("liked").contains(identifiedEntry)){
                liked.setImageResource(R.drawable.like_enabled);
            }else{
                liked.setImageResource(R.drawable.like);
            }*///TODO playlist
        });

        View.OnClickListener liked_listener = view -> {
            /*IdentifiedEntry _audio = diffusionViewModel.getEntry().getValue();
            if(_audio!=null){
                Playlist p = playlist_manager.get("liked");
                if(p.contains(_audio)){
                    p.remove(_audio);
                    liked.setImageResource(R.drawable.like);
                }else{
                    p.add(_audio);
                    liked.setImageResource(R.drawable.like_enabled);
                }
            }*///TODO playlist
        };

        liked_listener.onClick(liked); //init button
        liked.setOnClickListener(liked_listener);
    }

    private void setupController(){
        Bundle bun = new Bundle();
        bun.putBinder("service",binder);
        getSupportFragmentManager().beginTransaction().replace(R.id.small_controller_fragment_container, SmallControllerFragment.class,bun).setReorderingAllowed(true).commit();

        FragmentContainerView controller = findViewById(R.id.small_controller_fragment_container);
        controller.setVisibility(View.GONE);
        Observer<Audio> controller_apparition_observer = new Observer<Audio>() {
            @Override
            public void onChanged(Audio audio) {
                if(audio.display_name.equals("")){ //TODO define EMPTY
                    controller.setVisibility(View.GONE);
                }else{
                    controller.setVisibility(View.VISIBLE);
                }
            }
        };

        diffusionViewModel.getEntry().observe(this,controller_apparition_observer);
    }

    private void setupCategorySelector(){
        //init
        group = findViewById(R.id.fragment_list);

        group.setOnCheckedChangeListener((radioGroup, i) -> {
            RadioButton rb = radioGroup.findViewById(i);
            //listViewModel.setActiveList(Category.valueOf());
            Bundle bun = new Bundle();
            bun.putString("category",rb.getText().toString());
            bun.putBinder("service",binder);
            getSupportFragmentManager().beginTransaction().replace(R.id.list_displayer_fragment_container,AudioEntryDisplayer.class,bun).setReorderingAllowed(true).commit();
        });


        addCategory(Category.PLAYLISTS.getName());
        addCategory(Category.PISTES.getName()).setChecked(true);
        addCategory(Category.QUEUE.getName());
    }

    private void setupSearchBar(){
        AutoCompleteTextView search_field = findViewById(R.id.search_field);
        SearchFieldAutoCompleter searchFieldAutoCompleter = new SearchFieldAutoCompleter(this,android.R.layout.simple_list_item_1);
        search_field.setAdapter(searchFieldAutoCompleter);
        search_field.setThreshold(1);

        listViewModel.getList().observe(this, searchFieldAutoCompleter::setContent);

        ImageButton search_button = findViewById(R.id.search_button);
        search_button.setOnClickListener(view -> {
            listViewModel.setFilterMask(search_field.getText().toString());
            search_field.clearFocus();
        });
        search_field.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            boolean ret = true;
            //IME Input Methode
            if(actionId== EditorInfo.IME_ACTION_DONE){
                listViewModel.setFilterMask(search_field.getText().toString());
                search_field.clearFocus();
                ret = false;
            }
            //si action consumée ? (succes ou echec) pour fermer ou non le clavier
            return ret;
        });
    }

    private RadioButton addCategory(String str){
        RadioButton but = new RadioButton(this);
        RadioGroup.LayoutParams param = new RadioGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        int spacing = (int)getResources().getDimension(R.dimen.category_button_spacing);
        but.setPadding(spacing,0,spacing,0);
        but.setLayoutParams(param);

        but.setButtonDrawable(null);
        but.setBackground(AppCompatResources.getDrawable(this,R.drawable.radiobutton_selector));
        but.setText(str);
        but.setTextColor(this.getColor(R.color.white));
        group.addView(but);
        return but;
    }

    //public PlaylistManager getPlaylistManager() {
    //    return playlist_manager;
    //}
}