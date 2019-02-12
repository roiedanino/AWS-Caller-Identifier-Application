package com.example.roie.medubber;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import amazonSQS.AmazonSQSClientService;
import entities.Contact;


public class FindByPhoneFragment extends Fragment {

//    private OnFragmentInteractionListener mListener;

    private static final String ARG_SECTION_NUMBER = "section_number";


    private EditText phoneTextInput = null;

    private ImageButton findButton = null;

    private TextView resultView = null;

    private ImageView contactProfile = null;

    private AtomicReference<Bitmap> atomicImageRef = new AtomicReference<>(null);

    private static AtomicReference<String> atomicName = new AtomicReference<>("Not Found");

    public FindByPhoneFragment() {
        // Required empty public constructor
    }

    public static FindByPhoneFragment newInstance(int sectionNumber) {
        FindByPhoneFragment fragment = new FindByPhoneFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_find_by_phone, container, false);

        try {

            phoneTextInput = rootView.findViewById(R.id.phoneInput);

            findButton = rootView.findViewById(R.id.findButton);

            resultView = rootView.findViewById(R.id.resultTextView);

            contactProfile = rootView.findViewById(R.id.findByPhoneImage);

            resultView.setText(atomicName.get());

            if(atomicImageRef.get() != null){
                contactProfile.setImageBitmap(atomicImageRef.get());
            }

            findButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onFindButtonPressed(v);
                }
            });
        }catch (NullPointerException ex){
            Log.e("Null Pointer Exception", ex.getMessage());

        }

        return rootView;
    }



    private void onFindButtonPressed(View view){
        Snackbar.make(view, "Button pressed", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();

        ExecutorService es = Executors.newSingleThreadExecutor();

        String phoneToSearch = phoneTextInput.getText().toString();

        Snackbar.make(view, "Sending request...", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
        resultView.setText("Just a sec...");

        Lock lock = new ReentrantLock();
        Condition gotTheName = lock.newCondition();
        Handler uiHandled = new Handler(Looper.getMainLooper());

        es.submit(() -> backgroundThread(lock, gotTheName, phoneToSearch));

        uiHandled.post(() -> uiThread(lock));

    }

    private void backgroundThread(Lock lock, Condition gotTheName, String phoneToSearch){
        lock.lock();
        Contact result = AmazonSQSClientService.getInstance().findByPhoneNumber(phoneToSearch, getActivity());
        atomicName.set(result.getName());

        if(result.hasImage()) {
            try {
                Bitmap bitmap = BitmapFactory.decodeByteArray(result.getImage(), 0,result.getImage().length);
                atomicImageRef.set(bitmap);
            } catch (Exception e) {
                Log.e("::EXCEPTION IN ATOMIC IMAGE", Arrays.toString(e.getStackTrace()));
            }
        }else{
            atomicImageRef.set(null);
        }

        gotTheName.signalAll();
        lock.unlock();
    }

    private void uiThread(Lock lock){
        try {
            Thread.sleep(200);
            lock.lock();
            resultView.setText(atomicName.get());
            if(atomicImageRef.get() != null){
                contactProfile.setImageBitmap(atomicImageRef.get());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
    }

    public static void setAtomicName(String name) {
        FindByPhoneFragment.atomicName.set(name);
    }
}
