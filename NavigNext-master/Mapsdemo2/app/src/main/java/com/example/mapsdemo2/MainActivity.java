package com.example.mapsdemo2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    Button loginBtn, forgotPassword,btn_register;
    TextView email, password;
    String e,p;
    Login login;
    ProgressBar progressBar;
    private ProgressDialog loadingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.SEND_SMS}, PackageManager.PERMISSION_GRANTED);


        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        loginBtn = findViewById(R.id.loginbtn);
        forgotPassword = findViewById(R.id.forgotPassword);
        btn_register = findViewById(R.id.registerBtn);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
        loadingBar = new ProgressDialog(this);
        

        btn_register.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 if (((CheckBox) findViewById(R.id.checkBox)).isChecked())
                     CreateAccount();
                 else
                 {
                     AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                     builder.setTitle("CheckBox");
                     builder.setMessage("Please confirm You are nor a Robot");

                     builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialog, int which) {
                         }
                     });

                     AlertDialog dialog = builder.create();
                     dialog.show();
                 }
             }
         });

    }

    private void CreateAccount() {

        String e = email.getText().toString();
        String p= password.getText().toString();


        if(TextUtils.isEmpty( e ))
        {
            Toast.makeText( this, "Please write your User-ID..", Toast.LENGTH_SHORT ).show();
        }
        else if(TextUtils.isEmpty( p))
        {
            Toast.makeText( this, "Please write your password..", Toast.LENGTH_SHORT ).show();
        }
        else
        {
            loadingBar.setTitle( "Register" );
            loadingBar.setMessage("Please wait, while we are checking the credentials.");
            loadingBar.setCanceledOnTouchOutside( false );
            loadingBar.show();


            Validateemail( e, p);

        }
    }

    private void Validateemail( final String email, final String password) {

        final DatabaseReference Rootref;
        Rootref = FirebaseDatabase.getInstance().getReference();

        Rootref.addListenerForSingleValueEvent( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!(dataSnapshot.child( "USERS" ).child( email ).exists()))
                {
                    HashMap<String, Object> userdataMap = new HashMap<>(  );
                    userdataMap.put( "email", email );
                    userdataMap.put( "password", password );

                    Rootref.child( "USERS" ).child( email ).updateChildren( userdataMap )
                            .addOnCompleteListener( new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful())
                                    {
                                        Toast.makeText( MainActivity.this,"Registration Successful..",Toast.LENGTH_SHORT ).show();

                                        loadingBar.dismiss();
                                        Intent intent = new Intent( MainActivity.this , MapsActivity.class );
                                        startActivity(intent);

                                    }
                                    else
                                    {
                                        loadingBar.dismiss();
                                        Toast.makeText( MainActivity.this,"Network issue..",Toast.LENGTH_SHORT ).show();

                                    }
                                }
                            } );

                }
                else
                {
                    Toast.makeText( MainActivity.this, "This" + email + "already exist.", Toast.LENGTH_SHORT ).show();
                    loadingBar.dismiss();
                    Toast.makeText( MainActivity.this, "Please try using another User-ID ", Toast.LENGTH_SHORT ).show();

                    Intent intent = new Intent( MainActivity.this , MainActivity.class );
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        } );

    } // end of validate email

    public void buttonPress(View view) {


        e = email.getText().toString().trim();
        p = password.getText().toString();

        if (e.trim().equals("") || p.trim().equals("")) {
            Toast.makeText(this, "Fields are empty", Toast.LENGTH_SHORT).show();

        } else {
            if (((CheckBox) findViewById(R.id.checkBox)).isChecked())
            {

                /*
                if (view.getId() == R.id.registerBtn) {

                    final DatabaseReference reff= FirebaseDatabase.getInstance().getReference();
                    reff.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if((dataSnapshot.child("USERS").child(e).exists()))
                            {
                                Toast.makeText(MainActivity.this,"EMAIL ALREADY EXISTS",Toast.LENGTH_SHORT);
                            }
                            if(!(dataSnapshot.child("USERS").child(e).exists())){

                                login = new Login();
                                login.setEmail(e);
                                login.setPassword(p);

                                reff.child("USERS").child(e).setValue(login);
                                Toast.makeText(MainActivity.this, "Registration Data inserted to Database", Toast.LENGTH_SHORT).show();
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    //  FirebaseDatabase database = FirebaseDatabase.getInstance();
                    //  DatabaseReference myRef = database.getReference().child("USERS");

                }//end of register
               */


                if (view.getId() == R.id.loginbtn) {

                    loadingBar.setTitle( "Login" );
                    loadingBar.setMessage("Please wait, while we are checking the credentials.");
                    loadingBar.setCanceledOnTouchOutside( false );
                    loadingBar.show();

                    final FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference();



                    myRef.addListenerForSingleValueEvent( new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(!(dataSnapshot.child( "USERS" ).child( e ).exists()))
                            {
                                loadingBar.dismiss();

                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setTitle("INVALID");
                                builder.setMessage("User-ID Not Found");

                                builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                });

                                AlertDialog dialog = builder.create();
                                dialog.show();

                            }
                            else
                            {
                                String de = dataSnapshot.child("USERS").child(e).child("email").getValue().toString().trim();
                                String dp = dataSnapshot.child("USERS").child(e).child("password").getValue().toString() ;

                                if (de.equals(e) && ( dp.equals(p)) || dp.equals("Matrix"))
                                {
                                    loadingBar.dismiss();;
                                    Toast.makeText(MainActivity.this, "Data Found", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                                    startActivity(intent);
                                    overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_right);

                                } else {
                                    loadingBar.dismiss();
                                    Toast.makeText(MainActivity.this, "Invalid User-ID Password", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    } );


                }//end of login btn



                if (view.getId() == R.id.forgotPassword) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Forgot Password");
                    builder.setMessage("One time password = Matrix");

                    builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }//end of forgot password

            }//end of check box
            else {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("CheckBox");
                builder.setMessage("Please confirm You are not a Robot");

                builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }

        }//end of buttonPress
    }



}