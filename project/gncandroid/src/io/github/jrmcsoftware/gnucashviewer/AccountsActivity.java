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

import io.github.jrmcsoftware.gnucashviewer.GNCDataHandler.Account;
import io.github.jrmcsoftware.gnucashviewer.GNCDataHandler.DataCollection;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Map;
import java.util.TreeMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * @author shyam.avvari
 * 
 */
public class AccountsActivity extends Activity implements OnItemClickListener {
	// TAG for this activity
	private static final String TAG = "AccountsActivity";
	// Application data
	private GnuCashViewer app;
	// The GUID which roots the tree of accounts.
	// ** NOTE: This GUID is the root of the accounts in this view, _not_ the gnucash "Root" account. **
	private String currRootGUID;
	// Map of accounts which will form the list view.
	private Map<String, Account> listData = new TreeMap<String, Account>();
	// The parsed book.
	private DataCollection dc;
	// The app's shared preferences.
	private SharedPreferences sp;
	// A poor man's condition variable for notifying changes to the UI.
	private long dataChangeCount;
	// The adapter which will form the view of this activity.
	private AccountsListAdapter lstAdapter;

	/*
	 * When activity is started, and if Data file is already read, then display
	 * account information tree.
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "Creating activity...");
		app = (GnuCashViewer) getApplication();
		sp = getSharedPreferences(GnuCashViewer.SPN, MODE_PRIVATE);
		// Get first object of data
		dc = app.gncDataHandler.getGncData();
		dataChangeCount = app.gncDataHandler.getChangeCount();
		getListData(dc.book.rootAccountGUID);
		// set view
		setContentView(R.layout.accounts);
		// get list
		ListView lv = (ListView) findViewById(R.id.accounts);
		lstAdapter = new AccountsListAdapter(this);
		lv.setAdapter(lstAdapter);
		lv.setOnItemClickListener(this);
		Log.i(TAG, "Activity created.");
	}

	@Override
	protected void onResume() {
		super.onResume();
		onWindowFocusChanged(true);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		// onResume is too early (the "Loading..." screen still shows), so update the data when we get focus.
		if (!hasFocus)
			return;
		// Synchronise this view with the data. If they have changed,
		// get the new data and tell the adapter to refresh.
		long cc = app.gncDataHandler.getChangeCount();
		if ( cc != dataChangeCount ) {
			Log.i(TAG, "onResume: data changed...");
			dc = app.gncDataHandler.getGncData();
			dataChangeCount = cc;
			getListData(dc.book.rootAccountGUID);
			lstAdapter.notifyDataSetChanged();
		}
	}


	/**
	 * This method will get all the sub accounts of root and adds it to the list
	 * for display.
	 * 
	 * @param root
	 */
	private void getListData(String rootGUID) {
		// get root account
		Account account = dc.accounts.get(rootGUID);
		listData = app.gncDataHandler.getSubAccounts(rootGUID);
		if ( sp.getBoolean(app.res.getString(R.string.pref_include_subaccount_in_balance), false) )
			app.gncDataHandler.getAccountBalanceWithChildren(account);

		currRootGUID = rootGUID;
	}

	/**
	 * Event Handler for List item selection
	 * 
	 * @see
	 * android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget
	 * .AdapterView, android.view.View, int, long)
	 */
	public void onItemClick(AdapterView<?> parent, View child, int position,
			long id) {
		Log.i(TAG, "Account Selected");
		// get adapter
		BaseAdapter b = (BaseAdapter) parent.getAdapter();
		// get current item
		Account account = (Account) b.getItem(position);
		// depending on root or item get list
		if (account.GUID.equalsIgnoreCase(currRootGUID))
			getListData(account.parentGUID);
		else
			getListData(account.GUID);
		// set refresh
		b.notifyDataSetChanged();
		// reset list position to top
		parent.scrollTo(0, 0);
		Log.i(TAG, "Accounts Refreshed");
	}

	/**
	 * This class implements Adapter methods for displaying accounts in a list
	 * view
	 * 
	 * @author avvari.shyam
	 * 
	 */
	private class AccountsListAdapter extends BaseAdapter {
		private LayoutInflater mInflater;

		/**
		 * Constructor - creates inflator's instance from context
		 * 
		 * @param context
		 */
		public AccountsListAdapter(Context context) {
			mInflater = LayoutInflater.from(context);
		}

		/**
		 * Returns the total number of items to be displayed
		 * 
		 * @see android.widget.Adapter#getCount()
		 */
		public int getCount() {
			return listData.size();
		}

		/**
		 * Returns the Item in specific position
		 * 
		 * @see android.widget.Adapter#getItem(int)
		 */
		public Object getItem(int i) {
			return listData.get(listData.keySet().toArray()[i]);
		}

		/**
		 * Returns the id of the item in specific position. In this case its the
		 * position itself.
		 * 
		 * @see android.widget.Adapter#getItemId(int)
		 */
		public long getItemId(int i) {
			return i;
		}

		/**
		 * This method creates the list item for specific position passed. It
		 * populates data from the list item at position.
		 * 
		 * @see android.widget.Adapter#getView(int, android.view.View,
		 * android.view.ViewGroup)
		 */
		public View getView(int position, View convertView, ViewGroup parent) {
			AccountItem item = new AccountItem();
			Account account = (Account) getItem(position);
			// always create new view
			convertView = mInflater.inflate(R.layout.account_item, null);
			item.btnExpand = (ImageView) convertView
					.findViewById(R.id.acc_more);
			item.txvAccName = (TextView) convertView
					.findViewById(R.id.acc_name);
			item.txvBalance = (TextView) convertView
					.findViewById(R.id.acc_balance);
			convertView.setTag(item);
			// set values for account line item
			item.txvAccName.setText(account.name);

			item.accGUID = account.GUID;
			
			Double balance;
			if (sp.getBoolean(app.res.getString(R.string.pref_include_subaccount_in_balance), false))
				balance = account.balanceWithChildren;
			else
				balance = account.balance;

			// Get a NumberFormat instance which will format a currency in the default Locale.
			NumberFormat formatter = NumberFormat.getCurrencyInstance();
			// Set the currency to the one specified in the account.
			if (formatter instanceof DecimalFormat && account.commodity.currency != null )
				formatter.setCurrency(account.commodity.currency);
			// Display the formatted balance.
			item.txvBalance.setText(formatter.format(balance));

			// set amount colour
			if (balance < 0)
				item.txvBalance.setTextColor(app.res
						.getColor(R.color.color_negative));
			else
				item.txvBalance.setTextColor(app.res
						.getColor(R.color.color_positive));
			// set image properties
			if (account.GUID.equalsIgnoreCase(currRootGUID))
				item.btnExpand.setImageResource(R.drawable.list_expanded);
			else if (account.hasChildren)
				item.btnExpand.setImageResource(R.drawable.list_collapsed);
			
			if ( !account.hasChildren ) {
				convertView.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						AccountItem item = (AccountItem)v.getTag();
						Intent intent = new Intent(AccountsActivity.this, TransactionActivity.class);
						Bundle b = new Bundle();
						b.putString(GnuCashViewer.TRANS_ACT_ACCOUNT_PARAM, item.accGUID); //Your id
						intent.putExtras(b); //Put your id to your next Intent
						startActivity(intent);
					}
				});
			}

			return convertView;
		}

		/**
		 * When the data in list listData changed, it is necessary to redraw the
		 * list view and fill it up with new data.
		 * 
		 * @see android.widget.BaseAdapter#notifyDataSetChanged()
		 */
		@Override
		public void notifyDataSetChanged() {
			// first invalidate data set
			super.notifyDataSetInvalidated();
			super.notifyDataSetChanged();
		}

		/**
		 * This methods tells the view layout that all the items in the list are
		 * not enabled by default.
		 * 
		 * @see android.widget.BaseAdapter#areAllItemsEnabled()
		 */
		@Override
		public boolean areAllItemsEnabled() {
			return false;
		}

		/**
		 * This method checks if the list item has children and then return true
		 * to enable the list item as clickable.
		 * 
		 * @see android.widget.BaseAdapter#isEnabled(int)
		 */
		@Override
		public boolean isEnabled(int position) {
			Account account = (Account) getItem(position);
			return account.hasChildren;
		}

		/**
		 * This is holder class for Account Summary Row
		 * 
		 * @author shyam.avvari
		 * 
		 */
		private class AccountItem {
			ImageView btnExpand;
			TextView txvAccName;
			TextView txvBalance;
			String accGUID;
		}
	}
}
