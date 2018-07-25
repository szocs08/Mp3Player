package com.example.gbor.mp3player;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class PlaylistDialogFragment extends DialogFragment {
    private OnPlaylistDialogFragmentInteractionListener mListener;
    private static final String PLAYLIST_FILE = "com.example.gbor.mp3player.Playlist";
    private SharedPreferences mPlaylistIDs;


    ListView mListView;
    Button mRemoveButton;
    Button mAddButton;
    List<Integer> mPositions = new ArrayList<>();
    boolean mIsSelecting = false;
    TreeMap<String,Boolean> mMap = new TreeMap<>(new PlaylistComparator());
    PlaylistAdapter mAdapter;
    PlaylistFragment.DialogTypes mType;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPlaylistIDs = getActivity().getSharedPreferences(PLAYLIST_FILE, Context.MODE_PRIVATE);
        mType = (PlaylistFragment.DialogTypes) getArguments().getSerializable("type");
        if (getActivity() instanceof OnPlaylistDialogFragmentInteractionListener) {
            mListener = (OnPlaylistDialogFragmentInteractionListener) getActivity();
        } else {
            throw new RuntimeException(getActivity().toString()
                    + " must implement OnPlayerFragmentInteractionListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
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
                    switch (mType) {
                        case SWITCHING:
                            mListener.playlistSelection(mAdapter.mItemData.get(position));
                            break;
                        case ADDING:
                            mListener.addSelectedSongs(mAdapter.mItemData.get(position));
                            break;
                    }
                    dismiss();
                }
            }
        });



        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                mIsSelecting = true;
                mRemoveButton.setEnabled(true);
                mAddButton.setEnabled(false);
                mMap.put((String)mMap.keySet().toArray()[0],true);
                selecting(position);
                mAdapter.notifyDataSetChanged();
                return true;
            }
        });

        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                final EditText editText = new EditText(getActivity());
                builder.setTitle(getString(R.string.new_playlist));
                editText.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(editText);
                builder.setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = editText.getText().toString();
                        mListener.playlistAddButton(name);
                        mMap.put(name,false);
                        mAdapter.add(name);
                        Collections.sort(mAdapter.mItemData,new PlaylistComparator());
                        mAdapter.notifyDataSetChanged();
                        dialog.dismiss();
                    }
                });
                builder.show();
            }
        });

        mRemoveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> names = new ArrayList<>();
                for (int i = 0; i < mAdapter.mItemData.size(); i++){
                    if (mPositions.contains(i)) {
                        names.add(mAdapter.mItemData.get(i));
                    }
                }
                mAdapter.mItemData.removeAll(names);
                mAdapter.notifyDataSetChanged();
                mListener.playlistRemoveButton(names);
                dismiss();
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        List<String> array = new ArrayList<>();
        array.add(getString(R.string.all_songs));
        for (Map.Entry<String, ?> entry : mPlaylistIDs.getAll().entrySet()){
            array.add(entry.getKey());
        }
        for (String string : array)
            mMap.put(string,false);

        try {
            mAdapter = new PlaylistAdapter(getActivity(),
                    new ArrayList<>(mMap.keySet()));
            mListView.setAdapter(mAdapter);
        }catch (Exception e) {
            e.printStackTrace();
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
        ArrayList<String> mItemData;


        PlaylistAdapter(Activity context, ArrayList<String> itemData) {
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
            holder.playlistName.setText(mItemData.get(position));
            if (position > 0) {
                if(mMap.get(mItemData.get(position))) {
                    convertView.setBackgroundColor(ContextCompat.getColor(getContext(),R.color.list_select_color));
                    holder.playlistName.setTextColor(ContextCompat.getColor(getContext(),R.color.list_color_playlist));
                }else {
                    convertView.setBackgroundColor(ContextCompat.getColor(getContext(),R.color.list_color_playlist));
                    holder.playlistName.setTextColor(ContextCompat.getColor(getContext(),R.color.list_color));
                }
            }else if (mMap.get(mItemData.get(position))) {
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
        void playlistRemoveButton(List<String> names);
        void playlistAddButton(String name);
        void playlistSelection(String name);
        void addSelectedSongs(String name);
    }

    static class PlaylistComparator implements Comparator<String>{
        @Override
        public int compare(String o1, String o2) {
            if (o1.equalsIgnoreCase(o2))
                return o1.compareToIgnoreCase(o2);
            if (o1.equalsIgnoreCase("all songs"))
                return -1;
            if (o2.equalsIgnoreCase("all songs"))
                return 1;
            return o1.compareToIgnoreCase(o2);
        }
    }

}
