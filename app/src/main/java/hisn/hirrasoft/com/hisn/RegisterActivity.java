package hisn.hirrasoft.com.hisn;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {
    private EditText email_register;
    private EditText password_register;
    private EditText password_repeat_register;

    private Button createAccountBtn;

    private ProgressDialog loadBar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        email_register = (EditText) findViewById(R.id.r_email_input);
        password_register = (EditText) findViewById(R.id.r_password_input);
        password_repeat_register = (EditText) findViewById(R.id.r_password_repeat_input);
        createAccountBtn = (Button) findViewById(R.id.createAccount);

        createAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAccount();
            }
        });

        loadBar = new ProgressDialog(this);
    }

    @Override
    protected void onStart(){
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null){
            ShowMain();
        }
    }

    public void ShowMain(){
        Intent main = new Intent(RegisterActivity.this, MainActivity.class);
        main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(main);
        finish();
    }

    private void createAccount(){
        String email = email_register.getText().toString();
        String password = password_register.getText().toString();
        String password_repeat = password_repeat_register.getText().toString();

        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Введите ваш email...", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Придумайте ваш пароль...", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(password_repeat)){
            Toast.makeText(this, "Просим вас повторить ваш пароль...", Toast.LENGTH_SHORT).show();
        }else if(password.length() < 6){
            Toast.makeText(this, "Ваш пароль должен быть более 6 символов...", Toast.LENGTH_SHORT).show();
        }else if(!password.equals(password_repeat)){
            Toast.makeText(this, "Упс! Пароли не совпадают...", Toast.LENGTH_SHORT).show();
        }else{
            loadBar.setTitle("Создание аккаунта");
            loadBar.setMessage("Идет соединение с сервером... Просим вас подождать...");
            loadBar.show();
            loadBar.setCanceledOnTouchOutside(true);

            Task<AuthResult> authResultTask = mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(RegisterActivity.this, "Вы успешно создали свой аккаунт!", Toast.LENGTH_SHORT).show();
                                loadBar.dismiss();
                                settingAppAccount();
                            }else {
                                String message = task.getException().getMessage();
                                Toast.makeText(RegisterActivity.this, "Упс! Произошла ошибка: " + message, Toast.LENGTH_SHORT).show();
                                loadBar.dismiss();
                            }
                        }
                    });
        }
    }



    public void  loginInMakedAccount(View view){
        Intent loginStart = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(loginStart);
    }

    private void settingAppAccount(){
        Intent setup = new Intent(RegisterActivity.this, SettingsActivity.class);
        setup.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setup);
        finish();;
    }
}
