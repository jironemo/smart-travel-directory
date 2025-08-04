package com.smarttravel.myanmar;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseDataSeeder {

    public static void seedSampleData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        List<Map<String, Object>> destinations = Arrays.asList(
                createDestination("Shwedagon Pagoda", "Golden pagoda and most sacred Buddhist site",
                        "Yangon", "Pagoda", "https://example.com/shwedagon.jpg", 4.8, "Yangon", true),

                createDestination("Bagan Archaeological Zone", "Ancient city with thousands of temples",
                        "Bagan", "Temple", "https://example.com/bagan.jpg", 4.9, "Bagan", true),

                createDestination("Inle Lake", "Scenic lake with floating gardens and stilt villages",
                        "Inle Lake", "Nature", "https://example.com/inle.jpg", 4.7, "Shan State", true),

                createDestination("Mandalay Palace", "Last royal palace of Myanmar",
                        "Mandalay", "Cultural", "https://example.com/mandalay.jpg", 4.5, "Mandalay", false),

                createDestination("Golden Rock", "Sacred boulder covered in gold leaf",
                        "Kyaiktiyo", "Pagoda", "https://example.com/golden_rock.jpg", 4.6, "Mon State", true),

                createDestination("Kalaw Hill Station", "Cool mountain town with trekking trails",
                        "Kalaw", "Adventure", "https://example.com/kalaw.jpg", 4.4, "Shan State", false),

                createDestination("Bogyoke Aung San Market", "Famous market for gems and handicrafts",
                        "Yangon", "Shopping", "https://example.com/bogyoke.jpg", 4.2, "Yangon", false),

                createDestination("Mrauk U", "Ancient capital with archaeological ruins",
                        "Mrauk U", "Cultural", "https://example.com/mrauku.jpg", 4.3, "Rakhine State", false),

                createDestination("Hsipaw", "Charming town with colonial architecture",
                        "Hsipaw", "Cultural", "https://example.com/hsipaw.jpg", 4.1, "Shan State", false),

                createDestination("Mingun Pagoda", "Unfinished massive pagoda",
                        "Mandalay", "Pagoda", "https://example.com/mingun.jpg", 4.0, "Mandalay", false)
        );

        for (Map<String, Object> destination : destinations) {
            db.collection("destinations").add(destination);
        }
    }

    private static Map<String, Object> createDestination(String name, String description,
                                                         String division, String category, String imageUrl, double rating,
                                                         String location, boolean isPopular) {
        Map<String, Object> destination = new HashMap<>();
        destination.put("name", name);
        destination.put("description", description);
        destination.put("division", division);
        destination.put("category", category);
        destination.put("imageUrl", imageUrl);
        destination.put("rating", rating);
        destination.put("location", location);
        destination.put("isPopular", isPopular);
        return destination;
    }
}