package janicka.zofia.sourcecodeviewer;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import janicka.zofia.sourcecodeviewer.db.WebsiteDataSource;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.search_button)
    Button searchButton;
    @BindView(R.id.text_view)
    TextView textView;
    @BindView(R.id.edit_text)
    TextInputEditText editText;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;
    @BindString(R.string.not_url)
    String notUrl;
    @BindString(R.string.error)
    String error;
    @BindString(R.string.no_internet)
    String noInternet;

    private WebsiteDataSource dataSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        dataSource = new WebsiteDataSource(this);
        dataSource.open();
        dataSource.clear();
    }

    @OnClick(R.id.search_button)
    public void onSearchButtonClick() {
        setTextToTextView("");
        getSourceCode(getUrl());
    }

    private void uploadSourceCode(String url) {
        progressBar.setVisibility(View.VISIBLE);
        Ion.with(this)
                .load(url)
                .asString()
                .withResponse()
                .setCallback((e, response) -> {
                    progressBar.setVisibility(View.GONE);
                    if (response != null) {
                        if (200 == response.getHeaders().code()) {
                            String result = response.getResult();
                            setTextToTextView(result);
                            dataSource.saveSourceCode(url, result);
                        } else {
                            setTextToTextView(response.getHeaders().message());
                        }
                    } else {
                        setTextToTextView(error);
                    }
                });
    }

    private String getCodeFromDB(String url) {
        return dataSource.getSourceCode(url);
    }

    private void uploadFromDB(String url) {
        String result = dataSource.getSourceCode(url);
        setTextToTextView(result);
    }

    private void getSourceCode(String url) {

        if (isUrlValid(url)) {
            if (isNetworkAvailable()) {
                uploadSourceCode(url);
            } else if (!getCodeFromDB(url).equals("")) {
                uploadFromDB(url);
                setTextToTextView(getCodeFromDB(url));
            } else {
                setTextToTextView(noInternet);
            }
        } else {
            setTextToTextView(notUrl);
        }
    }

    private void setTextToTextView(String string) {
        textView.setText(string);
    }

    private String getUrl() {
        String url = editText.getText().toString();
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return url;
        } else {
            return "http://" + url;
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private boolean checkDomain(String url) {
        try {
            InputStream fis = getResources().openRawResource(R.raw.domains);
            if (fis != null) {
                InputStreamReader chapterReader = new InputStreamReader(fis);
                BufferedReader buffreader = new BufferedReader(chapterReader);

                String line;
                do {
                    line = buffreader.readLine();
                    if (url.contains(line)) {
                        return true;
                    }
                } while (line != null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean isUrlValid(String url) {
        return (URLUtil.isValidUrl(url) && checkDomain(url));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dataSource.close();
    }
}
