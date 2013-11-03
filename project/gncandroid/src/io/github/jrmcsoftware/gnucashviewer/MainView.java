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

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TabHost;

/**
 * @author shyam.avvari
 * 
 */
public class MainView extends TabActivity {
	private static final String TAG = "MainView"; // TAG for this activity
	private GnuCashViewer app; // Application Reference
	private ProgressDialog pd; // progress bar
	private boolean screensCreated = false;

	/**
	 * Start of activity. Check if data file can be read, if not show dialog and
	 * navigate to preferences. Also add sub activities as tabs to self.
	 * 
	 * @see android.app.ActivityGroup#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// get application
		app = (GnuCashViewer) getApplication();
		// first check if data file is set otherwise show preferences
		// Read data if has
		if (app.canReadData()) {
			if (app.isReloadFile()) // The data may already be read
				new ReadDataTask().execute();
			else
				showScreen();
		} else {
			Log.i(TAG, "No Data file set.. Forcing preferences...");
			forcePreferences(app.res.getString(R.string.message_set_data_file));
		}
	}

	private void forcePreferences(String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message).setCancelable(false).setPositiveButton(
				app.res.getString(R.string.button_text_ok),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// show prefs
						Intent i = new Intent(getBaseContext(),
								Preferences.class);
						startActivity(i);
						return;
					}
				});
		builder.create().show();
	}

	/**
	 * This method is called once read data activity is finished so that the sub
	 * activities are initiated.
	 */
	private void showScreen() {
		Log.i(TAG, "Showing main screen...");
		if ( screensCreated )
			return;

		screensCreated = true;

		// The activity TabHost
		final TabHost tabHost = getTabHost();
		// Reusable TabSpec for each tab
		TabHost.TabSpec spec;
		// Reusable Intent for each tab
		Intent intent;
		// add accounts tab
		intent = new Intent().setClass(this, AccountsActivity.class);
		spec = tabHost.newTabSpec("accounts").setIndicator(
				getString(R.string.ic_tab_accounts),
				app.res.getDrawable(R.drawable.ic_tab_accounts)).setContent(
				intent);
		tabHost.addTab(spec);
		tabHost.setCurrentTab(0);
		Log.i(TAG, "Showing main screen...Done");
	}

	/**
	 * When menu is selected on this app, show options.
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	/**
	 * When any menu item is selected, perform specific action.
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.menu_prefs:
			// show preferences
			startActivity(new Intent(getBaseContext(), Preferences.class));
			return true;
		case R.id.menu_book:
			// Start intent to show book details
			if (app.gncDataHandler != null)
				startActivity(new Intent(getBaseContext(),
						BookDetailsActivity.class));
			return true;
		case R.id.menu_save:
			// Save data
			// #TODO Save data
			return true;
		case R.id.menu_discard:
			// cancel changes and reload - but ask before doing
			// #TODO discard changes
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * When the view is restarted when returned from preferences screen, check
	 * if the reload file flag is set and read data again if it does.
	 * 
	 * @see android.app.Activity#onRestart()
	 */
	@Override
	protected void onRestart() {
		super.onRestart();
		Log.i(TAG, "Activity Restarted.. Checking if data file changed...");
		// check if reload flag is set then read data again
		if (app.isReloadFile() && app.canReadData())
			// read data
			new ReadDataTask().execute();
	}

	/**
	 * This class implements AsyncTask and reads the data file in a new thread so
	 * that the Time Out trigger does not happen.
	 * 
	 * @author John Gray
	 * 
	 */
	private class ReadDataTask extends AsyncTask<Void, Void, Boolean> {
		/**
		 * Show progress dialog before execution.
		 * 
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
			if (pd == null)
				pd = ProgressDialog.show(MainView.this, "Please Wait...",
						"Loading...", true);
			else
				pd.show();
		}

		/**
		 * Call method readData of GNCAndroid in background task.
		 * 
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected Boolean doInBackground(Void... voids) {
			return app.readData();
		}

		/**
		 * Close progress dialog after execution.
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(Boolean result) {
			// Refresh View here
			pd.dismiss();

			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

			if (result != null && result)
				showScreen();
			else {
				Log.i(TAG, "GNCDataHandler failed to initialise... Forcing preferences...");
				forcePreferences(app.res
						.getString(R.string.message_failed_to_read_data_file));
			}
		}
	}
}
