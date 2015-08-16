package il.ac.huji.yudaleh;

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
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;

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

            TextView titleText = (TextView) view.findViewById(R.id.txtTitle);
            Cursor item = (Cursor) getItem(position);
            titleText.setText(item.getString(DBHelper.TITLE_COLUMN_INDEX));
            titleText.setTextColor(Color.BLACK);

            TextView dueDateText = (TextView) view.findViewById(R.id.txtTodoDueDate);
            if (item.isNull(DBHelper.DUE_COLUMN_INDEX)) {
                dueDateText.setText("No reminder");
                dueDateText.setTextColor(Color.BLACK);
            } else {
                Date dueDate = new Date(item.getLong(DBHelper.DUE_COLUMN_INDEX));
                dueDateText.setText(android.text.format.DateFormat.format("MM/dd/yy h:mmaa", dueDate.getTime()));
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
         * @param dueDate
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
     * Opens a dialog for adding a new to-do
     *
     * @return true
     */
    public void addNewItem() {
        Intent intent = new Intent(this, ItemEditActivity.class);
        startActivityForResult(intent, NEW_ITEM_REQUEST);
    }

    /**
     * Opens a dialog for adding a new to-do
     *
     * @param rowId
     * @return true
     */
    public void updateItem(long rowId) {
        Intent intent = new Intent(this, ItemEditActivity.class);
        intent.putExtra("rowId",rowId);
/*
        Bundle options = new Bundle();
        options.putLong("rowId",rowId);
*/
        startActivityForResult(intent, UPDATE_ITEM_REQUEST);
    }

    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent data) {
        if (resCode != Activity.RESULT_OK){
            return;
        }
        if (reqCode == NEW_ITEM_REQUEST) {
            // Add the item to DB
            String title = data.getStringExtra("title");
            Date dueDate = (Date) data.getSerializableExtra("dueDate");
            if (!title.equals("")) {
                helper.insert(title, dueDate);
                adapter.changeCursor(helper.getCursor());
                adapter.notifyDataSetChanged();
                if (dueDate != null) {
                    setAlarm(1, dueDate.getTime());
                }
            }
        }
        if (reqCode == UPDATE_ITEM_REQUEST) {
            // Update the item in DB
            long id = data.getLongExtra("id",NO_ID_PASSED);
            String title = data.getStringExtra("title");
            Date dueDate = (Date) data.getSerializableExtra("dueDate");
            if (!title.equals("")) {
                helper.update(id, title, dueDate);
                adapter.changeCursor(helper.getCursor());
                adapter.notifyDataSetChanged();
                if (dueDate != null) {
                    setAlarm(1, dueDate.getTime());
                }
            }
            System.out.println(id);
        }
    }

    /**
     * Sets a new notification alarm
     *
     * @param alarmId           - must be unique
     * @param timeInMillis - should be from calendar
     */
    private void setAlarm(int alarmId, long timeInMillis) {
        Intent alertIntent = new Intent(this, DueDateAlarm.class);
        alertIntent.setData(Uri.parse("timer:" + alarmId));

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timeInMillis);

        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), PendingIntent.getBroadcast(
                this, 1, alertIntent, PendingIntent.FLAG_UPDATE_CURRENT));

        Toast.makeText(
                this,
                "Reminders added to the calendar successfully for "
                        + android.text.format.DateFormat.format(
                        "MM/dd/yy h:mmaa",
                        cal.getTimeInMillis()),
                Toast.LENGTH_LONG).show(); // todo remove
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        helper = new DBHelper(this);

        ListView todoList = (ListView) findViewById(R.id.lstTodoItems);
        String[] from = new String[]{"title", "due"};
        int[] to = new int[]{R.id.txtTitle, R.id.txtTodoDueDate};
        adapter = new TodoListAdapter(this, R.layout.list_item, helper.getCursor(), from, to,
                SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        todoList.setAdapter(adapter);

        todoList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> av, View v, int pos, final long id) { // id = pos + 1
                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                String title = ((TextView) v.findViewById(R.id.txtTitle)).getText().toString();
                builder.setMessage(title);
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        adapter.changeCursor(helper.delete(id));
                        adapter.notifyDataSetChanged();
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
                        updateItem(id);
                    }
                });
                builder.show();
                return true;
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

        //noinspection SimplifiableIfStatement
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
