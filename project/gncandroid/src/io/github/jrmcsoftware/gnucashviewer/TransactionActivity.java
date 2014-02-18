package io.github.jrmcsoftware.gnucashviewer;

import io.github.jrmcsoftware.gnucashviewer.GNCDataHandler.Account;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.ListActivity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class TransactionActivity extends ListActivity {

    private GnuCashViewer app;
    private Account account;
    private SimpleDateFormat dateparser = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get application
        app = (GnuCashViewer) getApplication();

        Bundle b = getIntent().getExtras();
        String accountGuid = b.getString(GnuCashViewer.TRANS_ACT_ACCOUNT_PARAM);

        account = app.getGncDataHandler().getAccount(accountGuid, false);

        List<GnuCashTransaction> transactions = new ArrayList<GnuCashTransaction>();
        Cursor cursor = app.getGncDataHandler().getAccountTransations(account);

        BigDecimal runningTotal = BigDecimal.ZERO;
        for (int x = 0; x < cursor.getCount(); x++) {
            GnuCashTransaction transaction = new GnuCashTransaction();

            cursor.moveToPosition(x);
            BigDecimal amount = new BigDecimal(cursor.getString(cursor.getColumnIndex("amount")));
            transaction.setAmount(amount);
            transaction.setDescription(cursor.getString(cursor.getColumnIndex("description")));

            String postDateString = cursor.getString(cursor.getColumnIndex("post_date"));
            try {
                transaction.setPostDate(dateparser.parse(postDateString));
            } catch (ParseException e) {
                throw new RuntimeException("Can't parse date: '" + postDateString + "'");
            }

            transactions.add(transaction);
        }

        Collections.reverse(transactions);

        for (GnuCashTransaction transaction : transactions) {
            runningTotal = runningTotal.add(transaction.getAmount());
            transaction.setRunningTotal(runningTotal);
        }

        Collections.reverse(transactions);

        // set this adapter as your ListActivity's adapter
        this.setListAdapter(new TransactionsAdapter(this, transactions));
    }

    private class GnuCashTransaction {
        private Date postDate;
        private String description;
        private BigDecimal amount;
        private BigDecimal runningTotal;

        public Date getPostDate() {
            return postDate;
        }

        public void setPostDate(Date postDate) {
            this.postDate = postDate;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public BigDecimal getRunningTotal() {
            return runningTotal;
        }

        public void setRunningTotal(BigDecimal runningTotal) {
            this.runningTotal = runningTotal;
        }

        public boolean isAmountPositive() {
            return amount.compareTo(BigDecimal.ZERO) >= 0;
        }

        public boolean isRunningTotalPositive() {
            return runningTotal.compareTo(BigDecimal.ZERO) >= 0;
        }

    }

    private class TransactionsAdapter extends ArrayAdapter<GnuCashTransaction> {
        private final LayoutInflater mInflater;
        private DateFormat dateformatter;

        public TransactionsAdapter(Context context, List<GnuCashTransaction> transactions) {
            super(context, R.layout.transaction_row, transactions);
            mInflater = LayoutInflater.from(context);
            dateformatter = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            GnuCashTransaction transaction = getItem(position);
            if (view == null) {
                view = mInflater.inflate(R.layout.transaction_row, null);
            }

            TextView t = (TextView) view.findViewById(R.id.textView_date);
            t.setText(dateformatter.format(transaction.getPostDate()));

            t = (TextView) view.findViewById(R.id.textView_desc);
            t.setText(transaction.getDescription());

            t = (TextView) view.findViewById(R.id.textView_amount);

            // Get a NumberFormat instance which will format a currency in the
            // default Locale.
            NumberFormat formatter = NumberFormat.getCurrencyInstance();
            // Set the currency to the one specified in the account.
            if (formatter instanceof DecimalFormat && account.commodity.currency != null)
                formatter.setCurrency(account.commodity.currency);
            // Display the formatted balance.
            t.setText(formatter.format(transaction.getAmount()));

            // set amount colour
            if (transaction.isAmountPositive()) {
                t.setTextColor(app.res.getColor(R.color.color_negative));
            } else {
                t.setTextColor(app.res.getColor(R.color.color_positive));
            }

            TextView runningTotalView = (TextView) view.findViewById(R.id.textView_running_total);

            // Display the formatted balance.
            runningTotalView.setText(formatter.format(transaction.getRunningTotal()));

            // set amount colour
            if (transaction.isRunningTotalPositive()) {
                runningTotalView.setTextColor(app.res.getColor(R.color.color_negative));
            } else {
                runningTotalView.setTextColor(app.res.getColor(R.color.color_positive));
            }

            return view;
        }
    }
}
