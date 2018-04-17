package com.example.gbor.mp3player;

import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class FragmentPlayer extends Fragment implements SeekBar.OnSeekBarChangeListener {


    private ImageButton btnPlay;
    private ImageButton btnPrevious;
    private ImageButton btnNext;
    private ImageButton btnShuffle;
    private ImageButton btnRepeat;
    private ImageView imgAlbum;
    private SeekBar progressSeekBar;
    private TextView songTitleLabel;
    private TextView songArtistLabel;
    private TextView songAlbumLabel;
    private TextView currentTimeLabel;
    private TextView totalTimeLabel;


    private Handler mHandler = new Handler();
    private int songIndex;
    private ArrayList<String> playlist = new ArrayList<>();


    private OnPlayerFragmentInteractionListener interactionListener;


    interface OnPlayerFragmentInteractionListener {
        void play();

        void previous();

        void next();

        void repeat();

        void shuffle();

        void update();

        void seek(int progress);

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        List<String> songList= getArguments().getStringArrayList("playlist");
        if(songList != null){
            playlist.addAll(songList);
        }
        View view = inflater.inflate(R.layout.player_layout, container, false);

        btnPlay = (ImageButton) view.findViewById(R.id.play_button);
        btnPrevious = (ImageButton) view.findViewById(R.id.previous_button);
        btnNext = (ImageButton) view.findViewById(R.id.next_button);
        btnShuffle = (ImageButton) view.findViewById(R.id.shuffle_button);
        btnRepeat = (ImageButton) view.findViewById(R.id.repeat_button);
        imgAlbum = (ImageView) view.findViewById(R.id.song_album_thumbnail);
        progressSeekBar = (SeekBar) view.findViewById(R.id.seek_bar);
        songTitleLabel = (TextView) view.findViewById(R.id.song_title);
        songArtistLabel = (TextView) view.findViewById(R.id.song_artist);
        songAlbumLabel = (TextView) view.findViewById(R.id.song_album);
        currentTimeLabel = (TextView) view.findViewById(R.id.current_time);
        totalTimeLabel = (TextView) view.findViewById(R.id.end_time);
        songArtistLabel.setSelected(true);
        songTitleLabel.setSelected(true);
        songAlbumLabel.setSelected(true);

        progressSeekBar.setOnSeekBarChangeListener(this);

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                interactionListener.next();

            }
        });

        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                interactionListener.previous();
            }
        });

        btnRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                interactionListener.repeat();
            }
        });

        btnShuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                interactionListener.shuffle();
            }
        });

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                interactionListener.play();
            }
        });

        updateUI(songIndex);
        btnPlay.setImageResource(R.drawable.play_button);
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnPlayerFragmentInteractionListener) {
            interactionListener = (OnPlayerFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnPlayerFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        interactionListener = null;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
        interactionListener.seek(seekBar.getProgress());
        updateProgressBar();
    }

    public void updatePlayButton(boolean playing) {
        if (playing) {
            btnPlay.setImageResource(R.drawable.play_button);
        } else {
            btnPlay.setImageResource(R.drawable.pause_button);
        }
    }

    public void updateShuffleButton(boolean shuffle) {
        if (shuffle) {
            Toast.makeText(getContext(), "Shuffle is OFF", Toast.LENGTH_SHORT).show();
            btnShuffle.setImageResource(R.drawable.shuffle_button);
        } else {
            Toast.makeText(getContext(), "Shuffle is ON", Toast.LENGTH_SHORT).show();

            btnRepeat.setImageResource(R.drawable.repeat_button);
            btnShuffle.setImageResource(R.drawable.img_shuffle_pressed);
        }
    }

    public void updateRepeatButton(boolean repeat) {
        if (repeat) {
            Toast.makeText(getContext(), "Repeat is OFF", Toast.LENGTH_SHORT).show();
            btnRepeat.setImageResource(R.drawable.repeat_button);
        } else {
            Toast.makeText(getContext(), "Repeat is ON", Toast.LENGTH_SHORT).show();

            btnRepeat.setImageResource(R.drawable.img_repeat_pressed);
            btnShuffle.setImageResource(R.drawable.shuffle_button);
        }
    }


    public void updateUI(int songIndex) {
        try {
            Boolean empty = playlist.isEmpty();
            HashMap<String,String> song = SongManager.getSongData(getContext(),playlist.get(songIndex));
            if (!empty)
            if (playlist.isEmpty() || song.get("title") == null)
                songTitleLabel.setText(getString(R.string.song_title));
            else songTitleLabel.setText(song.get("title"));

            if (playlist.isEmpty() || song.get("artist") == null)
                songArtistLabel.setText(getString(R.string.song_artist));
            else songArtistLabel.setText(song.get("artist"));

            if (playlist.isEmpty() || song.get("album") == null)
                songAlbumLabel.setText(getString(R.string.album));
            else songAlbumLabel.setText(song.get("album"));
            if (playlist.isEmpty() || SongManager.getAlbumThumbnail(getContext(),song.get("albumID")) == null)
                imgAlbum.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.img_adele));
            else {
                Uri sArtworkUri = Uri
                        .parse("content://media/external/audio/albumart");
                Uri uri = ContentUris.withAppendedId(sArtworkUri, Long.parseLong(song.get("albumID")));
                imgAlbum.setImageURI(uri);
            }
            if (playlist.isEmpty()) {
                currentTimeLabel.setText(R.string.start_time);
                currentTimeLabel.setText(R.string.end_time);
            }

            btnPlay.setImageResource(R.drawable.pause_button);

            progressSeekBar.setProgress(0);
            progressSeekBar.setMax(100);
            updateProgressBar();

        } catch (IllegalArgumentException | IllegalStateException e) {
            e.printStackTrace();
        }

        songArtistLabel.setSelected(true);
    }

    public void updateUI(ArrayList<String> playlist){
        songIndex=0;
        this.playlist.clear();

        this.playlist.addAll(playlist);
        updateUI(songIndex);
        btnPlay.setImageResource(R.drawable.play_button);

    }


    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }


    private Runnable mUpdateTimeTask = new Runnable() {
        @Override
        public void run() {
            interactionListener.update();
            mHandler.postDelayed(this, 100);
        }
    };

    public void timerUpdate(long totalDuration, long currentDuration) {

        currentTimeLabel.setText(Utilities.milliSecondsToTimer(currentDuration));
        totalTimeLabel.setText(Utilities.milliSecondsToTimer(totalDuration));

        int progress = Utilities.getProgressPercentage(currentDuration, totalDuration);

        progressSeekBar.setProgress(progress);
    }

}
