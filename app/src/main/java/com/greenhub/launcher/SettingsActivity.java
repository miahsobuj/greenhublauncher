package com.greenhub.launcher;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_container, new SettingsFragment())
                .commit();

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Set default preferences
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat 
            implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);

            // Set up license preference
            findPreference("license").setOnPreferenceClickListener(preference -> {
                showLicenseDialog();
                return true;
            });

            // Set up version preference
            findPreference("version").setSummary(getAppVersion());

            // Set up privacy policy
            findPreference("privacy").setOnPreferenceClickListener(preference -> {
                openPrivacyPolicy();
                return true;
            });

            // Set up permissions
            findPreference("permissions").setOnPreferenceClickListener(preference -> {
                openAppSettings();
                return true;
            });

            // Set up rate app
            findPreference("rate_app").setOnPreferenceClickListener(preference -> {
                rateApp();
                return true;
            });
        }

        private void showLicenseDialog() {
            new AlertDialog.Builder(requireContext())
                    .setTitle("License")
                    .setMessage("GreenHub Launcher is an open-source project.\n\n" +
                            "You are free to:\n" +
                            "• Use the app for personal use\n" +
                            "• Modify and distribute\n" +
                            "• Share with others\n\n" +
                            "Built with ❤️ using Android SDK and Material Design 3")
                    .setPositiveButton("OK", null)
                    .show();
        }

        private String getAppVersion() {
            try {
                return requireContext().getPackageManager()
                        .getPackageInfo(requireContext().getPackageName(), 0).versionName;
            } catch (Exception e) {
                return "1.0.0";
            }
        }

        private void openPrivacyPolicy() {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://greenhub.app/privacy"));
            startActivity(intent);
        }

        private void openAppSettings() {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + requireContext().getPackageName()));
            startActivity(intent);
        }

        private void rateApp() {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=" + requireContext().getPackageName()));
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(requireContext(), "Play Store not available", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            // Handle preference changes
            if (key.equals("grid_columns")) {
                Toast.makeText(requireContext(), "Grid size changed. Restart launcher to apply.", 
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}