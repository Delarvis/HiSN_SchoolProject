package hisn.hirrasoft.com.hisn;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.soundcloud.android.crop.Crop;
import com.soundcloud.android.crop.CropImageActivity;
import com.soundcloud.android.crop.CropImageView;

import java.io.File;
import java.net.URI;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private EditText s_username;
    private EditText s_name;
    private EditText s_date;
    private EditText s_country;
    private EditText s_last_name;

    private CircleImageView profileAvatar;

    private RadioGroup genderChoice;

    private Button save_data;

    private DatabaseReference dbRef;
    private FirebaseAuth mAuth;
    private StorageReference praRef;

    private String current_userId;

    static final int Gallery_Pick = 1;


    private String gender;

    private String avatar_view = "null";

    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        s_username = (EditText) findViewById(R.id.s_username_input);
        s_country = (EditText) findViewById(R.id.s_user_country);
        s_name = (EditText) findViewById(R.id.s_user_fistname);
        s_last_name = (EditText) findViewById(R.id.s_user_last_name);

        s_date = (EditText) findViewById(R.id.s_user_birthday);
        profileAvatar = (CircleImageView) findViewById(R.id.s_user_avatar);

        pDialog = new ProgressDialog(this);

        genderChoice = (RadioGroup) findViewById(R.id.gender);

        genderChoice.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id) {
                switch (id){
                    case -1:
                        gender = "";
                        break;
                    case(R.id.male):
                        gender = "male";
                        break;
                    case(R.id.female):
                        gender = "female";
                        break;
                }
            }
        });

        mAuth = FirebaseAuth.getInstance();
        current_userId = mAuth.getCurrentUser().getUid();
        dbRef = FirebaseDatabase.getInstance().getReference().child("Users").child(current_userId);
        praRef = FirebaseStorage.getInstance().getReference().child("profile_image");

        save_data = (Button) findViewById(R.id.save_account_setup);

        save_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveAccountChanges();
            }
        });
    }


    private void saveAccountChanges(){
        pDialog.setTitle("Сохранение данных");
        pDialog.setMessage("Соединение с сервером и загрузка данных на него...");
        pDialog.show();

        String name = s_name.getText().toString();
        String country = s_country.getText().toString();
        String username = s_username.getText().toString();
        String date = s_date.getText().toString();
        String last_name = s_last_name.getText().toString();
        String uid  = mAuth.getCurrentUser().getUid();
        String ban_status = "0";
        String ver_status = "0";
        String ab_user = "";
        //String status = " ";
        String population_mark = "0";
        String frinds_count = "0";

        if(TextUtils.isEmpty(name)){
            Toast.makeText(this, "Скажите ваше настоящее имя...", Toast.LENGTH_SHORT).show();
            pDialog.dismiss();
        }else if(TextUtils.isEmpty(country)){
            Toast.makeText(this, "Скажите откуда вы...", Toast.LENGTH_SHORT).show();
            pDialog.dismiss();
        }else if(TextUtils.isEmpty(username)) {
            Toast.makeText(this, "Придумайте имя пользователя...", Toast.LENGTH_SHORT).show();
            pDialog.dismiss();
        }else if(TextUtils.isEmpty(date)) {
            Toast.makeText(this, "Просим указать вас вашу дату рождения...", Toast.LENGTH_SHORT).show();
            pDialog.dismiss();
        }else if(gender == ""){
            Toast.makeText(this, "Выберите свой пол (Гермофрадиты не могут пользоваться соц. сетью ^-^)", Toast.LENGTH_SHORT).show();
            pDialog.dismiss();
        }else if(avatar_view == "null") {
            Toast.makeText(this, "Просим вставить вашу аватарку...", Toast.LENGTH_SHORT).show();
            pDialog.dismiss();
        }else if(TextUtils.isEmpty(last_name)) {
            Toast.makeText(this, "Скажите вашу настоящую фамилию...", Toast.LENGTH_SHORT).show();
            pDialog.dismiss();
        }else {
                HashMap userMap = new HashMap();
                userMap.put("name", name);
                userMap.put("country", country);
                userMap.put("username", username);
                userMap.put("last_name", last_name);
                userMap.put("date", date);
                userMap.put("ver_status", ver_status);
                userMap.put("ban_status", ban_status);
                userMap.put("ab_user", ab_user);
                userMap.put("uid", uid);
                //userMap.put("status", status);
                userMap.put("friends_count", frinds_count);
                userMap.put("population_mark", population_mark);
                userMap.put("gender", gender);
                dbRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){
                            pDialog.dismiss();
                            Toast.makeText(SettingsActivity.this, "Все изменения сохранены!", Toast.LENGTH_SHORT).show();
                            LoadMainScreen();
                        }else{
                            String message = task.getException().getMessage();
                            pDialog.dismiss();
                            Toast.makeText(SettingsActivity.this, "Упс! Произошла ошибка: " + message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }




        private void LoadMainScreen(){
            Intent main = new Intent(SettingsActivity.this, MainActivity.class);
            main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(main);
            finish();
        }

    public void pickAvatar(View view) {
        Intent galleru = new Intent();
        galleru.setType("image/*");
        galleru.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(galleru, Gallery_Pick);
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode, Intent data) {
          super.onActivityResult(requestCode, resultCode, data);

          if(data != null) {
              pDialog.setTitle("Сохранение данных");
              pDialog.setMessage("Соединение с сервером и загрузка данных на него...");
              pDialog.show();

              Uri source_image = data.getData();
              Log.d("Settings", "Hello!" + source_image);
              Uri source = data.getData();
              Uri output = Uri.fromFile(new File(getCacheDir(), "croppedAvatar"));

              Crop.of(source, output).asSquare().start(this);

             // profileAvatar.setImageURI(Crop.getOutput(data));

              final Uri result = Uri.fromFile(new File(getCacheDir(), "croppedAvatar"));
              profileAvatar.setImageURI(result);

              Log.d("Main", "Hi: " + result);

              StorageReference filePath = praRef.child(current_userId + ".jpg");
              filePath.putFile(result).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                  @Override
                  public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                      if (task.isSuccessful()) {
                          final String download_url = task.getResult().getDownloadUrl().toString();

                          dbRef.child("profile_avatar").setValue(download_url)
                                  .addOnCompleteListener(new OnCompleteListener<Void>() {
                                      @Override
                                      public void onComplete(@NonNull Task<Void> task) {
                                          if (task.isSuccessful()){
                                              avatar_view = "has image!";
                                              Toast.makeText(SettingsActivity.this, "Ваша аватарка загружена!" , Toast.LENGTH_SHORT).show();
                                              pDialog.dismiss();
                                          }else{
                                              String message = task.getException().getMessage();
                                              pDialog.dismiss();
                                              Toast.makeText(SettingsActivity.this, "Упс! Произошла ошибка при загрузке в базу данных: " + message, Toast.LENGTH_SHORT).show();
                                          }
                                      }
                                  });
                      }else{
                          String message = task.getException().getMessage();
                          pDialog.dismiss();
                          Toast.makeText(SettingsActivity.this, "Упс! Произошла ошибка: " + message, Toast.LENGTH_SHORT).show();
                      }
                  }
              });
          }
    }
}

