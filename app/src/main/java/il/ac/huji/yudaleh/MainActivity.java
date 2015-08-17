package il.ac.huji.yudaleh;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.SaveCallback;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
//16:29 mic
/**
 * Todo:
 * - read DB onCreate - only query once!
 * - add push notification receiver
 * - prevent onCreate after add/edit
 * - add alarm cancel
 * - parse broadcast receiver nullpointerexception
 */
public class MainActivity extends AppCompatActivity {
    private static final int NEW_ITEM_REQUEST = 42;
    private static final int UPDATE_ITEM_REQUEST = 43;
    private static final long NO_ID_PASSED = -22;
    private TodoListAdapter adapter;
    private DBHelper helper;

    /**
     * Inner class: cursor adapter between the database and the to-do list
     */
    private class TodoListAdapter extends SimpleCursorAdapter {
        public TodoListAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.list_item, null);
            Cursor item = (Cursor) getItem(position);

            TextView titleText = (TextView) view.findViewById(R.id.txtTitle);
            titleText.setText(item.getString(DBHelper.TITLE_COLUMN_INDEX));
            titleText.setTextColor(Color.BLACK);

            TextView ownerText = (TextView) view.findViewById(R.id.txtOwner);
            ownerText.setText(item.getString(DBHelper.OWNER_COLUMN_INDEX));
            ownerText.setTextColor(Color.BLACK);

            TextView dueDateText = (TextView) view.findViewById(R.id.txtTodoDueDate);
            if (item.isNull(DBHelper.DUE_COLUMN_INDEX)) {
                dueDateText.setText("No reminder");
                dueDateText.setTextColor(Color.BLACK);
            } else {
                Date dueDate = new Date(item.getLong(DBHelper.DUE_COLUMN_INDEX));
                String format = "kk:mm MM/dd yyyy ";
                Calendar now = Calendar.getInstance();
                if(dueDate.getYear() == now.getTime().getYear()){
                    format = "kk:mm MM/dd";
                }
                dueDateText.setText(android.text.format.DateFormat.format(format, dueDate.getTime()));
                dueDateText.setTextColor(Color.BLACK);
                if (isOverdue(dueDate)) {
                    dueDateText.setTextColor(Color.RED);
                    titleText.setTextColor(Color.RED);
                }
            }
            return view;
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
    public void addNewItem() {
        Intent intent = new Intent(this, ItemEditActivity.class);
        startActivityForResult(intent, NEW_ITEM_REQUEST);
    }

    /**
     * Opens a dialog for adding a new to-do
     *
     * @param rowId the rowId field from the DB
     * @param pos   the actual index in the list (starts from 0)
     */
    public void updateItem(long rowId, int pos) {
        System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&***************************** rowId=" + rowId + ", pos=" + pos);
        Intent intent = new Intent(this, ItemEditActivity.class);
        intent.putExtra("rowId", rowId);
        Cursor item = helper.getItem(rowId); // todo remove - inefficient
//        Cursor item = (Cursor) adapter.getItem(pos);
        intent.putExtra("title", item.getString(DBHelper.TITLE_COLUMN_INDEX));
        if (!item.isNull(DBHelper.DUE_COLUMN_INDEX)) {
            intent.putExtra("dueDate", new Date(item.getLong(DBHelper.DUE_COLUMN_INDEX)));
        }
        intent.putExtra("desc", item.getString(DBHelper.DESCRIPTION_COLUMN_INDEX));
        intent.putExtra("owner", item.getString(DBHelper.OWNER_COLUMN_INDEX));
        startActivityForResult(intent, UPDATE_ITEM_REQUEST);
    }

    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent data) {
        if (resCode != Activity.RESULT_OK) {
            return;
        }
        if (reqCode == NEW_ITEM_REQUEST) { // Add the item to DB
            String title = data.getStringExtra("title");
            Date dueDate = (Date) data.getSerializableExtra("dueDate");
            String desc = data.getStringExtra("desc");
            String owner = data.getStringExtra("owner");
            if (!title.equals("")) {
                long newRowId = helper.insert(title, dueDate, desc, owner);
                adapter.changeCursor(helper.getCursor());
                adapter.notifyDataSetChanged();
                if (dueDate != null) {
                    setAlarm(newRowId, dueDate.getTime()); //fixme
                }
            }
        }
        if (reqCode == UPDATE_ITEM_REQUEST) { // Update the item in DB
            long rowId = data.getLongExtra("rowId", NO_ID_PASSED);
            String title = data.getStringExtra("title");
            Date dueDate = (Date) data.getSerializableExtra("dueDate");
            String desc = data.getStringExtra("desc");
            String owner = data.getStringExtra("owner");
            if (!title.equals("")) {
                helper.update(rowId, title, dueDate, desc, owner);
                adapter.changeCursor(helper.getCursor());
                adapter.notifyDataSetChanged();
                if (dueDate != null) {
                    setAlarm(rowId, dueDate.getTime());
                } else {
                    cancelAlarm(rowId);
                }
            }
        }
    }

    /**
     * Sets a new notification alarm.
     *
     * @param alarmId      must be unique
     * @param timeInMillis should be from calendar
     */
    private void setAlarm(long alarmId, long timeInMillis) {
        Intent alertIntent = new Intent(this, DueDateAlarm.class);
        alertIntent.setData(Uri.parse("timer:" + alarmId));

        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, timeInMillis, PendingIntent.getBroadcast(
                this, (int) alarmId, alertIntent, PendingIntent.FLAG_UPDATE_CURRENT)); // todo 1?

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
        am.cancel(PendingIntent.getBroadcast(this, (int) alarmId, alertIntent, PendingIntent.FLAG_UPDATE_CURRENT)); // todo 1?

        Toast.makeText(this, "REMOVED Reminder  " + alarmId, Toast.LENGTH_LONG).show(); // todo remove
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        int i = preferences.getInt("numberoflaunches", 1);
        if (i < 2) {
            // todo
            // This will be run only at first launch:
            Parse.initialize(this, "1wOBsSzT94l7KHNBFfofmIg0VvpAVVO2o9K7GXoF", "M6unV2mvfdN7e24AnoJ9GTE67YjWTf0jZI7Ky3LZ");
//        PushService.setDefaultPuchCallback(this, MainActivity.class); fixme
            ParseInstallation.getCurrentInstallation().saveInBackground();
            ParsePush.subscribeInBackground("", new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        Log.d("com.parse.push", "successfully subscribed to the broadcast channel.");
                        System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&& parse:");
                        System.out.println("subscribed");
                    } else {
                        Log.e("com.parse.push", "failed to subscribe for push", e);
                        System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&& parse:");
                        System.out.println(e.getMessage());
                    }
                }
            });

            i++;
            editor.putInt("numberoflaunches", i);
            editor.commit();
        }


        // todo run only once all following?
        helper = new DBHelper(this);

        ListView todoList = (ListView) findViewById(R.id.lstTodoItems);
        String[] from = new String[]{"title","owner", "due"};
        int[] to = new int[]{R.id.txtTitle, R.id.txtOwner, R.id.txtTodoDueDate};
        adapter = new TodoListAdapter(this, R.layout.list_item, helper.getCursor(), from, to,
                SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        todoList.setAdapter(adapter);

        todoList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> av, View v, final int pos, final long rowId) { // rowId = pos + 1
                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                String title = ((TextView) v.findViewById(R.id.txtTitle)).getText().toString();
                String owner = ((TextView) v.findViewById(R.id.txtOwner)).getText().toString();
                builder.setMessage("You owe " + owner + " " + title);
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        helper.delete(rowId);
                        adapter.changeCursor(helper.getCursor());
                        adapter.notifyDataSetChanged();
                        cancelAlarm(rowId);
                    }
                });
                if (title.toLowerCase().matches("call\\s[^\\s]+.*")) { // starts with 'call'
                    final String tel = title.substring(5);
                    builder.setNegativeButton("Call " + tel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            Intent dial = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + tel));
                            startActivity(dial);
                        }
                    });
                }
                builder.setNeutralButton("Edit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        updateItem(rowId, pos);
                    }
                });
                builder.show();
            }
        });
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
            addNewItem();
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
