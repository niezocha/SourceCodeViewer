package janicka.zofia.sourcecodeviewer;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.search_button)
    ImageButton searchButton;
    @BindView(R.id.text_view)
    TextView textView;
    @BindView(R.id.edit_text)
    TextInputEditText editText;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        ButterKnife.bind(this);

    }

    @OnClick(R.id.search_button)
    public void onSearchButtonClick(){
        getSourceCode(getUrl());
    }

    private String getUrl(){
        return editText.getText().toString();
    }

    private boolean isUrlValid(String url){
        return URLUtil.isValidUrl(url);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    private void getSourceCode(String url){

        if(isUrlValid(url)){
            if(isNetworkAvailable()){
                uploauSC(url);
            } else{
                textView.setText("No internet connection.");
            }
        }else {
            textView.setText(R.string.not_url);
        }
    }

    private void uploauSC(String url) {
        progressBar.setVisibility(View.VISIBLE);
        Ion.with(this)
                .load(url)
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        progressBar.setVisibility(View.GONE);
                        textView.setText(result);
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            super.onBackPressed();
        }else if(item.getItemId() == R.id.results_list){
            Toast.makeText(this, "pokaż wyniki", Toast.LENGTH_LONG).show();
        }
        return super.onOptionsItemSelected(item);
    }

}
