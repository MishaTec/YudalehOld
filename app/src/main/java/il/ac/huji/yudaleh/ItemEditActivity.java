package il.ac.huji.yudaleh;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import java.util.Date;

import com.github.jjobes.slidedatetimepicker.SlideDateTimeListener;
import com.github.jjobes.slidedatetimepicker.SlideDateTimePicker;

/**
 * Activity for adding new items and editing exiting,
 * Must be called only from {@link il.ac.huji.yudaleh.MainActivity} for a result.
 */
public class ItemEditActivity extends AppCompatActivity {
    private static final long NEW_ITEM = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_edit);

        final Intent request = getIntent();
        final Intent response = new Intent();
        final long rowId = request.getLongExtra("rowId", NEW_ITEM);

        // Redirect table name
        response.putExtra("table", request.getStringExtra("table"));

        if (rowId == NEW_ITEM) {
            // In case that add button was pressed
            //noinspection ConstantConditions
            getSupportActionBar().setTitle(getResources().getString(R.string.title_activity_item_add));
        } else {
            response.putExtra("rowId", rowId);

            final Bundle reqExtras = getIntent().getExtras();
            ((EditText) findViewById(R.id.edtTitle)).setText(reqExtras.getString("title"));
            final Date dueDate = (Date) reqExtras.getSerializable("dueDate");
            if (dueDate != null) {
                ((Button) findViewById(R.id.btnRemind)).setText(android.text.format.DateFormat.format("MM/dd/yy h:mmaa", dueDate.getTime()));
                ((CheckBox) findViewById(R.id.checkRemind)).setChecked(true);
                response.putExtra("dueDate", dueDate);
            }
            ((EditText) findViewById(R.id.edtDesc)).setText(reqExtras.getString("desc"));
            ((EditText) findViewById(R.id.edtOwner)).setText(reqExtras.getString("owner"));
        }

        final Button btnOK = (Button) findViewById(R.id.btnOK);
        btnOK.setOnClickListener(new View.OnClickListener() {

            /**
             * Sets all the extras from the corresponding fields.
             *
             * @param v current list item
             */
            public void onClick(View v) {
                String owner = ((EditText) findViewById(R.id.edtOwner)).getText().toString();
                response.putExtra("owner", owner);
                String title = ((EditText) findViewById(R.id.edtTitle)).getText().toString();
                response.putExtra("title", title);
                String desc = ((EditText) findViewById(R.id.edtDesc)).getText().toString();
                response.putExtra("desc", desc);
                if (!((CheckBox) findViewById(R.id.checkRemind)).isChecked()) {
                    // In case the extra was already set by the dialog
                    response.removeExtra("dueDate");
                }
                setResult(RESULT_OK, response);
                finish();
            }
        });
        final Button btnRemind = (Button) findViewById(R.id.btnRemind);
        btnRemind.setOnClickListener(new View.OnClickListener() {

            /**
             * Shows date-time picker and sets the dueDate extra.
             *
             * @param v current list item
             */
            public void onClick(View v) {
                SlideDateTimeListener listener = new SlideDateTimeListener() {

                    @SuppressWarnings("deprecation")
                    @Override
                    public void onDateTimeSet(Date date, long rowId) {
                        date.setSeconds(0);
                        ((Button) findViewById(R.id.btnRemind)).setText(android.text.format.DateFormat.format("MM/dd/yy h:mmaa", date.getTime()));
                        ((CheckBox) findViewById(R.id.checkRemind)).setChecked(true);
                        response.putExtra("dueDate", date);
                    }

                    @Override
                    public void onDateTimeCancel() {

                    }
                };
                new SlideDateTimePicker.Builder(getSupportFragmentManager())
                        .setListener(listener)
                        .setInitialDate(new Date())
                        .build()
                        .show();
            }
        });
        final Button btnCancel = (Button) findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setResult(RESULT_CANCELED, response);
                finish();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_item_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
/*        if (id == R.id.action_settings) {
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

}
