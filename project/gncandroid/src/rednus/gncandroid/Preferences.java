/** GnuCash for Android.
 *
 * Copyright (C) 2010 Rednus Limited http://www.rednus.co.uk
 * Copyright (C) 2010,2011 John Gray
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package rednus.gncandroid;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.util.Log;

/**
 * This class shows preferences and checks if all necessary information is set
 * when back button is pressed.
 * 
 * @author shyam.avvari
 * 
 */
public class Preferences extends PreferenceActivity {
	private static final String TAG = "Preferences";
	private static final int RESULT_FOR_FILE = 7;
	private GNCAndroid app;
	private String pref_data_file_key;

	/**
	 * On create shows preferences from XML. Sets listener to file chooser as
	 * self and starts file chooser activity when clicked. Also shows, about,
	 * help and version dialogs.
	 * 
	 * @see android.preference.PreferenceActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (GNCAndroid) getApplication();
		Log.i(TAG, "Showing Preferences screen..");
		// set activity title
		setTitle(getString(R.string.app_name) + " > "
				+ app.res.getString(R.string.menu_prefs));
		// set preferences file name
		getPreferenceManager().setSharedPreferencesName(GNCAndroid.SPN);
		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);
		// get custom field to capture on click
		pref_data_file_key = app.res.getString(R.string.pref_data_file_key);
		Preference dataFilePref = findPreference(pref_data_file_key);
		dataFilePref
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference pref) {
						if (pref.getKey().equalsIgnoreCase(pref_data_file_key)) {
							Log.i(TAG, "Clicked custom field.. show file chooser");
							Intent intent = new Intent(
									"rednus.GNCAndroid.action.FILECHOOSER");
							startActivityForResult(intent, RESULT_FOR_FILE);
							return true;
						}
						return false;
					}
				});
		// set version dialog
		String pref_version_key = app.res.getString(R.string.pref_version_key);
		Preference versionPref = findPreference(pref_version_key);
		versionPref
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference pref) {
						new AlertDialog.Builder(Preferences.this)
								// .setIcon(R.drawable.alert_dialog_icon)
								.setTitle(R.string.pref_app_version)
								.setMessage(R.string.app_version_history)
								.setPositiveButton(R.string.alert_dialog_ok,
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int whichButton) {
											}
										}).create().show();
						return true;
					}
				});
		// set version dialog
		String pref_about_key = app.res.getString(R.string.pref_about_key);
		Preference aboutPref = findPreference(pref_about_key);
		aboutPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference pref) {
				new AlertDialog.Builder(Preferences.this)
				// .setIcon(R.drawable.alert_dialog_icon)
						.setTitle(R.string.pref_app_about).setMessage(
								R.string.app_about_text).setPositiveButton(
								R.string.alert_dialog_ok,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
									}
								}).create().show();
				return true;
			}
		});
		// If the filename is already set, make it the summary. Else leave a helpful message.
		dataFilePref.setSummary(dataFilePref.getSharedPreferences().getString(app.res.getString(R.string.pref_data_file_key), "No data file selected."));
		Log.i(TAG, "Showing Preferences screen..Done");
	}

	/**
	 * Gets return from FileChooser activity for selected file name.
	 * 
	 * @see android.preference.PreferenceActivity#onActivityResult(int, int,
	 * android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == RESULT_FOR_FILE && resultCode == RESULT_OK) {
			String path = (String) data.getExtras().get(pref_data_file_key);
			Log.i(TAG, "Got path " + path + " from file chooser..");
			// set file name to summary
			findPreference(pref_data_file_key).setSummary(path);
			// set preference value
			findPreference(pref_data_file_key).getEditor().putString(
					pref_data_file_key, path).commit();
		} else
			super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * This method is triggered when back button is pressed. So, if the user
	 * does not set all required parameters and tries to return to application,
	 * error message should be displayed and navigation should be cancelled.
	 * 
	 * @see android.app.Activity#onBackPressed()
	 */
	@Override
	public void onBackPressed() {
		// check if required values are set
		if (!app.canReadData()) {
			Log.i(TAG, "Error in preferences - showing error message");
			new AlertDialog.Builder(Preferences.this).setTitle(
					R.string.error_prefs_not_set).setMessage(
					R.string.error_message_prefs_not_set).setPositiveButton(
					R.string.alert_dialog_ok,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
						}
					}).create().show();
			return;
		}
		super.onBackPressed();
		// here finish the activity to free memory
		Log.i(TAG, "Activity Finished");
		finish();
	}
}
