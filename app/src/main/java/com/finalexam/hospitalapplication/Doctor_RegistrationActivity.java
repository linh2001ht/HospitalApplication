package com.finalexam.hospitalapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class Doctor_RegistrationActivity extends AppCompatActivity {
    private TextInputLayout mName;
    private TextInputLayout mAge;
    private TextInputLayout mExperiance;
    private TextInputLayout mEducation;
    private TextInputLayout mContactNumber;
    private TextInputLayout mAddress;
    private TextInputLayout mEmail;
    private TextInputLayout mPassword;
    private Button mRegister;

    private Spinner mSpecialization, mShift;

    //RadioGroup & RadioButton
    private RadioGroup mGender;


    //Firebase Auth
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    //Database Reference
    private DatabaseReference mUserDetails = FirebaseDatabase.getInstance().getReference();

    private Toolbar mToolbar;
    private ProgressDialog mRegProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor__registration);


        // Toolbar
        mToolbar = (Toolbar) findViewById(R.id.register_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Doctor Registration");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRegProgress = new ProgressDialog(this);


        //User Details
        mName = (TextInputLayout) findViewById(R.id.reg_name_layout);
        mAge = (TextInputLayout) findViewById(R.id.reg_age_layout);
        mContactNumber = (TextInputLayout) findViewById(R.id.reg_contact_layout);
        mAddress = (TextInputLayout) findViewById(R.id.reg_address_layout);
        mEducation = findViewById(R.id.reg_education_layout);
        mExperiance = findViewById(R.id.reg_experiance_layout);
        mEmail = (TextInputLayout) findViewById(R.id.reg_email_layout);
        mPassword = (TextInputLayout) findViewById(R.id.reg_password_layout);

        mSpecialization = findViewById(R.id.specializationSpinner);
        mShift = findViewById(R.id.shiftSpinner);

        mRegister = (Button) findViewById(R.id.reg_button);

        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = mName.getEditText().getText().toString();
                String age = mAge.getEditText().getText().toString();
                String education = mEducation.getEditText().getText().toString();
                String experiance = mExperiance.getEditText().getText().toString();
                String specialization = mSpecialization.getSelectedItem().toString();
                String shift = mShift.getSelectedItem().toString();
                String contactnumber = mContactNumber.getEditText().getText().toString();
                String address = mAddress.getEditText().getText().toString();
                String email = mEmail.getEditText().getText().toString();
                String password = mPassword.getEditText().getText().toString();
                String gender = "";

                //RadioGroup
                mGender = (RadioGroup) findViewById(R.id.reg_gender_radiogroup);
                int checkedId = mGender.getCheckedRadioButtonId();

                if(checkedId == R.id.reg_male_radiobtn){
                    gender = "Male";
                }
                else if(checkedId == R.id.reg_female_radiobtn){
                    gender = "Female";
                }
                else if(checkedId == R.id.reg_other_radiobtn){
                    gender = "Other";
                }
                else {
                    Toast.makeText(getBaseContext(),"Select Gender",Toast.LENGTH_LONG).show();
                }

                if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(name) && !TextUtils.isEmpty(age) && !TextUtils.isEmpty(contactnumber) && !TextUtils.isEmpty(address)){

                    mRegProgress.setTitle("Creating Account");
                    mRegProgress.setMessage("Please Wait! We are Processing");
                    mRegProgress.setCanceledOnTouchOutside(false);
                    mRegProgress.show();


                    createAccount(name,age,gender,education, experiance, specialization, shift,contactnumber,address,email,password);

                }
                else{

                    Toast.makeText(Doctor_RegistrationActivity.this,"Please fill all field",Toast.LENGTH_LONG).show();

                }

            }
        });
    }
    private void createAccount(final String name, final String age,final String gender, final String education, final String experiance, final String specialization, final String shift, final String contactnumber, final String address, final String email, final String password) {

        mAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(Doctor_RegistrationActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(task.isSuccessful()){

                            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                            String uid = currentUser.getUid();

                            mUserDetails.child("Specialization").child(specialization).push().child("Doctor_ID").setValue(uid);

                            mUserDetails.child("User_Type").child(uid).child("Type").setValue("Doctor");

                            HashMap<String,String> userDetails = new HashMap<>();
                            userDetails.put("Name",name);
                            userDetails.put("Age",age);
                            userDetails.put("Gender",gender);
                            userDetails.put("Education",education);
                            userDetails.put("Experiance",experiance);
                            userDetails.put("Shift",shift);
                            userDetails.put("Specialization",specialization);
                            userDetails.put("Contact",contactnumber);
                            userDetails.put("Address",address);
                            userDetails.put("Email",email);
                            userDetails.put("Password",password);

                            mUserDetails.child("Doctor_Details").child(uid).setValue(userDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    mRegProgress.dismiss();
                                    Toast.makeText(Doctor_RegistrationActivity.this,"Account Successfully Created",Toast.LENGTH_SHORT).show();

                                    verifyEmail(email);

                                }
                            });


                        }
                        else {

                            mRegProgress.hide();
                            Toast.makeText(Doctor_RegistrationActivity.this,"Creating Account Failed",Toast.LENGTH_LONG).show();
                        }
                    }
                });

    }

    private void verifyEmail(final String email) {

        AlertDialog.Builder mBuiler = new AlertDialog.Builder(Doctor_RegistrationActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.verify_email, null);

        TextView userEmail = (TextView) mView.findViewById(R.id.verify_email);
        final TextView sentVerication = (TextView) mView.findViewById(R.id.verify_email_sent);
        Button verifyEmail = (Button) mView.findViewById(R.id.verify_button);
        Button continuebutton = (Button) mView.findViewById(R.id.verify_continue);

        userEmail.setText(email);

        verifyEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            sentVerication.setText("We have sent Email to "+email);

                        }
                        else {
                            sentVerication.setText("Failed to Sent Email for Verification");
                        }
                    }
                });
            }
        });

        continuebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent main_Intent = new Intent(Doctor_RegistrationActivity.this, MainActivity.class);
                startActivity(main_Intent);
            }
        });


        mBuiler.setView(mView);
        AlertDialog dialog = mBuiler.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }
}