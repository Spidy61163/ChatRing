package pk.edu.itu.bsai23023.chatring.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import pk.edu.itu.bsai23023.chatring.databinding.ActivityVerifyNumberBinding;

public class VerifyNumberActivity extends AppCompatActivity {

    ActivityVerifyNumberBinding binding;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVerifyNumberBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        if(auth.getCurrentUser() != null)
        {
            Intent intent = new Intent(VerifyNumberActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        binding.numberTxt.requestFocus();

        binding.continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(VerifyNumberActivity.this, OTPActivity.class);
                intent.putExtra("phoneNumber",binding.numberTxt.getText().toString());
                startActivity(intent);
            }
        });
    }
}