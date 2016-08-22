package space.hamsters.amoji;


import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class MainActivity extends AppCompatPreferenceActivity {
    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent launchIntent = getIntent();
        if (launchIntent.getStringArrayExtra(EXTRA_SHOW_FRAGMENT) == null)
            launchIntent.putExtra(EXTRA_SHOW_FRAGMENT, GeneralPreferenceFragment.class.getName());

        super.onCreate(savedInstanceState);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName);
    }

    /**
     * For Xposed use
     */
    @Override
    public SharedPreferences getSharedPreferences(String name, int mode) {
        return super.getSharedPreferences(name, MODE_WORLD_READABLE);
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);

            findPreference("restart_qq").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    restartQQ();
                    return true;
                }
            });

            findPreference("about").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    showAboutDialog();
                    return true;
                }
            });

            findPreference("about").setSummary(String.format(Locale.getDefault(),
                    getString(R.string.pref_desc_about), BuildConfig.VERSION_NAME));
        }


        private void restartQQ() {
            final ProgressDialog dialog = new ProgressDialog(getActivity());
            dialog.setIndeterminate(true);
            dialog.setMessage(getResources().getString(R.string.restarting_qq));

            RootCommandAsyncTask.CommandCallback callback = new RootCommandAsyncTask.CommandCallback() {
                @Override
                public void onSuccess() {
                    dialog.dismiss();

                    Intent intent = getActivity().getPackageManager().getLaunchIntentForPackage("com.tencent.mobileqq");
                    startActivity(intent);
                }

                @Override
                public void onFailure(int returnCode) {
                    dialog.dismiss();
                    Toast.makeText(getActivity(), R.string.restart_command_failed, Toast.LENGTH_SHORT).show();
                }
            };

            RootCommandAsyncTask task = new RootCommandAsyncTask(callback);
            task.execute("am force-stop com.tencent.mobileqq");
            dialog.show();
        }

        private void showAboutDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setView(LayoutInflater.from(getActivity()).inflate(R.layout.about_me_dialog, null));
            builder.setTitle(R.string.app_name);
            builder.setPositiveButton(android.R.string.ok, null);
            builder.show();
        }
    }

}
