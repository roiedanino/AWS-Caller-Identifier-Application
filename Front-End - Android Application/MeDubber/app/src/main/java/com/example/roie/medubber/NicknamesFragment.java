package com.example.roie.medubber;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import amazonSQS.AmazonSQSClientService;


public class NicknamesFragment extends Fragment {

    private Map<String, Integer> nicknamesMap = new HashMap<>();
    private ArrayList<String> nicknames = new ArrayList<>();
    private ArrayList<Integer> nicknamesCount = new ArrayList<>();

    private ListView nicknamesList;

    private FloatingActionButton downloadNicknamesButton;

    public NicknamesFragment() {
        // Required empty public constructor

       // nicknamesMap.addAll(Arrays.asList("Or", "Or Givati", "Or Mamuchka Givati", "Or <3"));
    }

    // TODO: Rename and change types and number of parameters
    public static NicknamesFragment newInstance() {
        NicknamesFragment fragment = new NicknamesFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_nicknames, container, false);

        nicknamesList = rootView.findViewById(R.id.nicknamesList);

        nicknamesList.setAdapter(new NicknameListViewItemAdapter());

        downloadNicknamesButton = rootView.findViewById(R.id.downloadNicknamesButton);

        downloadNicknamesButton.setOnClickListener(this::onButtonPressed);

        // Inflate the layout for this fragment
        return rootView;
    }

    public void onButtonPressed(View view) {
        AsyncTask.execute(() -> {
            nicknamesMap = AmazonSQSClientService.getInstance().getNicknames(getActivity());

            nicknamesMap.forEach((name, count) -> {
                nicknames.add(name);
                nicknamesCount.add(count);
            });

            nicknamesList.deferNotifyDataSetChanged();
        });
    }

    public class NicknameListViewItemAdapter extends BaseAdapter {

        // number of rows
        @Override
        public int getCount() {
            return nicknamesMap.size();
        }

        @Override
        public Object getItem(int position) {
            return nicknames.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        // render each row
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {


            convertView = getLayoutInflater().inflate(R.layout.custom_table_cell, null);

            TextView name_TextView = (TextView) convertView.findViewById(R.id.contact_name);
            TextView number_TextView = (TextView) convertView.findViewById(R.id.contact_number);
            //    ImageView contact_image = convertView.findViewById(R.id.profile_image);
//
            String countDescription = nicknamesCount.get(position) +
                    getResources().getString(R.string.countDescrip);

            name_TextView.setText(nicknames.get(position));
            number_TextView.setText(countDescription);
            //     contact_image.setImageResource(contacts[position].getImageIndex());

            return convertView;
        }
    }
}
