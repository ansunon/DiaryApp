package com.example.diaryproject.ui.notifications;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.diaryproject.DTO.ImageDTO;
import com.example.diaryproject.DTO.PostDTO;
import com.example.diaryproject.PostUploadActivity;
import com.example.diaryproject.R;
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

public class NotificationsFragment extends Fragment {

    private static final String NotificationsFragment_TAG = "NotificationsFragment";

    private RecyclerView recyclerView;
    private FirebaseAuth auth;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseStorage storage;

    private List<PostDTO> postDTOS = new ArrayList<>();
    private List<String> uidList = new ArrayList<>();

    private Button post_upload_btn; // 방명록 작성 버튼

    private NotificationsViewModel notificationsViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        notificationsViewModel = ViewModelProviders.of(this).get(NotificationsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_notifications, container, false);

        firebaseDatabase = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        recyclerView = root.findViewById(R.id.notifications_recyclerview); // 현재 리사이클러뷰의 변수를 가져오는 부분
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        final NotificationsFragment.NotificationsRecyclerviewAdapter notificationsRecyclerviewAdapter = new NotificationsFragment.NotificationsRecyclerviewAdapter(); // 그리고 어댑터를 내부 클래스로 정의할 것이다.
        recyclerView.setAdapter(notificationsRecyclerviewAdapter);


        firebaseDatabase.getReference().child("post").addValueEventListener(new ValueEventListener() { // 옵저버 패턴
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) { // 실시간으로 데이터변환이 일어나는 부분
                postDTOS.clear(); // 처음에 초기화를 해야한다. -> 미리 저장되어있던 데이터를 클리어
                uidList.clear(); // 처음에 초기화를 해야한다.
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) { // 하나씩 데이터를 읽어오는 부분
                    PostDTO postDTO = snapshot.getValue(PostDTO.class); // 데이터베이스에 이상하게 저장되어있으면 읽어올때 문제가 생긴다.
                    postDTOS.add(postDTO);
                    String uidKey = snapshot.getKey(); // 데이터베이스에있는 하나의 게시글의 key값을 가져온다.
                    uidList.add(uidKey);
                }
                notificationsRecyclerviewAdapter.notifyDataSetChanged(); // 계속 갱신 해줘야한다.
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { // 이게 무조건 있어야 한다.

            }
        });

        post_upload_btn = root.findViewById(R.id.post_upload_button);
        post_upload_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), PostUploadActivity.class)); // 게시글 작성 페이지로 이동한다. ※※※※※※※※※※※※※※※
            }
        });

        return root;
    }
    class NotificationsRecyclerviewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false); // 방명록의 xml을 가져온다.
            return new NotificationsFragment.NotificationsRecyclerviewAdapter.CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
            ((CustomViewHolder) holder).textView_title.setText(postDTOS.get(position).title);
            ((CustomViewHolder) holder).textView_description.setText(postDTOS.get(position).description);

            //Glide.with(((CustomViewHolder) holder).itemView.getContext()).load(postDTOS.get(position).profile_imageUrl).into(((CustomViewHolder) holder).profile_imageView); // itemView 가 무엇이지...?
            ((CustomViewHolder) holder).starButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onStarClicked(firebaseDatabase.getReference().child("post").child(uidList.get(position))); // images를 타고 그 다음으로 타고 들어가야하므로 child 를 두번해야한다.
                }
            });

            if (postDTOS.get(position).stars.containsKey(auth.getCurrentUser().getUid())) { // like button 을 눌렀니? 부분 start안에 내 uid가 있는지 확인하는 부분이다.
                ((NotificationsFragment.NotificationsRecyclerviewAdapter.CustomViewHolder) holder).starButton.setImageResource(R.drawable.ic_toggle_star_24);
            } else {
                ((NotificationsFragment.NotificationsRecyclerviewAdapter.CustomViewHolder) holder).starButton.setImageResource(R.drawable.ic_toggle_star_outline_24);
            }

//            ((NotificationsFragment.NotificationsRecyclerviewAdapter.CustomViewHolder)holder).deleteButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    delete_content(position);
//                }
//            });
        }

        @Override
        public int getItemCount() {
            return postDTOS.size();
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder {
            private ImageView profile_imageView;
            private TextView textView_title;
            private TextView textView_description;
            private ImageView starButton; // like button
            private ImageView deleteButton; // 삭제 버튼

            public CustomViewHolder(View view) {
                super(view);
                profile_imageView = view.findViewById(R.id.postAuthor_image);
                textView_title = view.findViewById(R.id.postTitle);
                textView_description = view.findViewById(R.id.postDescription);
                starButton = view.findViewById(R.id.post_starButton_imageView);
                //deleteButton = view.findViewById(R.id.item_delete_imageview); // -> 내가 사용자라면 띄어야하는 부분
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
                Log.d(NotificationsFragment_TAG, "postTransaction: onComplete:" + databaseError);
            }
        });
    }


    private void delete_content(final int position) { // 파일 position값을 넘어오게 하고 그 값으로 이미지 이름을 참조하는 것이 좋다. 이미지 이름을 넘기게 되면 데이터손실 생길시 위험부담이 있다. 게시글을 삭제하는 부분
        // 콜백 함수를 동시에 실행하는 것은 프로그램의 불안정성을 야기할수 있다. 그러면 하나의 과정이 실행하고 난후 다음 과정을 실행하는 것이 좋다.
        storage.getReference().child("images").child(postDTOS.get(position).imageNmae).delete().addOnSuccessListener(new OnSuccessListener<Void>() { // 제대로 지워졌는지 확인하려면 콜백 함수를 사용하면 된다.
            @Override
            public void onSuccess(Void aVoid) {
                // uidList.get(position) : 디비 키값이다 (하나의 트리의 키값(root 값)이라고 생각할 수 있다.)
                firebaseDatabase.getReference().child("images").child(uidList.get(position)).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() { // .setValue(null) 도 삭제하는 구문
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getContext(), "삭제가 완료 되었습니다.", Toast.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "삭제 실패되었습니다.", Toast.LENGTH_LONG).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "삭제 실패되었습니다.", Toast.LENGTH_LONG).show();
            }
        });

    }
}