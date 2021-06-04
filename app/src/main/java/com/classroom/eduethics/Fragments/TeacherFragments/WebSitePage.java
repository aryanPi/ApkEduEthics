package com.classroom.eduethics.Fragments.TeacherFragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.classroom.eduethics.Adapter.WebsiteClassroomAdapter;
import com.classroom.eduethics.Fragments.HomeClassroom;
import com.classroom.eduethics.Models.WebsiteModel;
import com.classroom.eduethics.R;
import com.classroom.eduethics.Utils.GlobalVariables;
import com.classroom.eduethics.Utils.LocalConstants;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import static android.app.Activity.RESULT_OK;
import static android.content.ClipDescription.MIMETYPE_TEXT_PLAIN;
import static com.classroom.eduethics.Utils.LocalConstants.PERMISSION_CODE.IMAGE_INS;
import static com.classroom.eduethics.Utils.LocalConstants.PERMISSION_CODE.IMAGE_PROFILE;

public class WebSitePage extends Fragment {

    View root;
    ImageView insLogo, profilePic;
    TextInputEditText insName, insAbout, insSubject, experiance, address, city, state, youtubeLink, fbLink;
    List<String> selected = new ArrayList<>();
    Button contiBtn;
    List<String> selectedSub = new ArrayList<>();
    List<Map<String, String>> demoLinks = new ArrayList<>();
    Uri profileUri = null, insLogouri = null;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_web_site_page, container, false);


        contiBtn = root.findViewById(R.id.continue_website);


        insLogo = root.findViewById(R.id.instituteLogo);
        profilePic = root.findViewById(R.id.profileLogo);
        insName = root.findViewById(R.id.insName);
        insAbout = root.findViewById(R.id.insAbout);
        insSubject = root.findViewById(R.id.insSubject);
        experiance = root.findViewById(R.id.experiance);
        address = root.findViewById(R.id.address);
        city = root.findViewById(R.id.city);
        state = root.findViewById(R.id.state);
        youtubeLink = root.findViewById(R.id.youtubeLink);
        fbLink = root.findViewById(R.id.fbLink);


        insLogo.setOnClickListener(v -> {
            Intent i = new Intent(
                    Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, IMAGE_INS);
        });

        profilePic.setOnClickListener(v -> {
            Intent i = new Intent(
                    Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, IMAGE_PROFILE);
        });

        contiBtn.setOnClickListener(v -> conti1());

        return root;
    }

    private void conti1() {
        boolean allDone = true;
        //insName, insAbout, insSubject, experiance, address, city, state, youtubeLink, fbLink
        if (insName.getText().toString().trim().equals("")) {
            allDone = false;
            insName.setError("Required!");
        }
        if (state.getText().toString().trim().equals("")) {
            allDone = false;
            state.setError("Required!");
        }
        if (insAbout.getText().toString().trim().equals("")) {
            allDone = false;
            insAbout.setError("Required!");
        }
        if (insSubject.getText().toString().trim().equals("")) {
            allDone = false;
            insSubject.setError("Required!");
        }
        if (experiance.getText().toString().trim().equals("")) {
            allDone = false;
            experiance.setError("Required!");
        }


        if (allDone) {
            TransitionManager.beginDelayedTransition(root.findViewById(R.id.MAIN));
            root.findViewById(R.id.firstWebsite).setVisibility(View.GONE);
            root.findViewById(R.id.secondWebsite).setVisibility(View.VISIBLE);
            FirebaseFirestore.getInstance()
                    .collection("teacher")
                    .document(GlobalVariables.uid)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            List<Map<String, Object>> data = (List) documentSnapshot.get("classroom");
                            RecyclerView recyclerView = root.findViewById(R.id.classesRecycler);
                            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                            recyclerView.setAdapter(new WebsiteClassroomAdapter(data, selected, selectedSub));
                            contiBtn.setOnClickListener(v -> conti2());
                        }
                    });

        }

    }

    private void conti2() {

        TransitionManager.beginDelayedTransition(root.findViewById(R.id.MAIN));
        root.findViewById(R.id.secondWebsite).setVisibility(View.GONE);
        root.findViewById(R.id.thiredWebsite).setVisibility(View.VISIBLE);
        RecyclerView recyclerView = root.findViewById(R.id.recyclerYoutube);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        List<String> urlsData = new ArrayList<>();
        List<String> titlesData = new ArrayList<>();
        List<String> desData = new ArrayList<>();
        innerYOutubeAdapter adapter = new innerYOutubeAdapter(urlsData, titlesData, desData, getContext());
        recyclerView.setAdapter(adapter);

        root.findViewById(R.id.addVideo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetDialog dialog = new BottomSheetDialog(getContext());
                dialog.setContentView(R.layout.bottom_addyouitube);
                TextInputEditText desbottom = dialog.findViewById(R.id.desbottom), title = dialog.findViewById(R.id.titlebottom), url = dialog.findViewById(R.id.youtubeUrl);
                dialog.findViewById(R.id.paste).setOnClickListener(v12 -> {
                    ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);

                    if ((clipboard.getPrimaryClipDescription().hasMimeType(MIMETYPE_TEXT_PLAIN)) && (clipboard.hasPrimaryClip())){
                        ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
                        url.setText(item.getText().toString());
                    }
                });

                dialog.findViewById(R.id.addBottom).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (url.getText().toString().equals("")) {
                            url.setError("Required !");
                        } else if (title.getText().toString().equals("")) {
                            title.setError("Required !");
                        } else {
                            String t = extractYTId(url.getText().toString());
                            if (t == null) {
                                url.setError("Enter Correct URL !");
                            } else {
                                urlsData.add(t);
                                titlesData.add(title.getText().toString());
                                desData.add(desbottom.getText().toString());
                                adapter.notifyItemInserted(urlsData.size() - 1);
                            }


                        }

                    }
                });

                dialog.show();
            }
        });
        contiBtn.setOnClickListener(v -> {
            for (int i = 0; i < urlsData.size(); i++) {
                Map<String, String> singleData = new HashMap<>();
                singleData.put("title", titlesData.get(i));
                singleData.put("url", urlsData.get(i));
                Toast.makeText(getContext(), "" + urlsData.get(i), Toast.LENGTH_SHORT).show();
                singleData.put("des", desData.get(i));
                demoLinks.add(singleData);
            }
            conti3();
        });
    }

    private void conti3() {
        TransitionManager.beginDelayedTransition(root.findViewById(R.id.MAIN));
        root.findViewById(R.id.forthWebsite).setVisibility(View.VISIBLE);
        root.findViewById(R.id.thiredWebsite).setVisibility(View.GONE);
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topToBottom = R.id.scroll;
        params.setMargins(32, 0, 32, 0);
        TextInputEditText websiteName = root.findViewById(R.id.websiteName);
        contiBtn.setText("Check avaiability");
        websiteName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                contiBtn.setText("Check Availability");
            }
        });

        contiBtn.setLayoutParams(params);

        contiBtn.setOnClickListener(v -> {
            if (contiBtn.getText().toString().equals("Check Availability")) {
                if (websiteName.getText().toString().equals("")) {
                    websiteName.setError("Fill this first");
                } else {
                    root.findViewById(R.id.progress).setVisibility(View.VISIBLE);
                    FirebaseFirestore.getInstance()
                            .collection("utilData")
                            .document("websitesDomain")
                            .get()
                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    if (documentSnapshot.exists()) {
                                        if (((List<String>) documentSnapshot.get("data")).contains(websiteName.getText().toString())) {
                                            websiteName.setError("Already Exist");
                                            root.findViewById(R.id.progress).setVisibility(View.GONE);
                                            websiteName.setHintTextColor(getResources().getColor(android.R.color.holo_green_dark));
                                        } else {
                                            contiBtn.setText("Build Now");
                                            root.findViewById(R.id.progress).setVisibility(View.GONE);
                                            Toast.makeText(getContext(), "Available", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        root.findViewById(R.id.progress).setVisibility(View.GONE);
                                        contiBtn.setText("Build Now");
                                    }
                                }
                            });
                }
            } else {
                websiteUrl = websiteName.getText().toString();
                root.findViewById(R.id.progress).setVisibility(View.VISIBLE);
                if (insLogouri != null || profileUri != null) {
                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference storageRef = storage.getReferenceFromUrl(LocalConstants.STORAGE_REF);    //change the url according to your firebase app

                    if (insLogouri != null) {
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        Cursor cursor = getActivity().getContentResolver().query(insLogouri,
                                filePathColumn, null, null, null);
                        cursor.moveToFirst();
                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        String name = cursor.getString(columnIndex);
                        cursor.close();

                        String exitention = name.substring(name.lastIndexOf("."));
                        StorageReference riversRef = storageRef.child(("websiteImages/" + GlobalVariables.uid + "/logo" + exitention));
                        riversRef.putFile(insLogouri)
                                .addOnSuccessListener(taskSnapshot -> {
                                    Toast.makeText(getActivity(), "Done first image", Toast.LENGTH_SHORT).show();
                                    logoUrl = taskSnapshot.getMetadata().getPath();
                                    checkAndPost();
                                })
                                .addOnFailureListener(e -> {
                                    checkAndPost();
                                    Toast.makeText(getContext(), "Unable to post your Image !", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        checkAndPost();
                    }
                    if (profileUri != null) {
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        Cursor cursor = getActivity().getContentResolver().query(profileUri,
                                filePathColumn, null, null, null);
                        cursor.moveToFirst();
                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        String name = cursor.getString(columnIndex);
                        cursor.close();

                        String exitention = name.substring(name.lastIndexOf("."));
                        StorageReference riversRef = storageRef.child(("websiteImages/" + GlobalVariables.uid + "/profile" + exitention));
                        riversRef.putFile(profileUri)
                                .addOnSuccessListener(taskSnapshot -> {
                                    profileUrl = taskSnapshot.getMetadata().getPath();
                                    checkAndPost();
                                })
                                .addOnFailureListener(e -> {
                                    checkAndPost();
                                    if (getActivity() == null) return;
                                    Toast.makeText(getContext(), "Unable to post your Image !", Toast.LENGTH_SHORT).show();

                                });
                    } else {
                        checkAndPost();
                    }

                } else uploadTofireBase();
            }
        });
    }

    int counter = 0;
    String logoUrl = "";
    String profileUrl = "";
    String websiteUrl = "";

    private void checkAndPost() {
        counter++;
        if (counter == 2) {
            uploadTofireBase();
        }
        Toast.makeText(getActivity(), "Here first " + counter, Toast.LENGTH_SHORT).show();
    }

    private void uploadTofireBase() {
        Toast.makeText(getActivity(), "Here", Toast.LENGTH_SHORT).show();
        WebsiteModel model = new WebsiteModel();
        model.setCity(city.getText().toString());
        model.setState(state.getText().toString());
        model.setAboutIns(insAbout.getText().toString());
        model.setAddress(address.getText().toString());
        model.setFbLink(fbLink.getText().toString());
        model.setInsLogo(logoUrl);
        model.setProfileLink(profileUrl);
        model.setInsName(insName.getText().toString());
        model.setSubjects(insSubject.getText().toString());
        model.setTeacherId(GlobalVariables.uid);
        model.setYearsExp(experiance.getText().toString());
        model.setYoutubeLink(youtubeLink.getText().toString());
        List<Map<String, String>> classroom = new ArrayList<>();
        for (int i = 0; i < selected.size(); i++) {
            Map<String, String> map = new HashMap<>();
            map.put("className", selected.get(i));
            map.put("subject", selectedSub.get(i));
            classroom.add(map);
        }
        model.setClassroom(classroom);
        model.setDemoLinks(demoLinks);
        FirebaseFirestore.getInstance()
                .collection("webD")
                .document(websiteUrl)
                .set(model).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                FirebaseFirestore.getInstance().collection("teacher").document(GlobalVariables.uid).update("isSite",websiteUrl);
                FirebaseFirestore.getInstance()
                        .collection("utilData")
                        .document("websitesDomain")
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                List<String> domains = new ArrayList<>();
                                if (documentSnapshot.exists())
                                    if (documentSnapshot.get("data") != null)
                                        domains = (List) documentSnapshot.get("data");
                                domains.add(websiteUrl);
                                Map<String, Object> map = new HashMap<>();
                                map.put("data", domains);
                                FirebaseFirestore.getInstance()
                                        .collection("utilData")
                                        .document("websitesDomain")
                                        .set(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        createDomain();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull @NotNull Exception e) {
                                        Toast.makeText(getContext(), "Failed3", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        Toast.makeText(getContext(), "Failed2", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                Toast.makeText(getContext(), "Failed1", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void createDomain() {
        WebView webview = new WebView(getContext());
        new Thread(new Runnable() {
            @Override
            public void run() {
                webview.getSettings().setJavaScriptEnabled(true);
                webview.loadUrl("http://216.10.250.247/createdomain/"+websiteUrl+"/"+GlobalVariables.uid);
            }
        }).start();

        getActivity().onBackPressed();
    }


    private static String extractYTId(String ytUrl) {
        String vId = null;
        Pattern pattern = Pattern.compile(
                "^https?://.*(?:youtu.be/|v/|u/\\w/|embed/|watch?v=)([^#&?]*).*$",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(ytUrl);
        if (matcher.matches()) {
            vId = matcher.group(1);
        }
        return vId;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_INS && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            insLogouri = selectedImage;
            Glide.with(getActivity()).load(selectedImage).centerCrop().into(insLogo);
        } else if (requestCode == IMAGE_PROFILE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            profileUri = selectedImage;
            Glide.with(getActivity()).load(selectedImage).centerCrop().into(profilePic);
        }
    }

    public static class innerYOutubeAdapter extends RecyclerView.Adapter<innerYOutubeAdapter.vh> {

        List<String> urlsData, titlesData, data;
        Context context;

        public innerYOutubeAdapter(List<String> urlsData, List<String> titlesData, List<String> data, Context context) {
            this.context = context;
            this.urlsData = urlsData;
            this.titlesData = titlesData;
            this.data = data;
        }

        @NonNull
        @NotNull
        @Override
        public vh onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
            return new vh(LayoutInflater.from(parent.getContext()).inflate(R.layout.youtube_single_layout, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull @NotNull WebSitePage.innerYOutubeAdapter.vh holder, int position) {
            ((TextView) holder.itemView.findViewById(R.id.titleYoutube)).setText(titlesData.get(position));

            holder.itemView.findViewById(R.id.deleteSingleYoutube).setOnClickListener(v -> {
                urlsData.remove(position);
                data.remove(position);
                titlesData.remove(position);
                notifyItemRemoved(position);
                notifyDataSetChanged();

            });

            holder.itemView.findViewById(R.id.editSingleYoutube).setOnClickListener(v -> {
                BottomSheetDialog dialog = new BottomSheetDialog(context);
                dialog.setContentView(R.layout.bottom_addyouitube);
                TextInputEditText desbottom = dialog.findViewById(R.id.desbottom), title = dialog.findViewById(R.id.titlebottom), url = dialog.findViewById(R.id.youtubeUrl);
                url.setText(urlsData.get(position));
                title.setText(titlesData.get(position));
                desbottom.setText(data.get(position));
                dialog.findViewById(R.id.paste).setOnClickListener(v12 -> {
                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    String pasteData = "";
                    // If it does contain data, decide if you can handle the data.
                    if (!(clipboard.hasPrimaryClip())) {

                    } else if (!(clipboard.getPrimaryClipDescription().hasMimeType(MIMETYPE_TEXT_PLAIN))) {
                        // since the clipboard has data but it is not plain text

                    } else {
                        ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
                        url.setText(item.getText().toString());
                    }
                });
                dialog.findViewById(R.id.addBottom).setOnClickListener(v1 -> {
                    if (url.getText().toString().equals("")) {
                        url.setError("Required !");
                    } else if (title.getText().toString().equals("")) {
                        title.setError("Required !");
                    } else {
                        String t = extractYTId(url.getText().toString());
                        if (t == null) {
                            url.setError("Enter correct URL !");
                        } else {
                            urlsData.set(position, extractYTId(url.getText().toString()));
                            titlesData.set(position, title.getText().toString());
                            data.set(position, desbottom.getText().toString());
                            notifyItemChanged(position);
                            dialog.dismiss();

                        }
                    }
                });
                dialog.show();
            });

        }

        @Override
        public int getItemCount() {
            return urlsData.size();
        }

        static class vh extends RecyclerView.ViewHolder {

            public vh(@NonNull @NotNull View itemView) {
                super(itemView);
            }
        }
    }


}