package com.yudha.firebasedatabase;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    private DatabaseReference mDatabase;
    private String mUserId;
    private FirebaseUser mFirebaseUser;
    private FirebaseAuth mFirebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        if (mFirebaseUser == null) {
            // Jika tidak terdeteksi login, maka akan launch log in activity
            loadLogInView();
        } else {
            mUserId = mFirebaseUser.getUid();

            // Set up ListView
            final ListView listView = (ListView) findViewById(R.id.listView);
            final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1);
            listView.setAdapter(adapter);

            // Menambahkan item lewat edit text
            // Menginisialisasi button dan edit text
            final EditText text = (EditText) findViewById(R.id.todoText);
            final Button button = (Button) findViewById(R.id.addButton);
            final Button buttonLogout = (Button) findViewById(R.id.logoutButton);

            // Pemrosesan button untuk psot
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
//mDatabase.child("users").child(mUserId).child("items").push().child("title").setValue(text.getText().toString());
                    Item item = new Item(text.getText().toString());
                    mDatabase.child("users").child(mUserId).child("items").push().setValue(item);
                    text.setText("");
                }
            });

            // Button untuk proses logout
            buttonLogout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mFirebaseAuth.signOut();
                    loadLogInView();
                }
            });

            // Listener untuk merefresh data di dalam ListView.
            mDatabase.child("users").child(mUserId).child("items").addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    adapter.add((String) dataSnapshot.child("title").getValue());
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    adapter.remove((String) dataSnapshot.child("title").getValue());
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            // Menghapus item ketika di klik
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    mDatabase.child("users").child(mUserId).child("items")
                            .orderByChild("title")
                            .equalTo((String) listView.getItemAtPosition(position))
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChildren()) {
                                        DataSnapshot firstChild = dataSnapshot.getChildren().iterator().next();
                                        firstChild.getRef().removeValue();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                }
            });
        }

    }

    private void loadLogInView() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

    }
}
