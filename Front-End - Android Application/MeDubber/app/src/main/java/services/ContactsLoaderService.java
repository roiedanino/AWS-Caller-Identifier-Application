package services;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.ArrayList;

import entities.AndroidContact;

public class ContactsLoaderService {
    private static ContactsLoaderService ourInstance;
    private ArrayList<AndroidContact> contacts = new ArrayList<>();

    private ContactsLoaderService() {

    }

    public static ContactsLoaderService getInstance() {
        if(ourInstance == null) {
            ourInstance = new ContactsLoaderService();
        }

        return ourInstance;
    }

    public void loadContacts(final Activity activity){
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED) {

           fillContactsFromPhone(activity);

        }else {

        }

    }


    public void fillContactsFromPhone(Activity activity) {
        Cursor contactsCursor = null;
        ContentResolver contentResolver = activity.getContentResolver();

        try {
            contactsCursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        } catch (Exception ex) {
            Log.e("Error on contact", ex.getMessage());
        }

        Log.i("SUCCESS","after contactsCursorInit!!!");


        if (contactsCursor.getCount() > 0) {

            while (contactsCursor.moveToNext()) {
                AndroidContact androidAndroidContact = new AndroidContact("", "", 0);
                String contactId = contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.Contacts._ID));
                String contactName = contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                androidAndroidContact.setName(contactName);

               // Log.i("SUCCESS","CONTACT NAME: " + contactName);
                int hasPhoneNumber = Integer.parseInt(contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)));
                if (hasPhoneNumber > 0) {

                  //  Log.i("SUCCESS","HAS PHONE NUMBER: " + hasPhoneNumber);
                    Cursor phoneCursor = contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI
                            , null
                            , ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?"
                            , new String[]{contactId}
                            , null);

                    while (phoneCursor.moveToNext()) {
                        String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        androidAndroidContact.setNumber(phoneNumber);

                        //Log.i("SUCCESS","PHONE NUMBER: " + phoneNumber);
                    }
                    phoneCursor.close();
                }

                contacts.add(androidAndroidContact);
            }
        }
    }

    public ArrayList<AndroidContact> getContacts() {
        return contacts;
    }


}
