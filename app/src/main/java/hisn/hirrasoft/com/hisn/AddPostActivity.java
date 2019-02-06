package hisn.hirrasoft.com.hisn;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Random;

public class AddPostActivity extends AppCompatActivity {

    static final int Gallery_Pick = 1;

    private EditText postText;

    private ImageButton galleryPick;

    private Uri image_uri;

    private StorageReference storageReference;
    private DatabaseReference userDbRef, postDBRef;

    private FirebaseAuth mAuth;

    private String text;
    private String saveCurrentData, saveCurrentTime, postRandomName, download_url, current_UserID;

    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        mAuth = FirebaseAuth.getInstance();
        current_UserID = mAuth.getCurrentUser().getUid();

        storageReference = FirebaseStorage.getInstance().getReference();
        userDbRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postDBRef = FirebaseDatabase.getInstance().getReference().child("Posts");


        postText = (EditText) findViewById(R.id.postdata);

        galleryPick = (ImageButton) findViewById(R.id.galleryPhoto);

        pDialog = new ProgressDialog(this);
    }

    public void goBackToMainAcrivityFromAddPost(View v){
        Intent back = new Intent(this, MainActivity.class);
        startActivity(back);
    }


    public void pickImageFromGallery(View view) {
        OpenGallery();
    }

    private void OpenGallery() {
        Intent galleru = new Intent();
        galleru.setType("image/*");
        galleru.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(galleru, Gallery_Pick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == Gallery_Pick && resultCode == RESULT_OK && data != null){
            image_uri = data.getData();

            galleryPick.setImageURI(image_uri);
        }
    }

    public void sendPost(View view) {
        checkPostValidate();
    }

    private void checkPostValidate() {
        text = postText.getText().toString();

        if(TextUtils.isEmpty(text)){
            Toast.makeText(this, "Введите текст поста!", Toast.LENGTH_SHORT);
        }else if(image_uri == null){
            makePostWithoutImage();
        }else{
            makePostWithImage();
        }
    }

    private void makePostWithoutImage() {
        String saveCurrentDate, saveCurrentTime;

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MM dd, yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm, aa");
        saveCurrentTime = currentTime.format(calForTime.getTime());

        final HashMap post = new HashMap();
        post.put("date", saveCurrentDate);
        post.put("text", text);
        post.put("time", saveCurrentTime);
        post.put("uid", current_UserID);

        postDBRef.child(current_UserID).updateChildren(post).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                LoadMainScreen();
            }
        });
    }


    private void makePostWithImage() {
        final String saveCurrentDate, saveCurrentTime;

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd MM, yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm, aa");
        saveCurrentTime = currentTime.format(calForTime.getTime());



        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("users_post_images");
        StorageReference filePath = storageRef.child(current_UserID + text + ".jpg");

        filePath.putFile(image_uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    String download_url;
                    download_url =  task.getResult().getDownloadUrl().toString();

                    final HashMap post = new HashMap();
                    post.put("date", saveCurrentDate);
                    post.put("text", text);
                    post.put("time", saveCurrentTime);
                    post.put("post_image", download_url);
                    post.put("uid", current_UserID);

                    postDBRef.child(current_UserID + saveCurrentDate + saveCurrentTime + 1).updateChildren(post).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            LoadMainScreen();
                        }
                    });
                } else {
                    String message = task.getException().getLocalizedMessage();
                    pDialog.dismiss();
                    Toast.makeText(AddPostActivity.this, "Упс! Произошла ошибка: " + message, Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    private void LoadMainScreen(){
        Intent main = new Intent(this, MainActivity.class);

        startActivity(main);
    }


}

