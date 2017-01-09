package de.peterloos.friendzzgosquash;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import de.peterloos.models.Message;
import de.peterloos.models.User;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "PeLo";

    private static final int GALLERY_INTENT_REQUEST_ID = 1;
    private static final int PERMISSION_REQUEST_ID = 1;

    private static final String CHILD_USERS = "users";

    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;

    private FirebaseStorage storage;
    private StorageReference storageReference;

    private FirebaseDatabase database;
    private DatabaseReference reference;

    private Button buttonSetDisplayname;
    private Button buttonChangeProfilePicture;

    private EditText edittextDisplayName;
    private EditText edittextLoggedOnUser;
    private TextView textviewStatus;
    private ImageView imageviewPhoto;

    // footprint of current user
    private String userEMailAddress;
    private String userDisplayName;
    private String userPhotoUrl;
    private String userUID;

    private boolean storagePermissionGranted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_settings);

        Log.d(TAG, "SettingsActivity::onCreate");

        // setup event handler
        this.buttonSetDisplayname = (Button) this.findViewById(R.id.button_set_displayname);
        this.buttonSetDisplayname.setOnClickListener(this);
        this.buttonChangeProfilePicture = (Button) this.findViewById(R.id.button_change_profilepicture);
        this.buttonChangeProfilePicture.setOnClickListener(this);

        // setup input controls
        this.edittextDisplayName = (EditText) this.findViewById(R.id.edittext_displayname);
        this.edittextLoggedOnUser = (EditText) this.findViewById(R.id.edittext_currentuser);
        this.edittextLoggedOnUser.setEnabled(false);
        this.textviewStatus = (TextView) this.findViewById(R.id.textview_status);
        this.imageviewPhoto = (ImageView) this.findViewById(R.id.settings_imageview);

        this.database = FirebaseDatabase.getInstance();
        this.reference = this.database.getReference();

        // retrieve currently signed-in user
        this.auth = FirebaseAuth.getInstance();
        this.user = this.auth.getCurrentUser();

        // setup firebase storage
        this.storage = FirebaseStorage.getInstance();
        this.storageReference = this.storage.getReference();

        // check permission for storage access
        this.storagePermissionGranted = this.isStoragePermissionGranted();
    }

    // check permission
    private boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {

            // take care on Android 6.0 runtime permissions model
            int permissionCheck =
                    ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "SettingsActivity::isStoragePermissionGranted: Permission granted");
                return true;
            } else {
                Log.d(TAG, "SettingsActivity::isStoragePermissionGranted: Permission is revoked");
                String[] permissions = new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
                ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_ID);
                return false;
            }
        } else {
            // permission is automatically granted on SDK < 23 upon installation
            Log.v(TAG, "Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "SettingsActivity::onRequestPermissionsResult");

        if (requestCode == PERMISSION_REQUEST_ID) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "PERMISSION GRANTED", Toast.LENGTH_SHORT).show();
                this.storagePermissionGranted = true;
            } else {
                Toast.makeText(getApplicationContext(), "PERMISSION DENIED", Toast.LENGTH_SHORT).show();
                this.storagePermissionGranted = false;
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "SettingsActivity::onStart");

        this.authStateListener = new FirebaseAuth.AuthStateListener() {

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {

                    final String uid = user.getUid();  // user is signed in

                    // listen for value events of this user
                    final DatabaseReference currentUser = SettingsActivity.this.reference.child(CHILD_USERS).child(uid);

                    currentUser.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            User tempUser = dataSnapshot.getValue(User.class);

                            Log.d(TAG, " UPPIEEEEEE New DataSnapshot");

                            SettingsActivity.this.userEMailAddress = tempUser.getEmail();
                            SettingsActivity.this.userDisplayName = tempUser.getDisplayName();
                            SettingsActivity.this.userPhotoUrl = tempUser.getPhotoUrl();
                            SettingsActivity.this.userUID = uid;

                            // update user interface
                            SettingsActivity.this.edittextLoggedOnUser.setText(SettingsActivity.this.userEMailAddress);

                            if (SettingsActivity.this.userDisplayName == null || SettingsActivity.this.userDisplayName.equals("")) {
                                SettingsActivity.this.edittextDisplayName.setText(R.string.empty_display_name);
                            } else {
                                SettingsActivity.this.edittextDisplayName.setText(SettingsActivity.this.userDisplayName);
                            }

                            // show photo of user, if available
                            if (!(SettingsActivity.this.userPhotoUrl == null || SettingsActivity.this.userPhotoUrl.equals(""))) {
                                Glide.with(SettingsActivity.this)
                                        .load(SettingsActivity.this.userPhotoUrl)
                                        .into(SettingsActivity.this.imageviewPhoto);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                            // getting current user failed, log a message
                            Log.w(TAG, "SettingsActivity::onStart:onCancelled", databaseError.toException());
                        }
                    });

                } else {
                    // user is signed out
                    Log.d(TAG, "SettingsActivity::onStart:onCancelled -- No User signed in ?!? Internal Error !!! ");
                }
            }
        };

        this.auth.addAuthStateListener(this.authStateListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "SettingsActivity::onStop");

        this.auth.removeAuthStateListener(this.authStateListener);
    }

    @Override
    public void onClick(View view) {

        if (view == this.buttonSetDisplayname) {
            this.setDisplayName();
        } else if (view == this.buttonChangeProfilePicture) {
            this.changeProfilePicture();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_INTENT_REQUEST_ID && resultCode == RESULT_OK) {

            this.showProgressDialog("Uploading ... ");

            Uri uri = data.getData();

            // ===================================

            // trying now to compress the file ....
            String uriString = uri.toString();
            String compressedFile = this.compressImage(uriString);

            Log.d(TAG, "      ############### compressImage ==> " + compressedFile);

            // ===================================

            Log.d(TAG, "onActivityResult: Uri = " + uri.toString());
            Log.d(TAG, "onActivityResult: getLastPathSegment = " + uri.getLastPathSegment());

            StorageReference filepath = this.storageReference.child("Photos").child(this.userUID).child("Photo");
            Log.d(TAG, "onActivityResult: filepath = " + filepath.toString());

            // UploadTask task = filepath.putFile(uri);    // ALTE VERSION -- GFEHT !!!

            Uri compressedUri = Uri.fromFile(new File(compressedFile));
            UploadTask task = filepath.putFile(compressedUri);    // UMSTIEG auf komprimierte Datei !!!

            task.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    SettingsActivity.this.hideProgressDialog();

                    // store new Url
                    Uri downloadUri = taskSnapshot.getDownloadUrl();
                    SettingsActivity.this.userPhotoUrl = downloadUri.toString();

                    Log.d(TAG, "onActivityResult: new Photo Url ==> " + SettingsActivity.this.userPhotoUrl);

                    // set download uri in users section ...
                    String uid = SettingsActivity.this.user.getUid();
                    SettingsActivity.this.reference.child(CHILD_USERS).child(uid).child("photoUrl").setValue(downloadUri.toString());

                    // ... and also in the firebase users profile
                    UserProfileChangeRequest.Builder builder = new UserProfileChangeRequest.Builder();
                    builder.setPhotoUri(downloadUri);
                    UserProfileChangeRequest request = builder.build();

                    Task<Void> task = SettingsActivity.this.user.updateProfile(request);

                    task.addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Log.d(TAG, "User profile updated.");
                            Toast.makeText(SettingsActivity.this, "Upload Done !", Toast.LENGTH_LONG).show();
                        }
                    });

                    task.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "User profile Update (Photo) failed  !!! " + e.getMessage());
                        }
                    });

                    // show new photo ...
                    if (!(SettingsActivity.this.userPhotoUrl == null || SettingsActivity.this.userPhotoUrl.equals(""))) {
                        Glide.with(SettingsActivity.this)
                                .load(SettingsActivity.this.userPhotoUrl)
                                .into(SettingsActivity.this.imageviewPhoto);
                    }

                    // ... and finally some stats
                    long bytesTransferred = taskSnapshot.getBytesTransferred();
                    Log.d(TAG, "onActivityResult: bytesTransferred = " + bytesTransferred);

                    // just to inform calling activity
                    // SettingsActivity.this.hasPhotoUrlChanged = true;
                }
            });

            task.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    SettingsActivity.this.hideProgressDialog();

                    Log.d(TAG, "onActivityResult: onFailure => " + e.getMessage());
                }
            });
        }
    }

    private void setDisplayName() {
        Log.v(TAG, "set display name");

        final String newDisplayname = this.edittextDisplayName.getText().toString();

        if (newDisplayname.matches("[A-Za-z0-9 ]+")) {

            UserProfileChangeRequest.Builder builder = new UserProfileChangeRequest.Builder();
            builder.setDisplayName(newDisplayname);
            UserProfileChangeRequest request = builder.build();

            Task<Void> task = this.user.updateProfile(request);

            task.addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Log.d(TAG, "User profile updated.");
                    Toast.makeText(SettingsActivity.this, "User profile updated !", Toast.LENGTH_LONG).show();

                    // update firebase database within 'users' section
                    SettingsActivity.this.reference.child(CHILD_USERS).child(SettingsActivity.this.userUID).child("displayName").setValue(newDisplayname);

                    // update firebase database within 'messages' section
                    SettingsActivity.this.firebaseUpdateDisplayName(newDisplayname);
                }
            });

            task.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "User profile Update (Display Name) failed  !!! " + e.getMessage());
                }
            });
        } else {
            this.edittextDisplayName.setText("<empty>");
            this.textviewStatus.setText(R.string.error_wrong_input);
        }
    }

    // private helper methods
    private void changeProfilePicture() {

        if (!storagePermissionGranted) {
            Toast.makeText(getApplicationContext(), "Permission not granted !", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");  // specify an explicit MIME data type to indicate the type of data to return
        this.startActivityForResult(intent, GALLERY_INTENT_REQUEST_ID);
    }

    // =============================================================================================
    // picture stuff ....
    // =============================================================================================

    // private helper methods to compress image before uploading
    private String compressImage(String imageUri) {

        String filePath = this.getRealPathFromURI(imageUri);

        Bitmap scaledBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();

        //  the actual bitmap pixels are *not* loaded into memory
        options.inJustDecodeBounds = true;

        Bitmap bmp;
        try {
            bmp = BitmapFactory.decodeFile(filePath, options);
        } catch (Exception ex) {
            Log.d(TAG, "SettingsActivity::decodeFile ERROR: " + ex.getMessage());
            return "";
        }

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

        // maxHeight and maxWidth values of the compressed image are taken as 816x612
        // TODO: To be adjusted !!!!!!!!!!!!!!!!!!!!!

//        float maxHeight = 816.0f;
//        float maxWidth = 612.0f;
        float maxHeight = 200.0f;
        float maxWidth = 200.0f;
        float imgRatio = actualWidth / actualHeight;
        float maxRatio = maxWidth / maxHeight;

        // width and height values are set maintaining the aspect ratio of the image
        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;
            }
        }

        // setting inSampleSize value allows to load a scaled down version of the original image
        options.inSampleSize = this.calculateInSampleSize(options, actualWidth, actualHeight);

        // inJustDecodeBounds now set to *false* to load the actual bitmap
        options.inJustDecodeBounds = false;

        // this options allow android to claim the bitmap memory if it runs low on memory
        options.inTempStorage = new byte[16 * 1024];

        try {
            // load bitmap
            bmp = BitmapFactory.decodeFile(filePath, options);
        } catch (OutOfMemoryError ex) {
            ex.printStackTrace();
        }

        try {
            scaledBitmap = Bitmap.createScaledBitmap(bmp, actualWidth, actualHeight, true);
        } catch (OutOfMemoryError ex) {
            ex.printStackTrace();
        }


//        try {
//            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
//        } catch (OutOfMemoryError ex) {
//            ex.printStackTrace();
//        }
//
//        float ratioX = actualWidth / (float) options.outWidth;
//        float ratioY = actualHeight / (float) options.outHeight;
//        float middleX = actualWidth / 2.0f;
//        float middleY = actualHeight / 2.0f;
//
//        Matrix scaleMatrix = new Matrix();
//        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);
//
//        Canvas canvas = new Canvas(scaledBitmap);
//        canvas.setMatrix(scaleMatrix);
//        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));
//
////      check the rotation of the image and display it properly
//        ExifInterface exif;
//        try {
//            exif = new ExifInterface(filePath);
//
//            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
//
//            Log.d("EXIF", "Exif: " + orientation);
//            Matrix matrix = new Matrix();
//
//            if (orientation == 6) {
//                matrix.postRotate(90);
//                Log.d("EXIF", "Exif: " + orientation);
//            } else if (orientation == 3) {
//                matrix.postRotate(180);
//                Log.d("EXIF", "Exif: " + orientation);
//            } else if (orientation == 8) {
//                matrix.postRotate(270);
//                Log.d("EXIF", "Exif: " + orientation);
//            }
//            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
//                    scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
//                    true);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        FileOutputStream out = null;
        String filename = this.getFilename();
        try {
            out = new FileOutputStream(filename);

            // write the compressed bitmap at the destination specified by filename.
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return filename;
    }


    private String compressImage_Variante_1_Geht(String imageUri) {

        String filePath = this.getRealPathFromURI(imageUri);

        Bitmap scaledBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();

        //  the actual bitmap pixels are *not* loaded into memory
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(filePath, options);
        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

        // maxHeight and maxWidth values of the compressed image are taken as 816x612
        // TODO: To be adjusted !!!!!!!!!!!!!!!!!!!!!

//        float maxHeight = 816.0f;
//        float maxWidth = 612.0f;
        float maxHeight = 200.0f;
        float maxWidth = 200.0f;
        float imgRatio = actualWidth / actualHeight;
        float maxRatio = maxWidth / maxHeight;

        // width and height values are set maintaining the aspect ratio of the image
        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;
            }
        }

        // setting inSampleSize value allows to load a scaled down version of the original image
        options.inSampleSize = this.calculateInSampleSize(options, actualWidth, actualHeight);

        // inJustDecodeBounds now set to *false* to load the actual bitmap
        options.inJustDecodeBounds = false;

        // this options allow android to claim the bitmap memory if it runs low on memory
        options.inTempStorage = new byte[16 * 1024];

        try {
            // load bitmap
            bmp = BitmapFactory.decodeFile(filePath, options);
        } catch (OutOfMemoryError ex) {
            ex.printStackTrace();
        }

        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError ex) {
            ex.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

//      check the rotation of the image and display it properly
        ExifInterface exif;
        try {
            exif = new ExifInterface(filePath);

            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);

            Log.d("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();

            if (orientation == 6) {
                matrix.postRotate(90);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 3) {
                matrix.postRotate(180);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 8) {
                matrix.postRotate(270);
                Log.d("EXIF", "Exif: " + orientation);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                    scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                    true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileOutputStream out = null;
        String filename = this.getFilename();
        try {
            out = new FileOutputStream(filename);

            // write the compressed bitmap at the destination specified by filename.
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return filename;
    }


    public String getFilename() {
        File file = new File(Environment.getExternalStorageDirectory().getPath(), "PeLoFolder/Images");
        if (!file.exists()) {
            file.mkdirs();
        }
        String uriSting = (file.getAbsolutePath() + "/" + System.currentTimeMillis() + ".jpg");
        return uriSting;

    }

    // TODO ??????????????? Was tut die ????
    private String getRealPathFromURI(String contentURI) {
        Uri contentUri = Uri.parse(contentURI);
        Cursor cursor = getContentResolver().query(contentUri, null, null, null, null);
        if (cursor == null) {
            return contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(index);
        }
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {

        final int height = options.outHeight;
        final int width = options.outWidth;

        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;

        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }

    private ProgressDialog progressDialog;

    // progress dialog handling
    private void showProgressDialog(String msg) {
        if (this.progressDialog == null) {
            this.progressDialog = new ProgressDialog(this);
            this.progressDialog.setMessage(msg);
            this.progressDialog.setIndeterminate(true);
        }

        this.progressDialog.show();
    }

    private void hideProgressDialog() {
        if (this.progressDialog != null && progressDialog.isShowing()) {
            this.progressDialog.dismiss();
        }
    }

    private void firebaseUpdateDisplayName(final String displayName) {

        final DatabaseReference reference = this.database.getReference("messages");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    // use built-in JSON-to-POJO deserializer to convert snapshot to Message object
                    Message message = snapshot.getValue(Message.class);
                    String result = String.format("  Message: %s", message.toString());
                    Log.v(TAG, result);

                    if (SettingsActivity.this.userUID.equals(message.getUid())) {

                        String key = snapshot.getKey();

                        // update firebase database according users new display name
                        reference.child(key).child("displayName").setValue(displayName);
                        Log.v(TAG, "   ... updated message with new display name ...");
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.v(TAG, "FUCK");
            }
        });
    }
}
