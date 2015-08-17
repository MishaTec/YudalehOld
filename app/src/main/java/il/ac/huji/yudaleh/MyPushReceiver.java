package il.ac.huji.yudaleh;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.parse.ParsePushBroadcastReceiver;


/**
 * Created by Michael on 17/08/2015.
 */
public class MyPushReceiver extends ParsePushBroadcastReceiver {
    protected Class<? extends Activity> getActivity(Context context,
                                                    Intent intent) {
        return MainActivity.class;
    }
}
