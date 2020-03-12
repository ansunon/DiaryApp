package com.example.diaryproject.DTO;

import java.util.HashMap;
import java.util.Map;

public class FirebasePost {
    public String categoryName;
    public String gallery_imagePath;

    public FirebasePost(){
        // Default constructor required for calls to DataSnapshot.getValue(FirebasePost.class)
    }

    public FirebasePost(String id, String name) {
        this.categoryName = id;
        this.gallery_imagePath = name;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("categoryName", categoryName);
        result.put("gallery_imagePath", gallery_imagePath);
        return result;
    }
}