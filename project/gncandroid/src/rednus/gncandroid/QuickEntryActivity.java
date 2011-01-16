/**
 * Copyright (C) 2010 Rednus Limited http://www.rednus.co.uk
 * 
 * #TODO License
 */
package rednus.gncandroid;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TreeMap;

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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

/**
 * This class displays Quick entry screen.
 * 
 * @author John Gray
 * 
 */
public class QuickEntryActivity extends Activity {
	// TAG for this activity
	private static final String TAG = "QuickEntryActivity";
	// Log information boolean
	private GNCAndroid app;

	static final int DATE_DIALOG_ID = 0;

	private int currentView = 0;

	private int mYear;
	private int mMonth;
	private int mDay;

	private AutoCompleteTextView mDescription;
	private Spinner mTo;
	private Spinner mFrom;
	private EditText mAmount;
	private Button dateButton;
	private Spinner transtypeSpinner;
	private String[] descs;

	private String[] toAccountNames;
	private String[] toAccountGUIDs;
	private String[] fromAccountNames;
	private String[] fromAccountGUIDs;
	
	// Support for the account type filters on the to/from spinners
	// These 4 arrays are used as set (kinda two sets)  
	private CharSequence[] accountTypeKeys;		// The user friendly account type names
	private String[] accountTypeValues;			// The account type values as they are used in the db
	private boolean[] toAccountTypes;			// Which types to use in the to account filter
	private boolean[] fromAccountTypes;			// Which types to use in the from account filter


	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// get application
		app = (GNCAndroid) getApplication();
		// add log entry
		if (app.localLOGV)
			Log.i(TAG, "Activity created");
		setContentView(R.layout.quickentry);

		constructAccountTypeFilters();
		
		Button saveButton = (Button) findViewById(R.id.ButtonSave);
		Button clearButton = (Button) findViewById(R.id.ButtonClear);

		transtypeSpinner = (Spinner) findViewById(R.id.transtype_spinner);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				QuickEntryActivity.this, R.array.transtype_array,
				android.R.layout.simple_spinner_item);
		adapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		transtypeSpinner.setAdapter(adapter);

		transtypeSpinner
				.setOnItemSelectedListener(new TransTypeOnItemSelectedListener());

		String[] toAccountFilter = {"EXPENSE"};
		setBitmapFromAccountList(toAccountFilter,toAccountTypes,accountTypeValues);
		constructToAccountLists(getAccountListFromBitmap(toAccountTypes,accountTypeValues));
		
		String[] fromAccountFilter = {"CREDIT", "BANK"};
		setBitmapFromAccountList(fromAccountFilter,fromAccountTypes,accountTypeValues);
		constructFromAccountLists(getAccountListFromBitmap(fromAccountTypes,accountTypeValues));

		setupTransferControls();

		saveButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				int toPos = mTo.getSelectedItemPosition();
				int fromPos = mFrom.getSelectedItemPosition();

				String toGUID = toAccountGUIDs[toPos];
				String fromGUID = fromAccountGUIDs[fromPos];

				String date = dateButton.getText().toString();
				String amount = mAmount.getText().toString();

				app.gncDataHandler.insertTransaction(toGUID, fromGUID,
						mDescription.getText().toString(), amount, date);
			}
		});

		clearButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (currentView == 0) {
					final Calendar c = Calendar.getInstance();
					dateButton.setText(DateFormat.format("MM/dd/yyyy", c));

					mDescription.setText("");
					mAmount.setText("");
				}
			}
		});

		// add log entry
		if (app.localLOGV)
			Log.i(TAG, "Activity Finished");
	}

	private void constructAccountTypeFilters() {
		TreeMap<String, String> accountTypeMapping = app.gncDataHandler.GetAccountTypeMapping();
		int size = accountTypeMapping.size();
		
		accountTypeKeys = new CharSequence[size];
		accountTypeValues = new String[size];
		toAccountTypes = new boolean[size];
		fromAccountTypes = new boolean[size];

		int i=0;
		for (String key: accountTypeMapping.keySet()) {
			accountTypeKeys[i] = key;
			accountTypeValues[i] = accountTypeMapping.get(key);
			toAccountTypes[i] = false;
			fromAccountTypes[i] = false;
			i++;
		}
	}
	
	private void constructToAccountLists(String[] filter) {
		TreeMap<String, String> toAccounts = app.gncDataHandler
				.GetAccountList(filter);
		toAccountNames = new String[toAccounts.size()];
		toAccountGUIDs = new String[toAccounts.size()];
		toAccounts.keySet().toArray(toAccountNames);
		for (int i = 0; i < toAccounts.size(); i++)
			toAccountGUIDs[i] = toAccounts.get(toAccountNames[i]);
	}

	private void constructFromAccountLists(String[] filter) {
		TreeMap<String, String> fromAccounts = app.gncDataHandler
				.GetAccountList(filter);
		fromAccountNames = new String[fromAccounts.size()];
		fromAccountGUIDs = new String[fromAccounts.size()];
		fromAccounts.keySet().toArray(fromAccountNames);
		for (int i = 0; i < fromAccounts.size(); i++)
			fromAccountGUIDs[i] = fromAccounts.get(fromAccountNames[i]);
	}

	private void setBitmapFromAccountList(String values[], boolean[] accountTypeBitmap, String[] accountList) {
		for(int i=0;i<accountList.length;i++) {
			boolean found = false;
			for(String v: values)
				if ( accountList[i].equals(v))
					found = true;
			accountTypeBitmap[i] = found;
		}		
	}
	
	private String[] getAccountListFromBitmap(boolean[] selectedAccoutTypes, String[] values) {
		ArrayList<String> l = new ArrayList<String>();
		
		for (int i=0;i<selectedAccoutTypes.length;i++)
			if (selectedAccoutTypes[i] )
				l.add(values[i]);
		
		String[] ret = new String[l.size()];
		return l.toArray(ret);
	}

	private void setToFromAdapter(Spinner spinner, String[] values) {
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, values);
		
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		spinner.setAdapter(adapter);
	}
	
	private void setupTransferControls() {
		mDescription = (AutoCompleteTextView) findViewById(R.id.EditTextDescriptoin);
		mTo = (Spinner) findViewById(R.id.spinner_to);
		mFrom = (Spinner) findViewById(R.id.spinner_from);
		mAmount = (EditText) findViewById(R.id.amount);
		dateButton = (Button) findViewById(R.id.ButtonDate);
		
		Button toFilterButton = (Button) findViewById(R.id.to_filter_button);
		Button fromFilterButton = (Button) findViewById(R.id.from_filter_button);

		setToFromAdapter(mTo, toAccountNames);
		setToFromAdapter(mFrom, fromAccountNames);

		descs = app.gncDataHandler.GetTransactionDescriptions();
		ArrayAdapter<String> descAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_dropdown_item_1line, descs);
		mDescription.setAdapter(descAdapter);
		mDescription
				.setOnItemClickListener(new DescriptionOnItemClickListener());

		// get the current date
		final Calendar c = Calendar.getInstance();
		dateButton.setText(DateFormat.format("MM/dd/yyyy", c));

		mYear = c.get(Calendar.YEAR);
		mMonth = c.get(Calendar.MONTH);
		mDay = c.get(Calendar.DAY_OF_MONTH);

		dateButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showDialog(DATE_DIALOG_ID);
			}
		});
		
		toFilterButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(QuickEntryActivity.this);
				builder.setTitle("Select Account Types");
				builder.setMultiChoiceItems(accountTypeKeys, toAccountTypes, new DialogInterface.OnMultiChoiceClickListener() {
					
					public void onClick(DialogInterface dialog, int which, boolean isChecked) {
						toAccountTypes[which] = isChecked;
					}
				});
				AlertDialog alert = builder.create();
				alert.setOnDismissListener(new DialogInterface.OnDismissListener() {
					
					public void onDismiss(DialogInterface arg0) {
						constructToAccountLists(getAccountListFromBitmap(toAccountTypes,accountTypeValues));
						setToFromAdapter(mTo, toAccountNames);
					}
					
				});
				alert.show();
			}
		});

		fromFilterButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(QuickEntryActivity.this);
				builder.setTitle("Select Account Types");
				builder.setMultiChoiceItems(accountTypeKeys, fromAccountTypes, new DialogInterface.OnMultiChoiceClickListener() {
					
					public void onClick(DialogInterface dialog, int which, boolean isChecked) {
						fromAccountTypes[which] = isChecked;
					}
				});
				AlertDialog alert = builder.create();
				alert.setOnDismissListener(new DialogInterface.OnDismissListener() {
					
					public void onDismiss(DialogInterface arg0) {
						constructFromAccountLists(getAccountListFromBitmap(fromAccountTypes,accountTypeValues));
						setToFromAdapter(mFrom, fromAccountNames);
					}
					
				});
				alert.show();
			}
		});

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

				transtypeSpinner = (Spinner) findViewById(R.id.transtype_spinner);
				ArrayAdapter<CharSequence> adapter = ArrayAdapter
						.createFromResource(QuickEntryActivity.this,
								R.array.transtype_array,
								android.R.layout.simple_spinner_item);
				adapter
						.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				transtypeSpinner.setAdapter(adapter);
				transtypeSpinner.setSelection(pos);

				transtypeSpinner
						.setOnItemSelectedListener(new TransTypeOnItemSelectedListener());
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
					.GetAccountsFromTransactionDescription(mDescription
							.getText().toString());
			for (int i = 0; i < accountGUIDs.length; i++) {
				for (int j = 0; j < toAccountGUIDs.length; j++)
					if (toAccountGUIDs[j].equals(accountGUIDs[i])) {
						mTo.setSelection(j);
						break;
					}
				for (int k = 0; k < fromAccountGUIDs.length; k++)
					if (fromAccountGUIDs[k].equals(accountGUIDs[i])) {
						mFrom.setSelection(k);
						break;
					}
			}
		}
	}
}
