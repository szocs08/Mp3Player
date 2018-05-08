package com.example.gbor.mp3player;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
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

import java.io.File;


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
    private Cursor cursor;
    private Context context;

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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.player_layout, container, false);
        context=getContext();
        btnPlay = view.findViewById(R.id.play_button);
        btnPrevious = view.findViewById(R.id.previous_button);
        btnNext = view.findViewById(R.id.next_button);
        btnShuffle = view.findViewById(R.id.shuffle_button);
        btnRepeat = view.findViewById(R.id.repeat_button);
        imgAlbum = view.findViewById(R.id.song_album_thumbnail);
        progressSeekBar = view.findViewById(R.id.seek_bar);
        songTitleLabel = view.findViewById(R.id.song_title);
        songArtistLabel = view.findViewById(R.id.song_artist);
        songAlbumLabel = view.findViewById(R.id.song_album);
        currentTimeLabel = view.findViewById(R.id.current_time);
        totalTimeLabel = view.findViewById(R.id.end_time);
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
            if ((cursor.getCount()>0)){
                cursor.moveToPosition(songIndex);
                songTitleLabel.setText(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));

                songArtistLabel.setText(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));

                songAlbumLabel.setText(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)));

                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                mmr.setDataSource(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
                if(mmr.getEmbeddedPicture()!=null)
                    imgAlbum.setImageBitmap(BitmapFactory.decodeByteArray(mmr.getEmbeddedPicture(),0,mmr.getEmbeddedPicture().length));
                else
                    imgAlbum.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.img_adele));

            }else {
                songTitleLabel.setText(getString(R.string.song_title));

                songArtistLabel.setText(getString(R.string.song_artist));

                songAlbumLabel.setText(getString(R.string.album));
                imgAlbum.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.img_adele));
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
        btnPlay.setImageResource(R.drawable.pause_button);

    }

    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    public Cursor swapCursor(Cursor newCursor) {
        if (newCursor == cursor) {
            return null;
        }
        Cursor oldCursor = cursor;
        cursor = newCursor;
        if (newCursor != null) {
            updateUI(0);
            btnPlay.setImageResource(R.drawable.play_button);
        }
        return oldCursor;
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
