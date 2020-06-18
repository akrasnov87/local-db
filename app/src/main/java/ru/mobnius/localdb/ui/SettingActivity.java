package ru.mobnius.localdb.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.IOException;
import java.text.DecimalFormat;
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
        private int clickToVersion = 0;

        private Preference pVersion;
        private Preference pServerVersion;
        private Preference pLogin;
        private SwitchPreference spDebug;
        private Preference pLoginReset;
        private Preference pNodeUrl;
        private Preference pRpcUrl;
        private ListPreference lpSize;

        @Override
        public void onCreatePreferences(Bundle bundle, String s) {
            addPreferencesFromResource(R.xml.pref);

            pServerVersion = findPreference(PreferencesManager.SERVER_APP_VERSION);
            Objects.requireNonNull(pServerVersion).setOnPreferenceClickListener(this);

            pVersion = findPreference(PreferencesManager.APP_VERSION);
            Objects.requireNonNull(pVersion).setOnPreferenceClickListener(this);

            pLogin = findPreference(PreferencesManager.LOGIN);
            pNodeUrl = findPreference(PreferencesManager.NODE_URL);
            pRpcUrl = findPreference(PreferencesManager.RPC_URL);

            spDebug = findPreference(PreferencesManager.DEBUG);
            Objects.requireNonNull(spDebug).setEnabled(PreferencesManager.getInstance().isDebug());
            spDebug.setOnPreferenceChangeListener(this);

            pLoginReset = findPreference(PreferencesManager.LOGIN_RESET);
            Objects.requireNonNull(pLoginReset).setOnPreferenceClickListener(this);
            pLoginReset.setVisible(PreferencesManager.getInstance().isDebug());

            lpSize = findPreference(PreferencesManager.SIZE);
            Objects.requireNonNull(lpSize).setOnPreferenceChangeListener(this);
            lpSize.setEnabled(PreferencesManager.getInstance().isDebug());
        }

        @Override
        public void onResume() {
            super.onResume();

            pVersion.setSummary("Установлена последняя версия " + VersionUtil.getVersionName(requireActivity()));

            spDebug.setSummary(String.format(debugSummary, PreferencesManager.getInstance().isDebug() ? "включен" : "отключен"));
            spDebug.setChecked(PreferencesManager.getInstance().isDebug());

            String loginSummary = "Логин для авторизации на сервере: %s";
            pLogin.setSummary(String.format(loginSummary, PreferencesManager.getInstance().getLogin()));

            pNodeUrl.setSummary(PreferencesManager.getInstance().getNodeUrl());
            pRpcUrl.setSummary(PreferencesManager.getInstance().getRpcUrl());
            DecimalFormat df = new DecimalFormat(Names.INT_FORMAT);
            lpSize.setSummary(df.format(PreferencesManager.getInstance().getSize()));

            new ServerAppVersionAsyncTask().execute();
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            if (PreferencesManager.APP_VERSION.equals(preference.getKey())) {
                clickToVersion++;
                if (clickToVersion >= 6) {
                    PreferencesManager.getInstance().setDebug(true);
                    spDebug.setChecked(true);
                    spDebug.setEnabled(true);
                    pLoginReset.setVisible(true);
                    lpSize.setEnabled(true);

                    Toast.makeText(getActivity(), "Режим отладки активирован.", Toast.LENGTH_SHORT).show();
                    clickToVersion = 0;
                }
            }

            if(PreferencesManager.LOGIN_RESET.equals(preference.getKey())) {
                confirm("После сброса вход в приложение будет заблокирован. Сбросить локальную авторизацию?", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which == DialogInterface.BUTTON_POSITIVE) {
                            PreferencesManager.getInstance().setLogin(null);
                            PreferencesManager.getInstance().setPassword(null);

                            requireContext().startActivity(AuthActivity.getIntent(requireContext()));
                        }
                    }
                });
            }
            return false;
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (PreferencesManager.DEBUG.equals(preference.getKey())) {
                boolean debugValue = Boolean.parseBoolean(String.valueOf(newValue));
                spDebug.setSummary(String.format(debugSummary, debugValue ? "включен" : "отключен"));
                spDebug.setEnabled(debugValue);
                lpSize.setEnabled(debugValue);

                PreferencesManager.getInstance().setDebug(debugValue);
                pLoginReset.setVisible(debugValue);
            }

            if(PreferencesManager.SIZE.equals(preference.getKey())) {
                DecimalFormat df = new DecimalFormat(Names.INT_FORMAT);
                lpSize.setSummary(df.format(Integer.parseInt(String.valueOf(newValue))));

                PreferencesManager.getInstance().setSize(Integer.parseInt(String.valueOf(newValue)));
            }
            return true;
        }

        protected void confirm(String message, DialogInterface.OnClickListener listener) {
            AlertDialog dialog = new AlertDialog.Builder(requireContext()).create();
            dialog.setTitle("Сообщение");
            dialog.setMessage(message);
            dialog.setCancelable(false);
            dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.yes), listener);
            dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.no), listener);
            dialog.setIcon(R.drawable.ic_baseline_warning_24);
            dialog.show();
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