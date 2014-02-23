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
package io.github.jrmcsoftware.gnucashviewer;

import java.io.File;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.util.Log;

/**
 * This is the main application class. Every class in package should have a
 * reference to this. Keeps a copy of resources. Reads preferences. Defines data
 * handler. Implements preference change listener to flag if the data file has
 * to be read again or not.
 * 
 * @author shyam.avvari
 * 
 */
public class GnuCashViewer extends Application implements
		OnSharedPreferenceChangeListener {
	// TAG for this activity
	private static final String TAG = "GnuCashViewer";
	public static final String SPN = "gnc4aprefs";
	public static final String TRANS_ACT_ACCOUNT_PARAM = "Account.GUID";
	public Resources res;
	private GNCDataHandler gncDataHandler;
	private SharedPreferences sp;
	private boolean reloadFile = true;

	/**
	 * This method checks preferences and confirms if all information is
	 * available to read data file.
	 */
	public boolean canReadData() {
	    String filePath = sp.getString(res.getString(R.string.pref_data_file_key), "");
	    File file = new File(filePath);
	    return file.exists();
	}

	/**
	 * Returns if the reload file flag is set or not
	 */
	public boolean isReloadFile() {
		return reloadFile;
	}

	/**
	 * Method is triggered when application starts. Gets a copy of resources
	 * object and reads user preferences.
	 * 
	 * @see android.app.Application#onCreate()
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		// keep a copy of resources for later use in application
		res = getResources();
		sp = getSharedPreferences(SPN, MODE_PRIVATE);
		// set listener to this
		sp.registerOnSharedPreferenceChangeListener(this);
	}

	/**
	 * This method listens for changes to preferences. If any of the value is
	 * changed, let application know that the data file should be read
	 * again.
	 * 
	 * @see android.content.SharedPreferences.OnSharedPreferenceChangeListener#
	 *      onSharedPreferenceChanged(android.content.SharedPreferences,
	 *      java.lang.String)
	 */
	public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
		Log.i(TAG, "Pref " + key + " changed... reading new value...");
		// Set to reload file
		reloadFile = true;
	}

	/**
	 * Creates new gncDataHandler with values from preferences.
	 *
	 * @return False if something went wrong, in which case the old GNCDataHandler
	 *         has been destroyed.
	 */
	public boolean readData() {
		if (sp.getString(res.getString(R.string.pref_data_file_key), null) == null)
			return false;
		Log.i(TAG, "Reading Data from " + sp.getString(res.getString(R.string.pref_data_file_key), null) + "...");
		// Finalise the previous data handler if we had one.
		if (gncDataHandler != null)
			gncDataHandler.close();
		try {
			gncDataHandler = new GNCDataHandler(this);
			reloadFile = false;
		}
		catch (Exception e) {
			// GNCDataHandler can fail to initialise if the data
			// file doesn't exist or is in some way invalid.
			// Log a basic error, clear any old GNDDataHandler
			// and report failure.
			Log.v(TAG, "Exception caught", e);
			gncDataHandler = null;
			return false;
		}
		return true;
	}

	public GNCDataHandler getGncDataHandler() {
		return gncDataHandler;
	}

	public void setGncDataHandler(GNCDataHandler gncDataHandler) {
		this.gncDataHandler = gncDataHandler;
	}
}
