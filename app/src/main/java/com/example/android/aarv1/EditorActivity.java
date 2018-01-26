package com.example.android.aarv1;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.android.aarv1.model.AAR;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

/**
 * Created by Dillon on 12/1/2017.
 */

public class EditorActivity extends AppCompatActivity implements EventListener<DocumentSnapshot>{

    private static final String TAG = "EditorActivity";

    public static final String KEY_AAR_ID = "key_aar_id";

    // EditText fields from the aar_editor.xml layout
    private EditText mTitleEditText;
    private EditText mDescriptionEditText;
    private EditText mCauseEditText;
    private EditText mRecommendationsEditText;
    private ImageButton mPhotoPickerButton;
    private Spinner mCategorySpinner;
    private Spinner mLocationSpinner;
    private ImageView mSelectedImageView;
    private Bitmap mBitmapPicture;
    private String mDownloadUrl;
    private TextView mPhotoPickerTextView;
    private ImageView mRemovePhotoImageView;
    private int mUpVotes = 0;
    private int mViews = 0;
    Uri filePath;
    private String mDate;
    ProgressDialog pd;
    // Constant for the photo picker?? idk why this is needed.
    private static final int RC_PHOTO_PICKER = 2;

    /**
     * Category for Drilling.
     */
    private String mCategory = "";
    private String mLocation = "";

    //// firebase instance variables
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mAARPhotosStorageReference;
    private FirebaseFirestore mFirestore;
    private FirebaseAuth mFirebaseAuth;
    private String mAarId;
    private DocumentReference mAarRef;
    private ListenerRegistration mAarRegistration;

    private boolean mHasExtras;

    // Access a Cloud Firestore instance from your Activity
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Access the Firebase Storage instance from my Activity
    FirebaseStorage storage = FirebaseStorage.getInstance();

    //creating reference to firebase storage
    StorageReference storageRef = storage.getReferenceFromUrl("gs://aarv1-c9483.appspot.com/drilling_aar_photos");
    //gs://aarv1-c9483.appspot.com/drilling_aar_photos

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aar_editor);

        // Initialize firestore
        mFirestore = FirebaseFirestore.getInstance();

        Intent intent = getIntent();
        Log.v(TAG,"this is the intent" + intent);
        mHasExtras = getIntent().hasExtra(KEY_AAR_ID);

        // Determine whether the user is adding an aar or editing one
        if (mHasExtras) {
            // Then it is coming the user wanting to edit
            mAarId = getIntent().getExtras().getString(KEY_AAR_ID);
            // Get reference to the aars
            mAarRef = mFirestore.collection("aars").document(mAarId);
        }

        pd = new ProgressDialog(this);
        pd.setMessage("Uploading... please wait");

        // get an instance of the firebaseDatabase class
        // Initialize Firebase Components
        mFirebaseStorage = FirebaseStorage.getInstance();
        // initialize firebase authentication
        mFirebaseAuth = FirebaseAuth.getInstance();

        // find all of the relevant views that we will need to read user input from
        mTitleEditText = (EditText) findViewById(R.id.edit_aar_title);
        mDescriptionEditText = (EditText) findViewById(R.id.edit_aar_description);
        mCauseEditText= (EditText) findViewById(R.id.edit_aar_cause);
        mRecommendationsEditText = (EditText) findViewById(R.id.edit_aar_recommendations);
        mPhotoPickerButton = (ImageButton) findViewById(R.id.photoPickerButton);
        mCategorySpinner = (Spinner) findViewById(R.id.edit_aar_category_spinner);
        mLocationSpinner = (Spinner) findViewById(R.id.edit_aar_location);
        mSelectedImageView = (ImageView) findViewById(R.id.edit_aar_imageView);
        mPhotoPickerTextView = (TextView) findViewById(R.id.photo_picker_TextView);
        mRemovePhotoImageView = (ImageView) findViewById(R.id.remove_photo_image_view);

        //call setupSpinner method
        setupSpinner();

        // ImagePickerButton shows an image picker to upload an image or take a picture...
        mPhotoPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v(TAG,"mPhotoPickerButton clicked on");
                Intent intent = new Intent(); //Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY,true); // only from this device no other storage
                //intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true); MIGHT NEED TO MAKE THIS A FUNCTION LATER TO ALLOW MULTIPLE PICS
                startActivityForResult(Intent.createChooser(intent,"Select Image"),RC_PHOTO_PICKER);
            }
            });

        mRemovePhotoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // remove the photo somehow...
                Log.v(TAG,"mRemovePhotoImageView clicked on");
                // if there is a photo... remove it.
                // set the filepath to null so it does not try to add it to db.
                // now has to remove download url too.
                if (mSelectedImageView.getDrawable() != null) {
                    mSelectedImageView.setImageDrawable(null);

                    if (mDownloadUrl != null) {
                        // Deletes the photo from DB storage
                        StorageReference urlRef = storage.getReferenceFromUrl(mDownloadUrl);
                        Log.v(TAG,"this is the urlRef =" + urlRef);
                        urlRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(EditorActivity.this, "Photo removed", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(EditorActivity.this, "Failed to delete Photo", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    filePath = null;
                    mDownloadUrl = null;

                } else {
                    Toast.makeText(EditorActivity.this,"No image to remove",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        // this is a listener for the firestore to know to be accessed? essentially?
        if (mHasExtras) {mAarRegistration = mAarRef.addSnapshotListener(this);}
    }

    @Override
    protected void onStop() {
        super.onStop();

        // removes listener for onEvent I believe
        if(mAarRegistration != null) {
            mAarRegistration.remove();
            mAarRegistration = null;
        }
    }

    // Once a user has selected an image, it takes the image and displays it, so they know they selected the correct one.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK && data != null && data.getData() != null){
            filePath = data.getData();
            Log.v("EditorActivity.java", "filePath after getData() is: " + filePath);

            Glide.with(this)
                    .load(filePath)
                    .fitCenter()
                    .into(mSelectedImageView);
                }
            }
    /**
     * Setup the dropdown spinner that allows the user to select the category of the AAR and the Location
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout

        ArrayAdapter categoryDrillingSpinnderAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_drilling_category_options, android.R.layout.simple_spinner_item);

        ArrayAdapter locationSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_location_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        categoryDrillingSpinnderAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        locationSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mCategorySpinner.setAdapter(categoryDrillingSpinnderAdapter);
        mLocationSpinner.setAdapter(locationSpinnerAdapter);

        // Set the String mCategory equal to one of the following categories
        mCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.rig_move))) {
                        mCategory = getString(R.string.rig_move);
                    } else if(selection.equals(getString(R.string.drilling))) {
                        mCategory = getString(R.string.drilling);
                    } else if(selection.equals(getString(R.string.casing))) {
                        mCategory = getString(R.string.casing);
                    } else if(selection.equals(getString(R.string.cementing))) {
                        mCategory = getString(R.string.cementing);
                    } else if (selection.equals(getString(R.string.skidding)));
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {
                mCategory = "Not Selected";
            }
        });

        // Set the String equal to one of the following categories
        mLocationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.eagleford))){
                        mLocation = getString(R.string.eagleford);
                    } else if (selection.equals(getString(R.string.haynesville))) {
                        mLocation = getString(R.string.haynesville);
                    } else if(selection.equals(getString(R.string.permian))) {
                        mLocation = getString(R.string.permian);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { mLocation = "Not Selected";

            }
        });
    }

    // method for when the Save AAR Button is clicked
    // need to check and make sure the form is filled out correctly...
    public void saveAAR(View view) {

        // grabs all of the necessary user inputs
        EditText titleEditText = (EditText) findViewById(R.id.edit_aar_title);
        final String hasTitle = titleEditText.getText().toString().trim();

        EditText descriptionEditText = (EditText) findViewById(R.id.edit_aar_description);
        final String hasDescription = descriptionEditText.getText().toString().trim();

        EditText causeEditText = (EditText) findViewById(R.id.edit_aar_cause);
        final String hasCause = causeEditText.getText().toString().trim();

        EditText recommendationsEditText = (EditText) findViewById(R.id.edit_aar_recommendations);
        final String hasRecommendations = recommendationsEditText.getText().toString().trim();

        // gets the current date and time (Sat Dec 02 00:04:26 EST 2017)
        final Date hasTimeStamp= Calendar.getInstance().getTime();
        final String hasFormattedDate = formatDate(hasTimeStamp);

        // get the current user ID and adds it to the AAR POJO, has to have it...
        final String currentUserId = mFirebaseAuth.getUid();
        // Might need this to be a token?? idk why... creates a security issue?

        if (currentUserId == null) {
            onBackPressed();
            Toast.makeText(EditorActivity.this, "Must be signed in to add AAR", Toast.LENGTH_SHORT).show();
        }

        // If there is a photo, then go through this and add image to storage, then upload to db with file
        if (filePath != null) {
            pd.show();
            // upload the image to Storage... compress the image
            StorageReference byteRefTest = storageRef.child(hasTitle + filePath.getLastPathSegment() + getSaltString());
            // Get the data from an imageView
            mSelectedImageView.setDrawingCacheEnabled(true); // dunno what this does...
            mSelectedImageView.buildDrawingCache();
            Bitmap bitmap = mSelectedImageView.getDrawingCache();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos); // 100 is 1.3 MB, 75 is 135 Kb, 90 is 250 kb
            byte[] data = baos.toByteArray();

            UploadTask byteUploadTask = byteRefTest.putBytes(data);
            byteUploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    pd.dismiss();
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    mDownloadUrl = downloadUrl.toString(); //changed toString, so able to push to db

                    // check if the inputs are valid prior to entering into DB
                    // returns true if they are valid, false if not
                    if (!checkIsValid(hasTitle,hasDescription,hasCause,hasRecommendations)){
                        return;
                    }

                    // Create a new AAR POJO, then set value inputs
                    AAR aar = new AAR();

                    // check if the inputs are valid prior to entering into DB
                    checkIsValid(hasTitle,hasDescription,hasCause,hasRecommendations);

                    aar.setCategory(mCategory);
                    aar.setTitle(hasTitle);
                    aar.setDescription(hasDescription);
                    aar.setCause(hasCause);
                    aar.setRecommendations(hasRecommendations);
                    aar.setLocation(mLocation);
                    aar.setUpVotes(mUpVotes);
                    aar.setViews(mViews);
                    aar.setPhoto(mDownloadUrl);
                    aar.setUser(currentUserId);

                    // Add a new document with a generated ID and pass into the Firebase Storage
                    // hasExtras is checking if this is an existing aar or new.
                    if (mHasExtras) {
                        db.collection("aars").document(mAarId)
                                .set(aar)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(EditorActivity.this, "AAR updated", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(EditorActivity.this, "AAR failed to Update, Try again", Toast.LENGTH_SHORT).show();
                                    }
                                });
                        // This updates the date with the first date it was created
                        db.collection("aars").document(mAarId).update("date", mDate);
                    } else {
                        // This set date is moved down... so it does not get updated when a user edits their aar
                        aar.setDate(hasFormattedDate);
                        aar.setTimestamp(hasTimeStamp);
                        db.collection("aars")
                                .add(aar)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                          @Override
                                                          public void onSuccess(DocumentReference documentReference) {
                                                              Log.d("EditorActivity.java", "DocumentSnapshot added with ID: " + documentReference.getId());
                                                              Toast.makeText(EditorActivity.this, "AAR Successfully Added", Toast.LENGTH_SHORT).show();
                                                          }
                                                      }
                                )
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w("EditorActivity.java", "Error adding document", e);
                                        Toast.makeText(EditorActivity.this, "Upload Failure", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }

                    // closes EditorActivity
                    pd.dismiss();
                    finish();

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    pd.dismiss();
                    Toast.makeText(EditorActivity.this, "Upload Failed -> " + e, Toast.LENGTH_SHORT).show();

                    // closes EditorActivity
                    finish();
                }
            });

        }else {
            // ELSE if no image is selected.
            // Create a new AAR POJO,

            // check if the inputs are valid prior to entering into DB
            // returns true if they are valid, false if not
            if (!checkIsValid(hasTitle,hasDescription,hasCause,hasRecommendations)){
                return;
            }

            AAR aar = new AAR();

            aar.setCategory(mCategory);
            aar.setTitle(hasTitle);
            aar.setDescription(hasDescription);
            aar.setCause(hasCause);
            aar.setRecommendations(hasRecommendations);
            aar.setLocation(mLocation);
            aar.setUpVotes(mUpVotes);
            aar.setViews(mViews);
            aar.setUser(currentUserId);

            if (mHasExtras) {
                db.collection("aars").document(mAarId)
                        .set(aar)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(EditorActivity.this, "AAR updated", Toast.LENGTH_SHORT).show();
                                pd.dismiss();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(EditorActivity.this, "AAR failed to Update, Try again", Toast.LENGTH_SHORT).show();
                                pd.dismiss();
                            }
                        });
                // if the aar alreadys exists, then keep the date the same it was initially created
                db.collection("aars").document(mAarId).update("date", mDate);
                // if a photo already exists, will keep it the same
                if (mDownloadUrl != null) {
                    db.collection("aars").document(mAarId).update("photo", mDownloadUrl);
                }
            } else {
                // This set date is moved down... so it does not get updated when a user edits their aar
                aar.setDate(hasFormattedDate);
                aar.setTimestamp(hasTimeStamp);

                // Add a new document with a generated ID and pass into the Firebase DB
                db.collection("aars")
                        .add(aar)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                  @Override
                                                  public void onSuccess(DocumentReference documentReference) {
                         Log.d("EditorActivity.java", "DocumentSnapshot added with ID: " + documentReference.getId());
                          Toast.makeText(EditorActivity.this, "AAR submitted", Toast.LENGTH_SHORT).show();
                                                  }
                                              }
                        )
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("EditorActivity.java", "Error adding document", e);
                            }
                        });

            }

            // closes EditorActivity
            pd.dismiss();
            finish();

        }}

    /**
     * Return the formatted date string (i.e. "Mar 3, 1984") from a Date object.
     */
    private String formatDate(Date dateObject) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("LLL dd, yyyy");
        return dateFormat.format(dateObject);
    }

    // this is so the memory can be released once the activity is closed. Or else uploading pics wont work if backed out
    @Override
    protected void onDestroy() {
        Log.v(TAG,"onDestroy being called:");
        super.onDestroy();
        //if (mBitmapPicture != null){
        //    mBitmapPicture.recycle();
        //    mBitmapPicture = null;
        //}
    }

    @Override
    public void onBackPressed() {
        Log.v(TAG,"onBackPressed in Editor activity: ");
        super.onBackPressed();
        finish();
    }


    // This is for the user clicking on an existing aar
    @Override
    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
        if (e != null) {
            Log.w(TAG, "aar:onEvent", e);
            return;
        }
        if (documentSnapshot.exists()) {
            onAarLoaded(documentSnapshot.toObject(AAR.class));
        }

    }

    // for loading user input into edit text
    private void onAarLoaded(AAR aar) {
        Log.v(TAG,"getCategory():" + aar.getCategory());
        Log.v(TAG,"getTitle():" + aar.getTitle());
        mCategorySpinner.setSelection(((ArrayAdapter<String>)mCategorySpinner.getAdapter()).getPosition(aar.getCategory()));
        mTitleEditText.setText(aar.getTitle());
        mDescriptionEditText.setText(aar.getDescription());
        mCauseEditText.setText(aar.getCause());
        mRecommendationsEditText.setText(aar.getRecommendations());
        mLocationSpinner.setSelection(((ArrayAdapter<String>)mLocationSpinner.getAdapter()).getPosition(aar.getLocation()));
        mDate = aar.getDate();

        // if there is a photo, grab the initial url, then put it at the bottom. & if filrepath(a new image) is not selected).
        if (aar.getPhoto() != null && filePath == null) {
            mDownloadUrl = aar.getPhoto(); // Think this is right?
            Glide.with(mSelectedImageView.getContext())
                    .load(aar.getPhoto())
                    .into(mSelectedImageView);
            Log.v(TAG,"this is the getPhoto() " + aar.getPhoto());
            Log.v(TAG,"glide applied image");
        }
    }

    // Dont want options menu to show unless there is an active AAR
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mAarId != null) {
            getMenuInflater().inflate(R.menu.editor_activity_menu, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_delete_aar:

                // Delete Photo from db Storage
                if (mDownloadUrl != null) {
                    StorageReference urlRef = storage.getReferenceFromUrl(mDownloadUrl);
                    Log.v(TAG,"this is the urlRef =" + urlRef);
                    urlRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(EditorActivity.this, "Photo removed", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(EditorActivity.this, "Failed to delete Photo", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                // Then Delete the entire AAR from the db, need to see if it is in the users profile of up votes... every users...
                // or just eliminate it when going through the other deals...
                // when you figure out how to query properly, then remove it then??

                db.collection("aars").document(mAarId).delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(EditorActivity.this, "AAR Deleted", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditorActivity.this, "AAR Failed to be Deleted...", Toast.LENGTH_SHORT).show();
                    }
                });
                break;
        }
        finish();
        return super.onOptionsItemSelected(item);
    }

    // This is used to create a random string so that the pictures will not be saved under the same file name
    protected String getSaltString() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 6) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;
    }

    // Method checks if the user entries are not empty, throws an error if they are, try it out brah!
    public boolean checkIsValid(String userHasTitle, String userHasDescription, String userHasCause,
            String userHasRecommendations) {

        if (TextUtils.isEmpty(userHasTitle)){
            mTitleEditText.setError("Please add Title");
        }
        if (TextUtils.isEmpty(userHasDescription)) {
            mDescriptionEditText.setError("Please add Description");

        }
        if (TextUtils.isEmpty(userHasCause)) {
            mCauseEditText.setError("Please add root cause");

        }
        if (TextUtils.isEmpty(userHasRecommendations)) {
            mRecommendationsEditText.setError("Please add recommendations");

        }

        if (TextUtils.isEmpty(userHasTitle) || TextUtils.isEmpty(userHasDescription) || TextUtils.isEmpty(userHasCause) ||
                TextUtils.isEmpty(userHasRecommendations)) {
            return false;
        } else {
            return true;
        }

    }

}