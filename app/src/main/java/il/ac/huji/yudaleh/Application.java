package il.ac.huji.yudaleh;

import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.PushService;

public class Application extends android.app.Application {

  public Application() {
  }

  @Override
  public void onCreate() {
    super.onCreate();

	// Initialize the Parse SDK.
	Parse.initialize(this, "1wOBsSzT94l7KHNBFfofmIg0VvpAVVO2o9K7GXoF", "M6unV2mvfdN7e24AnoJ9GTE67YjWTf0jZI7Ky3LZ");

	// Specify an Activity to handle all pushes by default.
	PushService.setDefaultPushCallback(this, MainActivity.class);
//    ParseInstallation.getCurrentInstallation().saveInBackground();
  }
}