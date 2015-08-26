package il.ac.huji.yudaleh;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginSignupActivity extends AppCompatActivity {

    Button loginButton;
    Button signupButton;
    String usernametxt;
    String passwordtxt;
    EditText password;
    EditText username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_signup);

        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        loginButton = (Button) findViewById(R.id.login);
        signupButton = (Button) findViewById(R.id.signup);

        loginButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                usernametxt = username.getText().toString().trim();
                passwordtxt = password.getText().toString().trim();
                if (usernametxt.equals("")) {
                    username.setError("Please enter username");//it gives user to info message
                    return;
                }
                if (passwordtxt.equals("")) {
                    password.setError("Please enter password");//it gives user to info message
                    return;
                }
                ParseUser.logInInBackground(usernametxt, passwordtxt,
                        new LogInCallback() {
                            @Override
                            public void done(ParseUser user, ParseException e) {
                                if (user != null) {
                                    Intent intent = new Intent(
                                            LoginSignupActivity.this,
                                            MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(
                                            getApplicationContext(),
                                            "Wrong username or password. Try again.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
        signupButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                usernametxt = username.getText().toString().trim();
                passwordtxt = password.getText().toString().trim();
                if (usernametxt.equals("")) {
                    username.setError("Please enter username");//it gives user to info message
                    return;
                }
                if (passwordtxt.equals("")) {
                    password.setError("Please enter password");//it gives user to info message
                    return;
                }
                ParseUser user = new ParseUser();
                user.setUsername(usernametxt);
                user.setPassword(passwordtxt);
                user.signUpInBackground(new SignUpCallback() {

                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            Toast.makeText(getApplicationContext(),
                                    "Successfully Signed up!",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(),
                                    "Error: " + e.getMessage(), Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }
                });

            }
        });
    }
}