package ca.idi.tekla;

import ca.idi.tekla.R;
import android.preference.PreferenceActivity;
import android.os.Bundle;

public class TeklaIMESettings extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.preferences);
        //mQuickFixes = (CheckBoxPreference) findPreference(QUICK_FIXES_KEY);
        //mShowSuggestions = (CheckBoxPreference) findPreference(SHOW_SUGGESTIONS_KEY);
    }

}
