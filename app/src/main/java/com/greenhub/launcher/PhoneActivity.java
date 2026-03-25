package com.greenhub.launcher;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

public class PhoneActivity extends AppCompatActivity {
    
    private TextView numberDisplay;
    private TextView nameDisplay;
    private StringBuilder currentNumber = new StringBuilder();
    private LinearLayout recentsContainer;
    private boolean isCalling = false;
    
    private static final int PERMISSION_CODE = 100;
    private static final String[] PERMISSIONS = {
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_PHONE_STATE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone);
        
        numberDisplay = findViewById(R.id.number_display);
        nameDisplay = findViewById(R.id.name_display);
        recentsContainer = findViewById(R.id.recents_container);
        
        setupKeypad();
        setupCallButtons();
        setupTabs();
        checkPermissions();
        
        // If called with a phone number from Contacts
        String phoneNumber = getIntent().getStringExtra("phone_number");
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            currentNumber.append(phoneNumber.replaceAll("[^0-9#*+]", ""));
            updateDisplay();
            lookupContact(phoneNumber);
        }
    }
    
    private void setupKeypad() {
        int[] buttonIds = new int[] {
            R.id.btn_0, R.id.btn_1, R.id.btn_2, R.id.btn_3, R.id.btn_4,
            R.id.btn_5, R.id.btn_6, R.id.btn_7, R.id.btn_8, R.id.btn_9,
            R.id.btn_star, R.id.btn_hash
        };
        
        for (int id : buttonIds) {
            findViewById(id).setOnClickListener(this::onDigitClicked);
        }
        
        // Long press on 0 for +
        findViewById(R.id.btn_0).setOnLongClickListener(v -> {
            currentNumber.append("+");
            updateDisplay();
            updateNameDisplay();
            return true;
        });
        
        findViewById(R.id.btn_delete).setOnClickListener(v -> {
            if (currentNumber.length() > 0) {
                currentNumber.deleteCharAt(currentNumber.length() - 1);
                updateDisplay();
                updateNameDisplay();
            }
        });
        
        findViewById(R.id.btn_delete).setOnLongClickListener(v -> {
            currentNumber.setLength(0);
            updateDisplay();
            nameDisplay.setText("");
            return true;
        });
        
        // Quick actions
        findViewById(R.id.btn_video_call).setOnClickListener(v -> {
            if (currentNumber.length() > 0) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("tel:" + currentNumber.toString()));
                intent.putExtra("android.intent.extra.videoCall", true);
                startActivity(intent);
            }
        });
        
        findViewById(R.id.btn_add_to_contacts).setOnClickListener(v -> {
            if (currentNumber.length() > 0) {
                Intent intent = new Intent(android.provider.ContactsContract.Intents.Insert.ACTION);
                intent.setType(android.provider.ContactsContract.RawContacts.CONTENT_TYPE);
                intent.putExtra(android.provider.ContactsContract.Intents.Insert.PHONE, currentNumber.toString());
                startActivity(intent);
            }
        });
    }
    
    private void onDigitClicked(View view) {
        Button button = (Button) view;
        String digit = button.getText().toString();
        currentNumber.append(digit);
        updateDisplay();
        updateNameDisplay();
        
        // Play dial tone effect (optional)
        playTone(digit);
    }
    
    private void playTone(String digit) {
        // Using system tone generator
        int tone = android.media.ToneGenerator.TONE_DTMF_0;
        try {
            int val = Integer.parseInt(digit);
            tone = android.media.ToneGenerator.TONE_DTMF_0 + val;
        } catch (Exception e) {
            if (digit.equals("*")) tone = android.media.ToneGenerator.TONE_DTMF_S;
            else if (digit.equals("#")) tone = android.media.ToneGenerator.TONE_DTMF_P;
        }
        
        android.media.ToneGenerator toneGenerator = 
                new android.media.ToneGenerator(android.media.AudioManager.STREAM_DTMF, 80);
        toneGenerator.startTone(tone, 150);
    }
    
    private void updateDisplay() {
        // Format phone number
        String formatted = formatPhoneNumber(currentNumber.toString());
        numberDisplay.setText(formatted.isEmpty() ? "Dial a number" : formatted);
    }
    
    private String formatPhoneNumber(String number) {
        if (number == null || number.isEmpty()) return "";
        
        // Simple formatting: (XXX) XXX-XXXX
        if (number.length() <= 3) return number;
        if (number.length() <= 6) {
            return "(" + number.substring(0, 3) + ") " + number.substring(3);
        }
        if (number.length() <= 10 && !number.startsWith("+")) {
            return "(" + number.substring(0, 3) + ") " + number.substring(3, 6) + "-" + number.substring(6);
        }
        return number;
    }
    
    private void updateNameDisplay() {
        // Look up contact name
        String number = currentNumber.toString().replaceAll("[^0-9+*#]", "");
        if (number.isEmpty()) {
            nameDisplay.setText("");
            return;
        }
        
        lookupContact(number);
    }
    
    private void lookupContact(String number) {
        // Simple contact lookup (would use ContentResolver in full implementation)
        new Thread(() -> {
            String name = ContactHelper.getContactNameByNumber(this, number);
            runOnUiThread(() -> {
                nameDisplay.setText(name != null ? name : "");
            });
        }).start();
    }
    
    private void setupCallButtons() {
        findViewById(R.id.btn_call).setOnClickListener(v -> makeCall());
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }
    
    private void setupTabs() {
        findViewById(R.id.tab_dialpad).setOnClickListener(v -> showDialpad());
        findViewById(R.id.tab_recents).setOnClickListener(v -> showRecents());
        findViewById(R.id.tab_contacts).setOnClickListener(v -> {
            Intent intent = new Intent(this, ContactsActivity.class);
            startActivity(intent);
        });
    }
    
    private void showDialpad() {
        findViewById(R.id.dialpad_container).setVisibility(View.VISIBLE);
        recentsContainer.setVisibility(View.GONE);
        
        findViewById(R.id.tab_dialpad).setSelected(true);
        findViewById(R.id.tab_recents).setSelected(false);
        findViewById(R.id.tab_contacts).setSelected(false);
    }
    
    private void showRecents() {
        findViewById(R.id.dialpad_container).setVisibility(View.GONE);
        recentsContainer.setVisibility(View.VISIBLE);
        
        findViewById(R.id.tab_dialpad).setSelected(false);
        findViewById(R.id.tab_recents).setSelected(true);
        findViewById(R.id.tab_contacts).setSelected(false);
        
        loadRecents();
    }
    
    private void loadRecents() {
        // Load recent calls
        new Thread(() -> {
            // In a full implementation, this would query the CallLog ContentProvider
            // For now, showing placeholder
            runOnUiThread(() -> {
                // Display placeholder recents
            });
        }).start();
    }
    
    private void makeCall() {
        String number = currentNumber.toString().replaceAll("[^0-9+*#]", "");
        if (number.isEmpty()) {
            Toast.makeText(this, "Enter number to call", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE}, PERMISSION_CODE);
        } else {
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + number));
            try {
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Cannot make call", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void checkPermissions() {
        for (String permission : PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) 
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_CODE);
                break;
            }
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(Manifest.permission.CALL_PHONE)) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "Phone permission granted", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }
}