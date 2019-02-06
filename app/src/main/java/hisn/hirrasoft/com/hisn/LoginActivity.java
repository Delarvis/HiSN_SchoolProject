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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private EditText email_login;
    private EditText password_login;

    private Button loginBtn;

    private FirebaseAuth mAuth;

    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginBtn = (Button) findViewById(R.id.login_in_soc_net);

        pDialog = new ProgressDialog(this);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginInSN();
            }
        });

        email_login = (EditText) findViewById(R.id.l_email_input);
        password_login = (EditText) findViewById(R.id.l_password_input);

        mAuth = FirebaseAuth.getInstance();


    }

    @Override
    protected void onStart(){
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null){
            ShowMain();
        }
    }

    public void registerAccount(View view){
        Intent registerStart = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(registerStart);
    }

    public void facebookLogin(View view){

    }

    public void twitterLogin(View view){

    }

    private void LoginInSN(){
        pDialog.setTitle("Выполняется вход");
        pDialog.setMessage("Выполняется соединение с сервером... Пожалуйста подождите...");
        pDialog.show();

        String email = email_login.getText().toString();
        String password = password_login.getText().toString();

        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Введите ваш логин...", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Введите ваш пароль...", Toast.LENGTH_SHORT).show();
        }else{
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(LoginActivity.this, "Вы успешно авторизованы!", Toast.LENGTH_SHORT).show();
                                ShowMain();
                                pDialog.dismiss();
                            }else{
                                String message = task.getException().getMessage();
                                Toast.makeText(LoginActivity.this, "Упс! Произошла ошибка: "+message, Toast.LENGTH_SHORT).show();
                                pDialog.dismiss();
                            }
                        }
                    });
        }
    }

    public void ShowMain(){
        Intent main = new Intent(LoginActivity.this, MainActivity.class);
        main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(main);
        finish();
    }


}
