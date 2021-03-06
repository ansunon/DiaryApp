package com.example.diaryproject.DTO;

import java.util.HashMap;
import java.util.Map;

public class BoardDTO {

    public String imageUrl;
    public String writer_profile_imageUrl; // 작성한 유저의 프로필 이미지 url
    public String writer_userName;
    public String imageNmae; // 삭제할 이미지의 이름을 저장하는 변수
    public String title;
    public String description;
    public String uid;
    public String userId;
    public int starCount = 0; // like button Count
    public Map<String, Boolean> stars = new HashMap<>(); // like Button Count
}
