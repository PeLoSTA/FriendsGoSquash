package de.peterloos.friendzzgosquash;

// TODO: Die ganze SettingsActivity schlägt fehl, wenn man sie aufruft, aber kein USER eingelogged ist ?!?!?!?
// TODO: Splash Screen:
// TODO: Hmmm, m�glicherweise komplizierter als gedacht:
// TODO: Google: "Android Splash Screen"
// TODO:         Meine Vorlage:
// TODO:         https://www.bignerdranch.com/blog/splash-screens-the-right-way/

// ==========================================================


import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.peterloos.models.Message;
import de.peterloos.models.User;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "PeLo";

    private static final int SETTINGS_INTENT_REQUEST_ID = 1;

    private static final String CHILD_USERS = "users";
    private static final String CHILD_MESSAGES = "messages";

    private FirebaseDatabase database;
    private DatabaseReference referenceMessages;

    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;

    private RecyclerView recyclerView;
    private FirebaseMessagesRecyclerAdapter recyclerViewAdapter;
    private LinearLayoutManager layoutManager;

    // watching for changes of current user
    private CurrentUserListener listener;
    private DatabaseReference currentUser;

    private Button buttonSend;
    private Button buttonNewUser;
    private Button buttonRead;
    private Button buttonWrite;

    private TextView textviewHeader;
    private EditText edittextMessage;

    // footprint of current user
    private String userUID;
    private String userEMailAddress;
    private String userDisplayName;
    private String userPhotoUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);

        Log.d(TAG, "MainActivity::onCreate");

        // setup event handler
        this.buttonSend = (Button) this.findViewById(R.id.button_send);
        this.buttonSend.setOnClickListener(this);
        this.buttonNewUser = (Button) this.findViewById(R.id.button_adding_a_new_user);
        this.buttonNewUser.setOnClickListener(this);
        this.buttonRead = (Button) this.findViewById(R.id.button_read);
        this.buttonRead.setOnClickListener(this);
        this.buttonWrite = (Button) this.findViewById(R.id.button_write);
        this.buttonWrite.setOnClickListener(this);

        this.textviewHeader = (TextView) this.findViewById(R.id.textview_header);
        this.edittextMessage = (EditText) this.findViewById(R.id.edittext_message);

        this.database = FirebaseDatabase.getInstance();
        this.referenceMessages = this.database.getReference(CHILD_MESSAGES);

        // initialize authentication
        this.auth = FirebaseAuth.getInstance();
        this.authStateListener = new UserAuthentication();

        this.recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        this.recyclerViewAdapter = new FirebaseMessagesRecyclerAdapter(
            Message.class,
            R.layout.messages_list_row,
            MessageViewHolder.class,
            this.referenceMessages);

        this.recyclerViewAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);

                int messagesCount = MainActivity.this.recyclerViewAdapter.getItemCount();
                int lastVisiblePosition = MainActivity.this.layoutManager.findLastCompletelyVisibleItemPosition();

                // if the recycler view is initially being loaded or the user is at the bottom of the list,
                // scroll to the bottom of the list to show the newly added message
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (messagesCount - 1) && lastVisiblePosition == (positionStart - 1))) {
                    MainActivity.this.recyclerView.scrollToPosition(positionStart);
                }
            }
        });

        this.layoutManager = new LinearLayoutManager(this.getApplicationContext());
        this.layoutManager.setStackFromEnd(true);

        this.recyclerView.setLayoutManager(layoutManager);
        this.recyclerView.setItemAnimator(new DefaultItemAnimator());
        this.recyclerView.setAdapter(this.recyclerViewAdapter);
        this.recyclerViewAdapter.notifyDataSetChanged();

        this.textviewHeader.setText("");

        this.listener = new CurrentUserListener();
        this.currentUser = null;

        this.userUID = "";
        this.userDisplayName = "";
        this.userPhotoUrl = "";
    }

    private class UserAuthentication implements FirebaseAuth.AuthStateListener {

        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

            FirebaseUser user = firebaseAuth.getCurrentUser();

            if (user != null) {

                // retrieve current users data
                MainActivity.this.userUID = user.getUid();
                MainActivity.this.userEMailAddress = user.getEmail();
                MainActivity.this.userDisplayName = user.getDisplayName();
                MainActivity.this.userPhotoUrl = (user.getPhotoUrl() != null) ? user.getPhotoUrl().toString() : "";

                String s = String.format("MainActivity::onAuthStateChanged ==> User %s signed in [%s]",
                    MainActivity.this.userEMailAddress, MainActivity.this.userUID);
                Log.d(TAG, s);

                // listen for value events of this user
                if (MainActivity.this.currentUser != null) {
                    MainActivity.this.currentUser.removeEventListener(MainActivity.this.listener);
                }

                MainActivity.this.currentUser = MainActivity.this.database.getReference().child(CHILD_USERS).child(userUID);
                MainActivity.this.currentUser.addValueEventListener(MainActivity.this.listener);

            } else {

                // user has signed out
                Log.d(TAG, "MainActivity::onAuthStateChanged ==> No User signed in or User has logged off !!!");

                MainActivity.this.userUID = "";
                MainActivity.this.userEMailAddress =  "";
                MainActivity.this.userDisplayName = "";
                MainActivity.this.userPhotoUrl = "";

                MainActivity.this.onBackPressed();
            }
        }
    }

    private class CurrentUserListener implements ValueEventListener {

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            User tempUser = dataSnapshot.getValue(User.class);

            // update informations of this user
            if (tempUser != null && !(tempUser.getDisplayName() == null || tempUser.getDisplayName().equals(""))) {

                MainActivity.this.userDisplayName = tempUser.getDisplayName();
                MainActivity.this.textviewHeader.setText("Hallo " + tempUser.getDisplayName());
                MainActivity.this.userPhotoUrl = tempUser.getPhotoUrl();


            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

            // getting current user failed, log a message
            Log.w(TAG, "CurrentUserListener::onDataChange:onCancelled", databaseError.toException());
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "MainActivity::onStart");

        this.auth.addAuthStateListener(this.authStateListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "MainActivity::onStop");

        if (this.currentUser != null) {
            this.currentUser.removeEventListener(this.listener);
            this.currentUser = null;
        }

        this.auth.removeAuthStateListener(this.authStateListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "MainActivity::onDestroy");

        // let adapter stop listening for changes in the Firebase database
        this.recyclerViewAdapter.cleanup();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.menu_extras, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_sign_out) {

            // if done after invocation of 'signOut', "Firebase Permission Errors" are generated
            if (this.currentUser != null) {
                this.currentUser.removeEventListener(this.listener);
                this.currentUser = null;
            }

            this.auth.signOut();
            return true;
        } else if (id == R.id.action_settings) {

            Intent intent = new Intent(this, SettingsActivity.class);
            this.startActivityForResult(intent, SETTINGS_INTENT_REQUEST_ID);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        Intent intent = new Intent(this, SignInActivity.class);
        this.startActivity(intent);
        this.finish();
    }

    @Override
    public void onClick(View view) {

        if (view == this.buttonSend) {

            String text = this.edittextMessage.getText().toString();
            Log.v(TAG, "sending message: " + text);

            Message message = new Message(this.userUID, this.userDisplayName, text, this.userPhotoUrl);
            this.referenceMessages.push().setValue(message);
            this.edittextMessage.setText("");
            Log.v(TAG, "done ....");
        } else if (view == this.buttonNewUser) {
            Log.v(TAG, "Bla");
        } else if (view == this.buttonRead) {
            Log.v(TAG, "Bla");
        } else if (view == this.buttonWrite) {
            Log.v(TAG, "Bla");
        }
    }
}
