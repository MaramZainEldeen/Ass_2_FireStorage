package com.example.ass_2_firestore_gallary;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {



    FirebaseStorage firebase_s ;
    StorageReference stor_r;
    StorageReference stor_child;
    ImageView image;
    Button button_up , button_ch ;
    Uri imageUli ;
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        image = findViewById(R.id.img);
        button_ch = findViewById(R.id.btn_Choose);
        button_up = findViewById(R.id.btn_Upload);

        firebase_s = FirebaseStorage.getInstance();
        // الوصول الى مجلد التخزين الكبير (قبل ال upload)
        // نحتاج لرفع الصورة ال (perent reference , child Referents)
        stor_r =  firebase_s.getReference();


        button_ch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });

        button_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                uploadImage();
            }
        });



    }
 //  خاصين باختيار الصورة
    private void chooseImage() {

        Intent gallary = new Intent(Intent.ACTION_PICK , MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        gallary.setType("image/*");
        gallary.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(gallary, "Select Picture"),1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            imageUli = data.getData();
            image.setImageURI(imageUli);
          /*  try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUli);
                image.setImageBitmap(bitmap);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }*/
        }
    }


    // خاصين برفع الصورة على ال (fire base)
    private void uploadImage() {
        if(imageUli != null)
        {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            Long current_image = System.currentTimeMillis()/1000 ;
            String path_image = "upload/image/"+current_image;
            // جلب الصورة من ال(perents reference)
            stor_child = stor_r.child(path_image);
            stor_child.putFile(imageUli)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Glide.with(getApplicationContext()).load(imageUli).into(image);
                            progressDialog.dismiss();
                            Toast.makeText(MainActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(MainActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded "+(int)progress+"%");
                        }
                    });
        }
    }



    private void UploadPicture() {

        pd = new ProgressDialog(this);
        pd.setTitle("Uploading Now.......");
        pd.show();

        final  String randomKey = UUID.randomUUID().toString();
        Uri uri = Uri.fromFile(new File("upload/images/" + randomKey));

        // وضع الصورة داخل المجلد الموجود داخل (child)
        stor_child.putFile(imageUli).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                stor_child.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                       pd.dismiss();
                        // مربع نص يخرج من الاسفل
                      Snackbar.make(findViewById(android.R.id.content) , "Image Upload..." ,Snackbar.LENGTH_LONG).show();
                        Toast.makeText(MainActivity.this ,"Save is Success...",Toast.LENGTH_SHORT).show();
                        //pr.setVisibility(View.GONE);
                        Glide.with(getApplicationContext()).load(uri).into(image);

                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(getApplicationContext() ,"failure Upload :( " ,Toast.LENGTH_LONG).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                // حساب الننسبة المئوية التحميل (progrees) من 100
                 double progress = (100.00 * snapshot.getBytesTransferred()/ snapshot.getTotalByteCount() );
                 pd.setMessage("Progress: " + (int) progress + "%");
            }
        });



}
}