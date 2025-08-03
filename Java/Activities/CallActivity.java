package pk.edu.itu.bsai23023.chatring.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;
import io.agora.rtc2.video.VideoCanvas;
import pk.edu.itu.bsai23023.chatring.Token.RetrofitClient;
import pk.edu.itu.bsai23023.chatring.Token.TokenApi;
import pk.edu.itu.bsai23023.chatring.Token.TokenRequest;
import pk.edu.itu.bsai23023.chatring.Token.TokenResponse;
import pk.edu.itu.bsai23023.chatring.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CallActivity extends AppCompatActivity {

    private static final int PERMISSION_REQ_ID = 22;
    private static final String[] REQUESTED_PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
    };

    private RtcEngine agoraEngine;
    private SurfaceView localSurfaceView, remoteSurfaceView;
    private boolean isMuted = false;
    private boolean isCameraOff = false;

    private String appId = "";  // Your appId
    private String certificate = "";  // Your certificate
    private String token = "";
    private String channelName = "";

    private final IRtcEngineEventHandler mRtcHandler = new IRtcEngineEventHandler() {
        @Override
        public void onUserJoined(int uid, int elapsed) {
            Log.d("CallActivity", "Remote user joined: " + uid);
            runOnUiThread(() -> setupRemoteVideo(uid));
        }

        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            Log.d("CallActivity", "Joined channel: " + channel);
        }

        @Override
        public void onUserOffline(int uid, int reason) {
            Log.d("CallActivity", "User offline: UID = " + uid);
        }
    };

    private boolean checkSelfPermission() {
        return ContextCompat.checkSelfPermission(this, REQUESTED_PERMISSIONS[0]) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, REQUESTED_PERMISSIONS[1]) == PackageManager.PERMISSION_GRANTED;
    }

    private void fetchToken() {
        TokenApi tokenApi = RetrofitClient.getInstance().create(TokenApi.class);

        TokenRequest request = new TokenRequest(
                "rtc",        // Token type
                appId,
                certificate,
                "0",
                channelName,
                3600
        );

        tokenApi.generateToken(request).enqueue(new Callback<TokenResponse>() {
            @Override
            public void onResponse(Call<TokenResponse> call, Response<TokenResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    token = response.body().token;
                    Log.d("CallActivity", "Token fetched: " + token);
                    Toast.makeText(CallActivity.this, "Token fetched successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CallActivity.this, "Failed to fetch token.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<TokenResponse> call, Throwable t) {
                Log.e("CallActivity", "API call failed: " + t.getMessage());
                Toast.makeText(CallActivity.this, "API call failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupAgoraEngine() {
        try {
            RtcEngineConfig config = new RtcEngineConfig();
            config.mContext = getBaseContext();
            config.mAppId = appId;
            config.mEventHandler = mRtcHandler;
            agoraEngine = RtcEngine.create(config);
            agoraEngine.enableVideo();

        } catch (Exception e) {
            Log.e("CallActivity", "Error initializing Agora SDK: " + e.getMessage());
        }
    }

    private void setupLocalVideo() {
        FrameLayout container = findViewById(R.id.localVideo);
        localSurfaceView = new SurfaceView(this);
        container.addView(localSurfaceView);
        agoraEngine.setupLocalVideo(new VideoCanvas(localSurfaceView, VideoCanvas.RENDER_MODE_HIDDEN, 0));
    }

    private void setupRemoteVideo(int uid) {
        FrameLayout container = findViewById(R.id.remoteVideo);
        remoteSurfaceView = new SurfaceView(this);
        container.addView(remoteSurfaceView);
        agoraEngine.setupRemoteVideo(new VideoCanvas(remoteSurfaceView, VideoCanvas.RENDER_MODE_HIDDEN, uid));
    }

    public void joinChannel(View view) {
        if (token.isEmpty()) {
            Toast.makeText(this, "Token not available. Fetching token...", Toast.LENGTH_SHORT).show();
            fetchToken();
            return;
        }

        if (checkSelfPermission()) {
            setupLocalVideo();
            localSurfaceView.setVisibility(View.VISIBLE);
            agoraEngine.startPreview();

            ChannelMediaOptions options = new ChannelMediaOptions();
            options.channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION;
            options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;

            agoraEngine.joinChannel(token, channelName, 0, options);
            Toast.makeText(this, "You joined the channel.", Toast.LENGTH_SHORT).show();
        } else {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, PERMISSION_REQ_ID);
        }
    }

    public void leaveChannel(View view) {
        if (agoraEngine != null) {
            agoraEngine.leaveChannel();
            agoraEngine.stopPreview();
        }
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        String senderId = getIntent().getStringExtra("senderId");
        String receiverId = getIntent().getStringExtra("receiverId");

        // Fetching the weirdId for sender and receiver asynchronously
        DatabaseReference senderRef = FirebaseDatabase.getInstance().getReference().child("users").child(senderId).child("weirdId");
        DatabaseReference receiverRef = FirebaseDatabase.getInstance().getReference().child("users").child(receiverId).child("weirdId");

        senderRef.get().addOnCompleteListener(task1 -> {
            if (task1.isSuccessful()) {
                long id1 = task1.getResult().getValue(Long.class);
                Log.d("CallActivity", "Sender Weird ID: " + id1);

                receiverRef.get().addOnCompleteListener(task2 -> {
                    if (task2.isSuccessful()) {
                        long id2 = task2.getResult().getValue(Long.class);
                        Log.d("CallActivity", "Receiver Weird ID: " + id2);

                        // Construct the channelName after fetching both IDs
                        long name = id1 + id2;

                        channelName = "" + name;
                        Log.d("CallActivity*************", "Channel Name: " + channelName);

                        // Proceed with Agora setup and token fetching
                        if (!checkSelfPermission()) {
                            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, PERMISSION_REQ_ID);
                        } else {
                            setupAgoraEngine();
                            fetchToken();
                        }

                    } else {
                        Log.e("CallActivity", "Failed to fetch receiver's weirdId");
                    }
                });

            } else {
                Log.e("CallActivity", "Failed to fetch sender's weirdId");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (agoraEngine != null) {
            agoraEngine.stopPreview();
            agoraEngine.leaveChannel();
            RtcEngine.destroy();
            agoraEngine = null;
        }
    }

}
