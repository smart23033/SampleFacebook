package example.tacademy.com.samplefacebook;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.DefaultAudience;
import com.facebook.login.LoginBehavior;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {


    LoginButton loginButton;
    CallbackManager callbackManager;
    LoginManager mLoginManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        callbackManager = CallbackManager.Factory.create();
        mLoginManager = LoginManager.getInstance();
        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions("email");
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Toast.makeText(MainActivity.this, "facebook login success", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });

        facebookButton = (Button) findViewById(R.id.btn_login);
        facebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isLogin()) {
                    logoutFacebook();
                } else {
                    loginFacebook();
                }
            }
        });
        setButtonLabel();

        Button btn = (Button) findViewById(R.id.btn_info);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getInfo();
            }
        });

        btn = (Button)findViewById(R.id.btn_read);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                readPost();
            }
        });

        btn = (Button)findViewById(R.id.btn_write);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    writePost();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void writePost() throws JSONException {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken != null) {
            if (accessToken.getPermissions().contains("publish_actions")) {
                GraphRequest request = GraphRequest.newPostRequest(
                        accessToken,
                        "/me/feed",
                        new JSONObject("{\"message\":\"This is Test Message\"}"),
                        new GraphRequest.Callback() {
                            @Override
                            public void onCompleted(GraphResponse response) {
                                // Insert your code here
                            }
                        });
                request.executeAsync();
                return;
            }
        }
        mLoginManager.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                try {
                    writePost();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });

        mLoginManager.logInWithPublishPermissions(this, Arrays.asList("publish_actions"));
    }
    private void readPost() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken != null) {
            if (accessToken.getPermissions().contains("user_posts")) {
                GraphRequest request = GraphRequest.newGraphPathRequest(
                        accessToken,
                        "/me/feed",
                        new GraphRequest.Callback() {
                            @Override
                            public void onCompleted(GraphResponse response) {
                                if (response.getJSONObject() != null) {
                                    Toast.makeText(MainActivity.this, response.getRawResponse(), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(MainActivity.this, response.getError().getErrorMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                request.executeAsync();
                return;
            }
        }
        mLoginManager.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                readPost();
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });

        mLoginManager.logInWithReadPermissions(this, Arrays.asList("user_posts"));
    }
    private void getInfo() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken != null) {
            if (accessToken.getPermissions().contains("email")) {
                GraphRequest request = GraphRequest.newMeRequest(
                        accessToken,
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                if (object != null) {
                                    String id = object.optString("id");
                                    String name = object.optString("name");
                                    String email = object.optString("email");
                                    Toast.makeText(MainActivity.this, id + "," + name + "," + email, Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(MainActivity.this, "error : " + response.getError().getErrorMessage(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email");
                request.setParameters(parameters);
                request.executeAsync();
                return;
            }
        }
        mLoginManager.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                getInfo();
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });

        mLoginManager.logInWithReadPermissions(this, Arrays.asList("email"));
    }

    private void setButtonLabel() {
        if (isLogin()) {
            facebookButton.setText("logout");
        } else {
            facebookButton.setText("login");
        }
    }

    AccessTokenTracker mTracker;

    @Override
    protected void onStart() {
        super.onStart();
        if (mTracker == null) {
            mTracker = new AccessTokenTracker() {
                @Override
                protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                    setButtonLabel();
                }
            };
        } else {
            mTracker.startTracking();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mTracker.stopTracking();
    }

    private void logoutFacebook() {
        mLoginManager.logOut();
    }

    Button facebookButton;

    private void loginFacebook() {
        mLoginManager.setDefaultAudience(DefaultAudience.FRIENDS);
        mLoginManager.setLoginBehavior(LoginBehavior.NATIVE_WITH_FALLBACK);
        mLoginManager.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Toast.makeText(MainActivity.this, "login manager...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });

        mLoginManager.logInWithReadPermissions(this, Arrays.asList("email"));
    }

    private boolean isLogin() {
        AccessToken token = AccessToken.getCurrentAccessToken();
        return token != null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

}
