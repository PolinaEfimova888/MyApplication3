package com.example.myapplication3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;


public class PersonalActivity extends AppCompatActivity implements ValueEventListener {

    GoogleSignInClient mGoogleSignInClient;

    LinearLayout ln;
    EditText tv_place;
    EditText tv_first_koord;
    EditText tv_sec_koord;
    TextView name, mail, tv_message;
    ImageView ava;
    ListView listView;

    DatabaseReference dbRef;

    int count = 0;

    ArrayList<String> points = new ArrayList<>();;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ln = (LinearLayout) findViewById(R.id.profile_lin);

        listView =(ListView) findViewById(R.id.listview);

        name = findViewById(R.id.name);
        mail = findViewById(R.id.mail);
        tv_message = findViewById(R.id.message);
        ava = findViewById(R.id.avatar);
        tv_place = findViewById(R.id.place);
        tv_first_koord = findViewById(R.id.first_koord);
        tv_sec_koord = findViewById(R.id.sec_koord);

        dbRef = FirebaseDatabase.getInstance().getReferenceFromUrl("https://my-app-1d6df-default-rtdb.firebaseio.com");

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                count =  (int)snapshot.child("places").getChildrenCount() + 1;

                for (DataSnapshot s : snapshot.child("places").getChildren()) {
                    Point dbpoint = s.getValue(Point.class);
                    String newpoint = dbpoint.bus_stop+Double.toString(dbpoint.first_location)+Double.toString(dbpoint.sec_location);

                    points.add(newpoint);
                    updateUI();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("mytag", "Failed to read value");
            }
        });

    }

    public void updateUI() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_1, points);
        listView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                count =  (int)snapshot.child("places").getChildrenCount() + 1;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("mytag", "Failed to read value");
            }
        });

    }

    public void onSend(View v) {
        String place = tv_place.getText().toString();
        String first_location = tv_first_koord.getText().toString();
        String second_location = tv_sec_koord.getText().toString();

        if (!place.isEmpty() && !first_location.isEmpty() && !second_location.isEmpty()) {
            Point new_p = new Point(place, Double.parseDouble(first_location), Double.parseDouble(second_location));
            dbRef.child("places").child(String.valueOf(count)).setValue(new_p);
        }
    }

    @Override
    public void onDataChange(@NonNull DataSnapshot snapshot) {
        Point place = snapshot.getValue(Point.class);
        Log.d("mytag", "place: " + place);
    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {
    }

    @Override
    public void onStart() {
        super.onStart();
        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(this);

        name.setText(signInAccount.getDisplayName());
        mail.setText(signInAccount.getEmail());
        Uri url = signInAccount.getPhotoUrl();

        Picasso p = new Picasso.Builder(getApplicationContext()).build();
        p.load(url).into(ava);
    }

    public void onGoogleSignOut(View v) {
        FirebaseAuth.getInstance().signOut();

        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(this.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mGoogleSignInClient.signOut();

        Intent intent = new Intent(ProfileActivity.this,MainActivity.class);
        startActivity(intent);
    }
}