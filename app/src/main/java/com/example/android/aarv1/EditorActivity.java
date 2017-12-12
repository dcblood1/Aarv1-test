package com.example.android.aarv1;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.aarv1.model.AAR;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Dillon on 12/1/2017.
 */

public class EditorActivity extends AppCompatActivity{

    private static final String TAG = "EditorActivity";

    // EditText fields from the aar_editor.xml layout
    private EditText mTitleEditText;
    private EditText mDescriptionEditText;
    private EditText mCauseEditText;
    private EditText mRecommendationsEditText;
    private EditText mLocationEditText;
    private ImageButton mPhotoPickerButton;
    private Spinner mCategorySpinner;
    private ImageView mSelectedImageView;
    private Bitmap mBitmapPicture;
    private String mDownloadUrl;
    private TextView mPhotoPickerTextView;
    private int mUpVotes = 0;
    private int mDownVotes = 0;
    Uri filePath;
    ProgressDialog pd;
    // Constant for the photo picker?? idk why this is needed.
    private static final int RC_PHOTO_PICKER = 2;

    /**
     * Category for Drilling.
     */
    private String mCategory = "";

    //// firebase instance variables
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mAARPhotosStorageReference;
    private FirebaseFirestore mFirestore;

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
        Log.v("EditorActivity.java","onCreate being called in Editor Activity:");

        pd = new ProgressDialog(this);
        pd.setMessage("Uploading... please wait");

        // get an instance of the firebaseDatabase class
        // Initialize Firebase Components
        mFirebaseStorage = FirebaseStorage.getInstance();

        // find all of the relevant views that we will need to read user input from
        mTitleEditText = (EditText) findViewById(R.id.edit_aar_title);
        mDescriptionEditText = (EditText) findViewById(R.id.edit_aar_description);
        mCauseEditText= (EditText) findViewById(R.id.edit_aar_cause);
        mRecommendationsEditText = (EditText) findViewById(R.id.edit_aar_recommendations);
        mLocationEditText = (EditText) findViewById(R.id.edit_aar_location);
        mPhotoPickerButton = (ImageButton) findViewById(R.id.photoPickerButton);
        mCategorySpinner = (Spinner) findViewById(R.id.edit_aar_category_spinner);
        mSelectedImageView = (ImageView) findViewById(R.id.edit_aar_imageView);
        mPhotoPickerTextView = (TextView) findViewById(R.id.photo_picker_TextView);

        //call setupSpinner method
        setupSpinner();

        // ImagePickerButton shows an image picker to upload an image or take a picture...
        mPhotoPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(); //Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY,true); // only from this device no other storage
                //intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true); MIGHT NEED TO MAKE THIS A FUNCTION LATER TO ALLOW MULTIPLE PICS
                startActivityForResult(Intent.createChooser(intent,"Select Image"),RC_PHOTO_PICKER);
            }
            });

    }

    // Once a user has selected an image, it takes the image and displays it, so they know they selected the correct one.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK && data != null && data.getData() != null){
            filePath = data.getData();
            Log.v("EditorActivity.java", "filePath after getDate() is: " + filePath);
            try {
                // getting image from gallery
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                //This lowers the resolution so that it can be displayed int he image view
                Bitmap resized = Bitmap.createScaledBitmap(bitmap,(int)(bitmap.getWidth()*0.7),(int)(bitmap.getHeight()*0.7),true);

                mBitmapPicture = resized;
                // setting image to ImageView
                mSelectedImageView.setImageBitmap(resized);

                // Once the image is set, remove the text and button for adding an image
                mPhotoPickerTextView.setVisibility(View.GONE);
                mPhotoPickerButton.setVisibility(View.GONE);

            } catch (Exception e){
                e.printStackTrace();
            }
                }
            }

    /**
     * Setup the dropdown spinner that allows the user to select the category of the AAR.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout

        ArrayAdapter categoryDrillingSpinnderAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_drilling_category_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        categoryDrillingSpinnderAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mCategorySpinner.setAdapter(categoryDrillingSpinnderAdapter);

        // Set the String mCategory equal to one of the following categories
        mCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals("Rig Move")) {
                        mCategory = "Rig Move";
                    } else if(selection.equals("Drilling")) {
                        mCategory = "Drilling";
                    } else if(selection.equals("Casing")) {
                        mCategory = "Casing";
                    } else if(selection.equals("Cementing")) {
                        mCategory = "Cementing";
                    } else if (selection.equals("Skidding"));
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {
                mCategory = "Not Selected";
            }
        });
    }

    // method for when the Save AAR Button is clicked
    public void saveAAR(View view) {

        // grabs all of the necessary user inputs
        EditText titleEditText = (EditText) findViewById(R.id.edit_aar_title);
        final String hasTitle = titleEditText.getText().toString();

        EditText descriptionEditText = (EditText) findViewById(R.id.edit_aar_description);
        final String hasDescription = descriptionEditText.getText().toString();

        EditText causeEditText = (EditText) findViewById(R.id.edit_aar_cause);
        final String hasCause = causeEditText.getText().toString();

        EditText recommendationsEditText = (EditText) findViewById(R.id.edit_aar_recommendations);
        final String hasRecommendations = recommendationsEditText.getText().toString();

        EditText locationEditText = (EditText) findViewById(R.id.edit_aar_location);
        final String hasLocation = locationEditText.getText().toString();

        // gets the current date and time (Sat Dec 02 00:04:26 EST 2017)
        Date hasTimeStamp= Calendar.getInstance().getTime();
        final String hasFormattedDate = formatDate(hasTimeStamp);

        Log.v("EditorActivity.java", "filePath before Map: " + filePath);

        // If there is a photo, then go through this and add image to storage, then upload to db with file
        if (filePath != null){
            pd.show();
            //
            StorageReference childRef = storageRef.child(filePath.getLastPathSegment());

            // upload the image
            UploadTask uploadTask = childRef.putFile(filePath);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    pd.dismiss();
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    mDownloadUrl = downloadUrl.toString(); //changed toString, so able to push to db
                    Log.v("EditorActivity.java", "mDownoladUrl=" + mDownloadUrl);

                    // Create a new AAR POJO, then set value inputs
                    AAR aar = new AAR();

                    aar.setCategory(mCategory);
                    aar.setTitle(hasTitle);
                    aar.setDescription(hasDescription);
                    aar.setCause(hasCause);
                    aar.setRecommendations(hasRecommendations);
                    aar.setLocation(hasLocation);
                    aar.setDate(hasFormattedDate);
                    aar.setUpVotes(mUpVotes);
                    aar.setDownVotes(mDownVotes);
                    aar.setPhoto(mDownloadUrl);


                    // Add a new document with a generated ID and pass into the Firebase Storage
                    db.collection("aars")
                            .add(aar)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                      @Override
                                                      public void onSuccess(DocumentReference documentReference) {
                                                          Log.d("EditorActivity.java", "DocumentSnapshot added with ID: " + documentReference.getId());
                                                      }
                            }
                            )
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w("EditorActivity.java", "Error adding document", e);
                                }
                            });

                    // closes EditorActivity
                    finish();

                    Toast.makeText(EditorActivity.this, "Upload successful", Toast.LENGTH_SHORT).show();
                    Toast.makeText(EditorActivity.this, "AAR submitted", Toast.LENGTH_SHORT).show();


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
        } else {
            // ELSE if no image is selected.
            // Create a new AAR POJO,
            AAR aar = new AAR();
            aar.setCategory(mCategory);
            aar.setTitle(hasTitle);
            aar.setDescription(hasDescription);
            aar.setCause(hasCause);
            aar.setRecommendations(hasRecommendations);
            aar.setLocation(hasLocation);
            aar.setUpVotes(mUpVotes);
            aar.setDownVotes(mDownVotes);
            aar.setDate(hasFormattedDate);

            // Add a new document with a generated ID and pass into the Firebase Storage
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

            // closes EditorActivity
            finish();
        }

    }

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
        if (mBitmapPicture != null){
            mBitmapPicture.recycle();
            mBitmapPicture = null;
        }
    }

    @Override
    public void onBackPressed() {
        Log.v(TAG,"onBackPressed in Editor activity: ");
        super.onBackPressed();
        finish();
    }



}
