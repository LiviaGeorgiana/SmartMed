package com.example.ffudulu.licenta;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import users.UserPersonalData;

public class SubmitPersonalDataPacient extends Activity {

    private EditText mPacientFirstName;
    private EditText mPacientLastName;
    private EditText mpacientCNP;
    private EditText mPacientID;
    private EditText mPacientAge;

    private String pacientFirstName = null;
    private String pacientLastName = null;
    private String pacientCNP = null;
    private String pacientID = null;
    private String pacientAge = null;

    private ImageView mPacientProfilePic;

    private ImageButton mPacientTakePhoto;
    private ImageButton mPacientUploadPhoto;

    private TextView mPacientTakePhotoLbl;
    private TextView mPacientUploadPhotoLbl;

    private Button mPacientSaveData;

    private String[] personalType = {"TBA" , "TBA"};
    private ArrayAdapter<String> adapterPersonalType;
    private Spinner mPersonalTypeSpinner;

    private FirebaseUser firebaseUser;

    private ProgressBar mProgressBarUpload;

    private Uri photoUrl;

    private DatabaseReference databaseRef;

    private static final int CAMERA_REQUEST_CODE = 1;

    private static final int SELECT_PICTURE = 1;

    private String selectedImagePath;

    private StorageReference mStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_submit_personal_data_pacient);

        //Spinner
        mPersonalTypeSpinner = (Spinner) findViewById(R.id.Pacient_spinnerFunction);
        adapterPersonalType = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, personalType);
        adapterPersonalType = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, personalType);
        adapterPersonalType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mPersonalTypeSpinner.setAdapter(adapterPersonalType);
        //END

        mPacientFirstName = (EditText) findViewById(R.id.Pacient_txtFirstName);
        mPacientLastName = (EditText) findViewById(R.id.Pacient_txtLastName);
        mpacientCNP = (EditText) findViewById(R.id.Pacient_txtCNP);
        mPacientID = (EditText) findViewById(R.id.Pacient_txtID);
        mPacientAge = (EditText) findViewById(R.id.Pacient_txtAge);

        mPacientProfilePic = (ImageView) findViewById(R.id.Pacient_ProfilePicture);

        mPacientTakePhoto = (ImageButton) findViewById(R.id.Pacient_imageButtonPhoto);
        mPacientUploadPhoto = (ImageButton) findViewById(R.id.Pacient_imageButtonUpload);

        mPacientTakePhotoLbl = (TextView) findViewById(R.id.Pacient_lblTakePhoto);
        mPacientUploadPhotoLbl = (TextView) findViewById(R.id.Pacient_lblUploadPhoto);

        mPacientSaveData = (Button) findViewById(R.id.Pacient_btnSaveChanges);


        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseRef = FirebaseDatabase.getInstance().getReference();

        mStorage = FirebaseStorage.getInstance().getReference();

        mProgressBarUpload = (ProgressBar) findViewById(R.id.Pacient_progressBarUpload);

        mPacientUploadPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,
                        "Alege poza!"), SELECT_PICTURE);
            }
        });

        mPacientSaveData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pacientFirstName = mPacientFirstName.getText().toString().trim();
                pacientLastName = mPacientLastName.getText().toString().trim();
                pacientCNP = mpacientCNP.getText().toString().trim();
                pacientID = mPacientID.getText().toString().trim();
                pacientAge =  mPacientAge.getText().toString().trim();

                if(!TextUtils.isEmpty(pacientFirstName) || !TextUtils.isEmpty(pacientLastName) ||
                        !TextUtils.isEmpty(pacientCNP) || !TextUtils.isEmpty(pacientID) ||
                        !TextUtils.isEmpty(pacientAge)){
                    UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                            .setDisplayName(pacientFirstName + " " + pacientLastName).setPhotoUri(photoUrl)
                            .build();
                    firebaseUser.updateProfile(profileUpdate)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        UserPersonalData userPacient = new UserPersonalData(
                                                pacientFirstName,pacientLastName,
                                                firebaseUser.getEmail(), pacientCNP, pacientID,
                                                pacientAge
                                        );


                                        savePersonalData(firebaseUser, userPacient);

                                        Toast.makeText(SubmitPersonalDataPacient.this,
                                                "înregistrare reușită!",
                                                Toast.LENGTH_SHORT).show();
                                        enableAll();
                                    }
                                    Intent mInitialize = new Intent(SubmitPersonalDataPacient.this, Initialization.class);
                                    startActivity(mInitialize);
                                }
                            });
                }
                else{
                    Toast.makeText(SubmitPersonalDataPacient.this, "Toate câmpurile sunt necesare!",
                            Toast.LENGTH_LONG).show();

                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                selectedImagePath = getPath(selectedImageUri);

                StorageReference filepath = mStorage.child(selectedImageUri.getLastPathSegment()).
                        child(selectedImageUri.getLastPathSegment());

                mProgressBarUpload.setVisibility(View.VISIBLE);
                disableAll();
                mPacientProfilePic.setVisibility(View.GONE);

                filepath.putFile(selectedImageUri).addOnSuccessListener(
                        new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Toast.makeText(SubmitPersonalDataPacient.this,
                                        "Încărcare reușită!" ,
                                        Toast.LENGTH_SHORT).show();
                                mProgressBarUpload.setVisibility(View.GONE);
                                photoUrl = taskSnapshot.getDownloadUrl();
                                Picasso.with(SubmitPersonalDataPacient.this).load(photoUrl)
                                        .resize(200, 200).noFade()
                                        .into(mPacientProfilePic);
                                enableAll();
                                mPacientProfilePic.setVisibility(View.VISIBLE);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
            }
        }
        if(requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK){
            Uri uri = data.getData();
        }
    }

    public String getPath(Uri uri) {
        // just some safety built in
        if( uri == null ) {
            // TODO perform some logging or show user feedback
            return null;
        }
        // try to retrieve the image from the media store first
        // this will only work for images selected from gallery
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if( cursor != null ){
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(column_index);
            cursor.close();
            return path;
        }
        // this is our fallback here
        return uri.getPath();
    }

    private void enableAll(){
        mPacientFirstName.setEnabled(true);
        mPacientLastName.setEnabled(true);
        mpacientCNP.setEnabled(true);
        mPacientID.setEnabled(true);
        mPacientAge.setEnabled(true);
        mPacientTakePhoto.setEnabled(true);
        mPacientUploadPhoto.setEnabled(true);
        mPacientTakePhotoLbl.setEnabled(true);
        mPacientUploadPhotoLbl.setEnabled(true);
        mPacientSaveData.setEnabled(true);
    }

    private void disableAll(){
        mPacientFirstName.setEnabled(false);
        mPacientLastName.setEnabled(false);
        mpacientCNP.setEnabled(false);
        mPacientID.setEnabled(false);
        mPacientAge.setEnabled(false);
        mPacientTakePhoto.setEnabled(false);
        mPacientUploadPhoto.setEnabled(false);
        mPacientTakePhotoLbl.setEnabled(false);
        mPacientUploadPhotoLbl.setEnabled(false);
        mPacientSaveData.setEnabled(false);
    }

    private void savePersonalData(FirebaseUser firebaseUser, UserPersonalData userPacient){
        databaseRef.child("Users").child("Pacient").child(firebaseUser.getUid())
                .setValue(userPacient);
    }
}