package com.example.gbor.mp3player;

import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;


public class PlaylistDialogFragment extends DialogFragment {
    private OnPlaylistDialogFragmentInteractionListener mListener;

    ListView mListView;
    Button mOKButton;
    int mPosition = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playlist_dialog, container, false);
        getDialog().getWindow().setTitle(getString(R.string.playlist_choosing));
        mListView = view.findViewById(R.id.playlist_list);
        try {
            mOKButton = view.findViewById(R.id.ok_button);

        }catch (Exception e) {
            e.printStackTrace();
        }

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(mPosition==position){
                    view.setSelected(false);
                    mPosition = -1;
                }else {
                    mPosition = position;
                    view.setSelected(true);
                }
            }
        });
        mOKButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(),String.valueOf(mPosition),Toast.LENGTH_LONG).show();
                dismiss();
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        try {
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                    R.layout.item_playlist_dialog, getArguments().getStringArray("array"));
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

    interface OnPlaylistDialogFragmentInteractionListener {

    }

}
