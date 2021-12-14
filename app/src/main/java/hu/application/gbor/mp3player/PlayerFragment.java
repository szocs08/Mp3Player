package hu.application.gbor.mp3player;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.gbor.mp3player.R;


public class PlayerFragment extends Fragment implements SeekBar.OnSeekBarChangeListener {


    private ImageButton mBtnPlay;
    private ImageButton mBtnShuffle;
    private ImageButton mBtnRepeat;
    private ImageView mImgAlbum;
    private SeekBar mProgressSeekBar;
    private TextView mSongTitleLabel;
    private TextView mSongArtistLabel;
    private TextView mSongAlbumLabel;
    private TextView mCurrentTimeLabel;
    private TextView mTotalTimeLabel;
    private final Handler mHandler = new Handler();
    private Context mContext;
    private OnPlayerFragmentInteractionListener mInteractionListener;
    private PlayerViewModel mViewModel;

    interface OnPlayerFragmentInteractionListener {
        void playButton();

        void previousButton();

        void nextButton();

        void repeatButton();

        void shuffleButton();

        void updateProgressBar();

        void seekButtonMovement(int progress);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player, container, false);
        mContext =getContext();
        mBtnPlay = view.findViewById(R.id.play_button);
        ImageButton mBtnPrevious = view.findViewById(R.id.previous_button);
        ImageButton mBtnNext = view.findViewById(R.id.next_button);
        mBtnShuffle = view.findViewById(R.id.shuffle_button);
        mBtnRepeat = view.findViewById(R.id.repeat_button);
        mImgAlbum = view.findViewById(R.id.song_album_thumbnail);
        mProgressSeekBar = view.findViewById(R.id.seek_bar);
        mSongTitleLabel = view.findViewById(R.id.song_title);
        mSongArtistLabel = view.findViewById(R.id.song_artist);
        mSongAlbumLabel = view.findViewById(R.id.song_album);
        mCurrentTimeLabel = view.findViewById(R.id.current_time);
        mTotalTimeLabel = view.findViewById(R.id.end_time);
        mSongArtistLabel.setSelected(true);
        mSongTitleLabel.setSelected(true);
        mSongAlbumLabel.setSelected(true);

        mProgressSeekBar.setOnSeekBarChangeListener(this);

        mBtnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInteractionListener.nextButton();

            }
        });

        mBtnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInteractionListener.previousButton();
            }
        });

        mBtnRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInteractionListener.repeatButton();
            }
        });

        mBtnShuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInteractionListener.shuffleButton();
            }
        });

        mBtnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInteractionListener.playButton();
            }
        });
        mViewModel = new ViewModelProvider(requireActivity()).get(PlayerViewModel.class);


        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel.getPlaylist().observe(getViewLifecycleOwner(), playlist -> {
            updateUI(playlist.getCurrentSong());
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnPlayerFragmentInteractionListener) {
            mInteractionListener = (OnPlayerFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnPlayerFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mInteractionListener = null;
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
        mInteractionListener.seekButtonMovement(seekBar.getProgress());
        updateProgressBar();
    }

    public void updatePlayButton(boolean playing) {
        if (!playing) {
            mBtnPlay.setImageResource(R.drawable.play_button);
        } else {
            mBtnPlay.setImageResource(R.drawable.pause_button);
        }
    }

    public void updateShuffleButton(boolean shuffle) {
        if (shuffle) {
            Toast.makeText(getContext(), "Shuffle is OFF", Toast.LENGTH_SHORT).show();
            mBtnShuffle.setImageResource(R.drawable.shuffle_button);
        } else {
            Toast.makeText(getContext(), "Shuffle is ON", Toast.LENGTH_SHORT).show();

            mBtnRepeat.setImageResource(R.drawable.repeat_button);
            mBtnShuffle.setImageResource(R.drawable.img_shuffle_pressed);
        }
    }

    public void updateRepeatButton(boolean repeat) {
        if (repeat) {
            Toast.makeText(getContext(), "Repeat is OFF", Toast.LENGTH_SHORT).show();
            mBtnRepeat.setImageResource(R.drawable.repeat_button);
        } else {
            Toast.makeText(getContext(), "Repeat is ON", Toast.LENGTH_SHORT).show();

            mBtnRepeat.setImageResource(R.drawable.img_repeat_pressed);
            mBtnShuffle.setImageResource(R.drawable.shuffle_button);
        }
    }


    public void updateUI(Song currentSong)
    {
        if (currentSong!=null){
            mSongTitleLabel.setText(currentSong.getTitle());

            mSongArtistLabel.setText(currentSong.getArtist());

            mSongAlbumLabel.setText(currentSong.getAlbum());

            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(currentSong.getData());
            if(mmr.getEmbeddedPicture()!=null)
                mImgAlbum.setImageBitmap(BitmapFactory.decodeByteArray(mmr.getEmbeddedPicture(),0,mmr.getEmbeddedPicture().length));
            else
                mImgAlbum.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.img_album_art));

        }else {
            mSongTitleLabel.setText(getString(R.string.song_title));

            mSongArtistLabel.setText(getString(R.string.song_artist));

            mSongAlbumLabel.setText(getString(R.string.album));
            mImgAlbum.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.img_album_art));
            mCurrentTimeLabel.setText(R.string.start_time);
            mCurrentTimeLabel.setText(R.string.end_time);
        }

        mBtnPlay.setImageResource(R.drawable.pause_button);

        mProgressSeekBar.setProgress(0);
        mProgressSeekBar.setMax(100);
        updateProgressBar();



        mSongArtistLabel.setSelected(true);
        mSongTitleLabel.setSelected(true);
        mSongAlbumLabel.setSelected(true);

        mBtnPlay.setImageResource(R.drawable.pause_button);

    }

    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

//    public void changeCursor(Cursor newCursor) {
//        mCursor = newCursor;
//        if (newCursor != null && mBtnPlay != null) {
//            mBtnPlay.setImageResource(R.drawable.play_button);
//        }
//    }

    private final Runnable mUpdateTimeTask = new Runnable() {
        @Override
        public void run() {
            mInteractionListener.updateProgressBar();
            mHandler.postDelayed(this, 100);
        }
    };

    public void timerUpdate(long totalDuration, long currentDuration) {

        mCurrentTimeLabel.setText(Utilities.milliSecondsToTimer(currentDuration));
        mTotalTimeLabel.setText(Utilities.milliSecondsToTimer(totalDuration));

        int progress = Utilities.getProgressPercentage(currentDuration, totalDuration);

        mProgressSeekBar.setProgress(progress);
    }

}
