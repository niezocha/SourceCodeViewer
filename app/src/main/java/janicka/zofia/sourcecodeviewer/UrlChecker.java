package janicka.zofia.sourcecodeviewer;

import android.content.Context;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class UrlChecker {

    public static String checkUrl(String url) {
        String result = url.replaceAll("\\s+", "");
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return result;
        } else {
            return "http://" + result;
        }
    }

    public static boolean checkDomain(Context context, String url) {
        try {
            InputStream fis = context.getResources().openRawResource(R.raw.domains);
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
}
