package sivani2020.blogspot.examrecyclerview;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

public class splishScreen extends AppCompatActivity {
    ImageView appLogo,nameLogo;
    Animation top,bottom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splish_screen);

        appLogo=findViewById(R.id.applogo);
        nameLogo=findViewById(R.id.nameLogo);

        top= AnimationUtils.loadAnimation(getApplicationContext(),R.anim.app_logo_animation);
        bottom= AnimationUtils.loadAnimation(getApplicationContext(),R.anim.app_name_logo_animation);

        appLogo.setAnimation(top);
        nameLogo.setAnimation(bottom);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                    Intent intent=new Intent(splishScreen.this,MainActivity.class);
                    startActivity(intent);
                    finish();
            }
        }, 4000);
    }
}