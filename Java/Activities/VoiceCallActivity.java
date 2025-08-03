package pk.edu.itu.bsai23023.chatring.Activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import pk.edu.itu.bsai23023.chatring.Models.User;
import pk.edu.itu.bsai23023.chatring.R;
import pk.edu.itu.bsai23023.chatring.Token.RetrofitClient;
import pk.edu.itu.bsai23023.chatring.Token.TokenApi;
import pk.edu.itu.bsai23023.chatring.Token.TokenRequest;
import pk.edu.itu.bsai23023.chatring.Token.TokenResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VoiceCallActivity extends AppCompatActivity {

    private static final int PERMISSION_REQ_ID = 22;
    private static final String[] REQUESTED_PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO
    };

    private RtcEngine agoraEngine;
    private boolean isMuted = false;

    private String appId = "b152c92dc33145ba8671be9f4c7ccee5";
    private String token = "";
    private String channelName = "";

    private TextView receiverNameTextView;
    private Button joinCallButton;

    private static final String[] userNames = {
            "User 1", "User 2", "User 3"  // For example, map UIDs to names
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_call);

        // Initialize UI components
        receiverNameTextView = findViewById(R.id.receiverName);
        joinCallButton = findViewById(R.id.joinCallButton);

        // Get receiver user from the Intent
        User receiverUser = (User) getIntent().getSerializableExtra("receiverUser");
        if (receiverUser != null) {
            receiverNameTextView.setText("Calling " + receiverUser.getName());
        }

        // Fetching the weirdId for sender and receiver asynchronously
        String senderId = getIntent().getStringExtra("senderId");
        String receiverId = getIntent().getStringExtra("receiverId");

        DatabaseReference senderRef = FirebaseDatabase.getInstance().getReference().child("users").child(senderId).child("weirdId");
        DatabaseReference receiverRef = FirebaseDatabase.getInstance().getReference().child("users").child(receiverId).child("weirdId");

        senderRef.get().addOnCompleteListener(task1 -> {
            if (task1.isSuccessful()) {
                long id1 = task1.getResult().getValue(Long.class);
                Log.d("VoiceCallActivity", "Sender Weird ID: " + id1);

                receiverRef.get().addOnCompleteListener(task2 -> {
                    if (task2.isSuccessful()) {
                        long id2 = task2.getResult().getValue(Long.class);
                        Log.d("VoiceCallActivity", "Receiver Weird ID: " + id2);

                        // Construct the channelName after fetching both IDs
                        long name = id1 + id2;
                        channelName = "" + name;
                        Log.d("VoiceCallActivity", "Channel Name: " + channelName);

                        // Fetch the token for the channel
                        fetchToken();
                    } else {
                        Log.e("VoiceCallActivity", "Failed to fetch receiver's weirdId");
                    }
                });

            } else {
                Log.e("VoiceCallActivity", "Failed to fetch sender's weirdId");
            }
        });

        // Check for permissions before initializing Agora engine
        if (!checkSelfPermission()) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, PERMISSION_REQ_ID);
        } else {
            setupAgoraEngine(); // Initialize Agora engine if permissions are granted
        }

        // Join call button click listener
        joinCallButton.setOnClickListener(this::joinCall);

        // Mute button click listener
        Button muteButton = findViewById(R.id.muteButton);
        muteButton.setOnClickListener(this::toggleMute);

        // End call button click listener
        Button endCallButton = findViewById(R.id.endCallButton);
        endCallButton.setOnClickListener(this::endCall);
    }

    private boolean checkSelfPermission() {
        return ContextCompat.checkSelfPermission(this, REQUESTED_PERMISSIONS[0]) == PackageManager.PERMISSION_GRANTED;
    }

    private void setupAgoraEngine() {
        try {
            // Initialize Agora Engine
            agoraEngine = RtcEngine.create(getBaseContext(), appId, mRtcHandler);
            agoraEngine.enableAudio(); // Enable audio for the voice call
        } catch (Exception e) {
            showMessage("Error initializing Agora SDK: " + e.getMessage());
            Log.e("VoiceCallActivity", "Error initializing Agora SDK", e);
        }
    }

    private final IRtcEngineEventHandler mRtcHandler = new IRtcEngineEventHandler() {
        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            Log.d("VoiceCallActivity", "Joined channel: " + channel + " with UID: " + uid);
            runOnUiThread(() -> {
                showMessage("You joined the channel.");
                joinCallButton.setText("Joined"); // Change the button text to 'Joined'
                // Keep the receiver's name as "Calling" until other user leaves
            });
        }

        @Override
        public void onUserJoined(int uid, int elapsed) {
            runOnUiThread(() -> {
                showMessage("User joined the call");
            });
        }

        @Override
        public void onUserOffline(int uid, int reason) {
            runOnUiThread(() -> {
                receiverNameTextView.setText("User left the call");
                showMessage("User left the call");
            });
        }
    };

    public void joinCall(View view) {
        if (agoraEngine == null) {
            showMessage("Agora Engine not initialized");
            return;
        }

        if (checkSelfPermission()) {
            ChannelMediaOptions options = new ChannelMediaOptions();
            options.channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION;
            options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;

            agoraEngine.joinChannel(token, channelName, 0, options);
            showMessage("You joined the voice channel.");
        } else {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, PERMISSION_REQ_ID);
        }
    }

    public void endCall(View view) {
        if (agoraEngine != null) {
            agoraEngine.leaveChannel(); // Leave the channel and end the call
        }
        showMessage("You left the voice channel.");
        finish(); // Close the activity
    }

    public void toggleMute(View view) {
        if (agoraEngine != null) {
            isMuted = !isMuted;
            agoraEngine.muteLocalAudioStream(isMuted);

            Button muteButton = (Button) view;
            muteButton.setText(isMuted ? "Unmute" : "Mute");
            showMessage(isMuted ? "Muted" : "Unmuted");
        }
    }

    private void showMessage(String message) {
        runOnUiThread(() -> Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show());
    }

    private void fetchToken() {
        TokenApi tokenApi = RetrofitClient.getInstance().create(TokenApi.class);
        TokenRequest request = new TokenRequest(
                "rtc",        // Token type
                appId,
                "efb86c6bb5b84e4cb5dbcca973b6309c",  // Your certificate
                "0",
                channelName,
                3600
        );

        tokenApi.generateToken(request).enqueue(new Callback<TokenResponse>() {
            @Override
            public void onResponse(Call<TokenResponse> call, Response<TokenResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    token = response.body().token;
                    Log.d("VoiceCallActivity", "Token fetched: " + token);
                    showMessage("Token fetched successfully!");
                } else {
                    showMessage("Failed to fetch token.");
                }
            }

            @Override
            public void onFailure(Call<TokenResponse> call, Throwable t) {
                Log.e("VoiceCallActivity", "API call failed: " + t.getMessage());
                showMessage("API call failed: " + t.getMessage());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (agoraEngine != null) {
            agoraEngine.leaveChannel(); // Ensure leaving the channel
            RtcEngine.destroy(); // Clean up resources
            agoraEngine = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQ_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupAgoraEngine();
            } else {
                showMessage("Permission denied");
            }
        }
    }
}