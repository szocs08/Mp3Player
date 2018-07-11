package com.example.gbor.mp3player;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;


public class PlaylistDialogFragment extends DialogFragment {
    private OnPlaylistDialogFragmentInteractionListener mListener;

    ListView mListView;
    Button mRemoveButton;
    Button mAddButton;
    List<Integer> mPositions = new ArrayList<>();
    final static String ARRAY_KEY = "array";
    boolean mIsSelecting = false;
    TreeMap<String,Boolean> mMap = new TreeMap<>();
    PlaylistAdapter mAdapter;


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
        mRemoveButton = view.findViewById(R.id.remove_button);
        mAddButton = view.findViewById(R.id.add_button);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mIsSelecting){
                    selecting(position);
                    mAdapter.notifyDataSetChanged();
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
                        v.vibrate(VibrationEffect.createOneShot(50,VibrationEffect.DEFAULT_AMPLITUDE));
                    }else{
                        v.vibrate(50);
                    }
                }
                mIsSelecting = true;
                mRemoveButton.setEnabled(true);
                mAddButton.setEnabled(false);
                mMap.put((String)mMap.keySet().toArray()[0],true);
                mMap.put((String)mMap.keySet().toArray()[position],true);
                selecting(position);
                mAdapter.notifyDataSetChanged();
                return true;
            }
        });

        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(),"add button", Toast.LENGTH_LONG).show();
                dismiss();
            }
        });

        mRemoveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(),"remove button", Toast.LENGTH_LONG).show();
                dismiss();
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle arguments = getArguments();
        String[] stringArray = arguments.getStringArray(ARRAY_KEY);
        mMap.put(getString(R.string.all_songs),false);
        if (stringArray != null) {
            for (String string : stringArray)
                mMap.put(string,false);
        }
        try {
            mAdapter = new PlaylistAdapter(getActivity(),
                    mMap.keySet().toArray(new String[mMap.keySet().size()]));
            mListView.setAdapter(mAdapter);
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

    private void selecting(int position){
        List<String> list = new ArrayList<>(mMap.keySet());
        if (position != 0) {
            if(mPositions.contains(position)){
                mMap.put(list.get(position),false);
                mPositions.remove(Integer.valueOf(position));
            }else {
                mPositions.add(position);
                mMap.put(list.get(position),true);
            }
        }
        if (mPositions.isEmpty()) {
            mIsSelecting = false;
            mRemoveButton.setEnabled(false);
            mAddButton.setEnabled(true);
            mMap.put((String)mMap.keySet().toArray()[0],false);
        }

    }


    static class ViewHolder{
        TextView playlistName;
    }

    private class PlaylistAdapter extends ArrayAdapter<String> {

        Activity mContext;
        String[] mItemData;


        PlaylistAdapter(Activity context, String[] itemData) {
            super(context,R.layout.item_playlist_dialog,itemData);
            mContext=context;
            mItemData = itemData;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            ViewHolder holder;
            if(convertView == null){
                holder = new ViewHolder();
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_playlist_dialog,parent,false);
                holder.playlistName =  convertView.findViewById(R.id.dialog_playlist_name);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }
            holder.playlistName.setText(mItemData[position]);
            if (position > 0) {
                if(mMap.get(mItemData[position])) {
                    convertView.setBackgroundColor(getResources().getColor(R.color.list_select_color));
                    holder.playlistName.setTextColor(getResources().getColor(R.color.list_color_playlist));
                }else {
                    convertView.setBackgroundColor(getResources().getColor(R.color.list_color_playlist));
                    holder.playlistName.setTextColor(getResources().getColor(R.color.list_color));
                }
            }else if (mMap.get(mItemData[position])) {
                convertView.setEnabled(false);
                holder.playlistName.setEnabled(false);
            }else {
                convertView.setEnabled(true);
                holder.playlistName.setEnabled(true);
            }
            return convertView;
        }
    }




    interface OnPlaylistDialogFragmentInteractionListener {

    }

}
