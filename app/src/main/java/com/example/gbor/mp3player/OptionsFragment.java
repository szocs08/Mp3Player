package com.example.gbor.mp3player;

import android.app.Activity;
import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class OptionsFragment extends ListFragment {


    private class OptionsAdapter extends ArrayAdapter<HashMap<String, String>> {

        private final Activity context;
        private ArrayList<HashMap<String, String>> itemData;

        public OptionsAdapter(Activity context, ArrayList<HashMap<String, String>> itemData) {
            super(context, R.layout.options_item, itemData);
            this.context = context;
            this.itemData = itemData;


        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater li = context.getLayoutInflater();
            View v = li.inflate(R.layout.options_item, null, true);
            TextView optionName = (TextView) v.findViewById(R.id.option_name);
            TextView oprionValue = (TextView) v.findViewById(R.id.option_value);

            optionName.setText(itemData.get(position).get("optionName"));
            oprionValue.setText(itemData.get(position).get("optionValue"));

            return v;
        }
    }

    private OnOptionsFragmentInteractionListener interactionListener;

    private OptionsAdapter optionsAdapter;


    public interface OnOptionsFragmentInteractionListener {
        void asd(int position);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        interactionListener.asd(position);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ArrayList<HashMap<String, String>> optionDataList = new ArrayList<>();
        HashMap<String, String> optionData;

        for (String option : getResources().getStringArray(R.array.options_list)) {
            optionData = new HashMap<>();
            optionData.put("optionName", option);
            optionData.put("optionValue", getResources().getString(R.string.default_folder));
            optionDataList.add(optionData);
        }

        optionsAdapter = new OptionsAdapter(getActivity(),optionDataList);




        ArrayAdapter arrayAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.options_list,android.R.layout.simple_list_item_1);
        setListAdapter(optionsAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.options, container, false);

        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnOptionsFragmentInteractionListener) {
            interactionListener = (OnOptionsFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnOptionsFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        interactionListener = null;
    }



}