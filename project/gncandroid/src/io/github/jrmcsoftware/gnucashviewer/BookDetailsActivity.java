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

import io.github.jrmcsoftware.gnucashviewer.GNCDataHandler.DataCollection;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.TableLayout;
import android.widget.TextView;

/**
 * This class displays opened Book details.
 * 
 * @author avvari.shyam
 * 
 */
public class BookDetailsActivity extends Activity {
	private static final String TAG = "BookDetailsActivity";
	private GnuCashViewer app;
	private SharedPreferences sp;

	/**
	 * When activity is started, and if Data file is already read, then display
	 * account information tree.
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (GnuCashViewer) getApplication();
		sp = app.getSharedPreferences(GnuCashViewer.SPN, Context.MODE_PRIVATE);
		Log.i(TAG, "Showing Book Details screen..");
		// set activity title
		setTitle(getString(R.string.app_name) + " > "
				+ app.res.getString(R.string.menu_book));
		// set view
		setContentView(R.layout.bookdetails);
		// set column 1 to shrinkable
		((TableLayout) findViewById(R.id.book_details_table))
				.setColumnShrinkable(1, true);
		// now set data
		setFieldValues();
	}

	private void setFieldValues() {
		// get data file
		((TextView) this.findViewById(R.id.data_file_name))
				.setText(sp.getString(app.res.getString(R.string.pref_data_file_key), null));
		// get data collection
		DataCollection gncData = app.getGncDataHandler().getGncData();
		// set book version
		((TextView) this.findViewById(R.id.book_version))
				.setText(gncData.book.version);
		// set company details
		((TextView) this.findViewById(R.id.comp_name))
				.setText(gncData.book.compName);
		((TextView) this.findViewById(R.id.comp_id))
				.setText(gncData.book.compId);
		((TextView) this.findViewById(R.id.comp_addr))
				.setText(gncData.book.compAddr);
		((TextView) this.findViewById(R.id.comp_email))
				.setText(gncData.book.compEmail);
		((TextView) this.findViewById(R.id.comp_url))
				.setText(gncData.book.compUrl);
		((TextView) this.findViewById(R.id.comp_phone))
				.setText(gncData.book.compPhone);
		((TextView) this.findViewById(R.id.comp_fax))
				.setText(gncData.book.compFax);
		((TextView) this.findViewById(R.id.comp_contact))
				.setText(gncData.book.compContact);
	}
}
