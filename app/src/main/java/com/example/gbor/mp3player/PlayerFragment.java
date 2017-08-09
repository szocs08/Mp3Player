package com.example.gbor.mp3player;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
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


public class PlayerFragment extends Fragment implements SeekBar.OnSeekBarChangeListener {


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
    private ArrayList<HashMap<String, String>> songList = new ArrayList<>();
    private MediaMetadataRetriever mmr = new MediaMetadataRetriever();


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

        HashMap<String, String> songData;


        for (String song : getArguments().getStringArrayList("playlist")) {
            mmr.setDataSource(song);
            mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            songData = new HashMap<>();
            songData.put("songPath", song);
            songData.put("songArtist", mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
            songData.put("songTitle", mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
            songData.put("songAlbum", mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
            songList.add(songData);
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


    public void updateUI(int songindex) {
        try {
            mmr.setDataSource(songList.get(songindex).get("songPath"));

            if (songList.get(songindex).get("songTitle").isEmpty()) {
                songTitleLabel.setText(getString(R.string.song_title));
            }else {
                songTitleLabel.setText(songList.get(songindex).get("songTitle"));
            }
            if (songList.get(songindex).get("songArtist").isEmpty()) {
                songArtistLabel.setText(getString(R.string.song_artist));
            } else {
                songArtistLabel.setText(songList.get(songindex).get("songArtist"));
            }
            if (songList.get(songindex).get("songAlbum").isEmpty()) {
                songAlbumLabel.setText(getString(R.string.album));
            } else {
                songAlbumLabel.setText(songList.get(songindex).get("songAlbum"));
            }

            Bitmap img = BitmapFactory.decodeByteArray(mmr.getEmbeddedPicture(), 0, mmr.getEmbeddedPicture().length);
            imgAlbum.setImageBitmap(img);


            btnPlay.setImageResource(R.drawable.pause_button);

            progressSeekBar.setProgress(0);
            progressSeekBar.setMax(100);

            updateProgressBar();
        } catch (IllegalArgumentException | IllegalStateException e) {
            e.printStackTrace();
        }
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
