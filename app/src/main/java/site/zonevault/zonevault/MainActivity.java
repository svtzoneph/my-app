package site.zonevault.zonevault;
import android.app.Activity;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
public class MainActivity extends Activity {
    private WebView webView;
    private static final String WEBSITE_URL = "https://zonevault.site/";
    private static final String PREFS = "site.zonevault.zonevault.prefs";
    private static final String TRACK_PREFS = "site.zonevault.zonevault.tracking";
    private SwipeRefreshLayout swipeRefresh;
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY|View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        trackAppOpen();
        setContentView(R.layout.activity_main);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        webView = findViewById(R.id.webview);
        setupWebView();
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override public void onRefresh() { webView.reload(); new Handler().postDelayed(new Runnable() { @Override public void run() { swipeRefresh.setRefreshing(false); } }, 1500); }
        });
        loadWebsite();
    }
    private void setupWebView() {
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true); s.setDomStorageEnabled(true);
        s.setLoadWithOverviewMode(true); s.setUseWideViewPort(true);
        s.setBuiltInZoomControls(false); s.setCacheMode(WebSettings.LOAD_DEFAULT);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        s.setMediaPlaybackRequiresUserGesture(false);
        webView.addJavascriptInterface(new CredentialBridge(), "AndroidCreds");
        webView.setWebViewClient(new WebViewClient() {
            @Override public void onPageFinished(WebView view, String pageUrl) {
                SharedPreferences p = getSharedPreferences(PREFS, MODE_PRIVATE);
                String em = p.getString("email", "");
                if (!em.isEmpty()) {
                    String js = "javascript:(function(){var ef=document.querySelectorAll('input[type=email],input[name*=email],input[id*=email]');var pf=document.querySelectorAll('input[type=password]');var em2=window.AndroidCreds.getEmail();var pw2=window.AndroidCreds.getPassword();if(ef.length>0&&em2)ef[0].value=em2;if(pf.length>0&&pw2)pf[0].value=pw2;document.querySelectorAll('form').forEach(function(f){f.addEventListener('submit',function(){var e=document.querySelector('input[type=email],input[name*=email]');var p=document.querySelector('input[type=password]');if(e&&p&&e.value&&p.value)window.AndroidCreds.save(e.value,p.value);});});})();";
                    view.loadUrl(js);
                }
            }
            @Override public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                showOfflinePage(view);
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
        });
    }
    private void showOfflinePage(WebView view){
        String html="<html><body style='background:#111;color:#eee;font-family:sans-serif;display:flex;flex-direction:column;align-items:center;justify-content:center;height:100vh;margin:0;text-align:center'><div style='font-size:64px'>&#128241;</div><h2 style='margin:16px 0 8px'>No Connection</h2><p style='color:#888;max-width:280px'>Check your internet and try again.</p><button onclick='location.reload()' style='margin-top:24px;padding:12px 32px;background:#fff;color:#111;border:none;border-radius:8px;font-size:15px;cursor:pointer;font-weight:bold'>Retry</button></body></html>";
        view.loadData(html,"text/html","UTF-8");
    }
    private void trackAppOpen(){
        SharedPreferences p=getSharedPreferences(TRACK_PREFS,MODE_PRIVATE);
        int total=p.getInt("total_opens",0)+1;
        String today=new SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(new Date());
        p.edit().putInt("total_opens",total).putString("last_open",new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault()).format(new Date())).putString("first_install",p.getString("first_install",today)).apply();
    }
    class CredentialBridge {
        @JavascriptInterface public void save(String email,String password){getSharedPreferences(PREFS,MODE_PRIVATE).edit().putString("email",email).putString("password",android.util.Base64.encodeToString(password.getBytes(),android.util.Base64.NO_WRAP)).apply();}
        @JavascriptInterface public String getEmail(){return getSharedPreferences(PREFS,MODE_PRIVATE).getString("email","");}
        @JavascriptInterface public String getPassword(){String enc=getSharedPreferences(PREFS,MODE_PRIVATE).getString("password","");if(enc.isEmpty())return "";try{return new String(android.util.Base64.decode(enc,android.util.Base64.NO_WRAP));}catch(Exception e){return enc;}}
    }
    private void loadWebsite(){
        ConnectivityManager cm=(ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
        //noinspection deprecation
        NetworkInfo ni=cm!=null?cm.getActiveNetworkInfo():null;
        boolean online=ni!=null&&ni.isConnected();
        if(online)webView.loadUrl(WEBSITE_URL);else showOfflinePage(webView);
    }
    @Override public void onBackPressed(){if(webView!=null&&webView.canGoBack())webView.goBack();else super.onBackPressed();}
    @Override protected void onResume(){super.onResume();if(webView!=null)webView.onResume();}
    @Override protected void onPause(){super.onPause();if(webView!=null)webView.onPause();}
    @Override protected void onDestroy(){if(webView!=null){webView.stopLoading();webView.destroy();}super.onDestroy();}
}