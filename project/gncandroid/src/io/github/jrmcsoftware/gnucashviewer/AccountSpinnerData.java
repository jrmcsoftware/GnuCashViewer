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

import java.util.ArrayList;
import java.util.TreeMap;

/**
 * This class holds all the data for an account spinner and its account type filter.
 *
 * @author gray
 */
public class AccountSpinnerData {
	private GnuCashViewer app;

	// Two parallel arrays of matching account names and GUIDs
	private String[] accountNames;
	private String[] accountGUIDs;

	// Support for the account type filters on the to/from spinners
	// These 3 arrays are used as set
	private CharSequence[] accountTypeKeys;		// The user friendly account type names
	private String[] accountTypeValues;			// The account type values as they are used in the db
	// accountType is public so the account filter dialogs can access it directly
	public boolean[] accountTypes;				// Which types to use in the to account filter

	/**
	 * Instantiates a new account spinner data.
	 *
	 * @param app The app
	 * @param values The initial account types
	 */
	public AccountSpinnerData(GnuCashViewer app, String values[]) {
		this.app = app;

		TreeMap<String, String> accountTypeMapping = app.getGncDataHandler().getAccountTypeMapping();
		int size = accountTypeMapping.size();

		accountTypeKeys = new CharSequence[size];
		accountTypeValues = new String[size];
		accountTypes = new boolean[size];

		int i = 0;
		for (String key: accountTypeMapping.keySet()) {
			accountTypeKeys[i] = key;
			accountTypeValues[i] = accountTypeMapping.get(key);
			accountTypes[i] = false;
			i++;
		}

		setBitmapFromAccountList(values);
		updateAccountNames();
	}

	/**
	 * Gets the account GUID.
	 *
	 * @param pos The position of the requested account.
	 * @return The account GUID
	 */
	public String getAccountGUID(int pos) {
		return accountGUIDs[pos];
	}

	/**
	 * Gets the account names.
	 *
	 * @return The account names
	 */
	public String[] getAccountNames() {
		return accountNames;
	}

	/**
	 * Gets the account type keys.
	 *
	 * @return The account type keys
	 */
	public CharSequence[] getAccountTypeKeys() {
		return accountTypeKeys;
	}

	/**
	 * Update account names. Used when there is a new bitmap of filtered accounts.
	 */
	public void updateAccountNames() {
		constructAccountLists(getAccountListFromBitmap());
	}

	/**
	 * Gets the account GUIDs.
	 *
	 * @return The account GUIDs
	 */
	public String[] getAccountGUIDs() {
		return accountGUIDs;
	}

	/**
	 * Construct account lists.
	 *
	 * @param filter The account type filter
	 */
	private void constructAccountLists(String[] filter) {
		TreeMap<String, String> accounts = app.getGncDataHandler()
				.getAccountList(filter);

		if ( accounts == null )
			return;

		accountNames = new String[accounts.size()];
		accountGUIDs = new String[accounts.size()];
		accounts.keySet().toArray(accountNames);
		accounts.values().toArray(accountGUIDs);
	}

	/**
	 * Sets the bitmap from account list.
	 *
	 * @param values The list of accounts that should be set to true on the account bitmap.
	 */
	private void setBitmapFromAccountList(String values[]) {
		for (int i = 0; i < accountTypeValues.length; i++) {
			for (String v: values)
				if ( accountTypeValues[i].equals(v)) {
					accountTypes[i] = true;
					break;
				}
		}
	}

	/**
	 * Gets the account list from bitmap.
	 *
	 * @return The account list from bitmap.
	 */
	private String[] getAccountListFromBitmap() {
		ArrayList<String> l = new ArrayList<String>();

		for (int i = 0; i < accountTypes.length; i++)
			if (accountTypes[i] )
				l.add(accountTypeValues[i]);

		String[] ret = new String[l.size()];
		return l.toArray(ret);
	}
}
