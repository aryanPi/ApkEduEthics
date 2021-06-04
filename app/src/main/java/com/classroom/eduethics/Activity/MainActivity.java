package com.classroom.eduethics.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.classroom.eduethics.Fragments.ClassroomStudentFragment;
import com.classroom.eduethics.Fragments.ClassroomTeacherFragment;
import com.classroom.eduethics.Fragments.ContactUsFragment;
import com.classroom.eduethics.Fragments.HomeClassroom;
import com.classroom.eduethics.Fragments.ProfileFragment;
import com.classroom.eduethics.Fragments.SubFragments.NotificationFrag;
import com.classroom.eduethics.Fragments.WebLogin;
import com.classroom.eduethics.R;
import com.classroom.eduethics.Utils.GlobalVariables;
import com.classroom.eduethics.Utils.LocalConstants;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import static com.classroom.eduethics.Utils.LocalConstants.TO_FRAG.TO_ASSIGNMENT_FRAG;
import static com.classroom.eduethics.Utils.LocalConstants.TO_FRAG.TO_BACK_ENROLL;
import static com.classroom.eduethics.Utils.LocalConstants.TO_FRAG.TO_CLASSROOM;
import static com.classroom.eduethics.Utils.LocalConstants.TO_FRAG.TO_HOME_CLASSROOM;
import static com.classroom.eduethics.Utils.LocalConstants.TO_FRAG.TO_TEST_FRAG;


public class MainActivity extends AppCompatActivity {

    public static TextView actionBarTitle;
    public static ImageView toolbarImageFilter;
    public static int TO_WHICH_FRAG = -1;


    ActionBarDrawerToggle actionBarDrawerToggle;
    DrawerLayout drawawerMain;
    NavigationView navigationMainPage;
    int TorS;

    public static Fragment frag = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = getApplicationContext().getSharedPreferences("USER_PREF",
                Context.MODE_PRIVATE);

        TorS = prefs.getString("type", "none").equals("teacher") ? LocalConstants.TYPE.TEACHER : LocalConstants.TYPE.STUDENT;

        GlobalVariables.isStudent = !prefs.getString("type", "none").equals("teacher");

        GlobalVariables.uid = prefs.getString("uid", "none");
        GlobalVariables.name = prefs.getString("name", "none");

        Toast.makeText(this, "uid : " + GlobalVariables.uid, Toast.LENGTH_SHORT).show();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setDisplayShowCustomEnabled(true);
            getSupportActionBar().setCustomView(R.layout.custom_action_bar);
        } else if (getActionBar() != null) {
            getActionBar().setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP);
            getActionBar().setDisplayShowCustomEnabled(true);
            getActionBar().setCustomView(R.layout.custom_action_bar);
        }

        if (getSupportActionBar() != null) {

            toolbarImageFilter = getSupportActionBar().getCustomView().findViewById(R.id.toolbarImage);
            actionBarTitle = getSupportActionBar().getCustomView().findViewById(R.id.actionBarTitle);
            getSupportActionBar().getCustomView().findViewById(R.id.notiToolbar).setOnClickListener(v -> getSupportFragmentManager().beginTransaction().replace(R.id.fragmentFrame,new NotificationFrag()).commit());

            getSupportActionBar().getCustomView().findViewById(R.id.webLoginToolbar).setOnClickListener(v -> getSupportFragmentManager().beginTransaction().replace(R.id.fragmentFrame,new WebLogin()).commit());

            getSupportActionBar().getCustomView().findViewById(R.id.toggleStudentAndParent).setVisibility(View.VISIBLE);
            getSupportActionBar().getCustomView().findViewById(R.id.toggleStudentAndParent).setOnClickListener(v -> {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("type", TorS != LocalConstants.TYPE.STUDENT ? "student" : "teacher");
                editor.putString("uid", TorS != LocalConstants.TYPE.STUDENT ? "kVc7ekHJu2awUYL8gIeNwAJW4Gs1" : "zmQPYdJIyvZNGUbNsw85C59qnzB3");
                editor.putString("name", TorS != LocalConstants.TYPE.STUDENT ? "S" : "T");
                editor.apply();
                startActivity(new Intent(MainActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            });
        } else {
            if (getActionBar() != null) {

            }
            toolbarImageFilter = getActionBar().getCustomView().findViewById(R.id.toolbarImage);
            actionBarTitle = getActionBar().getCustomView().findViewById(R.id.actionBarTitle);
            getActionBar().getCustomView().findViewById(R.id.toggleStudentAndParent).setVisibility(View.VISIBLE);
            getActionBar().getCustomView().findViewById(R.id.toggleStudentAndParent).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("type", TorS != LocalConstants.TYPE.STUDENT ? "student" : "teacher");
                    editor.putString("uid", TorS != LocalConstants.TYPE.STUDENT ? "kVc7ekHJu2awUYL8gIeNwAJW4Gs1" : "zmQPYdJIyvZNGUbNsw85C59qnzB3");
                    editor.putString("name", TorS != LocalConstants.TYPE.STUDENT ? "S" : "T");
                    editor.apply();
                    startActivity(new Intent(MainActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                }
            });
        }


        drawawerMain = findViewById(R.id.DrawawerMain);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawawerMain, R.string.open, R.string.close);
        drawawerMain.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.getDrawerArrowDrawable().setColor(ResourcesCompat.getColor(getResources(),R.color.black,null));
        actionBarDrawerToggle.syncState();

        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        if (getSupportActionBar() != null) {
            this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getColor(R.color.BackGround)));
        }


        navigationMainPage = findViewById(R.id.navigationMainPage);
        navigationMainPage.setNavigationItemSelectedListener(item -> {
            setThisIdFragment(item.getItemId());
            return true;
        });

        navigationMainPage.setCheckedItem(R.id.nav_dashboard);
        setThisIdFragment(R.id.nav_dashboard);
    }

    private void setThisIdFragment(int itemId) {
        Fragment fragment = null;
        switch (itemId) {
            case R.id.nav_dashboard:
                if (TorS == LocalConstants.TYPE.STUDENT) {
                    fragment = new ClassroomStudentFragment(this);
                } else {
                    fragment = new ClassroomTeacherFragment(this);
                }
                break;
            case R.id.nav_profile:
                fragment = new ProfileFragment(this);
                break;
            case R.id.nav_contactUs:
                fragment = new ContactUsFragment(this);
                break;
            case R.id.nav_language:
                Toast.makeText(this, "Working", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_privacy:
                String urlString = "http://216.10.250.247/privacy";
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlString));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setPackage("com.android.chrome");
                startActivity(intent);
                break;
            case R.id.nav_useClassroom:
//                fragment = new HowToUseFragment();
                Toast.makeText(this, "Working", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_rate:
                break;
            case R.id.nav_share:
                break;
            case R.id.nav_logout:
                logout();
                break;
            default:
                break;
        }

        if (fragment != null)
            getSupportFragmentManager().beginTransaction().setCustomAnimations(
                    R.anim.slide_in,  // enter
                    R.anim.fade_out,  // exit
                    R.anim.fade_in,   // popEnter
                    R.anim.slide_out  // popExit
            ).replace(R.id.fragmentFrame, fragment).commit();

        drawawerMain.closeDrawer(GravityCompat.START);
        navigationMainPage.setCheckedItem(itemId);
    }

    private void logout() {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("USER_PREF",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        GlobalVariables.isStudent = null;
        GlobalVariables.name = "";
        GlobalVariables.uid = "";
        editor.putString("type", "none");
        editor.clear();
        editor.apply();
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(this, SpalashActivity.class));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) return true;
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        switch (TO_WHICH_FRAG) {
            case TO_ASSIGNMENT_FRAG:
            case TO_TEST_FRAG:
                HomeClassroom.toWhichFrag = TO_WHICH_FRAG;
                getSupportFragmentManager().beginTransaction().setCustomAnimations(
                        R.anim.slide_in,  // enter
                        R.anim.fade_out,  // exit
                        R.anim.fade_in,   // popEnter
                        R.anim.slide_out  // popExit
                ).replace(R.id.fragmentFrame, new HomeClassroom(this, HomeClassroom.classId, HomeClassroom.from)).commit();
                break;
            case TO_CLASSROOM:
                if (!GlobalVariables.isStudent)
                    getSupportFragmentManager().beginTransaction().setCustomAnimations(
                            R.anim.slide_in,  // enter
                            R.anim.fade_out,  // exit
                            R.anim.fade_in,   // popEnter
                            R.anim.slide_out  // popExit
                    ).replace(R.id.fragmentFrame, new ClassroomTeacherFragment(this)).commit();
                else
                    getSupportFragmentManager().beginTransaction().setCustomAnimations(
                            R.anim.slide_in,  // enter
                            R.anim.fade_out,  // exit
                            R.anim.fade_in,   // popEnter
                            R.anim.slide_out  // popExit
                    ).replace(R.id.fragmentFrame, new ClassroomStudentFragment(this)).commit();
                break;
            case TO_HOME_CLASSROOM:
                HomeClassroom.toWhichFrag = TO_WHICH_FRAG;
                getSupportFragmentManager().beginTransaction().setCustomAnimations(
                        R.anim.slide_in,  // enter
                        R.anim.fade_out,  // exit
                        R.anim.fade_in,   // popEnter
                        R.anim.slide_out  // popExit
                ).replace(R.id.fragmentFrame, new HomeClassroom(this, HomeClassroom.classId, HomeClassroom.from)).commit();
                break;
            case TO_BACK_ENROLL:
                ((ClassroomStudentFragment)frag).backEnroll();
                break;
            default:
                super.onBackPressed();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Toast.makeText(this, "Get something", Toast.LENGTH_SHORT).show();
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (scanningResult != null) {
            if (scanningResult.getContents() != null) {
                WebView webView = new WebView(this);
                webView.getSettings().setJavaScriptEnabled(true);
                webView.getSettings().setDomStorageEnabled(true);
                webView.loadUrl("http://216.10.250.247/confirm_login_web/"+scanningResult.getContents().toString().trim()+"/"+GlobalVariables.uid+"/"+GlobalVariables.isStudent);
                Snackbar.make(findViewById(R.id.fragmentFrame),"Connected",1000).show();


            }

        }else{
        }
    }

}