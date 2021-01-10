package com.example.guardiana.repository;

import com.example.guardiana.App;
import com.example.guardiana.listeners.OnDataCompleteListener;
import com.example.guardiana.listeners.OnDataErrorListener;
import com.example.guardiana.model.Profile;
import com.example.guardiana.pageable.Pageable;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.guardiana.App.getUserId;

public class ProfileFirebaseRepository implements FirebasePagingAndSortingRepository<Profile, String> {
    private static final String USERS_ADDRESS_MAPPING = "UsersAddressMapping";
    private static final String PROFILE = "Profile";
    private static ProfileFirebaseRepository profileFirebaseRepository;
    private final CollectionReference collectionReference;
    private static DocumentSnapshot lastResult;
    private static String currentUserId;

    private ProfileFirebaseRepository() {
        currentUserId = App.getUserId();
        collectionReference = FirebaseFirestore
                .getInstance()
                .collection(USERS_ADDRESS_MAPPING)
                .document(getUserId())
                .collection(PROFILE);
    }

    public static ProfileFirebaseRepository getInstance() {
        if (profileFirebaseRepository == null || !currentUserId.equals(App.getUserId())) {
            profileFirebaseRepository = new ProfileFirebaseRepository();
        }
        return profileFirebaseRepository;
    }

    @Override
    public void findAll(Pageable pageable, OnDataCompleteListener<Profile> data, OnDataErrorListener err) {
        Query query = lastResult == null ?
                collectionReference.orderBy(pageable.getSort().getSortBy(), pageable.getSort().getDirection()).limit(pageable.getPageSize()) :
                collectionReference.orderBy(pageable.getSort().getSortBy(), pageable.getSort().getDirection()).startAfter(lastResult).limit(pageable.getPageSize());
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().size() > 0) {
                data.onDataCompleteListener(task.getResult().getDocuments().stream().map(e -> e.toObject(Profile.class)).collect(Collectors.toList()));
                lastResult = task.getResult().getDocuments().get(task.getResult().getDocuments().size() - 1);
            }
        }).addOnFailureListener(err::onError);
    }

    @Override
    public void save(Profile content, OnDataCompleteListener<Profile> data, OnDataErrorListener err) {
        collectionReference.document().set(content)
                .addOnCompleteListener((task) -> data.onDataCompleteListener(Collections.singletonList(content)))
                .addOnFailureListener(err::onError);
    }

    @Override
    public void findById(String id, OnDataCompleteListener<Profile> onComplete, OnDataErrorListener err) {
        collectionReference.document(id).get()
                .addOnCompleteListener(task -> onComplete
                        .onDataCompleteListener(Collections.singletonList(task.getResult().toObject(Profile.class))))
                .addOnFailureListener(err::onError);
    }

    @Override
    public void update(String id, Profile content) {
        collectionReference.document(id)
                .update(updateData(content));
    }

    @Override
    public void delete(String id) {
        collectionReference.document(id).delete();
    }

    public void setLastResult(DocumentSnapshot lastResult) {
        this.lastResult = lastResult;
    }

    private Map<String, Object> updateData(Profile profile) {
        Map<String, Object> map = new HashMap<>();
        map.put("icon", profile.getIcon());

        return map;
    }
}
