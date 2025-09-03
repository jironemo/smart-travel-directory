package com.smarttravel.myanmar;

import android.os.AsyncTask;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

public class ImgbbUploader {
    public interface UploadCallback {
        void onSuccess(String imageUrl);
        void onFailure(Exception e);
    }

    public static void trustAllCertificates() {
        try {
            javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[]{
                new javax.net.ssl.X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() { return null; }
                    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
                }
            };
            javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
        } catch (Exception ignored) {}
    }

    public static void uploadImage(String base64Image, String apiKey, UploadCallback callback) {
        trustAllCertificates();
        new AsyncTask<Void, Void, String>() {
            Exception error = null;
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    URL url = new URL("https://api.imgbb.com/1/upload?key=" + apiKey);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);
                    String data = "image=" + java.net.URLEncoder.encode(base64Image, "UTF-8");
                    OutputStream os = conn.getOutputStream();
                    os.write(data.getBytes());
                    os.flush();
                    os.close();
                    int responseCode = conn.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        java.util.Scanner s = new java.util.Scanner(conn.getInputStream()).useDelimiter("\\A");
                        String response = s.hasNext() ? s.next() : "";
                        JSONObject json = new JSONObject(response);
                        return json.getJSONObject("data").getString("url");
                    } else {
                        // Read error stream for more details
                        String errorResponse = "";
                        try {
                            java.util.Scanner s = new java.util.Scanner(conn.getErrorStream()).useDelimiter("\\A");
                            errorResponse = s.hasNext() ? s.next() : "";
                        } catch (Exception ex) {}
                        error = new Exception("HTTP error: " + responseCode + ", Response: " + errorResponse);
                        return null;
                    }
                } catch (Exception e) {
                    error = e;
                    return null;
                }
            }
            @Override
            protected void onPostExecute(String imageUrl) {
                if (imageUrl != null) {
                    callback.onSuccess(imageUrl);
                } else {
                    callback.onFailure(error);
                }
            }
        }.execute();
    }
}
