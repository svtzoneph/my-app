package site.zonevault.zonevault;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;
public class SplashActivity extends Activity {
    @Override protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);
        ImageView icon=findViewById(R.id.splashIcon);
        TextView name=findViewById(R.id.splashName);
        ScaleAnimation scale=new ScaleAnimation(0.7f,1f,0.7f,1f,Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
        scale.setDuration(600);
        AlphaAnimation fade=new AlphaAnimation(0f,1f);fade.setDuration(600);
        AnimationSet anim=new AnimationSet(true);anim.addAnimation(scale);anim.addAnimation(fade);
        icon.startAnimation(anim);
        AlphaAnimation fadeText=new AlphaAnimation(0f,1f);fadeText.setDuration(700);fadeText.setStartOffset(300);
        name.startAnimation(fadeText);
        new Handler().postDelayed(new Runnable(){@Override public void run(){startActivity(new Intent(SplashActivity.this,MainActivity.class));overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);finish();}},2000);
    }
    @Override public void onBackPressed(){}
}