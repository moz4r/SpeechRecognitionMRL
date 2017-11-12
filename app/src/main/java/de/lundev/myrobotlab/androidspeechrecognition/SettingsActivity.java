package de.lundev.myrobotlab.androidspeechrecognition;

import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;


/**
 *
 * @author Marvin
 */
public class SettingsActivity extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.activity_settings);
        //TODO - don't use deprecated method
        //TODO - don't use deprecated method
        final Context ctx = this;
    }
}
