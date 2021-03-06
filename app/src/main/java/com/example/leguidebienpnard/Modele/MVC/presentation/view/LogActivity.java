package com.example.leguidebienpnard.Modele.MVC.presentation.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.leguidebienpnard.Modele.MVC.data.GbpApi;
import com.example.leguidebienpnard.Modele.MVC.presentation.controler.LogController;
import com.example.leguidebienpnard.Modele.MVC.presentation.controler.MainController;
import com.example.leguidebienpnard.Modele.MVC.presentation.model.Objet;
import com.example.leguidebienpnard.Modele.MVC.presentation.model.User;
import com.example.leguidebienpnard.Modele.MVC.presentation.model.UserConstructor;
import com.example.leguidebienpnard.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class LogActivity extends BaseActivity implements View.OnClickListener{


    private ProgressBar progressBar;
    public EditText editTextEmail;
    public EditText editTextMDP;
    public EditText editTextMDP2;
    public FirebaseAuth mAuth;
    private Button buttonSwitch;
    private TextView textViewMode;
    Toast toast;
    private static final String TAG = "EmailPassword";
    private String email;

    private LogController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_main);
        setProgressBar(R.id.progressBar);

        controller = new LogController();

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        // View
        textViewMode = findViewById(R.id.textViewMode);
        // Views
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextMDP = findViewById(R.id.editTextMDP);
        editTextMDP2 = findViewById(R.id.editTextMDP2);

        //Buttons
        buttonSwitch = (Button) findViewById(R.id.buttonSwich);
        findViewById(R.id.buttonValider).setOnClickListener(this);
        findViewById(R.id.buttonCreateAccount).setOnClickListener(this);
        buttonSwitch.setOnClickListener(this);

        toast = new Toast(this);

        mAuth = FirebaseAuth.getInstance();
        //authentification = new Authentification(getApplicationContext());

    }

    @Override
    public void onClick(View v){
        int i = v.getId();
        if(i == R.id.buttonValider){ // action lors d'un click sur le bouton validé
            if (!validateForm()) {
                return;
            }
            signIn(editTextEmail.getText().toString(), editTextMDP.getText().toString());
        } else if (i == R.id.buttonCreateAccount) { // action lors d'un click sur le bouton créer
            if (!validateForm()) {
                return;
            }
            String first = editTextMDP.getText().toString();
            String second = editTextMDP2.getText().toString();
            if(!first.equals(second)){
                editTextMDP.setText("");
                editTextMDP2.setText("");
                editTextMDP.setError("Veuillez entrer le même mot de passe");
                return;
            }
            createAccount(editTextEmail.getText().toString(), editTextMDP.getText().toString());
            updateUI(null);
            buttonSwitch.setText("Créer un compte");
            findViewById(R.id.buttonValider).setVisibility(View.VISIBLE);
            findViewById(R.id.buttonCreateAccount).setVisibility(View.GONE);
        } else if(i == R.id.buttonSwich){
            if(buttonSwitch.getText().toString().equals("Créer un compte")){
                buttonSwitch.setText("S'authentifier");
                textViewMode.setText("Créer un compte");
                findViewById(R.id.buttonValider).setVisibility(View.GONE);
                findViewById(R.id.tableRowMDP2).setVisibility(View.VISIBLE);
                findViewById(R.id.buttonCreateAccount).setVisibility(View.VISIBLE);
            } else if(buttonSwitch.getText().toString().equals("S'authentifier")){
                buttonSwitch.setText("Créer un compte");
                textViewMode.setText("Connexion");
                findViewById(R.id.buttonValider).setVisibility(View.VISIBLE);
                findViewById(R.id.tableRowMDP2).setVisibility(View.GONE);
                findViewById(R.id.buttonCreateAccount).setVisibility(View.GONE);
            }
        }
    }




    private void updateUI(FirebaseUser user){
        if(user != null){
            Intent intent = new Intent(this, MainActivity.class);
            String[] userAdrress = user.getEmail().split("@");
            String userName = userAdrress[0];
            intent.putExtra("userName", userName);
            startActivity(intent);
        }
        hideProgressBar();
        editTextEmail.setText("");
        editTextEmail.requestFocus();
        editTextMDP.setText("");
        editTextMDP2.setText("");

    }

    protected void connexionSucess(){
        toast.makeText(getApplicationContext(), "Authentication réussi.",
                Toast.LENGTH_SHORT).show();
    }

    protected void verificationNeeded(){
        toast.makeText(getApplicationContext(), "Votre adresse email doit être vérifier.",
                Toast.LENGTH_SHORT).show();
    }

    protected void connexionFail(){
        toast.makeText(getApplicationContext(), "L'authentication a échoué.",
                Toast.LENGTH_SHORT).show();
    }

    protected void accountCreated(){
        toast.makeText(getApplicationContext(), "Compte créé, veuillez verifier l'email",
                Toast.LENGTH_SHORT).show();
    }

    protected void creationFailed(){
        toast.makeText(getApplicationContext(), "La création du compte a échoué",
                Toast.LENGTH_SHORT).show();
    }



    protected boolean validateForm(){
        if(controller.isEmailValid(editTextEmail.getText().toString())){
            if(!editTextMDP.getText().toString().equals("") || editTextMDP.getText().length()>6){
                return true;
            } else {
                editTextMDP.setError("Le mot de passe doit faire minimum 6 charactères.");
            }
        } else {
            editTextEmail.setError("Veuilez entrer une adresse valide.");
        }
        return false;
    }



    public void signIn(String email, String password){
        showProgressBar();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            if(mAuth.getCurrentUser().isEmailVerified()){
                                Log.d(TAG, "signInWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                updateUI(user);
                            } else {
                                verificationNeeded();
                            }
                        } else {
                            Log.w(TAG, "signInWithEmail:failure",task.getException());
                            connexionFail();
                        }

                        // [START_EXCLUDE]
                        hideProgressBar();
                        // [END_EXCLUDE]
                    }
                });
    }

    public void createAccount(String userEmail, String password) {
        showProgressBar();
        email = userEmail;
        // [START create_user_with_email]
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "createUserWithEmail:success");
                            sendEmailVerification();
                            accountCreated();
                            controller.makeApiCallPatchNewUser(email);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            creationFailed();
                        }
                        // [START_EXCLUDE]
                        hideProgressBar();
                        // [END_EXCLUDE]
                    }
                });
    }



    public void sendEmailVerification() {

        // Send verification email
        // [START send_email_verification]
        final FirebaseUser user = mAuth.getCurrentUser();
        user.sendEmailVerification()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // [START_EXCLUDE]
                        // Re-enable button

                        if (task.isSuccessful()) {
                            Log.d(TAG, "sendingEmail:success");
                        } else {
                            Log.e(TAG, "sendEmailVerification", task.getException());
                        }
                        // [END_EXCLUDE]
                    }
                });
        // [END send_email_verification]
    }


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        mAuth.signOut();
    }
}
