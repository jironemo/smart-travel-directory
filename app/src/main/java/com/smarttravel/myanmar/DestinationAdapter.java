package com.smarttravel.myanmar;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class DestinationAdapter extends RecyclerView.Adapter<DestinationAdapter.ViewHolder> {

    private Context context;
    private List<Destination> destinations;

    public DestinationAdapter(Context context, List<Destination> destinations) {
        this.context = context;
        this.destinations = destinations;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_destination, parent, false);
        return new ViewHolder(view);
    }

    static void disableSslVerification() {
        try
        {
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
            };

            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };

            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Destination destination = destinations.get(position);

        holder.nameTextView.setText(destination.getName());
        holder.descriptionTextView.setText(destination.getDescription());
        holder.locationTextView.setText(destination.getLocation());
        holder.categoryTextView.setText(destination.getCategory());
        holder.ratingTextView.setText(String.format("%.1f", destination.getRating()));
        holder.destinationId = destination.getId();
        holder.cardView.setOnClickListener(
                v-> {
                    Intent intent = new Intent(context, DestinationDetailActivity.class);
                    intent.putExtra("destination_id", destination.getId());
                    context.startActivity(intent);
                    Log.i("DEBUG","Clicked on destination: " + destination.getName());
                }
        );
        disableSslVerification();
        // Load image with Glide
        Glide.with(context)
                .load(destination.getImageUrl())
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .into(holder.imageView);

        // Show popular badge
        holder.popularBadge.setVisibility(destination.isPopular() ? View.VISIBLE : View.GONE);

    }

    @Override
    public int getItemCount() {
        return destinations.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        ImageView imageView;
        TextView nameTextView;
        TextView descriptionTextView;
        TextView locationTextView;
        TextView categoryTextView;
        TextView ratingTextView;
        View popularBadge;
        String destinationId;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            imageView = itemView.findViewById(R.id.imageView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            locationTextView = itemView.findViewById(R.id.locationTextView);
            categoryTextView = itemView.findViewById(R.id.categoryTextView);
            ratingTextView = itemView.findViewById(R.id.ratingTextView);
            popularBadge = itemView.findViewById(R.id.popularBadge);
        }
    }
}