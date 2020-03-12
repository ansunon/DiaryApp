package com.example.diaryproject.DTO;

import java.util.HashMap;
import java.util.Map;


// -> DTO 안에 파이어베이스 데이터베이스를 접근해서 생성자를 이용한후 정리를 한다.
public class CategoryDTO {

    public String imageUrl; // 카테고리 이미지
    public String imageNmae; // 삭제할 이미지의 이름을 저장하는 변수
    public String category_name; // 카테고리 이름

    public String uid;
    public String userId;
    public int starCount = 0; // like button Count
    public Map<String, Boolean> stars = new HashMap<>(); // like Button Count
}
