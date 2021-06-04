package com.classroom.eduethics.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import com.bumptech.glide.Glide;
import com.classroom.eduethics.R;
import com.rajat.pdfviewer.PdfViewerActivity;

public class ImagePage extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_image_page);



        Uri url = getIntent().getParcelableExtra("urlImage");
        String type = getIntent().getStringExtra("type");


        switch (type) {
            case "pdf":
                Intent intent = new Intent(this, PdfViewerActivity.class);
                intent.putExtra("pdf_file_url", url.toString());
                intent.putExtra("pdf_file_title", url.toString().substring(url.toString().length() - 10));
                intent.putExtra("pdf_file_directory", getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
                intent.putExtra("enable_download", true);
                intent.putExtra("isPDFFromPath", false);
                startActivity(intent);
                finish();
                break;
            case "image":
                Glide.with(this).load(url).into((ImageView) findViewById(R.id.image));
                break;
            case "download":
                Toast.makeText(this, "Download URL", Toast.LENGTH_SHORT).show();
                break;
            case "youtube":

                break;
        }


    }
}