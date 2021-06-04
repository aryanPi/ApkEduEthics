package com.classroom.eduethics.Utils;


import java.util.Calendar;

import static android.Manifest.permission;

public interface LocalConstants {


    String NOTIFICATION_ID = "com.newprop.classroom";
    String NOTIFICATION_NAME = "Classroom Notification";
    String STORAGE_REF = "gs://pc-api-6009912515183436118-930.appspot.com";

    String[] PERMISSIONS = {
            permission.MODIFY_AUDIO_SETTINGS,
            permission.CALL_PHONE,
            permission.RECORD_AUDIO,
            permission.BLUETOOTH,
            permission.CAMERA,
            permission.READ_EXTERNAL_STORAGE
    };

    interface TYPE {
        int TEACHER = 1;
        int STUDENT = -1;
    }

    interface PERMISSION_CODE {
        int SELECT_PICTURE = 100;
        int PICKFILE_RESULT_CODE = 101;
        int CAMERA_REQUEST = 102;
        int IMAGE_PROFILE = 103;
        int IMAGE_INS = 104;
    }

    interface REQ_CODE {
        int CAMERA_REQUEST = 200;
        int ALL = 201;
    }

    interface FROM {
        int FROM_CREATE_ASSIGNMENT = 0;
        int FROM_CREATE_TEST = 1;
        int FROM_SUMBIT_TEST = 2;
        int FROM_ADD_SOLUTION = 4;
        int FROM_TEST_FRAG = 5;
        int FROM_ASSIGNMENT_FRAG = 6;
        int FROM_EVALUATE_FRAG = 7;
        int FROM_SUMBIT_TEST_ATT = 8;
    }

    interface TO_FRAG {
        int TO_HOME_CLASSROOM = 0;
        int TO_CLASSROOM = 1;
        int TO_TEST_FRAG = 4;
        int TO_ASSIGNMENT_FRAG = 3;
        int TO_BACK_ENROLL = 5;
    }

    String[] MONTHS = {
            "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December", "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"
    };

    String[] DAYS = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};


}
