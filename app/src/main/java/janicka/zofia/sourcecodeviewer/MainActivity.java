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

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import janicka.zofia.sourcecodeviewer.db.WebsiteDao;

public class MainActivity extends AppCompatActivity {

    static final String EDIT_TEXT = "edit_text";
    private String saved_edit_text;
    static final String TEXT_VIEW = "text_view";
    private String saved_text_view;


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
//    @BindView(R.id.no_internet)
//    TextView noInternet;
//    @BindView(R.id.error)
//    TextView error;
//    @BindView(R.id.no_url)
//    TextView noUrl;
    @BindString(R.string.no_internet)
    String noInternet;
    @BindString(R.string.not_url)
    String noUrl;
    @BindString(R.string.error)
    String error;

    private WebsiteDao dataSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        dataSource = new WebsiteDao(this);
        dataSource.open();
//        dataSource.clear();
        flipper.setDisplayedChild(flipper.indexOfChild(textView));
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        saved_edit_text = editText.getText().toString();
        saved_text_view = textView.getText().toString();
        savedInstanceState.putString(EDIT_TEXT, saved_edit_text);
        savedInstanceState.putString(TEXT_VIEW, saved_text_view);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        editText.setText(savedInstanceState.getString(EDIT_TEXT));
        textView.setText(savedInstanceState.getString(TEXT_VIEW));
    }

    @OnClick(R.id.search_button)
    public void onSearchButtonClick() {
        getSourceCode(getUrl());
    }

    private void uploadSourceCode(String url) {
        flipper.setDisplayedChild(R.id.progress_bar);
        Ion.with(this)
                .load(url).asString().withResponse()
                .setCallback((e, response) -> {
                    if (response != null) {
                        if (200 == response.getHeaders().code()) {
                            String result = response.getResult();
                            setTextToTextView(result);
                            flipper.setDisplayedChild(flipper.indexOfChild(textView));
                            dataSource.saveSourceCode(url, result);
                        } else {
                            setTextToTextView(response.getHeaders().message());
                            flipper.setDisplayedChild(flipper.indexOfChild(textView));
                        }
                    } else {
                        setTextToTextView(error);
                        flipper.setDisplayedChild(flipper.indexOfChild(textView));
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
                setTextToTextView(noInternet);
                flipper.setDisplayedChild(flipper.indexOfChild(textView));
            }
        } else {
            setTextToTextView(noUrl);
            flipper.setDisplayedChild(flipper.indexOfChild(textView));
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
        return UrlChecker.checkUrl(url);
    }

    private boolean isValidAddress(String url) {
        return URLUtil.isValidUrl(url) && UrlChecker.checkDomain(this, url);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dataSource.close();
    }

}

