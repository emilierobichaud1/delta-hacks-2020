package app.hackathon.com.deltahacks2020;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class SignupActivity extends AppCompatActivity {
    public static final String userId = "userId";
    public static final String MyPREFERENCES = "MyPrefs";
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private Button signUpButton;
    private Button cancelButton;
    private EditText userText;
    private EditText emailText;
    private EditText passwordText;
    private EditText passwordConfText;
    private EditText nameText;
    private FirebaseUser user;
    private User currentUser;
    private String node;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();


        //TODO add names of buttons and text fields
        //Get parts of the layout
        signUpButton = (Button) findViewById(R.id.signUpButton);
        cancelButton = (Button) findViewById(R.id.cancelButton);
        userText = (EditText) findViewById(R.id.usernameEditText);
        emailText = (EditText) findViewById(R.id.emailEditText);
        nameText = (EditText) findViewById(R.id.nameEditText);

        progressDialog = new ProgressDialog(this);


        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
                //setContentView(R.layout.activity_signup_preferences);

            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setContentView(R.layout.activity_home);

            }
        });
    }

    private void registerUser() {
        //Get info from fields
        String username = userText.getText().toString().trim();
        String email = emailText.getText().toString().trim();
        String password = passwordText.getText().toString().trim();
        String passwordConf = passwordConfText.getText().toString().trim();
        String name = nameText.getText().toString().trim();
        Pattern p = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(username);
        Matcher m2 = p.matcher(password);
        boolean b = m.find();
        boolean b2 = m2.find();

        //Error check user input here
        if (TextUtils.isEmpty(username)) {
            Toast.makeText(this, "Please enter username", Toast.LENGTH_SHORT).show();  //Toast is popup msg at bottom
            return; //Return to stop registration
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(passwordConf)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(this, "Passwords needs to be 6 characters or more", Toast.LENGTH_SHORT).show();
            return;
        }
        //Progress bar (Since registering online might take a while)
        progressDialog.setMessage("Registering User");
        progressDialog.show();

        //Create user in database
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(SignupActivity.this, "Registered Successfully", Toast.LENGTH_SHORT).show();
                    FirebaseUser user = mAuth.getCurrentUser();
                    saveUserInfo(user.getUid());    //add properties to database
                    updateView(null);

                } else {
                    Toast.makeText(SignupActivity.this, "Unsuccessful registration. Please try again.", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }


    //Adds user properties to database
    private void saveUserInfo(String userId) {
        String name = nameText.getText().toString().trim();
        String email = emailText.getText().toString().trim();
        String username = userText.getText().toString().trim();
        //TODO figure out how to process paypal user
        //PayPalUser userinfo = new PayPalUser();

        //Create user object to pass into database call
        currentUser = new User(name, email, userinfo);
        //currentUser.addEvent("");

        //add users/ to front of node name to keep database easily searchable
        node = "users/" + userId;

        //Creates new node in database and saves data
        myRef.child(node).setValue(currentUser);

        //Save username to username list (for login with username)
        String usernameList = "users/usernames/" + username;
        myRef.child(usernameList).setValue(email);
    }

    public void cancel(View view) {
        //go back to main page when cancel is pressed

        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);

    }

    //method id called upon sucessful registration
    public void updateView(View view) {
        //TODO change screen to go to charity chooser next
        //Intent intent = new Intent(this, UserPreferencesActivity.class);
        //startActivity(intent);
    }
}
