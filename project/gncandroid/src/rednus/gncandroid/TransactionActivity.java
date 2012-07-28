package rednus.gncandroid;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.ListActivity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class TransactionActivity extends ListActivity {

	private GNCAndroid app;

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// get application
		app = (GNCAndroid) getApplication();

		Bundle b = getIntent().getExtras();
		String account = b.getString(GNCAndroid.TRANS_ACT_ACCOUNT_PARAM);
		
		Cursor cursor = app.gncDataHandler.getAccountTransations(account);

        // set this adapter as your ListActivity's adapter
        this.setListAdapter(new TransactionsAdapter(this,cursor));
	}
	
	private class TransactionsAdapter extends CursorAdapter {
		private final LayoutInflater mInflater;
		private SimpleDateFormat dateparser =  new SimpleDateFormat("yyyyMMddHHmmss");
		private DateFormat dateformatter;

		public TransactionsAdapter(Context context, Cursor cursor) {
			super(context, cursor, true);
			mInflater = LayoutInflater.from(context);
			dateformatter = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			TextView t = (TextView) view.findViewById(R.id.textView_date);
			String date = cursor.getString(cursor.getColumnIndex("post_date"));
			Date d;
			try {
				d = dateparser.parse(date);
				t.setText(dateformatter.format(d));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				t.setText(date);
			}

			t = (TextView) view.findViewById(R.id.textView_desc);
			t.setText(cursor.getString(cursor
					.getColumnIndex("description")));

			t = (TextView) view.findViewById(R.id.textView_amount);
			String amount = cursor.getString(cursor.getColumnIndex("amount"));
			Double damount = Double.parseDouble(amount);
			t.setText(String.format("%.2f", damount));
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			final View view = mInflater.inflate(R.layout.transaction_row, parent,
					false);
			return view;
		}
	}
}

