package com.goodchild.zibalba.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import com.facebook.*;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.goodchild.zibalba.ClientConstants;
import com.goodchild.zibalba.R;
import com.goodchild.zibalba.ServerThread;
import com.goodchild.zibalba.Tools;
import com.goodchild.zibalba.widgets.FunkyText;

import java.util.Random;

public class SplashActivity extends Activity implements View.OnClickListener, Animation.AnimationListener {
    ServerThread server;
    private UiLifecycleHelper uiHelper;
    boolean animationFinished;
    Session session;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        findViewById(R.id.guestLogin).setOnClickListener(this);

        uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);

        FunkyText funkyText = (FunkyText) findViewById(R.id.funkyText);
        funkyText.setAnimationListener(this);

        LoginButton facebookLogin = ((LoginButton)findViewById(R.id.facebookLogin));

        facebookLogin.setBackgroundResource(R.drawable.tabbackground);
        facebookLogin.setPadding(20, 0, 0, 0);
        facebookLogin.setTextColor(0xFF4F3F00);
        facebookLogin.setTypeface(Typeface.createFromAsset(getAssets(), "aztec.ttf"));

        if(Session.getActiveSession().isOpened() &&  Session.getActiveSession().getPermissions().contains("publish_actions")){
            session = Session.getActiveSession();
        }
    }
    private void onSessionStateChange(final Session session, SessionState state, Exception exception) {
        if (session.isOpened()) {
            if(session.getPermissions().contains("publish_actions")){
                this.session = session;
                if(animationFinished){
                    facebookLogin(session);
                }
            }else{
                session.requestNewPublishPermissions(new Session.NewPermissionsRequest(this, "publish_actions"));
                this.session = null;
            }
        } else if (session.isClosed()) {
            this.session = null;
        }
    }

    private void facebookLogin(final Session session) {
        if (session != null && session.isOpened()) {
            Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {
                @Override
                public void onCompleted(GraphUser user, Response response) {
                    if (session == Session.getActiveSession() && user != null) {
                            Intent intent = new Intent(SplashActivity.this, TableSelectActivity.class);
                            intent.putExtra(ClientConstants.USER_NAME_EXTRA, user.getName());
                            intent.putExtra(ClientConstants.USER_ICON_EXTRA, 0);
                            intent.putExtra(ClientConstants.USER_PICTURE_PATH_EXTRA, "http://graph.facebook.com/"+user.getUsername()+"/picture");
                            intent.putExtra(ClientConstants.IS_FACEBOOK_LOGIN, true);
                            intent.putExtra(ClientConstants.FACEBOOK_ACCESS_TOKEN, session.getAccessToken());
                            intent.putExtra(ClientConstants.FACEBOOK_APP_ID, session.getApplicationId());
                            intent.putExtra(ClientConstants.FACEBOOK_USER_ID, user.getId());

                            startActivity(intent);
                            finish();
                    }else{
                        if(session != null){
                            session.closeAndClearTokenInformation();
                        }
                        showButtons();
                    }
                }
            });
            Request.executeBatchAsync(request);
        }else{
            showButtons();
        }
    }


    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };


    private void showButtons() {
        if(findViewById(R.id.guestLogin).getVisibility() == View.VISIBLE) return;
        findViewById(R.id.guestLogin).setVisibility(View.VISIBLE);
        findViewById(R.id.facebookLogin).setVisibility(View.VISIBLE);
        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(500);
        animation.setInterpolator(new DecelerateInterpolator());
        animation.setFillBefore(true);

        findViewById(R.id.guestLogin).startAnimation(animation);
        findViewById(R.id.facebookLogin).startAnimation(animation);

    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(this, TableSelectActivity.class);

        SharedPreferences sharedPreferences = getSharedPreferences("SHARED_PREFERENCES", Context.MODE_PRIVATE);
        Random r = new Random();
        String name = sharedPreferences.getString(ClientConstants.USER_NAME_VALUE, Tools.getGuestName());
        int icon = sharedPreferences.getInt(ClientConstants.USER_ICON_VALUE, r.nextInt(ClientConstants.ICON_RESOURCES.length));
        intent.putExtra(ClientConstants.USER_NAME_EXTRA, name);
        intent.putExtra(ClientConstants.USER_ICON_EXTRA, icon);
        intent.putExtra(ClientConstants.USER_PICTURE_PATH_EXTRA, "");
        intent.putExtra(ClientConstants.IS_FACEBOOK_LOGIN, false);

        startActivity(intent);

        finish();
    }

    public void onResume() {
        super.onResume();
        uiHelper.onResume();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {
        animationFinished = true;
        if(session != null){
            facebookLogin(session);
        }else{
            showButtons();
        }
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }
}
