package in.oormi.apguide;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;

public class ResourceShow extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        WebView mywebview = (WebView) findViewById(R.id.webViewInfo);

        mywebview.loadUrl("file:///android_asset/infodata.html");      }
}

