package com.example.roie.medubber;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import services.ContactsLoaderService;
import entities.AndroidContact;

public class ContactsFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    private ArrayList<AndroidContact> androidContacts = new ArrayList<>();

    private MyListViewAdapter customAdapter = new MyListViewAdapter();


    private ListView contactsList;

    public ContactsFragment() {
        // Required empty public constructor

    }

    public static ContactsFragment newInstance(int sectionNumber) {
        ContactsFragment fragment = new ContactsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public void setAndroidContacts(ArrayList<AndroidContact> androidContacts) {
        this.androidContacts = androidContacts;
        customAdapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_contacts, container, false);

        try {
            setAndroidContacts(ContactsLoaderService.getInstance().getContacts());
            contactsList = rootView.findViewById(R.id.contactsListView);
            contactsList.setAdapter(customAdapter);
        }catch (NullPointerException ex){
            Log.e("EXCEPTIOOONNN", ex.getMessage());
        }
        // Inflate the layout for this fragment
        return rootView;
    }


    public class MyListViewAdapter extends BaseAdapter {

        // number of rows
        @Override
        public int getCount() {
            return androidContacts.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
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
            name_TextView.setText(androidContacts.get(position).getName());
            number_TextView.setText(androidContacts.get(position).getNumber());
            //     contact_image.setImageResource(androidContacts[position].getImageIndex());

            return convertView;
        }
    }
}
