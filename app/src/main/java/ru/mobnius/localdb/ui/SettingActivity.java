package ru.mobnius.localdb.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.IOException;
import java.util.Objects;

import ru.mobnius.localdb.Names;
import ru.mobnius.localdb.R;
import ru.mobnius.localdb.data.PreferencesManager;
import ru.mobnius.localdb.utils.Loader;
import ru.mobnius.localdb.utils.VersionUtil;

public class SettingActivity extends AppCompatActivity {

    public static Intent getIntent(Context context) {
        return new Intent(context, SettingActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simple_container);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction()
                .add(R.id.single_fragment_container, new PrefFragment())
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class PrefFragment extends PreferenceFragmentCompat implements
            Preference.OnPreferenceChangeListener,
            Preference.OnPreferenceClickListener {

        private final String debugSummary = "Режим отладки: %s";
        private final String volumeSummary = "Уровень громкости по умолчанию: %s";
        private final String nightSummary = "Минимальный уровень света: %s";
        private int clickToVersion = 0;

        private Preference pVersion;
        private Preference pServerVersion;
        private SwitchPreference spDebug;

        @Override
        public void onCreatePreferences(Bundle bundle, String s) {
            addPreferencesFromResource(R.xml.pref);

            pServerVersion = findPreference(PreferencesManager.SERVER_APP_VERSION);
            Objects.requireNonNull(pServerVersion).setOnPreferenceClickListener(this);

            pVersion = findPreference(PreferencesManager.APP_VERSION);
            Objects.requireNonNull(pVersion).setOnPreferenceClickListener(this);

            spDebug = findPreference(PreferencesManager.DEBUG);
            Objects.requireNonNull(spDebug).setEnabled(PreferencesManager.getInstance().isDebug());
            spDebug.setOnPreferenceChangeListener(this);
        }

        @Override
        public void onResume() {
            super.onResume();

            pVersion.setSummary("Установлена последняя версия " + VersionUtil.getVersionName(requireActivity()));

            spDebug.setSummary(String.format(debugSummary, PreferencesManager.getInstance().isDebug() ? "включен" : "отключен"));
            spDebug.setChecked(PreferencesManager.getInstance().isDebug());

            new ServerAppVersionAsyncTask().execute();
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            if (PreferencesManager.APP_VERSION.equals(preference.getKey())) {
                clickToVersion++;
                if (clickToVersion >= 6) {
                    PreferencesManager.getInstance().getSharedPreferences().edit().putBoolean(PreferencesManager.DEBUG, true).apply();
                    spDebug.setChecked(true);
                    spDebug.setEnabled(true);

                    Toast.makeText(getActivity(), "Режим отладки активирован.", Toast.LENGTH_SHORT).show();
                    clickToVersion = 0;
                }
            }
            return false;
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (PreferencesManager.DEBUG.equals(preference.getKey())) {
                boolean debugValue = Boolean.parseBoolean(String.valueOf(newValue));
                spDebug.setSummary(String.format(debugSummary, debugValue ? "включен" : "отключен"));
                spDebug.setEnabled(debugValue);

                PreferencesManager.getInstance().setDebug(debugValue);
            }
            return true;
        }

        @SuppressLint("StaticFieldLeak")
        private class ServerAppVersionAsyncTask extends AsyncTask<Void, Void, String> {

            @Override
            protected String doInBackground(Void... voids) {
                try {
                    return Loader.getInstance().version();
                } catch (IOException e) {
                    return "0.0.0.0";
                }
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);

                if (pServerVersion != null) {
                    if(!s.equals("0.0.0.0")) {
                        if(VersionUtil.isUpgradeVersion(requireActivity(), s, PreferencesManager.getInstance().isDebug())) {
                            pServerVersion.setVisible(true);
                            pServerVersion.setSummary("Доступна новая версия " + s);
                            pServerVersion.setIntent(new Intent().setAction(Intent.ACTION_VIEW).setData(
                                    Uri.parse(Names.UPDATE_URL)));

                            if (pVersion != null) {
                                pVersion.setSummary(VersionUtil.getVersionName(requireActivity()));
                            }

                            return;
                        }
                    }

                    pServerVersion.setVisible(false);
                    if (pVersion != null) {
                        pVersion.setSummary("Установлена последняя версия " + VersionUtil.getVersionName(requireActivity()));
                    }
                }
            }
        }
    }
}