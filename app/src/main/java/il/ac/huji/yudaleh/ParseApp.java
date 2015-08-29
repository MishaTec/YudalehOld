package il.ac.huji.yudaleh;

/*import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.PushService;*/
import android.app.Application;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseUser;
import com.parse.PushService;

public class ParseApp extends Application {

  public ParseApp() {
  }

  @Override
  public void onCreate() {
    super.onCreate();

    Parse.enableLocalDatastore(this);

	// Initialize the Parse SDK.
	Parse.initialize(this, "1wOBsSzT94l7KHNBFfofmIg0VvpAVVO2o9K7GXoF", "M6unV2mvfdN7e24AnoJ9GTE67YjWTf0jZI7Ky3LZ");


    ParseUser.enableAutomaticUser();
    ParseACL defauAcl = new ParseACL();

    defauAcl.setPublicReadAccess(true);
    ParseACL.setDefaultACL(defauAcl, true);

	// Specify an Activity to handle all pushes by default.
	//PushService.setDefaultPushCallback(this, MainActivity.class);// FIXME: 28/08/2015

//    ParseInstallation.getCurrentInstallation().saveInBackground();todo remove
  }
}