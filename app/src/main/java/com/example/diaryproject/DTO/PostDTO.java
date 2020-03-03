package com.example.diaryproject.DTO;

import java.util.HashMap;
import java.util.Map;

public class PostDTO {
    public String profile_imageUrl; // 프로필 이미지
    public String imageNmae; // 삭제할 이미지의 이름을 저장하는 변수 --> 필요없는 변수인것 같다
    public String title;  // 방명록 제목
    public String description; // 내용
    public String uid;
    public String userId;
    public int starCount = 0; // like button Count
    public Map<String, Boolean> stars = new HashMap<>(); // like Button Count
}
