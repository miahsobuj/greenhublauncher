package com.greenhub.launcher;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.greenhub.launcher.adapters.ContactsAdapter;
import com.greenhub.launcher.models.Contact;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ContactsActivity extends AppCompatActivity implements ContactsAdapter.OnContactClickListener {

    private RecyclerView recyclerView;
    private ContactsAdapter contactsAdapter;
    private List<Contact> contactList = new ArrayList<>();
    private View emptyView;
    
    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        
        initViews();
        checkPermission();
    }
    
    private void initViews() {
        recyclerView = findViewById(R.id.recycler_contacts);
        emptyView = findViewById(R.id.empty_view);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        FloatingActionButton fab = findViewById(R.id.fab_add_contact);
        fab.setOnClickListener(v -> addNewContact());
        
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }
    
    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    PERMISSION_REQUEST_CODE);
        } else {
            loadContacts();
        }
    }
    
    private void loadContacts() {
        new Thread(() -> {
            contactList.clear();
            ContentResolver contentResolver = getContentResolver();
            
            Cursor cursor = contentResolver.query(
                    ContactsContract.Contacts.CONTENT_URI,
                    null, null, null,
                    ContactsContract.Contacts.DISPLAY_NAME + " ASC"
            );
            
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                    String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    
                    // Get phone number
                    String phone = "";
                    if (cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                        Cursor phoneCursor = contentResolver.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                new String[]{id},
                                null
                        );
                        if (phoneCursor != null) {
                            if (phoneCursor.moveToFirst()) {
                                phone = phoneCursor.getString(phoneCursor.getColumnIndex(
                                        ContactsContract.CommonDataKinds.Phone.NUMBER));
                            }
                            phoneCursor.close();
                        }
                    }
                    
                    contactList.add(new Contact(id, name, phone));
                }
                cursor.close();
            }
            
            runOnUiThread(() -> {
                contactsAdapter = new ContactsAdapter(this, contactList, this);
                recyclerView.setAdapter(contactsAdapter);
                updateEmptyView();
            });
        }).start();
    }
    
    private void updateEmptyView() {
        if (contactList.isEmpty()) {
            emptyView.setVisibility(android.view.View.VISIBLE);
            recyclerView.setVisibility(android.view.View.GONE);
        } else {
            emptyView.setVisibility(android.view.View.GONE);
            recyclerView.setVisibility(android.view.View.VISIBLE);
        }
    }
    
    private void addNewContact() {
        // Open system contacts app to add new contact
        Intent intent = new Intent(android.provider.ContactsContract.Intents.Insert.ACTION);
        intent.setType(android.provider.ContactsContract.RawContacts.CONTENT_TYPE);
        startActivity(intent);
    }
    
    @Override
    public void onContactClick(Contact contact) {
        if (!contact.getPhone().isEmpty()) {
            Intent callIntent = new Intent(android.content.Intent.ACTION_DIAL);
            callIntent.setData(android.net.Uri.parse("tel:" + contact.getPhone()));
            startActivity(callIntent);
        } else {
            Toast.makeText(this, "No phone number available", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public void onCallClick(Contact contact) {
        Intent intent = new Intent(this, PhoneActivity.class);
        intent.putExtra("phone_number", contact.getPhone());
        startActivity(intent);
    }
    
    @Override
    public void onMessageClick(Contact contact) {
        Intent smsIntent = new Intent(android.content.Intent.ACTION_VIEW);
        smsIntent.setData(android.net.Uri.parse("sms:" + contact.getPhone()));
        startActivity(smsIntent);
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadContacts();
            } else {
                Toast.makeText(this, "Contacts permission required", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

// Model class
package com.greenhub.launcher.models;

public class Contact {
    private String id;
    private String name;
    private String phone;
    
    public Contact(String id, String name, String phone) {
        this.id = id;
        this.name = name;
        this.phone = phone;
    }
    
    public String getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
}