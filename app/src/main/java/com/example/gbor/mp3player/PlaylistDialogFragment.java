package com.example.gbor.mp3player;

import android.app.DialogFragment;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class PlaylistDialogFragment extends DialogFragment {
    private OnPlaylistDialogFragmentInteractionListener mListener;

    ListView mListView;
    Button mRemoveButton;
    List<Integer> mPositions = new ArrayList<>();
    final static String ARRAY_KEY = "array";
    boolean mIsSelecting = false;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playlist_dialog, container, false);

        if (getDialog().getWindow() != null) {
            getDialog().getWindow().setTitle(getString(R.string.playlist_choosing));
        }
        mListView = view.findViewById(R.id.playlist_list);
        try {
            mRemoveButton = view.findViewById(R.id.remove_button);

        }catch (Exception e) {
            e.printStackTrace();
        }

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mIsSelecting){
                    selecting(position, view);
                }else {
                    Toast.makeText(getActivity(), String.valueOf(position), Toast.LENGTH_LONG).show();
                    dismiss();
                }
            }
        });

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                if (v != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(VibrationEffect.createOneShot(250,VibrationEffect.DEFAULT_AMPLITUDE));
                    }else{
                        v.vibrate(250);
                    }
                }
                mIsSelecting = true;
                mRemoveButton.setEnabled(true);
                selecting(position, view);
                return true;
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle arguments = getArguments();
        String[] stringArray = arguments.getStringArray(ARRAY_KEY);
        ArrayList<String> array = new ArrayList<>();
        array.add(getString(R.string.all_songs));
        if (stringArray != null) {
            array.addAll(Arrays.asList(stringArray));
        }

        try {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                    R.layout.item_playlist_dialog, array);
            mListView.setAdapter(adapter);

        }catch (Exception e) {
            e.printStackTrace();
        }



    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnPlaylistDialogFragmentInteractionListener) {
            mListener = (OnPlaylistDialogFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnPlaylistDialogFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void selecting(int position, View view){
        if(mPositions.contains(position)){
            view.setSelected(false);
            mPositions.remove(Integer.valueOf(position));
        }else {
            mPositions.add(position);
            view.setSelected(true);
        }
        if (mPositions.isEmpty()) {
            mIsSelecting = false;
            mRemoveButton.setEnabled(false);
        }
    }

    interface OnPlaylistDialogFragmentInteractionListener {

    }

}
