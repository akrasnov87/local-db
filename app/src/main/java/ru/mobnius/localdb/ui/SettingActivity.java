package ru.mobnius.localdb.ui;

import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import java.text.DecimalFormat;
import java.util.Objects;

import ru.mobnius.localdb.Names;
import ru.mobnius.localdb.R;
import ru.mobnius.localdb.data.PreferencesManager;
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
            Preference.OnPreferenceClickListener{

        private final String debugSummary = "Режим отладки: %s";
        private int clickToVersion = 0;

        private Preference pVersion;
        private Preference pServerVersion;
        private Preference pLogin;
        private SwitchPreference spDebug;
        private Preference pSQLite;
        private Preference pClearDB;
        private Preference pLoginReset;
        private Preference pNodeUrl;
        private Preference pRpcUrl;
        private ListPreference lpSize;
        private Preference pCreateError;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public void onCreatePreferences(Bundle bundle, String s) {
            addPreferencesFromResource(R.xml.pref);

            pSQLite = findPreference(PreferencesManager.SQL);
            Objects.requireNonNull(pSQLite).setVisible(PreferencesManager.getInstance().isDebug());
            Objects.requireNonNull(pSQLite).setOnPreferenceClickListener(this);

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

            pCreateError = findPreference(PreferencesManager.GENERATED_ERROR);
            Objects.requireNonNull(pCreateError).setVisible(PreferencesManager.getInstance().isDebug());
            pCreateError.setOnPreferenceClickListener(this);

            pClearDB = findPreference(PreferencesManager.CLEAR);
            Objects.requireNonNull(pClearDB).setVisible(PreferencesManager.getInstance().isDebug());
            pClearDB.setOnPreferenceClickListener(this);

        }

        @Override
        public void onResume() {
            super.onResume();

            pVersion.setSummary("Установлена версия " + VersionUtil.getVersionName(requireActivity()));

            spDebug.setSummary(String.format(debugSummary, PreferencesManager.getInstance().isDebug() ? "включен" : "отключен"));
            spDebug.setChecked(PreferencesManager.getInstance().isDebug());

            String loginSummary = "Логин для авторизации на сервере: %s";
            pLogin.setSummary(String.format(loginSummary, PreferencesManager.getInstance().getLogin()));

            pNodeUrl.setSummary(PreferencesManager.getInstance().getNodeUrl());
            pRpcUrl.setSummary(PreferencesManager.getInstance().getRpcUrl());
            DecimalFormat df = new DecimalFormat(Names.INT_FORMAT);
            lpSize.setSummary(df.format(PreferencesManager.getInstance().getSize()));
            lpSize.setValue(String.valueOf(PreferencesManager.getInstance().getSize()));
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            switch (preference.getKey()) {
                case PreferencesManager.APP_VERSION:
                    clickToVersion++;
                    if (clickToVersion >= 6) {
                        PreferencesManager.getInstance().getSharedPreferences().edit().putBoolean(PreferencesManager.DEBUG, true).apply();
                        spDebug.setChecked(true);
                        spDebug.setEnabled(true);
                        pSQLite.setVisible(true);
                        pLoginReset.setVisible(true);
                        lpSize.setEnabled(true);
                        pCreateError.setVisible(true);
                        pClearDB.setVisible(true);
                        spDebug.setSummary(String.format(debugSummary, "включен"));
                        Toast.makeText(getActivity(), "Режим отладки активирован.", Toast.LENGTH_SHORT).show();
                        clickToVersion = 0;
                    }
                    break;

                case PreferencesManager.SQL:
                    Intent i = new Intent(getContext(), SQLViewActivity.class);
                    startActivity(i);
                    break;

                case PreferencesManager.GENERATED_ERROR:
                    //noinspection ResultOfMethodCallIgnored
                    Integer.parseInt("Проверка обработки ошибок");
                    break;

                case PreferencesManager.LOGIN_RESET:
                    confirm("После сброса вход в приложение будет заблокирован. Сбросить локальную авторизацию?", (dialog, which) -> {
                        if (which == DialogInterface.BUTTON_POSITIVE) {
                            PreferencesManager.getInstance().setLogin(null);
                            PreferencesManager.getInstance().setPassword(null);

                            requireContext().startActivity(AuthActivity.getIntent(requireContext()));
                        }
                    });
                    break;
                case PreferencesManager.CLEAR:
                    confirm("Все локальные данные будут удалены, авторизация сброшена. Вы уверены?", (dialog, which) -> {
                        ActivityManager am = (ActivityManager) requireActivity().getSystemService(Context.ACTIVITY_SERVICE);
                        String myProcessPrefix = requireActivity().getApplicationInfo().processName;
                        String myProcessName = null;
                        try {
                            myProcessName = requireActivity().getPackageManager().getActivityInfo(requireActivity().getComponentName(), 0).processName;
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }
                        for (ActivityManager.RunningAppProcessInfo process : am.getRunningAppProcesses()) {
                            if (process.processName.startsWith(myProcessPrefix) && !process.processName.equals(myProcessName)) {
                                android.os.Process.killProcess(process.pid);
                            }
                        }
                        boolean success = am.clearApplicationUserData();
                        Toast.makeText(getActivity(), success ? "База данных успешно удалена" : "Не удалось удалить БД", Toast.LENGTH_SHORT).show();
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
                pSQLite.setVisible(debugValue);
                lpSize.setEnabled(debugValue);
                pCreateError.setVisible(debugValue);
                PreferencesManager.getInstance().setDebug(debugValue);
                pLoginReset.setVisible(debugValue);
            }

            if (PreferencesManager.SIZE.equals(preference.getKey())) {
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

        @Override
        public void onDestroy() {
            super.onDestroy();
        }
    }
}