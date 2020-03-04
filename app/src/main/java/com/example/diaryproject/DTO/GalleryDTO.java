package com.example.diaryproject.DTO;

import java.util.HashMap;
import java.util.Map;

public class GalleryDTO {

    public String imageUrl;
    public String imageNmae; // 삭제할 이미지의 이름을 저장하는 변수
    public String category_name;
    public String uid;
    public String userId;
    public int starCount = 0; // like button Count
    public Map<String, Boolean> stars = new HashMap<>(); // like Button Count
}
