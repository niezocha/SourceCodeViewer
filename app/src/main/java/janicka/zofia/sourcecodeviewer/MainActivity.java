package janicka.zofia.sourcecodeviewer;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.koushikdutta.ion.Ion;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

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
    @BindView(R.id.view_flipper)
    ViewFlipper flipper;
    @BindView(R.id.no_internet)
    TextView noInternet;
    @BindView(R.id.error)
    TextView error;
    @BindView(R.id.no_url)
    TextView noUrl;

    private WebsiteDataSource dataSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        dataSource = new WebsiteDataSource(this);
        dataSource.open();
        dataSource.clear();
        flipper.setDisplayedChild(flipper.indexOfChild(textView));
    }

    @OnClick(R.id.edit_text)
    public void onEditTextClick() {
        clearTV();
    }

    @OnClick(R.id.search_button)
    public void onSearchButtonClick() {
        getSourceCode(getUrl());
    }

    private void clearTV() {
        setTextToTextView("");
    }

    private void uploadSourceCode(String url) {
        flipper.setDisplayedChild(R.id.progress_bar);
        Ion.with(this)
                .load(url)
                .asString()
                .withResponse()
                .setCallback((e, response) -> {
                    if (response != null) {
                        if (200 == response.getHeaders().code()) {
                            String result = response.getResult();
                            setTextToTextView(result);
                            flipper.setDisplayedChild(flipper.indexOfChild(textView));
                            dataSource.saveSourceCode(url, result);
                        } else {
                            flipper.setDisplayedChild(flipper.indexOfChild(error));
                        }
                    } else {
                        flipper.setDisplayedChild(flipper.indexOfChild(error));
                    }
                });
    }

    private String uploadFromDB(String url) {
        flipper.setDisplayedChild(flipper.indexOfChild(progressBar));
        String result = dataSource.getSourceCode(url);
        setTextToTextView(result);
        return dataSource.getSourceCode(url);
    }

    private void getSourceCode(String url) {
        flipper.setDisplayedChild(flipper.indexOfChild(progressBar));
        if (isValidAddress(url)) {
            if (isNetworkAvailable()) {
                uploadSourceCode(url);
            } else if (!uploadFromDB(url).equals("")) {
                flipper.setDisplayedChild(flipper.indexOfChild(textView));
            } else {
                flipper.setDisplayedChild(flipper.indexOfChild(noInternet));
            }
        } else {
            flipper.setDisplayedChild(flipper.indexOfChild(noUrl));
        }
    }

    private void setTextToTextView(String string) {
        textView.setText(string);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private String getUrl() {
        String url = editText.getText().toString();
        String result = url.replaceAll("\\s+", "");
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return result;
        } else {
            return "http://" + result;
        }
    }

    private boolean isValidAddress(String url) {
        return (isValidUrl(url) && isValidDomain(url));
    }

    private boolean isValidUrl(String url) {
        return URLUtil.isValidUrl(url);
    }

    private boolean isValidDomain(String url) {
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dataSource.close();
    }
}
