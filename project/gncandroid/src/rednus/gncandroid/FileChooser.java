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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * @author shyam, davide
 * 
 */
public class FileChooser extends ListActivity {
	private final String TAG = "File Chooser";
	protected ArrayList<String> mFileList;
	protected File mRoot;
	private GNCAndroid app;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (GNCAndroid) getApplication();
		Log.i(TAG, "Filechooser started");
		// make this a dialogue
		requestWindowFeature(Window.FEATURE_LEFT_ICON);
		setContentView(R.layout.filechooser);
		getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
				android.R.drawable.stat_notify_sdcard);
		// TODO It would be nice to start from the directory of the previously
		// selected data file (if not null)
		this.initialize(app.res.getString(R.string.def_folder));
	}

	private void initialize(String path) {
		mFileList = new ArrayList<String>();
		// mFileList.add("..");
		if (getDirectory(path)) {
			getFiles(mRoot);
			Collections.sort(mFileList, String.CASE_INSENSITIVE_ORDER);
			displayFiles();
		}
	}

	private void refreshRoot(File f) {
		mRoot = f;
		mFileList.clear();
		if (!f.getName().equalsIgnoreCase("sdcard"))
			mFileList.add("..");
		getFiles(mRoot);
		Collections.sort(mFileList, String.CASE_INSENSITIVE_ORDER);
		((ArrayAdapter) this.getListAdapter()).notifyDataSetChanged();
	}

	private boolean getDirectory(String path) {
		TextView tv = (TextView) findViewById(R.id.filelister_message);
		// check to see if there's an SD card.
		String cardstatus = Environment.getExternalStorageState();
		if (cardstatus.equals(Environment.MEDIA_REMOVED)
				|| cardstatus.equals(Environment.MEDIA_UNMOUNTABLE)
				|| cardstatus.equals(Environment.MEDIA_UNMOUNTED)
				|| cardstatus.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
			tv.setText(getString(R.string.sdcard_error));
			return false;
		}
		// now get file object for the path
		mRoot = new File(path);
		if (!mRoot.exists())
			tv.setText(getString(R.string.directory_error, path));
		else
			return true;
		return false;
	}

	private void getFiles(File f) {
		if (f.isDirectory()) {
			File[] childs = f.listFiles();
			// listFile can return null if an error occurs!
			if (null == childs)
				return;
			for (File child : childs) {
				getFile(child);
			}
		} else
			getFile(f);
	}

	private void getFile(File f) {
		String filename = f.getName();
		mFileList.add(filename);
	}

	/**
	 * Opens the directory, puts valid files in array adapter for display
	 */
	private void displayFiles() {
		ArrayAdapter<String> fileAdapter;
		fileAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, mFileList);
		setListAdapter(fileAdapter);
	}

	/**
	 * Stores the path of clicked file in the intent and exits.
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		File f;
		if (mFileList.get(position) == "..")
			f = new File(mRoot.getParent());
		else
			f = new File(mRoot + "/" + mFileList.get(position));
		if (f.isDirectory()) {
			this.refreshRoot(f);
			return;
		}
		Log.i(TAG, "File selected, returning result");
		// send result
		Intent i = new Intent();
		i.putExtra(app.res.getString(R.string.pref_data_file_key), f
				.getAbsolutePath());
		setResult(RESULT_OK, i);
		// close activity
		finish();
	}
}
