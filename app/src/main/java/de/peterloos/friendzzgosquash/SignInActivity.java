package de.peterloos.friendzzgosquash;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import de.peterloos.models.User;

public class SignInActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "PeLo";

    private enum AuthenticationKind { CreateAccount, SignedIn }

    private TextView textviewStatus;
    private TextView textviewDetail;
    private EditText edittextEmail;
    private EditText edittextPassword;

    private Button buttonSignIn;
    private Button buttonCreateAccount;
    private Button buttonEnter;

    private RelativeLayout layoutWithCredentials;
    private RelativeLayout layoutNoCredentials;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference firebaseData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "SignInActivity::onCreate");

        this.setContentView(R.layout.activity_auth);

        // views
        this.textviewStatus = (TextView) findViewById(R.id.status);
        this.textviewDetail = (TextView) findViewById(R.id.detail);
        this.edittextEmail = (EditText) findViewById(R.id.field_email);
        this.edittextPassword = (EditText) findViewById(R.id.field_password);

        // buttons
        this.buttonSignIn = (Button) this.findViewById(R.id.sign_in_button);
        this.buttonCreateAccount = (Button) this.findViewById(R.id.create_account_button);
        this.buttonEnter = (Button) this.findViewById(R.id.enter_button);
        this.buttonSignIn.setOnClickListener(this);
        this.buttonCreateAccount.setOnClickListener(this);
        this.buttonEnter.setOnClickListener(this);

        // layout container
        this.layoutWithCredentials = (RelativeLayout) this.findViewById(R.id.layout_with_credentials);
        this.layoutNoCredentials = (RelativeLayout) this.findViewById(R.id.layout_enter_no_credentials);

        // initialize database access
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        // database.setPersistenceEnabled(true);    // TODO: Hat was mit Offline-Persistenz zu tun .. crashed aber, wenn nicht stringent alt ERSTES in der App aufgerufen !!!
        this.firebaseData = database.getReference();

        // initialize authentication
        this.firebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "SignInActivity::onStart");

        // check authentication state on activity start
        FirebaseUser user = this.firebaseAuth.getCurrentUser();
        this.updateUI(user);
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "SignInActivity::onStop");
    }

    @Override
    public void onClick(View view) {
        if (view == this.buttonCreateAccount) {
            String email = this.edittextEmail.getText().toString();
            String password = this.edittextPassword.getText().toString();
            this.createAccount(email, password);
        } else if (view == this.buttonSignIn) {
            String email = this.edittextEmail.getText().toString();
            String password = this.edittextPassword.getText().toString();
            this.signIn(email, password);
        } else if (view == this.buttonEnter) {
            FirebaseUser user = this.firebaseAuth.getCurrentUser();
            if (this.firebaseAuth.getCurrentUser() != null) {
                this.onAuthSuccess(user, AuthenticationKind.SignedIn);
            } else {
                Toast.makeText(SignInActivity.this, R.string.unexpected_error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    // main authentication methods
    private void createAccount(String email, String password) {
        Log.d(TAG, "createAccount: " + email);
        if (!this.validateForm()) {
            return;
        }

        this.showProgressDialog();

        Task<AuthResult> task = this.firebaseAuth.createUserWithEmailAndPassword(email, password);

        task.addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                Log.d(TAG, "createAccount: createUserWithEmail ==> onComplete: " + task.isSuccessful());

                SignInActivity.this.hideProgressDialog();

                if (!task.isSuccessful()) {
                    Toast.makeText(SignInActivity.this, R.string.auth_failed, Toast.LENGTH_SHORT).show();
                } else {
                    SignInActivity.this.onAuthSuccess(task.getResult().getUser(), AuthenticationKind.CreateAccount);
                }
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Error message: " + e.getMessage());
                SignInActivity.this.hideProgressDialog();
                Toast.makeText(SignInActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void signIn(String email, String password) {
        Log.d(TAG, "signIn: " + email);
        if (!this.validateForm()) {
            return;
        }

        this.showProgressDialog();

        Task<AuthResult> task = firebaseAuth.signInWithEmailAndPassword(email, password);

        task.addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                Log.d(TAG, "signIn: signInWithEmailAndPassword ==> onComplete: " + task.isSuccessful());

                SignInActivity.this.hideProgressDialog();

                if (! task.isSuccessful()) {
                    Log.w(TAG, "signInWithEmailAndPassword failed", task.getException());
                    Toast.makeText(SignInActivity.this, R.string.auth_failed, Toast.LENGTH_SHORT).show();
                    SignInActivity.this.textviewStatus.setText(R.string.auth_failed);
                } else {
                    SignInActivity.this.onAuthSuccess(task.getResult().getUser(), AuthenticationKind.SignedIn);
                }
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Error message: " + e.getMessage());
                SignInActivity.this.hideProgressDialog();
                Toast.makeText(SignInActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onAuthSuccess(FirebaseUser user, AuthenticationKind kind) {

        if (kind == AuthenticationKind.CreateAccount) {
            // write new user into firebase database
            String email = user.getEmail();
            String displayName = this.getDisplayNameFromEmail(email);
            String uid = user.getUid();
            this.writeUserToDatabase(uid, email, displayName);
        }

        // switch to main activity
        Intent mainIntent = new Intent(SignInActivity.this, MainActivity.class);
        this.startActivity(mainIntent);
        this.finish();
    }

    private void writeUserToDatabase(String uid, String email, String displayName) {
        User user = new User(email, displayName);
        this.firebaseData.child("users").child(uid).setValue(user);
    }

    private String getDisplayNameFromEmail(String email) {
        if (email.contains("@")) {
            return email.split("@")[0];
        } else {
            return email;
        }
    }

    private boolean validateForm() {

        boolean valid = true;

        String email = this.edittextEmail.getText().toString();
        if (TextUtils.isEmpty(email)) {
            this.edittextEmail.setError("Required.");
            valid = false;
        } else {
            this.edittextEmail.setError(null);
        }

        String password = this.edittextPassword.getText().toString();
        if (TextUtils.isEmpty(password)) {
            this.edittextPassword.setError("Required.");
            valid = false;
        } else {
            this.edittextPassword.setError(null);
        }

        return valid;
    }

    private void updateUI(FirebaseUser user) {

        if (user != null) {
            this.layoutWithCredentials.setVisibility(View.GONE);
            this.layoutNoCredentials.setVisibility(View.VISIBLE);

            this.textviewStatus.setText(getString(R.string.emailpassword_status_fmt, user.getEmail()));
            this.textviewDetail.setText(getString(R.string.firebase_status_fmt, user.getUid()));
        } else {
            this.layoutWithCredentials.setVisibility(View.VISIBLE);
            this.layoutNoCredentials.setVisibility(View.GONE);

            this.textviewStatus.setText(R.string.signed_out);
            this.textviewDetail.setText("");
        }
    }

    private ProgressDialog progressDialog;

    // progress dialog handling
    private void showProgressDialog() {
        if (this.progressDialog == null) {
            this.progressDialog = new ProgressDialog(this);
            this.progressDialog.setMessage(this.getString(R.string.loading));
            this.progressDialog.setIndeterminate(true);
        }

        this.progressDialog.show();
    }

    private void hideProgressDialog() {
        if (this.progressDialog != null && progressDialog.isShowing()) {
            this.progressDialog.dismiss();
        }
    }
}
