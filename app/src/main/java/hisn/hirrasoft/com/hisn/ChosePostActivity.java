package hisn.hirrasoft.com.hisn;

import android.content.Intent;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChosePostActivity extends AppCompatActivity {

    private String postKey, curent_userid, database_userid, profile_image;
    private DatabaseReference databaseReference, userReference;

    private FirebaseAuth mAuth;

    private ImageView editGallery, postImage;
    private EditText editText;

    private TextView username, date_show, post_body;

    private RelativeLayout can_edit, cant_edit;

    private CircleImageView avatar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chose_post);

        postKey = getIntent().getExtras().get("post_key").toString();

        mAuth = FirebaseAuth.getInstance();
        curent_userid = mAuth.getCurrentUser().getUid();

        avatar = (CircleImageView) findViewById(R.id.edit_profile_avatar_post);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Posts").child(postKey);
        userReference = FirebaseDatabase.getInstance().getReference().child("Users");

        editGallery = (ImageView) findViewById(R.id.edit_gallery_pick);
        postImage = (ImageView) findViewById(R.id.edit_image_post_view);

        editText = (EditText) findViewById(R.id.editpostdata);

        username = (TextView) findViewById(R.id.edit_username);
        date_show = (TextView) findViewById(R.id.date);
        post_body = (TextView) findViewById(R.id.edit_text_view);

        can_edit = (RelativeLayout) findViewById(R.id.editor);
        cant_edit = (RelativeLayout) findViewById(R.id.post_shower);




        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
               if(dataSnapshot.exists()){
                   String text = dataSnapshot.child("text").getValue().toString();
                  // profile_image = dataSnapshot.child("profile_image").getValue().toString();
                   String uid = dataSnapshot.child("uid").getValue().toString();
                   String date = dataSnapshot.child("date").getValue().toString();
                   String time = dataSnapshot.child("time").getValue().toString();
                   String fullname = dataSnapshot.child("fullname").getValue().toString();
                   database_userid = dataSnapshot.child("uid").getValue().toString();
                   if(dataSnapshot.child("post_image").getValue() != null){
                       String image = dataSnapshot.child("post_image").getValue().toString();
                       postImage.setVisibility(View.VISIBLE);
                       Picasso.get().load(image).into(editGallery);
                       Picasso.get().load(image).into(postImage);
                   }

                   editText.setText(text);
                   post_body.setText(text);



                   String full_date = date + " в " + time;

                   date_show.setText(full_date);

                   username.setText(fullname);

                   if(curent_userid.equals(database_userid)){
                       can_edit.setVisibility(View.VISIBLE);
                       cant_edit.setVisibility(View.GONE);
                   }else{
                       can_edit.setVisibility(View.GONE);
                       cant_edit.setVisibility(View.VISIBLE);
                   }

                   userReference.child(database_userid).addListenerForSingleValueEvent(new ValueEventListener() {
                       @Override
                       public void onDataChange(DataSnapshot dataSnapshot) {

                                Log.d("Hello: ", dataSnapshot.toString());

                               profile_image = dataSnapshot.child("profile_avatar").getValue().toString();
                               Log.d("Chose_post", profile_image);
                               Picasso.get().load(profile_image).into(avatar);


                       }

                       @Override
                       public void onCancelled(DatabaseError databaseError) {

                       }
                   });

               }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }



    public void DeletePostButton(View view) {
        databaseReference.removeValue();

        LoadMainScreen();
    }

    public void EditPost(View view) {
        databaseReference.child("text").setValue(editText.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(ChosePostActivity.this, "Ваш пост успешно обновлен!" , Toast.LENGTH_SHORT);
                            LoadMainScreen();
                        }else{
                            String message = task.getException().getMessage().toString();
                            Toast.makeText(ChosePostActivity.this, "Упс! Ошибка: " + message, Toast.LENGTH_SHORT);

                        }
                    }
                });
    }

    public void postImageEdit(View view) {
    }

    public void goBackToMainAcrivityFromAddPost(View view) {
        LoadMainScreen();
    }

    private void LoadMainScreen(){
        Intent main = new Intent(this, MainActivity.class);
        main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(main);
        finish();
    }
}
