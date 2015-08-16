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

import com.github.jjobes.slidedatetimepicker.SlideDateTimeListener;
import com.github.jjobes.slidedatetimepicker.SlideDateTimePicker;

import java.util.Date;


public class ItemEditActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_edit);


        final Intent result = new Intent();
        final long id = getIntent().getLongExtra("id",-1);
        if(id==-1){
            getSupportActionBar().setTitle(getResources().getString(R.string.title_activity_item_add));
        }
        else{
            result.putExtra("id",id);
        }
        System.out.println("******************************************** update id: "+id);
/*        TabHost tabHost = (TabHost) findViewById(R.id.tabHost);

        tabHost.setup();

        TabHost.TabSpec tabSpec = tabHost.newTabSpec("setDate");
        tabSpec.setContent(R.id.tabSetDate);
        tabSpec.setIndicator("Set Date");
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("setTime");
        tabSpec.setContent(R.id.tabSetTime);
        tabSpec.setIndicator("Set Time");
        tabHost.addTab(tabSpec);todo remove*/

        final Button btnOK = (Button) findViewById(R.id.btnOK);
        btnOK.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String owner = ((EditText) findViewById(R.id.edtOwner)).getText().toString();
                result.putExtra("owner", owner);
                String title = ((EditText) findViewById(R.id.edtOwner)).getText().toString();
                result.putExtra("title", title);
                String desc = ((EditText) findViewById(R.id.edtOwner)).getText().toString();
                result.putExtra("desc", desc);
                if (!((CheckBox) findViewById(R.id.checkRemind)).isChecked()){
                    result.removeExtra("dueDate");
                }
                /*DatePicker dp = (DatePicker) findViewById(R.id.datePicker);
                TimePicker tp = (TimePicker) findViewById(R.id.timePicker);
                Calendar cal = Calendar.getInstance();
                cal.set(dp.getYear(), dp.getMonth(), dp.getDayOfMonth(), tp.getCurrentHour(), tp.getCurrentMinute(), 0);
                result.putExtra("dueDate", cal.getTime()); // send java.util.Date todo remove*/
                setResult(RESULT_OK, result);
                finish();
            }
        });

        final Button btnCancel = (Button) findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setResult(RESULT_CANCELED, result);
                finish();
            }
        });

        final Button btnRemind = (Button) findViewById(R.id.btnRemind);
        btnRemind.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SlideDateTimeListener listener = new SlideDateTimeListener() {

                    @Override
                    public void onDateTimeSet(Date date, long rowId) {
                        /*todo  adapter.changeCursor(helper.update(id));adapter.notifyDataSetChanged();*/
                        System.out.println("******************* Date set, id:" + rowId); // todo  remove
                        ((Button) findViewById(R.id.btnRemind)).setText(android.text.format.DateFormat.format("MM/dd/yy h:mmaa", date.getTime()));
                        ((CheckBox) findViewById(R.id.checkRemind)).setChecked(true);
                        result.putExtra("dueDate", date);
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
