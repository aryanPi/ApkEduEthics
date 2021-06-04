package com.classroom.eduethics.Fragments.HomeFragemtns;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.classroom.eduethics.Activity.ImagePage;
import com.classroom.eduethics.Activity.MainActivity;
import com.classroom.eduethics.Adapter.FragmentAdapter;
import com.classroom.eduethics.Adapter.uploadImagesAdapter;
import com.classroom.eduethics.Fragments.HomeClassroom;
import com.classroom.eduethics.R;
import com.classroom.eduethics.Utils.GlobalVariables;
import com.classroom.eduethics.Utils.LocalConstants;
import com.google.android.gms.common.util.IOUtils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.makeramen.roundedimageview.RoundedImageView;


import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class StudyMaterialFragment extends Fragment {

    Context context;

    TabLayout tabLayout;
    ViewPager2 pager2;
    FragmentAdapter adapter;


    ArrayList<String> attachments;
    ArrayList<Object> attachmentsData;
    ArrayList<String> attachmentsOnEdit;
    ArrayList<String> forDelete;
    ArrayList<String> forAdd;

    EditText uploadText;

    boolean isEdit;

    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReferenceFromUrl(LocalConstants.STORAGE_REF);    //change the url according to your firebase app
    private int uploadImageCounter = 0;


    uploadImagesAdapter imagesadapter = new uploadImagesAdapter(attachments, this, LocalConstants.FROM.FROM_ADD_SOLUTION,null);

    List<String> urls = new ArrayList<>();

    public StudyMaterialFragment(Context context) {
        this.context = context;
    }

    public View root;


    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TransitionInflater inflater = TransitionInflater.from(requireContext());
        setEnterTransition(inflater.inflateTransition(R.transition.slide_right));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_study_material, container, false);

        if (GlobalVariables.isStudent)
            ((TextView) root.findViewById(R.id.tit)).setText("Study Material posted by the \nteacher can be found here");

        FirstTask();
        return root;
    }

    public void FirstTask() {
        selectedDialog = -1;
        attachments = new ArrayList<>();
        attachmentsData = new ArrayList<>();
        attachmentsOnEdit = new ArrayList<>();
        forDelete = new ArrayList<>();
        forAdd = new ArrayList<>();

        uploadImageCounter = 0;

        imagesadapter = new uploadImagesAdapter(attachments, this, LocalConstants.FROM.FROM_ADD_SOLUTION,root.findViewById(R.id.progress));
        urls = new ArrayList<>();


        MainActivity.toolbarImageFilter.setOnClickListener(v -> Toast.makeText(context, "TOAST", Toast.LENGTH_SHORT).show());
        MainActivity.TO_WHICH_FRAG = LocalConstants.TO_FRAG.TO_CLASSROOM;
        pd = root.findViewById(R.id.progress);


        if (HomeClassroom.from == LocalConstants.TYPE.STUDENT) {
            root.findViewById(R.id.submit).setVisibility(View.GONE);
        } else {
            root.findViewById(R.id.submit).setOnClickListener(v -> upload());
        }

        tabLayout = root.findViewById(R.id.tab_layout);
        pager2 = root.findViewById(R.id.pageViewerHomeClassroom);

        runTask();

    }


    Dialog dialog;

    private void upload() {

        dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.upload_dialog);
        dialog.findViewById(R.id.camera).setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_DENIED)
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, LocalConstants.REQ_CODE.CAMERA_REQUEST);
            else {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, LocalConstants.PERMISSION_CODE.CAMERA_REQUEST);
            }
        });

        dialog.findViewById(R.id.pdf).setOnClickListener(v -> {
            Intent chooseFile;
            Intent intent;
            chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
            chooseFile.setType("application/pdf");
            intent = Intent.createChooser(chooseFile, "Choose a file");
            startActivityForResult(intent, LocalConstants.PERMISSION_CODE.PICKFILE_RESULT_CODE);
        });

        dialog.findViewById(R.id.image).setOnClickListener(v -> {
            Intent i = new Intent();
            i.setType("image/*");
            i.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(i, "Select Picture"), LocalConstants.PERMISSION_CODE.SELECT_PICTURE);
        });

        dialog.findViewById(R.id.urlText).setVisibility(View.VISIBLE);

        ((TextView) dialog.findViewById(R.id.upload)).setText("Upload");

        uploadText = dialog.findViewById(R.id.urlText);
        uploadText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    switch (selectedDialog) {
                        case 0:
                            ((TextView) dialog.findViewById(R.id.image)).setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.second_btn, null));
                            uploadText.clearFocus();
                            break;
                        case 1:
                            ((TextView) dialog.findViewById(R.id.pdf)).setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.second_btn, null));
                            uploadText.clearFocus();
                            break;
                        case 2:
                            ((TextView) dialog.findViewById(R.id.camera)).setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.second_btn, null));
                            uploadText.clearFocus();
                            break;
                        default:
                            uploadText.clearFocus();
                            break;
                    }
                } else {
                    ((TextView) dialog.findViewById(R.id.image)).setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.edit_text_selector, null));
                    ((TextView) dialog.findViewById(R.id.pdf)).setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.edit_text_selector, null));
                    ((TextView) dialog.findViewById(R.id.camera)).setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.edit_text_selector, null));
                }
            }
        });

        dialog.findViewById(R.id.upload).setOnClickListener(v -> {
            uploadNow();
            dialog.dismiss();
        });
        dialog.findViewById(R.id.cancel).setOnClickListener(v -> dialog.dismiss());
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }


    private void uploadNow() {
    pd.setVisibility(View.VISIBLE);
        if (!uploadText.getText().toString().equals("")) {
            if(uploadText.getText().toString().startsWith("http")){
                urls.add("URL:" + uploadText.getText().toString());
                uploadToClassroom();
            }else{
                uploadText.setError("Check your Address");
            }
        } else {
            if (isEdit) {
                for (String s : attachmentsOnEdit) {
                    if (!attachments.contains(s)) {
                        forDelete.add(s);
                        urls.remove(s);
                    }
                }

                if (forDelete.size() != 0) {
                    for (int i = 0; i < forDelete.size(); i++) {
                        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(forDelete.get(i));
                        int finalI = i;
                        storageReference.delete().addOnSuccessListener(aVoid -> {
                            if (finalI == forDelete.size() - 1) {

                                subFunctionUpload();
                            }
                        }).addOnFailureListener(exception -> {
                            if (finalI == forDelete.size() - 1) {
                                subFunctionUpload();
                            }
                        });
                    }

                } else {
                    subFunctionUpload();
                }

            } else {
                subFunctionUpload();
            }
        }
    }

    View pd;

    private void subFunctionUpload() {
        if (forAdd.size() == 0) {
            uploadToClassroom();
            return;
        }
        for (int i = 0; i < attachments.size(); i++) {
            Bitmap bitmap = null;
            Uri selectedDataPath = null;
            if (forAdd.contains(attachments.get(i))) {
                if (attachments.get(i).startsWith("314")) bitmap = (Bitmap) attachmentsData.get(i);
                else selectedDataPath = (Uri) attachmentsData.get(i);

                if (selectedDataPath == null) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] data = baos.toByteArray();

                    final String randomKey = UUID.randomUUID().toString();

                    UploadTask uploadTask = storageRef.child("studyMaterial/" + HomeClassroom.classId + "/" + randomKey + ".jpg").putBytes(data);
                    uploadTask.addOnFailureListener(exception -> Toast.makeText(getContext(), "failed", Toast.LENGTH_SHORT).show()).addOnSuccessListener(taskSnapshot -> {
                        urls.add(taskSnapshot.getMetadata().getPath());
                        uploadImageCounter++;
                        checkandpost();
                    });
                } else {
                    final String randomKey = UUID.randomUUID().toString();
                    String extension;
                    if (selectedDataPath.getPath().contains(".")) {
                        extension = selectedDataPath.getPath().substring(selectedDataPath.getPath().lastIndexOf("."));
                    } else {
                        extension = ".pdf";
                    }

                    StorageReference riversRef = storageRef.child(("studyMaterial/" + HomeClassroom.classId + "/" + randomKey + extension));


                    riversRef.putFile(selectedDataPath)
                            .addOnSuccessListener(taskSnapshot -> {
                                urls.add(taskSnapshot.getMetadata().getPath());
                                uploadImageCounter++;
                                checkandpost();
                            })
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Unable to post your Image !", Toast.LENGTH_SHORT).show());

                }
            }

        }
    }

    private void checkandpost() {
        if (uploadImageCounter == forAdd.size()) {
            uploadToClassroom();
        }
    }


    private void uploadToClassroom() {

        FirebaseFirestore.getInstance()
                .collection("classroom")
                .document(HomeClassroom.classId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<Map<String, Object>> down = (List) documentSnapshot.get("studyMaterial");
                    List<Map<String, Object>> f = new ArrayList<>();
                    for (Map<String, Object> stringObjectMap : down) {
                        if (!forDelete.contains(stringObjectMap.get("name").toString()))
                            f.add(stringObjectMap);
                        else f.add(stringObjectMap);
                    }

                    for (String url : urls) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("name", url);
                        map.put("time", Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + ":" + Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + ":" + Calendar.getInstance().get(Calendar.MINUTE));
                        f.add(map);
                    }

                    Map<String, Object> forupload = new HashMap<>();
                    forupload.put("studyMaterial", f);
                    FirebaseFirestore.getInstance().collection("classroom")
                            .document(HomeClassroom.classId)
                            .update(forupload)
                            .addOnSuccessListener(aVoid -> {
                                FirstTask();
                                pd.setVisibility(View.GONE);
                            });
                });
    }

    List<Map<String, Object>> data2 = new ArrayList<>();

    int selectedDialog;

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case LocalConstants.PERMISSION_CODE.PICKFILE_RESULT_CODE:
                attachmentsData.add(data.getData());
                attachments.add(data.getData().getPath());
                forAdd.add(data.getData().getPath());
                ((TextView) dialog.findViewById(R.id.image)).setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.edit_text_selector, null));
                ((TextView) dialog.findViewById(R.id.pdf)).setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.second_btn, null));
                ((TextView) dialog.findViewById(R.id.camera)).setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.edit_text_selector, null));
                imagesadapter.notifyItemInserted(attachments.size() - 1);
                selectedDialog = 1;
                break;
            case LocalConstants.PERMISSION_CODE.SELECT_PICTURE:
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    attachmentsData.add(selectedImageUri);
                    attachments.add(selectedImageUri.getPath());
                    forAdd.add(selectedImageUri.getPath());
                    ((TextView) dialog.findViewById(R.id.image)).setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.second_btn, null));
                    ((TextView) dialog.findViewById(R.id.pdf)).setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.edit_text_selector, null));
                    ((TextView) dialog.findViewById(R.id.camera)).setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.edit_text_selector, null));
                    imagesadapter.notifyItemInserted(attachments.size() - 1);
                    selectedDialog = 0;
                }
                break;
            case LocalConstants.PERMISSION_CODE.CAMERA_REQUEST:
                attachmentsData.add((Bitmap) data.getExtras().get("data"));
                attachments.add("314" + UUID.randomUUID().toString());
                forAdd.add("314" + UUID.randomUUID().toString());
                ((TextView) dialog.findViewById(R.id.image)).setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.edit_text_selector, null));
                ((TextView) dialog.findViewById(R.id.pdf)).setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.edit_text_selector, null));
                ((TextView) dialog.findViewById(R.id.camera)).setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.second_btn, null));
                imagesadapter.notifyItemInserted(attachments.size() - 1);
                selectedDialog = 2;
                break;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LocalConstants.REQ_CODE.CAMERA_REQUEST) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getActivity(), "camera permission granted", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, LocalConstants.PERMISSION_CODE.CAMERA_REQUEST);
            } else {
                Toast.makeText(getActivity(), "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }


    public void runTask() {
        root.findViewById(R.id.progress).setVisibility(View.VISIBLE);
        FirebaseFirestore.getInstance()
                .collection("classroom")
                .document(HomeClassroom.classId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        data2 = (List) documentSnapshot.get("studyMaterial");
                        if (getActivity() == null) return;
                        forward();
                    }
                });
    }


    private void forward() {

        if (getActivity() == null) {
            return;
        }

        List<String> images = new ArrayList<>();
        List<String> docs = new ArrayList<>();
        List<String> links = new ArrayList<>();

        List<String> timeIm = new ArrayList<>();
        List<String> timeDo = new ArrayList<>();
        List<String> timeLi = new ArrayList<>();


        for (Map<String, Object> datum : data2) {
            if (datum.get("name").toString().endsWith("pdf")) {
                docs.add(datum.get("name").toString());
                timeDo.add(datum.get("time").toString());
            } else if (datum.get("name").toString().endsWith("jpeg") || datum.get("name").toString().endsWith("png") || datum.get("name").toString().endsWith("jpg")) {
                images.add(datum.get("name").toString());
                timeIm.add(datum.get("time").toString());
            } else {
                links.add(datum.get("name").toString().substring(4));
                timeLi.add(datum.get("time").toString());
            }
        }


        FragmentManager fm = getActivity().getSupportFragmentManager();
        ArrayList<Fragment> fragments = new ArrayList<>();
        tabLayout.removeAllTabs();

        fragments.add(new DocsFragment(docs, timeDo, this));
        fragments.add(new ImagesFragment(images, timeIm, this));
        fragments.add(new LinksFragment(links, timeLi, this));

        adapter = new FragmentAdapter(fm, getLifecycle(), fragments);
        pager2.setAdapter(adapter);

        tabLayout.addTab(tabLayout.newTab().setText("Docs"));
        tabLayout.addTab(tabLayout.newTab().setText("Images"));
        tabLayout.addTab(tabLayout.newTab().setText("Links"));

        tabLayout.setTabGravity(TabLayout.GRAVITY_CENTER);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                pager2.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


        pager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                tabLayout.selectTab(tabLayout.getTabAt(position));

            }
        });


        pager2.setCurrentItem(0);
        root.findViewById(R.id.progress).setVisibility(View.GONE);
    }


    public static class LinksFragment extends Fragment {

        List<String> data;
        List<String> time;
        Fragment fragment;

        public LinksFragment(List<String> data, List<String> time, Fragment fragment) {
            this.data = data;
            this.time = time;
            this.fragment = fragment;
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View root = inflater.inflate(R.layout.recycler_view, container, false);
            RecyclerView RECYCLER_VIEW = root.findViewById(R.id.RECYCLER_VIEW);
            RECYCLER_VIEW.setLayoutManager(new LinearLayoutManager(getContext()));
            RECYCLER_VIEW.setAdapter(new adapter(data, time, "links", fragment));
            if (data.size()==0){
                root.findViewById(R.id.empty).setVisibility(View.VISIBLE);
                ((TextView)root.findViewById(R.id.emptyMessage)).setText("No Links Found!");
            }
            return root;
        }
    }

    public static class DocsFragment extends Fragment {

        List<String> data;
        List<String> time;
        Fragment fragment;

        public DocsFragment(List<String> data, List<String> time, Fragment fragment) {
            this.data = data;
            this.fragment = fragment;
            this.time = time;
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View root = inflater.inflate(R.layout.recycler_view, container, false);
            RecyclerView RECYCLER_VIEW = root.findViewById(R.id.RECYCLER_VIEW);
            RECYCLER_VIEW.setLayoutManager(new LinearLayoutManager(getContext()));
            RECYCLER_VIEW.setAdapter(new adapter(data, time, "docs", fragment));
            if (data.size()==0){
                root.findViewById(R.id.empty).setVisibility(View.VISIBLE);
                ((TextView)root.findViewById(R.id.emptyMessage)).setText("No Documents Found!");
            }
            return root;
        }
    }

    public static class ImagesFragment extends Fragment {

        List<String> data;
        List<String> time;
        Fragment fragment;

        public ImagesFragment(List<String> data, List<String> time, Fragment fragment) {
            this.data = data;
            this.time = time;
            this.fragment = fragment;
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View root = inflater.inflate(R.layout.recycler_view, container, false);
            RecyclerView RECYCLER_VIEW = root.findViewById(R.id.RECYCLER_VIEW);
            RECYCLER_VIEW.setLayoutManager(new LinearLayoutManager(getContext()));
            RECYCLER_VIEW.setAdapter(new adapter(data, time, "image", fragment));
            if (data.size()==0){
                root.findViewById(R.id.empty).setVisibility(View.VISIBLE);
                ((TextView)root.findViewById(R.id.emptyMessage)).setText("No Images Found!");
            }
            return root;
        }
    }

    public static class adapter extends RecyclerView.Adapter<adapter.vh> {

        List<String> data;
        String type;
        Fragment context;
        List<String> time;

        public adapter(List<String> data, List<String> time, String type, Fragment context) {
            this.type = type;
            this.data = data;
            this.context = context;
            this.time = time;
        }

        @NonNull
        @Override
        public vh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new vh(LayoutInflater.from(parent.getContext()).inflate(R.layout.single_item_study_material, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull vh holder, int position) {
            if (type.equals("image")) {
                Glide.with(context).load(R.drawable.ic_image).into((AppCompatImageView) holder.itemView.findViewById(R.id.imageType));
            } else if (type.equals("docs")) {
                Glide.with(context).load(R.drawable.ic_attachment_pin).into((AppCompatImageView) holder.itemView.findViewById(R.id.imageType));
            } else try {
                Glide.with(context).load(Uri.parse("http://img.youtube.com/vi/" + data.get(position).substring(data.get(position).indexOf("v") + 1) + "") + "/0.jpg").into((RoundedImageView) holder.itemView.findViewById(R.id.youtubeImage));
                holder.itemView.findViewById(R.id.imageType).setVisibility(View.GONE);
                holder.itemView.findViewById(R.id.youtubeImage).setVisibility(View.VISIBLE);
            } catch (Exception e) {
                holder.itemView.findViewById(R.id.imageType).setVisibility(View.VISIBLE);
                holder.itemView.findViewById(R.id.youtubeImage).setVisibility(View.GONE);
            }

            ((AppCompatTextView) holder.itemView.findViewById(R.id.nameImage)).setText(data.get(position));
            ((AppCompatTextView) holder.itemView.findViewById(R.id.time)).setText(time.get(position));

            if (HomeClassroom.from == LocalConstants.TYPE.STUDENT)
                holder.itemView.findViewById(R.id.deleteBtnImage).setVisibility(View.GONE);
            else holder.itemView.findViewById(R.id.deleteBtnImage).setOnClickListener(v -> {
                ((StudyMaterialFragment) context).root.findViewById(R.id.progress).setVisibility(View.VISIBLE);
                if (!type.equals("links"))
                    FirebaseStorage.getInstance().getReference().child(data.get(position)).delete().addOnSuccessListener(aVoid -> post(position)).addOnFailureListener(exception -> post(position));
                else post(position);
            });

            holder.itemView.setOnClickListener(v -> {
                if (!type.equals("links")) {
                    FirebaseStorage.getInstance().getReference().child(data.get(position))
                            .getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                if (data.get(position).endsWith("pdf")) {
                                    Activity activity = context.getActivity();
                                    Intent intent = new Intent(activity, ImagePage.class);
                                    intent.putExtra("urlImage", uri).putExtra("type", "pdf");
                                    context.startActivity(intent);
                                } else if (data.get(position).endsWith("png") || data.get(position).endsWith("jpg") || data.get(position).endsWith("jpeg")) {
                                    Activity activity = context.getActivity();
                                    Intent intent = new Intent(activity, ImagePage.class);
                                    intent.putExtra("urlImage", uri).putExtra("type", "image");
                                    context.startActivity(intent);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {

                }
            });
        }

        private void post(int position) {
            List<Map<String, Object>> list = new ArrayList<>();
            for (int i = 0; i < data.size(); i++) {
                if (i != position) {
                    Map<String, Object> map2 = new HashMap<>();
                    map2.put("name", data.get(position));
                    map2.put("time", time.get(position));
                    list.add(map2);
                }
            }
            Map<String, Object> map = new HashMap<>();
            map.put("studyMaterial", list);
            FirebaseFirestore.getInstance().collection("classroom").document(HomeClassroom.classId).update(map).addOnSuccessListener(aVoid -> {
                ((StudyMaterialFragment) context).FirstTask();
            });
        }


        @Override
        public int getItemCount() {
            return data.size();
        }

        class vh extends RecyclerView.ViewHolder {

            public vh(@NonNull View itemView) {
                super(itemView);
            }
        }
    }





}