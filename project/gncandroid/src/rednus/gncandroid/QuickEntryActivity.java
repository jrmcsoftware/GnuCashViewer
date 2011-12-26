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

import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.Toast;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.ViewById;

/**
 * This class displays Quick entry screen.
 * 
 * @author John Gray
 * 
 */
@EActivity(R.layout.quickentry)
public class QuickEntryActivity extends Activity {
	// TAG for this activity
	private static final String TAG = "QuickEntryActivity";
	private GNCAndroid app;

	static final int DATE_DIALOG_ID = 0;

	private int currentView = 0;

	private int mYear;
	private int mMonth;
	private int mDay;

	@ViewById
	AutoCompleteTextView edittext_descriptoin;
	@ViewById
	Spinner spinner_to;
	@ViewById
	Spinner spinner_from;
	@ViewById
	EditText edittext_amount;
	@ViewById
	Button button_date;
	@ViewById
	Spinner transtype_spinner;
	private String[] descs;

	@ViewById
	Button to_filter_button;
	@ViewById
	Button from_filter_button;

	@ViewById
	Button saveButton;
	
	@ViewById
	Button clearButton;


	AccountSpinnerData toAccountData;
	AccountSpinnerData fromAccountData;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@AfterViews
	void updateTextWithDate() {
		// get application
		app = (GNCAndroid) getApplication();

		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				QuickEntryActivity.this, R.array.transtype_array,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		transtype_spinner.setAdapter(adapter);

		transtype_spinner.setOnItemSelectedListener(new TransTypeOnItemSelectedListener());

		String[] toAccountFilter = {"EXPENSE"};
		toAccountData = new AccountSpinnerData(app, toAccountFilter);

		String[] fromAccountFilter = {"CREDIT", "BANK", "CASH"};
		fromAccountData = new AccountSpinnerData(app, fromAccountFilter);

		setupTransferControls();
	}
	
	@Click
	void clearButton() {
		if (currentView == 0) {
			final Calendar c = Calendar.getInstance();
			button_date.setText(DateFormat.format("MM/dd/yyyy", c));

			edittext_descriptoin.setText("");
			edittext_amount.setText("");
		}		
	}
	
	@Click
	void saveButton() {
		int toPos = spinner_to.getSelectedItemPosition();
		int fromPos = spinner_from.getSelectedItemPosition();

		Log.v(TAG, "Save button clicked. toPos = " + toPos + ", fromPos = " + fromPos);
		if (toPos < 0 || fromPos < 0) {
			AlertDialog.Builder builder = new AlertDialog.Builder(QuickEntryActivity.this);
			builder.setTitle(R.string.alert_select_title).setMessage(R.string.alert_select_message);
			builder.setPositiveButton(R.string.alert_dialog_ok,
                new DialogInterface.OnClickListener() {
                        public void onClick(
                                        DialogInterface dialog,
                                        int whichButton) {
                        }
                });
			builder.create().show();
			return;
		}
		String toGUID = toAccountData.getAccountGUID(toPos);
		String fromGUID = fromAccountData.getAccountGUID(fromPos);

		String date = button_date.getText().toString();
		String amount = edittext_amount.getText().toString();

		boolean result = app.gncDataHandler.insertTransaction(toGUID, fromGUID,
				edittext_descriptoin.getText().toString(), amount, date);
		if ( result  )
			Toast.makeText(QuickEntryActivity.this, "Transaction added...", Toast.LENGTH_LONG).show();
		else
			Toast.makeText(QuickEntryActivity.this, "Insert failed!", Toast.LENGTH_LONG).show();
	}

	private void setToFromAdapter(Spinner spinner, String[] values) {
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, values);

		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		spinner.setAdapter(adapter);
	}

	private void setupTransferControls() {

		setToFromAdapter(spinner_to, toAccountData.getAccountNames());
		setToFromAdapter(spinner_from, fromAccountData.getAccountNames());

		descs = app.gncDataHandler.getTransactionDescriptions();
		ArrayAdapter<String> descAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_dropdown_item_1line, descs);
		edittext_descriptoin.setAdapter(descAdapter);
		edittext_descriptoin.setOnItemClickListener(new DescriptionOnItemClickListener());

		// get the current date
		final Calendar c = Calendar.getInstance();
		button_date.setText(DateFormat.format("MM/dd/yyyy", c));

		mYear = c.get(Calendar.YEAR);
		mMonth = c.get(Calendar.MONTH);
		mDay = c.get(Calendar.DAY_OF_MONTH);

	}
	
	@Click
	void button_date() {
		showDialog(DATE_DIALOG_ID);
	}
	
	@Click
	void to_filter_button() {
		AlertDialog.Builder builder = new AlertDialog.Builder(QuickEntryActivity.this);
		builder.setTitle("Select Account Types");
		builder.setMultiChoiceItems(toAccountData.getAccountTypeKeys(), toAccountData.accountTypes, new DialogInterface.OnMultiChoiceClickListener() {
			public void onClick(DialogInterface dialog, int which, boolean isChecked) {
				toAccountData.accountTypes[which] = isChecked;
			}
		});
		AlertDialog alert = builder.create();
		alert.setOnDismissListener(new DialogInterface.OnDismissListener() {
			public void onDismiss(DialogInterface arg0) {
				toAccountData.updateAccountNames();
				setToFromAdapter(spinner_to, toAccountData.getAccountNames());
			}
		});
		alert.show();
	}
	
	@Click
	void from_filter_button() {
		AlertDialog.Builder builder = new AlertDialog.Builder(QuickEntryActivity.this);
		builder.setTitle("Select Account Types");
		builder.setMultiChoiceItems(fromAccountData.getAccountTypeKeys(), fromAccountData.accountTypes, new DialogInterface.OnMultiChoiceClickListener() {
			public void onClick(DialogInterface dialog, int which, boolean isChecked) {
				fromAccountData.accountTypes[which] = isChecked;
			}
		});
		AlertDialog alert = builder.create();
		alert.setOnDismissListener(new DialogInterface.OnDismissListener() {
			public void onDismiss(DialogInterface arg0) {
				fromAccountData.updateAccountNames();
				setToFromAdapter(spinner_from, fromAccountData.getAccountNames());
			}
		});
		alert.show();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DATE_DIALOG_ID:
			return new DatePickerDialog(this, mDateSetListener, mYear, mMonth,
					mDay);
		}
		return null;
	}

	// the call back received when the user "sets" the date in the dialog
	private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {

		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			mYear = year;
			mMonth = monthOfYear;
			mDay = dayOfMonth;

			Button dateButton = (Button) findViewById(R.id.ButtonDate);

			dateButton.setText(new StringBuilder()
					// Month is 0 based so add 1
					.append(mMonth + 1).append("/").append(mDay).append("/")
					.append(mYear));
		}
	};

	public class TransTypeOnItemSelectedListener implements
			OnItemSelectedListener {

		public void onItemSelected(AdapterView<?> parent, View view, int pos,
				long id) {
			if (currentView != pos) {
				currentView = pos;

				TableLayout field_table = (TableLayout) findViewById(R.id.field_table);
				field_table.removeAllViews();

				// Create new LayoutInflater - this has to be done this way, as
				// you can't directly inflate an XML without creating an
				// inflater object first
				LayoutInflater inflater = getLayoutInflater();

				switch (pos) {
				case 0:
					field_table.addView(inflater.inflate(R.layout.transfer,
							null));
					setupTransferControls();
					break;
				case 1:
					field_table.addView(inflater
							.inflate(R.layout.invoice, null));
					break;
				case 2:
					field_table.addView(inflater.inflate(
							R.layout.expensevoucher, null));
					break;
				}

				transtype_spinner = (Spinner) findViewById(R.id.transtype_spinner);
				ArrayAdapter<CharSequence> adapter = ArrayAdapter
						.createFromResource(QuickEntryActivity.this,
								R.array.transtype_array,
								android.R.layout.simple_spinner_item);
				adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				transtype_spinner.setAdapter(adapter);
				transtype_spinner.setSelection(pos);

				transtype_spinner.setOnItemSelectedListener(new TransTypeOnItemSelectedListener());
			}
		}

		public void onNothingSelected(AdapterView<?> parent) {
			// Do nothing.
		}
	}

	public class DescriptionOnItemClickListener implements OnItemClickListener {
		public void onItemClick(AdapterView<?> parent, View view, int pos,
				long id) {
			String[] accountGUIDs = app.gncDataHandler
					.getAccountsFromTransactionDescription(edittext_descriptoin
							.getText().toString());
			String[] toAccountGUIDs = toAccountData.getAccountGUIDs();
			String[] fromAccountGUIDs = fromAccountData.getAccountGUIDs();
			if ( accountGUIDs != null ) {
				for (String GUID : accountGUIDs) {
					for (int j = 0; j < toAccountGUIDs.length; j++)
						if (toAccountGUIDs[j].equals(GUID)) {
							spinner_to.setSelection(j);
							break;
						}
					for (int k = 0; k < fromAccountGUIDs.length; k++)
						if (fromAccountGUIDs[k].equals(GUID)) {
							spinner_from.setSelection(k);
							break;
						}
				}
			}
		}
	}
}
