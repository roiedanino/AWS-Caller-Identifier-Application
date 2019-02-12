package com.example.roie.medubber;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import amazonSQS.AmazonSQSClientService;
import entities.UserImage;

public class UploadPhotoFragment extends Fragment {

    // TODO: Rename and change types of parameters

    public static final int UPLOAD_IMAGE_REQUEST = 1;


    private ImageView profilePicView;
    private ImageButton uploadPhotoButton;
    private Bitmap profileBitMap = null;
    private View rootView;
    private String picturePath;

    private File profilePicFile;

    public UploadPhotoFragment() {
        // Required empty public constructor

    }

    private void initProfilePicture(){
        profilePicFile = new File(getContext().getFilesDir() +    File.separator+"meDubberProfilePic.jpg");

        if(!profilePicFile.exists()) {
            try {
                boolean created = profilePicFile.createNewFile();
                Log.i("CREATED::", created ? "Yes" : "No");
            } catch (IOException e) {
                Log.e("ERROR CREATING FILE::", e.getMessage());
            }
        }else {
            loadProfilePicturePath();
        }

    }

    private void loadProfilePicturePath(){
        try(DataInputStream dataInputStream = new DataInputStream(new FileInputStream(profilePicFile))) {
            picturePath = dataInputStream.readUTF();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveProfilePicturePath(){
        try(DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(profilePicFile))) {
            dataOutputStream.writeUTF(picturePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // TODO: Rename and change types and number of parameters
    public static UploadPhotoFragment newInstance() {
        UploadPhotoFragment fragment = new UploadPhotoFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }

        initProfilePicture();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        rootView = inflater.inflate(R.layout.fragment_upload_photo, container, false);

        profilePicView = rootView.findViewById(R.id.profilePic);

        uploadPhotoButton = rootView.findViewById(R.id.uploadImageButton);

        uploadPhotoButton.setOnClickListener(this::onButtonPressed);

        if(picturePath != null){
            profilePicView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
        }

        return rootView;
    }



    public void onButtonPressed(View view) {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(photoPickerIntent, UPLOAD_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK)
            switch (requestCode){
                case UploadPhotoFragment.UPLOAD_IMAGE_REQUEST:
                    try {
                        Uri selectedImage = data.getData();
                        String[] filePathColumn = { MediaStore.Images.Media.DATA };
                        Cursor cursor = getActivity().getContentResolver().query(selectedImage,
                                filePathColumn, null, null, null);
                        cursor.moveToFirst();
                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        picturePath = cursor.getString(columnIndex);
                        saveProfilePicturePath();
                        profilePicView = rootView.findViewById(R.id.profilePic);
                        Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
                        profilePicView.setImageBitmap(bitmap);

                        File compressed = compressBitmapTo200kb(bitmap, picturePath);

                        if(compressed != null){
                            picturePath = compressed.getAbsolutePath();
                        }

                        UserImage userImage = toUserImage(picturePath);

                        if(compressed != null) {
                            AsyncTask.execute(() -> AmazonSQSClientService.getInstance().uploadImage(userImage));
                        }
                        cursor.close();
                    } catch (NullPointerException e) {
                        Log.i("Exception::", "Some exception " + e);
                    }
                    break;
            }
    }

    private File compressBitmapTo200kb(Bitmap bitmap, String sourcePath){
        final int MAX_IMAGE_SIZE = 200000;
        final int BITMAP_SIZE = bitmap.getByteCount();

        if(BITMAP_SIZE > MAX_IMAGE_SIZE){
            File compressedFile = new File(getContext().getFilesDir() +    File.separator + "CompressedProfilePic.jpg");

            try{

                Log.i("::: Trying to compress:", " Bitmap Original Size: " + BITMAP_SIZE +"bytes, ratio:" + BITMAP_SIZE / MAX_IMAGE_SIZE);

                int quality = BITMAP_SIZE / MAX_IMAGE_SIZE;

                FileOutputStream outputStream = new FileOutputStream(compressedFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);
                outputStream.close();

                FileOutputStream outputStream2 = new FileOutputStream(compressedFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream2);
                outputStream2.close();

                Log.i("Compressed:::","SUCCESSFULLY");
                return compressedFile;
            } catch (IOException e) {
                Log.i("::IO Exception::", "Could not compress " + e);
            }
        }else{
            return new File(sourcePath);
        }
        return null;
    }

    private UserImage toUserImage(String path){
        File file = new File(path);
        byte imageData[] = new byte[(int)file.length()];
        byte result[] = new byte[(int)file.length()];

        DataInputStream fileStream;
        try {
            fileStream = new DataInputStream(new FileInputStream(file));
            fileStream.readFully(imageData);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String myPhoneNumber = AmazonSQSClientService.getMyPhoneNumber(getActivity());
        UserImage imageObject= new UserImage(myPhoneNumber, ".jpg",imageData);

        return imageObject;
    }
}
