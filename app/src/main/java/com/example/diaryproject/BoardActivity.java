package com.example.diaryproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.diaryproject.DTO.ImageDTO;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

public class BoardActivity extends AppCompatActivity {
    private static final String BoardActivity_TAG = "BoardActivity";

    private FirebaseAuth auth;

    private RecyclerView recyclerView;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseStorage storage;

    private List<ImageDTO> imageDTOS = new ArrayList<>();
    private List<String> uidList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);

        firebaseDatabase = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        recyclerView = findViewById(R.id.recyclerview);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        final BoardRecyclerviewAdapter boardRecyclerviewAdapter = new BoardRecyclerviewAdapter();
        recyclerView.setAdapter(boardRecyclerviewAdapter);

        firebaseDatabase.getReference().child("images").addValueEventListener(new ValueEventListener() { // 옵저버 패턴
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) { // 실시간으로 데이터변환이 일어나는 부분
                imageDTOS.clear(); // 처음에 초기화를 해야한다. -> 미리 저장되어있던 데이터를 클리어
                uidList.clear(); // 처음에 초기화를 해야한다.
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) { // 하나씩 데이터를 읽어오는 부분
                    ImageDTO imageDTO = snapshot.getValue(ImageDTO.class); // 데이터베이스에 이상하게 저장되어있으면 읽어올때 문제가 생긴다.
                    imageDTOS.add(imageDTO);
                    String uidKey = snapshot.getKey(); // 데이터베이스에있는 하나의 게시글의 key값을 가져온다.
                    uidList.add(uidKey);
                }
                boardRecyclerviewAdapter.notifyDataSetChanged(); // 계속 갱신 해줘야한다.
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    class BoardRecyclerviewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_board, parent, false);

            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
            ((CustomViewHolder) holder).textView.setText(imageDTOS.get(position).title);
            ((CustomViewHolder) holder).textView2.setText(imageDTOS.get(position).description);
            Glide.with(((CustomViewHolder) holder).itemView.getContext()).load(imageDTOS.get(position).imageUrl).into(((CustomViewHolder) holder).imageView);
            ((CustomViewHolder) holder).starButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onStarClicked(firebaseDatabase.getReference().child("images").child(uidList.get(position))); // images를 타고 그 다음으로 타고 들어가야하므로 child 를 두번해야한다.
                }
            });

            if (imageDTOS.get(position).stars.containsKey(auth.getCurrentUser().getUid())) { // like button 을 눌렀니? 부분 start안에 내 uid가 있는지 확인하는 부분이다.
                ((CustomViewHolder) holder).starButton.setImageResource(R.drawable.like_icon_black);
            } else {
                ((CustomViewHolder) holder).starButton.setImageResource(R.drawable.like_icon_border_black);
            }

            ((CustomViewHolder)holder).deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    delete_content(position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return imageDTOS.size();
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder {
            private ImageView imageView;
            private TextView textView;
            private TextView textView2;
            private ImageView starButton; // like button
            private ImageView deleteButton; // 삭제 버튼

            public CustomViewHolder(View view) {
                super(view);
                imageView = view.findViewById(R.id.item_imageview);
                textView = view.findViewById(R.id.item_textView);
                textView2 = view.findViewById(R.id.item_textView2);
                starButton = view.findViewById(R.id.item_startButton_imageview);
                deleteButton = view.findViewById(R.id.item_delete_imageview);
            }
        }
    }

    private void onStarClicked(DatabaseReference postRef) { // like button 클릭할때 여러 사용자가 같은 게시물에 동시에 like을 누를때
        postRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                ImageDTO imageDTO = mutableData.getValue(ImageDTO.class);
                if (imageDTO == null) {
                    return Transaction.success(mutableData);
                }

                if (imageDTO.stars.containsKey(auth.getCurrentUser().getUid())) { // like button 을 눌렀니? 부분
                    // Unstar the post and remove self from stars
                    imageDTO.starCount = imageDTO.starCount - 1;
                    imageDTO.stars.remove(auth.getCurrentUser().getUid());
                } else { // 그 게시글에 내 아이디가 없으면 like button 을 누를수있다.
                    // Star the post and add self to stars
                    imageDTO.starCount = imageDTO.starCount + 1;
                    imageDTO.stars.put(auth.getCurrentUser().getUid(), true);
                }

                // Set value and report transaction success
                mutableData.setValue(imageDTO);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
                // Transaction completed
                Log.d(BoardActivity_TAG, "postTransaction:onComplete:" + databaseError);
            }
        });
    }

    private void delete_content(int position) { // 파일 position값을 넘어오게 하고 그 값으로 이미지 이름을 참조하는 것이 좋다. 이미지 이름을 넘기게 되면 데이터손실 생길시 위험부담이 있다. 게시글을 삭제하는 부분
        storage.getReference().child("images").child(imageDTOS.get(position).imageNmae).delete().addOnSuccessListener(new OnSuccessListener<Void>() { // 제대로 지워졌는지 확인하려면 콜백 함수를 사용하면 된다.
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(BoardActivity.this, "삭제 완료", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(BoardActivity.this, "삭제 실패", Toast.LENGTH_LONG).show();
            }
        });
    }
}
