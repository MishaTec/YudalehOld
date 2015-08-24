package il.ac.huji.yudaleh;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;

/**
 * Todo:
 * - read DB onCreate - only query once!
 * - add push notification receiver
 * - prevent onCreate after add/edit
 * - add night mode
 * - remove notification on install
 * - add clear button
 */
public class MainActivity extends AppCompatActivity {
    private static final int NEW_ITEM_REQUEST = 42;
    private static final int UPDATE_ITEM_REQUEST = 43;
    private static final long NO_ID_PASSED = -22;
    private static final String I_OWE_TAB_TAG = "iOwe";
    private static final String OWE_ME_TAB_TAG = "oweMe";
    private ListAdapter iOweAdapter;
    private ListAdapter oweMeAdapter;
    private DBHelper helper;
    private TabHost tabHost;

    /**
     * Inner class: cursor adapter between the database and the to-do list
     */
    private class ListAdapter extends SimpleCursorAdapter {
        private String table;

        /**
         * Creates the message for the on-long-click listener dialog
         *
         * @param title debt title
         * @param owner debt owner
         * @return the created message
         */
        private String createDialogMessage(String title, String owner) {
            if (table.equals(DBHelper.I_OWE_TABLE)) {
                return "You owe " + owner + " " + title;
            } else {
                return owner + " owes you " + title;
            }
        }

        public ListAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags, String table) {
            super(context, layout, c, from, to, flags);
            this.table = table;
        }

        @SuppressLint("InflateParams")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.list_item, null);
            }

            Cursor item = (Cursor) getItem(position);

            TextView titleText = (TextView) convertView.findViewById(R.id.txtTitle);
            titleText.setText(item.getString(DBHelper.TITLE_COLUMN_INDEX));
            titleText.setTextColor(Color.BLACK);

            TextView ownerText = (TextView) convertView.findViewById(R.id.txtOwner);
            ownerText.setText(item.getString(DBHelper.OWNER_COLUMN_INDEX));
            ownerText.setTextColor(Color.BLACK);

            TextView dueDateText = (TextView) convertView.findViewById(R.id.txtDueDate);
            if (item.isNull(DBHelper.DUE_COLUMN_INDEX)) {
                dueDateText.setText("No reminder");
                dueDateText.setTextColor(Color.BLACK);
            } else {
                Date dueDate = new Date(item.getLong(DBHelper.DUE_COLUMN_INDEX));
                String format = "kk:mm MM/dd yyyy ";
                Calendar now = Calendar.getInstance();
                //noinspection deprecation
                if (dueDate.getYear() == now.getTime().getYear()) {
                    format = "kk:mm MM/dd";
                }
                dueDateText.setText(android.text.format.DateFormat.format(format, dueDate.getTime()));
                dueDateText.setTextColor(Color.BLACK);
                if (isOverdue(dueDate)) {
                    dueDateText.setTextColor(Color.RED);
                    titleText.setTextColor(Color.RED);
                }
            }
            return convertView;
        }


        /**
         * Checks if the dueDate is before the current date and time
         *
         * @param dueDate Date and Time object
         * @return true iff dueDate is before the current time
         */
        private boolean isOverdue(Date dueDate) {
            if (dueDate == null) {
                return false;
            }
            Calendar now = Calendar.getInstance();
            Calendar due = Calendar.getInstance();
            due.setTime(dueDate);
            return due.before(now);
        }
    }

    /**
     * Opens activity for adding new item
     */
    public void addNewItem(String table) {
        Intent intent = new Intent(this, ItemEditActivity.class);
        intent.putExtra("table", table);
        startActivityForResult(intent, NEW_ITEM_REQUEST);
    }

    /**
     * Opens a dialog for adding a new to-do
     *
     * @param table which table to use
     * @param rowId the rowId field from the DB
     * @param pos   the actual index in the list (starts from 0)
     */
    public void updateItem(String table, long rowId, int pos) {
        System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&***************************** rowId=" + rowId + ", pos=" + pos); // TODO: 20/08/2015 remove
        Intent intent = new Intent(this, ItemEditActivity.class);
        intent.putExtra("rowId", rowId);
        Cursor item = helper.getItem(table, rowId); // todo remove - inefficient
//        Cursor item = (Cursor) iOweAdapter.getItem(pos);
        intent.putExtra("title", item.getString(DBHelper.TITLE_COLUMN_INDEX));
        if (!item.isNull(DBHelper.DUE_COLUMN_INDEX)) {
            intent.putExtra("dueDate", new Date(item.getLong(DBHelper.DUE_COLUMN_INDEX)));
        }
        intent.putExtra("desc", item.getString(DBHelper.DESCRIPTION_COLUMN_INDEX));
        intent.putExtra("owner", item.getString(DBHelper.OWNER_COLUMN_INDEX));
        intent.putExtra("table", table);
        startActivityForResult(intent, UPDATE_ITEM_REQUEST);
    }

    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent data) {
        if (resCode != Activity.RESULT_OK) {
            return;
        }

        String title = data.getStringExtra("title");
        Date dueDate = (Date) data.getSerializableExtra("dueDate");
        String desc = data.getStringExtra("desc");
        String owner = data.getStringExtra("owner");
        String table = data.getStringExtra("table");

        ListAdapter adapter;
        if (table.equals(DBHelper.OWE_ME_TABLE)) {
            adapter = oweMeAdapter;
            System.out.println("################################################################ ++ " + adapter.table);
        } else {
            adapter = iOweAdapter;
            System.out.println("################################################################ i owe++ " + adapter.table);

        }

        if (reqCode == NEW_ITEM_REQUEST) { // Add the item to DB
            if (!title.equals("")) {
                long newRowId = helper.insert(table, title, dueDate, desc, owner);
                adapter.changeCursor(helper.getCursor(table));
                adapter.notifyDataSetChanged();
                System.out.println("################################################################ " + adapter.table);
                if (table.equals(DBHelper.OWE_ME_TABLE)) {
                    newRowId = -newRowId;
                }
                if (dueDate != null) {
                    setAlarm(newRowId, title, dueDate, owner);
                }
            }
        }
        if (reqCode == UPDATE_ITEM_REQUEST) { // Update the item in DB
            if (!title.equals("")) {
                long rowId = data.getLongExtra("rowId", NO_ID_PASSED);
                helper.update(table, rowId, title, dueDate, desc, owner);
                adapter.changeCursor(helper.getCursor(table));
                adapter.notifyDataSetChanged();
                if (table.equals(DBHelper.OWE_ME_TABLE)) {
                    rowId = -rowId;
                }
                if (dueDate != null) {
                    setAlarm(rowId, title, dueDate, owner);
                } else {
                    cancelAlarm(rowId);
                }
            }
        }
    }

    /**
     * Sets a new notification alarm.
     *
     * @param alarmId must be unique
     * @param title debt title
     * @param dueDate debt due date
     * @param owner debt owner
     */
    private void setAlarm(long alarmId, String title, Date dueDate, String owner) {
        long timeInMillis = dueDate.getTime();
        Intent alertIntent = new Intent(this, DueDateAlarm.class);
        alertIntent.putExtra("title", title);
        alertIntent.putExtra("owner", owner);
        alertIntent.setData(Uri.parse("timer:" + alarmId));

        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, timeInMillis, PendingIntent.getBroadcast(
                this, (int) alarmId, alertIntent, PendingIntent.FLAG_UPDATE_CURRENT));

        Toast.makeText(
                this,
                "Reminder  " + alarmId + " at "
                        + android.text.format.DateFormat.format(
                        "MM/dd/yy h:mmaa",
                        timeInMillis),
                Toast.LENGTH_LONG).show(); // todo remove
    }

    /**
     * Cancels notification alarm.
     *
     * @param alarmId must be unique
     */
    private void cancelAlarm(long alarmId) {
        Intent alertIntent = new Intent(this, DueDateAlarm.class);
        alertIntent.setData(Uri.parse("timer:" + alarmId));

        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.cancel(PendingIntent.getBroadcast(this, (int) alarmId, alertIntent, PendingIntent.FLAG_UPDATE_CURRENT));

        Toast.makeText(this, "REMOVED Reminder  " + alarmId, Toast.LENGTH_LONG).show(); // todo remove
    }

    /**
     * Initializes all listeners for the given adapter and view
     *
     * @param adapter the adapter to init
     * @param view    the view on which the adapter works
     */
    private void initListAdapter(final ListAdapter adapter, ListView view) {
        view.setAdapter(adapter);
        view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> av, View v, final int pos, final long rowId) {
                updateItem(adapter.table, rowId, pos);
            }
        });
        view.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> av, View v, final int pos, final long rowId) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                String title = ((TextView) v.findViewById(R.id.txtTitle)).getText().toString();
                String owner = ((TextView) v.findViewById(R.id.txtOwner)).getText().toString();
                builder.setMessage(adapter.createDialogMessage(title, owner));
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        helper.delete(adapter.table, rowId);
                        adapter.changeCursor(helper.getCursor(adapter.table));
                        adapter.notifyDataSetChanged();
                        cancelAlarm(rowId);
                    }
                });
                if (title.toLowerCase().matches("call\\s[^\\s]+.*")) { // starts with 'call' todo
                    final String tel = title.substring(5);
                    builder.setNegativeButton("Call " + tel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            Intent dial = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + tel));
                            startActivity(dial);
                        }
                    });
                }
                builder.show();
                return true;
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        System.out.println("/////////////////////////////////////////////////////// create  "+(savedInstanceState==null?"null":"NOT null"));
/*        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this); // TODO: 20/08/2015 remove
        SharedPreferences.Editor editor = preferences.edit();
        int i = preferences.getInt("numberoflaunches", 1);
        if (i < 2) {
            // This will be run only at first launch.

            i++;
            editor.putInt("numberoflaunches", i);
            editor.commit();
        }*/
        // TODO: 20/08/2015 init only if null
        // todo run only once all following?

        tabHost = (TabHost) findViewById(R.id.tabHost);

        tabHost.setup();

        TabHost.TabSpec tabSpec = tabHost.newTabSpec(I_OWE_TAB_TAG);
        tabSpec.setContent(R.id.tabIOwe);
        tabSpec.setIndicator("I owe :(");
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec(OWE_ME_TAB_TAG);
        tabSpec.setContent(R.id.tabOweMe);
        tabSpec.setIndicator("Owe me :)");
        tabHost.addTab(tabSpec);

        helper = new DBHelper(this);

        ListView iOweList = (ListView) findViewById(R.id.lstIOwe);
        ListView oweMeList = (ListView) findViewById(R.id.lstOweMe);
        String[] from = new String[]{"title", "owner", "due"};
        int[] to = new int[]{R.id.txtTitle, R.id.txtOwner, R.id.txtDueDate};
        iOweAdapter = new ListAdapter(this, R.layout.list_item, helper.getCursor(DBHelper.I_OWE_TABLE), from, to,
                SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER, DBHelper.I_OWE_TABLE);
        oweMeAdapter = new ListAdapter(this, R.layout.list_item, helper.getCursor(DBHelper.OWE_ME_TABLE), from, to,
                SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER, DBHelper.OWE_ME_TABLE);
        initListAdapter(iOweAdapter, iOweList);
        initListAdapter(oweMeAdapter, oweMeList);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.menuItemAdd) {
            String table;
            if (tabHost.getCurrentTabTag().equals(I_OWE_TAB_TAG)) {
                table = DBHelper.I_OWE_TABLE;
                System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% I OWE ADD");
            } else {
                table = DBHelper.OWE_ME_TABLE;
                System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% OWE ME ADD");
            }
            addNewItem(table);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

/*
    @Override
    public boolean onKeyDown(int keycode, KeyEvent e) {
        switch(keycode) {
            case KeyEvent.KEYCODE_MENU:
                addNewItem();
                return true;
        }

        return super.onKeyDown(keycode, e);
    }
*/

}