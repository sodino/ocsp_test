package dns.ocsp.com;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXCertPathValidatorResult;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String URL = "https://api.meipai.com/";

    private OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn = (Button) findViewById(R.id.btnGet);
        btn.setOnClickListener(this);



        // Install the all-trusting trust manager

        client = new OkHttpClient.Builder().dns(new HttpDNS()).build();


    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btnGet:{
                reqGetUrl();
            }break;
        }
    }

    private void reqGetUrl() {
        new Thread(){
            @Override
            public void run() {
                Security.setProperty("ocsp.enable", "true");
                Log.d("Test", "start req...");
                String response = runReq();
                Log.d("Test", response);
            }
        }.start();

    }

    private String runReq() {
        Security.setProperty("ocsp.enable", String.valueOf(true));
        Request request = new Request.Builder().url(URL).build();

        try{
            Response response = client.newCall(request).execute();
            return response.body().string();
        }catch(Exception e){
            e.printStackTrace();
            return "Exception";
        }
    }
}
