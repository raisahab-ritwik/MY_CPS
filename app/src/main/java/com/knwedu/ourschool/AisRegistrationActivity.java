package com.knwedu.ourschool;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.knwedu.college.utils.CollegeDataStructureFramwork;
import com.knwedu.calcuttapublicschool.R;
import com.knwedu.ourschool.db.DatabaseAdapter;
import com.knwedu.ourschool.db.SchoolApplication;
import com.knwedu.ourschool.utils.Constants;
import com.knwedu.ourschool.utils.DataStructureFramwork;
import com.knwedu.ourschool.utils.JsonParser;
import com.knwedu.ourschool.utils.SchoolAppUtils;
import com.knwedu.ourschool.utils.Urls;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.utils.StorageUtils;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ddasgupta on 1/19/2017.
 */

public class AisRegistrationActivity extends Activity {

    private RadioGroup radioUserGroup;
    EditText teacher_name, student_name, parent_name, email, mobile_no, edt_password, cnf_password;
    Button register;
    String user_password, user_id_login;
    Spinner spinnerClass, spinnerSection;
    String selected_section_id, selected_class_id, user_type_id;
    String username = "";
    String password = "";
    String regid, curVersion;
    ProgressDialog dialog;
    private TextView login_link;
    private int menu_val;
    private DatabaseAdapter mDatabase;
    AlertDialog.Builder dialoggg;
    ImageView logo_image;
    private DisplayImageOptions options;
    ImageLoaderConfiguration config;
    File cacheDir;

    private ArrayList<DataStructureFramwork.TimeTable> timeTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_ais_registration);
        user_type_id = "5";

        Bundle bundle = new Bundle();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.getString("devicetoken") != null) {
                regid = extras.getString("devicetoken");
            }
            if (extras.getString("curversion") != null) {
                curVersion = extras.getString("curversion");

            }
        }

        teacher_name = (EditText) findViewById(R.id.edt_teacher_name);
        parent_name = (EditText) findViewById(R.id.edt_parent_name);
        student_name = (EditText) findViewById(R.id.edt_student_name);
        spinnerClass = (Spinner) findViewById(R.id.spinnerClass);
        spinnerSection = (Spinner) findViewById(R.id.spinnerSection);
        email = (EditText) findViewById(R.id.edt_email);
        mobile_no = (EditText) findViewById(R.id.edt_phone);
        edt_password = (EditText) findViewById(R.id.edt_password);
        cnf_password = (EditText) findViewById(R.id.edt_cnf_password);
        register = (Button) findViewById(R.id.btn_reg);
        login_link = (TextView) findViewById(R.id.login_link);
        logo_image = (ImageView) findViewById(R.id.image);

        cacheDir = StorageUtils.getCacheDirectory(this);
        config = new ImageLoaderConfiguration.Builder(this).memoryCacheExtraOptions(480, 800) // default = device screen dimensions
                .diskCacheExtraOptions(480, 800, null)
                .threadPriority(Thread.NORM_PRIORITY - 2) // default
                .tasksProcessingOrder(QueueProcessingType.FIFO) // default
                .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
                .memoryCacheSize(2 * 1024 * 1024)
                .memoryCacheSizePercentage(13) // default
                .diskCache(new UnlimitedDiskCache(cacheDir)) // default
                .diskCacheFileCount(1000)
                .diskCacheFileNameGenerator(new HashCodeFileNameGenerator()) // default
                .imageDownloader(new BaseImageDownloader(this)) // default
                .defaultDisplayImageOptions(DisplayImageOptions.createSimple()) // default
                .writeDebugLogs()
                .build();
        ImageLoader.getInstance().init(config);

        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.progress_animation)
                .showImageForEmptyUri(R.drawable.app_icon)
                .showImageOnFail(R.drawable.app_icon)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
        String ais_logo_url = SchoolAppUtils.GetSharedParameter(
                AisRegistrationActivity.this, Constants.AIS_APP_LOGO);

        ImageLoader.getInstance()
                .displayImage(ais_logo_url, logo_image, options);


        mDatabase = ((SchoolApplication) getApplication()).getDatabase();

        login_link.setPaintFlags(login_link.getPaintFlags()
                | Paint.UNDERLINE_TEXT_FLAG);

        login_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int errorCount = 0;

                if (user_type_id.equals("5") || user_type_id.equals("4")) {
                    if (parent_name.getText().toString().isEmpty()) {
                        parent_name.setError("Enter Parent Name");
                        parent_name.requestFocus();
                        errorCount++;
                    } else {
                        parent_name.setError(null);
                    }

                    if (student_name.getText().toString().isEmpty()) {
                        student_name.setError("Enter Student Name");
                        student_name.requestFocus();
                        errorCount++;
                    } else {
                        student_name.setError(null);
                    }

                    if (selected_section_id.equals("0")) {
                        Toast.makeText(
                                AisRegistrationActivity.this,
                                "Select valid Class & Section",
                                Toast.LENGTH_LONG).show();
                        errorCount++;
                    }


                } else {
                    if (teacher_name.getText().toString().isEmpty()) {
                        teacher_name.setError("Enter Student Name");
                        teacher_name.requestFocus();
                        errorCount++;
                    } else {
                        teacher_name.setError(null);
                    }

                }

                if (!SchoolAppUtils
                        .isValidEmail(email.getText().toString())) {
                    email.setError("Enter valid Email");
                    email.requestFocus();
                    errorCount++;
                } else {
                    email.setError(null);
                }

                if (mobile_no.getText().toString().isEmpty()) {
                    mobile_no.setError("Enter Phone Number");
                    mobile_no.requestFocus();
                    errorCount++;
                } else {
                    mobile_no.setError(null);
                }

                if (mobile_no.getText().toString().length() < 10 || mobile_no.getText().toString().length() > 10) {
                    mobile_no.setError("Enter valid Phone Number");
                    mobile_no.requestFocus();
                    errorCount++;
                } else {
                    mobile_no.setError(null);
                }

                String pass = edt_password.getText().toString();
                String conf = cnf_password.getText().toString();

                if (pass.equals(conf)) {
                    cnf_password.setError(null);
                } else {
                    cnf_password.setError("Password and Confirm password should be same.");
                    cnf_password.requestFocus();
                    errorCount++;
                }

                if (edt_password.getText().length() == 0) {
                    edt_password.setError("Enter Password");
                    edt_password.requestFocus();
                    errorCount++;
                } else {
                    edt_password.setError(null);
                }

                if (edt_password.getText().length() < 6) {
                    edt_password.setError("Password length ");
                    edt_password.requestFocus();
                    errorCount++;
                } else {
                    edt_password.setError(null);
                }

                if (errorCount == 0) {
                    View aview = AisRegistrationActivity.this.getCurrentFocus();
                    if (aview != null) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }


                    List<NameValuePair> nvp = new ArrayList<NameValuePair>();
                    user_password = edt_password.getText().toString();
                    user_id_login = email.getText().toString();

                    if (user_type_id.equals("5") || user_type_id.equals("4")) {
                        nvp.add(new BasicNameValuePair("parent_name", parent_name.getText().toString()));
                        nvp.add(new BasicNameValuePair("student_name", student_name.getText().toString()));
                        nvp.add(new BasicNameValuePair("section_id", selected_section_id));
                        nvp.add(new BasicNameValuePair("mobile", mobile_no.getText().toString()));
                    } else {
                        nvp.add(new BasicNameValuePair("name", teacher_name.getText().toString()));
                        nvp.add(new BasicNameValuePair("mobile", mobile_no.getText().toString()));
                    }
                    nvp.add(new BasicNameValuePair("user_type_id", user_type_id));
                    nvp.add(new BasicNameValuePair("password", edt_password.getText().toString()));
                    nvp.add(new BasicNameValuePair("email", email.getText().toString()));
                    nvp.add(new BasicNameValuePair("organization_id", SchoolAppUtils
                            .GetSharedParameter(AisRegistrationActivity.this,
                                    Constants.UORGANIZATIONID)));

                    new RegisterUserAsync().execute(nvp);
                }


            }
        });


        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
        nameValuePairs.add(new BasicNameValuePair("organization_id", SchoolAppUtils
                .GetSharedParameter(AisRegistrationActivity.this,
                        Constants.UORGANIZATIONID)));
        new SetClassListAsyn().execute(nameValuePairs);

    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.radio_student:
                if (checked)
                    // Ninjas rule
                    /*Toast.makeText(AisRegistrationActivity.this,
                            "student Clicked", Toast.LENGTH_SHORT)
                            .show();*/
                    teacher_name.setVisibility(View.GONE);
                parent_name.setVisibility(View.VISIBLE);
                student_name.setVisibility(View.VISIBLE);
                spinnerClass.setVisibility(View.VISIBLE);
                spinnerSection.setVisibility(View.VISIBLE);
                user_type_id = "4";
                break;
            case R.id.radio_parent:
                if (checked)
                    // Ninjas rule
                    /*Toast.makeText(AisRegistrationActivity.this,
                            "Parent Clicked", Toast.LENGTH_SHORT)
                            .show();*/
                    teacher_name.setVisibility(View.GONE);
                parent_name.setVisibility(View.VISIBLE);
                student_name.setVisibility(View.VISIBLE);
                spinnerClass.setVisibility(View.VISIBLE);
                spinnerSection.setVisibility(View.VISIBLE);
                user_type_id = "5";
                break;
            case R.id.radio_teacher:
                if (checked)
                    // Ninjas rule
                   /* Toast.makeText(AisRegistrationActivity.this,
                            "Teacher Clicked", Toast.LENGTH_SHORT)
                            .show();*/
                    teacher_name.setVisibility(View.VISIBLE);
                parent_name.setVisibility(View.GONE);
                student_name.setVisibility(View.GONE);
                spinnerClass.setVisibility(View.GONE);
                spinnerSection.setVisibility(View.GONE);
                user_type_id = "3";

                break;
        }
    }


    private class SetClassListAsyn extends
            AsyncTask<List<NameValuePair>, Void, Boolean> {
        ProgressDialog dialog;
        String error = "";
        ArrayList<DataStructureFramwork.ClassOfOrg> classes;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(AisRegistrationActivity.this);
            //dialog.setTitle(getResources().getString(isSchool?R.string.fetch_classes: R.string.fetch_program));
            dialog.setMessage(getResources().getString(R.string.please_wait));
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected Boolean doInBackground(List<NameValuePair>... params) {

            List<NameValuePair> namevaluepair = params[0];

            JsonParser jParser = new JsonParser();
            JSONObject json = jParser.getJSONFromUrlfrist(
                    namevaluepair,
                    SchoolAppUtils.GetSharedParameter(
                            AisRegistrationActivity.this, Constants.COMMON_URL)
                            + Urls.api_ais_class_section_list);
            try {

                if (json != null) {
                    if (json.getString("result").equalsIgnoreCase("1")) {
                        classes = new ArrayList<DataStructureFramwork.ClassOfOrg>();
                        classes.clear();
                        JSONArray array;
                        try {
                            array = json.getJSONArray("data");
                        } catch (Exception e) {
                            error = "Error in Data";
                            return false;
                        }

                        for (int i = 0; i < array.length(); i++) {
                            DataStructureFramwork.ClassOfOrg classOfOrg = new DataStructureFramwork.ClassOfOrg(
                                    array.getJSONObject(i));
                            classes.add(classOfOrg);
                        }
                        return true;
                    } else {
                        try {
                            error = json.getString("data");
                        } catch (Exception e) {

                        }
                        return false;
                    }
                } else {
                    error = getResources().getString(R.string.unknown_response);
                }

            } catch (JSONException e) {
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {

            super.onPostExecute(result);
            if (dialog != null) {
                dialog.dismiss();
                dialog = null;
            }

            if (result) {
                ArrayList<String> mList = new ArrayList<String>();
                mList.add("Select Class");

                for (int i = 0; i < classes.size(); i++) {
                    mList.add(classes.get(i).class_name);
                }
                ArrayAdapter<String> adapterClass = new ArrayAdapter<String>(
                        AisRegistrationActivity.this,
                        R.layout.simple_spinner_dropdown_item_custom, mList);
                adapterClass
                        .setDropDownViewResource(R.layout.simple_spinner_dropdown_item_custom_new);
                spinnerClass.setAdapter(adapterClass);

                spinnerClass
                        .setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                            @Override
                            public void onItemSelected(AdapterView<?> arg0,
                                                       View arg1, int n, long arg3) {
                                ArrayList<String> mList = new ArrayList<String>();
                                mList.add("Select Section");

                                if (n > 0) {
                                    // TODO Auto-generated method stub
                                    DataStructureFramwork.ClassOfOrg classOfOrg = classes.get(n - 1);
                                    for (int i = 0; i < classOfOrg.section_List
                                            .size(); i++) {
                                        mList.add(classOfOrg.section_List
                                                .get(i).section_name);
                                    }

                                }
                                ArrayAdapter<String> adapterClass = new ArrayAdapter<String>(
                                        AisRegistrationActivity.this,
                                        R.layout.simple_spinner_dropdown_item_custom,
                                        mList);
                                adapterClass
                                        .setDropDownViewResource(R.layout.simple_spinner_dropdown_item_custom_new);
                                spinnerSection.setAdapter(adapterClass);
                                spinnerSection
                                        .setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                                            @Override
                                            public void onItemSelected(
                                                    AdapterView<?> arg0,
                                                    View arg1, int n, long arg3) {
                                                // TODO Auto-generated method
                                                // stub
                                                if (n > 0) {
                                                    selected_section_id = classes.get(spinnerClass
                                                            .getSelectedItemPosition() - 1).section_List
                                                            .get(n - 1).section_id;
                                                    selected_class_id = classes.get(spinnerClass
                                                            .getSelectedItemPosition() - 1).class_id;
                                                } else {
                                                    selected_section_id = "0";
                                                    selected_class_id = "0";
                                                }

                                            }

                                            @Override
                                            public void onNothingSelected(
                                                    AdapterView<?> arg0) {
                                                // TODO Auto-generated method
                                                // stub

                                            }
                                        });
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> arg0) {
                                // TODO Auto-generated method stub

                            }
                        });

            } else {

                if (error.length() > 0) {
                    SchoolAppUtils.showDialog(AisRegistrationActivity.this,
                            "Registration", error);
                } else {
                    SchoolAppUtils.showDialog(AisRegistrationActivity.this,
                            "Registration",
                            getResources().getString(R.string.error));
                }

            }

        }
    }

    private class
    RegisterUserAsync extends
            AsyncTask<List<NameValuePair>, Void, Boolean> {

        String error = "";
        String message = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(AisRegistrationActivity.this);
            //dialog.setTitle(getResources().getString(isSchool?R.string.fetch_classes: R.string.fetch_program));
            dialog.setMessage(getResources().getString(R.string.please_wait));
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected Boolean doInBackground(List<NameValuePair>... params) {

            List<NameValuePair> namevaluepair = params[0];
            JsonParser jParser = new JsonParser();
            String register_url;

            if (user_type_id.equals("5") || user_type_id.equals("4")) {

                register_url = SchoolAppUtils.GetSharedParameter(AisRegistrationActivity.this, Constants.COMMON_URL) + Urls.api_ais_student_parent_register;
            } else {
                register_url = SchoolAppUtils.GetSharedParameter(AisRegistrationActivity.this, Constants.COMMON_URL) + Urls.api_ais_teacher_register;
            }


            JSONObject json = jParser.getJSONFromUrlfrist(namevaluepair, register_url);
            try {
                if (json != null) {
                    if (json.getString("result").equalsIgnoreCase("1")) {
                        try {
                            message = json.getString("data");
                            Log.d("messg signup>>>>>>", message);
                        } catch (Exception e) {
                        }
                        return true;
                    } else {
                        try {
                            error = json.getString("data");
                        } catch (Exception e) {

                        }
                        return false;
                    }
                } else {
                    error = getResources().getString(R.string.unknown_response);
                }

            } catch (JSONException e) {
            }


            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {

            super.onPostExecute(result);

            if (result) {
                //SchoolAppUtils.showDialog(AisRegistrationActivity.this,
                //  "Registration", "Successfully registered.");
                //Intent regint = new Intent(AisRegistrationActivity.this,SigninActivity.class);
                //startActivity(regint);
                dialoggg = new AlertDialog.Builder(
                        AisRegistrationActivity.this);
                dialoggg.setTitle("Information");
                dialoggg.setMessage(message);
                dialoggg.setNeutralButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
                                        4);
                                nameValuePairs.add(new BasicNameValuePair("email", user_id_login));
                                nameValuePairs.add(new BasicNameValuePair("username", user_id_login));
                                nameValuePairs.add(new BasicNameValuePair("password", user_password));
                                nameValuePairs.add(new BasicNameValuePair("devicetype",
                                        "android"));
                                nameValuePairs.add(new BasicNameValuePair("organization_id",
                                        SchoolAppUtils.GetSharedParameter(AisRegistrationActivity.this,
                                                Constants.UORGANIZATIONID)));
                                nameValuePairs
                                        .add(new BasicNameValuePair("devicetoken", regid));
                                nameValuePairs.add(new BasicNameValuePair("version_number",
                                        curVersion));

                                new SignInAsyntask().execute(nameValuePairs);

                            }
                        });
                dialoggg.show();


            } else {
                if (dialog != null) {
                    dialog.dismiss();
                    dialog = null;
                }
                if (error.length() > 0) {
                    SchoolAppUtils.showDialog(AisRegistrationActivity.this,
                            "Registration", error);
                } else {
                    SchoolAppUtils.showDialog(AisRegistrationActivity.this,
                            "Registration",
                            getResources().getString(R.string.error));
                }

            }
        }


    }


    private class SignInAsyntask extends
            AsyncTask<List<NameValuePair>, Void, Boolean> {
        DataStructureFramwork.UserInfo userInfo;

        String error = "";
        DataStructureFramwork.Permission permission;
        DataStructureFramwork.PermissionAdd permissionadd;
        DataStructureFramwork.Request requests;
        CollegeDataStructureFramwork.CollegePermission per;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            /*dialog = new ProgressDialog(AisRegistrationActivity.this);
            dialog.setTitle(getResources().getString(R.string.signing_in));
            dialog.setMessage(getResources().getString(R.string.please_wait));
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);

            dialog.show();*/

        }

        @Override
        protected Boolean doInBackground(List<NameValuePair>... params) {


            List<NameValuePair> nameValuePairs = params[0];
            JsonParser jParser = new JsonParser(AisRegistrationActivity.this);
            JSONObject json = jParser.getJSONFromUrlnew(nameValuePairs,
                    Urls.api_login);

            // Log parameters:
            Log.d("url extension: ", Urls.api_login);
            String parameters = "";
            for (NameValuePair nvp : nameValuePairs) {
                parameters += nvp.getName() + "=" + nvp.getValue() + ",";
            }
            Log.d("Parameters: ", parameters);

            try {

                if (json != null) {
                    if (json.getString("result").equalsIgnoreCase("1")) {
                        JSONObject object = json.getJSONObject("data");
                        String menu_tag;
                        String menu_title;
                        if (SchoolAppUtils.GetSharedParameter(AisRegistrationActivity.this,
                                Constants.APP_TYPE).equals(
                                Constants.APP_TYPE_COMMON_STANDARD)) {
                            menu_tag = json.getString("menu_info");
                            menu_title = json.getString("menu_caption");
                        } else {
                            menu_tag = json.getString("menu_info") + "|webaccess";
                            menu_title = json.getString("menu_caption") + "|Web Access";
                        }

                        //notification counter
                        // String menu_Noticounter=json.getString("menu_counter")+"|web access";
                        JSONObject permission_config = json
                                .getJSONObject("activity_permission");
                        JSONObject request_config = json
                                .getJSONObject("requests");
                        JSONObject permission_add = json
                                .getJSONObject("permission");


                        JSONObject orgConfigJSONObj = json.getJSONObject("org_config");
                        //if(orgConfigJSONObj != null){
                        if (orgConfigJSONObj != null) {
                            //--------------------------------INSURANCE and premium--------------//
                            try {
                                String isInsuranceAvailable = orgConfigJSONObj.getString("is_insurance").trim();
                                String isPremiumServiceAvailable = orgConfigJSONObj.getString("is_premium_service").trim();
                                String transportType = orgConfigJSONObj.getString("transport_mobile_based").trim();
                                SchoolAppUtils.SetSharedParameter(
                                        AisRegistrationActivity.this,
                                        Constants.INSURANCE_AVIALABLE,
                                        isInsuranceAvailable);

                                SchoolAppUtils.SetSharedParameter(
                                        AisRegistrationActivity.this,
                                        Constants.PREMIUM_SERVICE_AVAILABLE,
                                        isPremiumServiceAvailable);

                                SchoolAppUtils.SetSharedParameter(
                                        AisRegistrationActivity.this,
                                        Constants.NEWTRANSPORT_TYPE,
                                        transportType);


                            } catch (Exception e1) {
                                e1.printStackTrace();
                            }
                        }


                        if (object != null) {
                            // user details
                            userInfo = new DataStructureFramwork.UserInfo(object);
                            SchoolAppUtils.SetSharedParameter(
                                    AisRegistrationActivity.this,
                                    Constants.USERTYPEID,
                                    userInfo.usertypeid);
                            SchoolAppUtils.SetSharedParameter(
                                    AisRegistrationActivity.this, Constants.USERID,
                                    userInfo.userid);
                            SchoolAppUtils
                                    .SetSharedParameter(
                                            AisRegistrationActivity.this,
                                            Constants.UALTEMAIL,
                                            userInfo.alt_email);

                            SchoolAppUtils
                                    .SetSharedParameter(AisRegistrationActivity.this,
                                            Constants.ORGEMAIL,
                                            userInfo.org_email);

                            SchoolAppUtils
                                    .SetSharedParameter(
                                            AisRegistrationActivity.this,
                                            Constants.UMOBILENO,
                                            userInfo.mobile_no);
                            SchoolAppUtils.SetSharedParameter(AisRegistrationActivity.this, Constants.PASSWORD, user_password);

                            SchoolAppUtils
                                    .SetSharedParameter(
                                            AisRegistrationActivity.this,
                                            Constants.DEVICE_TOKEN,
                                            regid);


                        }
                        // Menu tag
                        if (menu_tag != null) {
                            Log.d("menu", menu_tag.toString());
                            SchoolAppUtils.SetSharedParameter(
                                    AisRegistrationActivity.this,
                                    Constants.MENU_TAGS,
                                    menu_tag.toString());
                            SchoolAppUtils.SetSharedParameter(
                                    AisRegistrationActivity.this,
                                    Constants.MENU_TITLES,
                                    menu_title.toString());
                        }

                        // Permissions
                        if (permission_config != null) {
                            permission = new DataStructureFramwork.Permission(permission_config);
                            SchoolAppUtils.SetSharedParameter(
                                    AisRegistrationActivity.this,
                                    Constants.ASSIGNMENT_MARK_PERMISSION,
                                    permission.assignment_permission
                                            .toString());
                            SchoolAppUtils.SetSharedParameter(
                                    AisRegistrationActivity.this,
                                    Constants.TEST_MARK_PERMISSION,
                                    permission.test_permission.toString());
                            SchoolAppUtils.SetSharedParameter(
                                    AisRegistrationActivity.this,
                                    Constants.ACTIVITY_MARK_PERMISSION,
                                    permission.activity_permission
                                            .toString());
                        }

                        // Parent Request
                        if (request_config != null) {
                            requests = new DataStructureFramwork.Request(request_config);
                            SchoolAppUtils.SetSharedParameter(
                                    AisRegistrationActivity.this,
                                    Constants.LEAVE_REQUEST,
                                    requests.leave_request.toString());
                            SchoolAppUtils.SetSharedParameter(
                                    AisRegistrationActivity.this,
                                    Constants.REQUEST_FOR_BOOK,
                                    requests.request_for_book.toString());
                            SchoolAppUtils.SetSharedParameter(
                                    AisRegistrationActivity.this,
                                    Constants.REQUEST_FOR_ID_CARD,
                                    requests.request_for_id.toString());
                            SchoolAppUtils
                                    .SetSharedParameter(
                                            AisRegistrationActivity.this,
                                            Constants.REQUEST_FOR_UNIFORM,
                                            requests.request_for_uniform
                                                    .toString());
                            SchoolAppUtils.SetSharedParameter(
                                    AisRegistrationActivity.this,
                                    Constants.SPECIAL_REQUEST,
                                    requests.special_request.toString());
                        }

                        // Permission Add
                        if (permission_add != null) {
                            permissionadd = new DataStructureFramwork.PermissionAdd(
                                    permission_add);
                            SchoolAppUtils
                                    .SetSharedParameter(
                                            AisRegistrationActivity.this,
                                            Constants.ASSIGNMENT_ADD_PERMISSION,
                                            permissionadd.assignment_add
                                                    .toString());
                            SchoolAppUtils.SetSharedParameter(
                                    AisRegistrationActivity.this,
                                    Constants.TEST_ADD_PERMISSION,
                                    permissionadd.test_add.toString());
                            SchoolAppUtils.SetSharedParameter(
                                    AisRegistrationActivity.this,
                                    Constants.ACTIVITY_ADD_PERMISSION,
                                    permissionadd.activity_add.toString());
                            SchoolAppUtils.SetSharedParameter(
                                    AisRegistrationActivity.this,
                                    Constants.ANNOUNCEMENT_ADD_PERMISSION,
                                    permissionadd.announcement_add
                                            .toString());
                            SchoolAppUtils.SetSharedParameter(
                                    AisRegistrationActivity.this,
                                    Constants.COURSEWORK_ADD_PERMISSION,
                                    permissionadd.classwork_add.toString());
                            SchoolAppUtils.SetSharedParameter(
                                    AisRegistrationActivity.this,
                                    Constants.LESSONS_ADD_PERMISSION,
                                    permissionadd.lessons_add.toString());
                            SchoolAppUtils.SetSharedParameter(
                                    AisRegistrationActivity.this,
                                    Constants.ATTENDANCE_TYPE_PERMISSION,
                                    permissionadd.attendance_type
                                            .toString());
                            Log.d("assignment",
                                    permissionadd.attendance_type
                                            .toString());
                        }

                        if (userInfo.usertypeid
                                .equalsIgnoreCase(Constants.USERTYPE_PARENT)) {
                            JSONArray child_info_arry = json
                                    .getJSONArray("child_info");
                            JSONObject object_chield_info = child_info_arry
                                    .getJSONObject(0);
                            DataStructureFramwork.StudentProfileInfo selectedStudent = new DataStructureFramwork.StudentProfileInfo(
                                    object_chield_info);
                            SchoolAppUtils.SetSharedParameter(
                                    AisRegistrationActivity.this,
                                    Constants.CHILD_ID,
                                    selectedStudent.user_id);
                            SchoolAppUtils.SetSharedParameter(
                                    AisRegistrationActivity.this,
                                    Constants.CHILD_NAME,
                                    selectedStudent.fullname);
                            SchoolAppUtils.SetSharedParameter(
                                    AisRegistrationActivity.this,
                                    Constants.STUDENT_CLASS_SECTION,
                                    selectedStudent.class_section);
                            SchoolAppUtils.SetSharedParameter(
                                    AisRegistrationActivity.this,
                                    Constants.SELECTED_CHILD_OBJECT,
                                    object_chield_info.toString());
                            SchoolAppUtils.SetSharedParameter(
                                    AisRegistrationActivity.this,
                                    Constants.SECTION_ID,
                                    selectedStudent.section_id);

                        } else if (userInfo.usertypeid
                                .equalsIgnoreCase(Constants.USERTYPE_STUDENT)) {
                            SchoolAppUtils.SetSharedParameter(
                                    AisRegistrationActivity.this,
                                    Constants.CHILD_ID, userInfo.userid);

                        }
                        return true;
                    } else {
                        try {
                            error = json.getString("data");
                        } catch (Exception e) {
                        }
                        return false;
                    }
                } else {
                    error = getResources().getString(
                            R.string.unknown_response);
                }

            } catch (JSONException e) {

            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {

            super.onPostExecute(result);
            /*if (dialog != null) {
				dialog.dismiss();
				dialog = null;
			}*/

            if (result) {
                if (!SchoolAppUtils.GetSharedParameter(AisRegistrationActivity.this,
                        Constants.USERTYPEID).equalsIgnoreCase(
                        Constants.USERTYPE_PARENT)) {

                    SchoolAppUtils.SetSharedParameter(AisRegistrationActivity.this,
                            Constants.USERINFO, userInfo.toString());

                    if (SchoolAppUtils.GetSharedParameter(
                            AisRegistrationActivity.this, Constants.USERTYPEID)
                            .equalsIgnoreCase(Constants.USERTYPE_STUDENT)) {
                        SchoolAppUtils.SetSharedBoolParameter(
                                AisRegistrationActivity.this, Constants.UISSIGNIN,
                                true);
                        Intent ints = new Intent(AisRegistrationActivity.this,
                                MainActivity.class);
                        ints.putExtra("fromlogin", "1");
                        ints.putExtra("menu_val", menu_val);
                        startActivity(ints);
                        if (dialog != null) {
                            dialog.dismiss();
                            dialog = null;
                        }
                        finish();
                    } else if (SchoolAppUtils.GetSharedParameter(
                            AisRegistrationActivity.this, Constants.USERTYPEID)
                            .equalsIgnoreCase(Constants.USERTYPE_TEACHER)) {
                        SchoolAppUtils.SetSharedBoolParameter(
                                AisRegistrationActivity.this, Constants.ISSIGNIN,
                                true);

                        // Download data for offline access
                        new GetOfflineDataAsyntask().execute();
                    }
                } else {
                    SchoolAppUtils.SetSharedBoolParameter(
                            AisRegistrationActivity.this, Constants.ISSIGNIN, true);
                    SchoolAppUtils.SetSharedBoolParameter(
                            AisRegistrationActivity.this, Constants.ISPARENTSIGNIN,
                            true);

                    Intent intent = new Intent(AisRegistrationActivity.this,
                            ParentMainActivity.class);
                    intent.putExtra("menu_val", menu_val);
                    intent.putExtra("prompt", "1");
                    startActivity(intent);

                    if (dialog != null) {
                        dialog.dismiss();
                        dialog = null;
                    }
                    finish();
                }

            }

        }

    }

    private class GetOfflineDataAsyntask extends AsyncTask<Void, Void, Boolean> {
        String error;
        private ArrayList<DataStructureFramwork.Subject> subjects;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            subjects = new ArrayList<DataStructureFramwork.Subject>();
            subjects.clear();
			/*dialog = new ProgressDialog(SigninActivity.this);
			dialog.setTitle(getResources().getString(R.string.fetching_offline));
			dialog.setMessage(getResources().getString(R.string.please_wait));
			dialog.setCanceledOnTouchOutside(false);
			dialog.setCancelable(false);
			dialog.show();*/
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
            nameValuePairs.add(new BasicNameValuePair("user_type_id",
                    SchoolAppUtils.GetSharedParameter(AisRegistrationActivity.this,
                            Constants.USERTYPEID)));
            nameValuePairs.add(new BasicNameValuePair("organization_id",
                    SchoolAppUtils.GetSharedParameter(AisRegistrationActivity.this,
                            Constants.UORGANIZATIONID)));
            nameValuePairs
                    .add(new BasicNameValuePair("user_id", SchoolAppUtils
                            .GetSharedParameter(AisRegistrationActivity.this,
                                    Constants.USERID)));
            // Log parameters:
            Log.d("url extension: ", Urls.api_activity_get_subjects);
            String parameters = "";
            for (NameValuePair nvp : nameValuePairs) {
                parameters += nvp.getName() + "=" + nvp.getValue() + ",";
            }
            Log.d("Parameters: ", parameters);

            JsonParser jParser = new JsonParser();
            JSONObject json = jParser.getJSONFromUrlnew(nameValuePairs,
                    Urls.api_activity_get_subjects);
            try {

                if (json != null) {
                    if (json.getString("result").equalsIgnoreCase("1")) {

                        JSONArray array;
                        try {
                            array = json.getJSONArray("data");
                        } catch (Exception e) {
                            return true;
                        }
                        for (int i = 0; i < array.length(); i++) {
                            DataStructureFramwork.Subject subject = new DataStructureFramwork.Subject(
                                    array.getJSONObject(i));

                            subjects.add(subject);
                        }

                        return true;
                    } else {
                        try {
                            error = json.getString("data");
                        } catch (Exception e) {
                        }
                        return false;
                    }
                }

            } catch (JSONException e) {

            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {

            super.onPostExecute(result);
			/*
			 * if (dialog != null) { // dialog.dismiss(); dialog = null; }
			 */

            if (subjects != null) {

                mDatabase.open();
                // Delete all data in database
                mDatabase.deleteAllSubjects();
                for (int i = 0; i < subjects.size(); i++) {
                    Log.d("Insert: ", "Inserting .." + i);
                    mDatabase.addSubjects(subjects.get(i));
                }
                mDatabase.close();

            }

            // Go to Teacher Main Page
			/*
			 * startActivity(new Intent(SigninActivity.this,
			 * TeacherMainActivity.class)); finish();
			 */
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
            nameValuePairs.add(new BasicNameValuePair("user_type_id",
                    SchoolAppUtils.GetSharedParameter(AisRegistrationActivity.this,
                            Constants.USERTYPEID)));
            nameValuePairs.add(new BasicNameValuePair("organization_id",
                    SchoolAppUtils.GetSharedParameter(AisRegistrationActivity.this,
                            Constants.UORGANIZATIONID)));
            nameValuePairs
                    .add(new BasicNameValuePair("user_id", SchoolAppUtils
                            .GetSharedParameter(AisRegistrationActivity.this,
                                    Constants.USERID)));
            new GetListAsyntask().execute(nameValuePairs);
            // Go to Teacher Main Page

        }

    }

    private class GetListAsyntask extends
            AsyncTask<List<NameValuePair>, Void, Boolean> {
        String error;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
			/*
			 * dialog = new ProgressDialog(getActivity());
			 * dialog.setTitle(getResources().getString(R.string.time_table));
			 * dialog
			 * .setMessage(getResources().getString(R.string.please_wait));
			 * dialog.setCanceledOnTouchOutside(false);
			 * dialog.setCancelable(false); dialog.show();
			 */
        }

        @Override
        protected Boolean doInBackground(List<NameValuePair>... params) {
            timeTable = new ArrayList<DataStructureFramwork.TimeTable>();
            List<NameValuePair> nvp = params[0];
            JsonParser jParser = new JsonParser();
            JSONObject json = jParser.getJSONFromUrlnew(nvp,
                    Urls.api_timetable_user);
            try {

                if (json != null) {
                    if (json.getString("result").equalsIgnoreCase("1")) {
                        JSONArray array;
                        try {
                            array = json.getJSONArray("data");
                        } catch (Exception e) {
                            return true;
                        }
                        timeTable = new ArrayList<DataStructureFramwork.TimeTable>();
                        for (int i = 0; i < array.length(); i++) {
                            DataStructureFramwork.TimeTable attendance = new DataStructureFramwork.TimeTable(
                                    array.getJSONObject(i));
                            timeTable.add(attendance);
                        }

                        return true;
                    } else {
                        try {
                            error = json.getString("data");
                        } catch (Exception e) {
                        }
                        return false;
                    }
                }

            } catch (JSONException e) {

            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {

            super.onPostExecute(result);
			/*
			 * if (dialog != null) { dialog.dismiss(); dialog = null; }
			 */
            if (timeTable != null) {
                if (timeTable.size() > 0) {
                    mDatabase.open();
                    // Delete all data in database
                    mDatabase.deleteAllTimetable();
                    for (int i = 0; i < timeTable.size(); i++) {
                        Log.d("Insert: ", "Inserting ..");
                        mDatabase.addTimetable(timeTable.get(i));
                    }
                    mDatabase.close();

                }
            }

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
            nameValuePairs.add(new BasicNameValuePair("user_type_id",
                    SchoolAppUtils.GetSharedParameter(AisRegistrationActivity.this,
                            Constants.USERTYPEID)));
            nameValuePairs.add(new BasicNameValuePair("organization_id",
                    SchoolAppUtils.GetSharedParameter(AisRegistrationActivity.this,
                            Constants.UORGANIZATIONID)));
            nameValuePairs
                    .add(new BasicNameValuePair("user_id", SchoolAppUtils
                            .GetSharedParameter(AisRegistrationActivity.this,
                                    Constants.USERID)));
            new GetListAttendanceAsyntask().execute(nameValuePairs);

        }
    }

    private class GetListAttendanceAsyntask extends
            AsyncTask<List<NameValuePair>, Void, Boolean> {
        String error;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
			/*
			 * dialog = new ProgressDialog(getActivity());
			 * dialog.setTitle(getResources().getString(R.string.time_table));
			 * dialog
			 * .setMessage(getResources().getString(R.string.please_wait));
			 * dialog.setCanceledOnTouchOutside(false);
			 * dialog.setCancelable(false); dialog.show();
			 */
        }

        @Override
        protected Boolean doInBackground(List<NameValuePair>... params) {
            timeTable = new ArrayList<DataStructureFramwork.TimeTable>();
            List<NameValuePair> nvp = params[0];
            JsonParser jParser = new JsonParser();
            JSONObject json = jParser.getJSONFromUrlnew(nvp,
                    Urls.api_teacher_offline_attendance_user);
            try {

                if (json != null) {
                    if (json.getString("result").equalsIgnoreCase("1")) {
                        JSONArray array;
                        try {
                            array = json.getJSONArray("data");
                        } catch (Exception e) {
                            return true;
                        }
                        timeTable = new ArrayList<DataStructureFramwork.TimeTable>();
                        for (int i = 0; i < array.length(); i++) {
                            DataStructureFramwork.TimeTable attendance = new DataStructureFramwork.TimeTable(
                                    array.getJSONObject(i));
                            timeTable.add(attendance);
                        }

                        return true;
                    } else {
                        try {
                            error = json.getString("data");
                        } catch (Exception e) {
                        }
                        return false;
                    }
                }

            } catch (JSONException e) {

            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {

            super.onPostExecute(result);
			/*
			 * if (dialog != null) { dialog.dismiss(); dialog = null; }
			 */
            if (timeTable != null) {
                if (timeTable.size() > 0) {
                    mDatabase.open();
                    // Delete all data in database
                    mDatabase.deleteAllofflineattendance();
                    for (int i = 0; i < timeTable.size(); i++) {
                        Log.d("Insert: ", "Inserting ..");
                        mDatabase.addOfflineteacherattendance(timeTable.get(i));
                    }
                    mDatabase.close();

                }
            }

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
            nameValuePairs.add(new BasicNameValuePair("user_type_id",
                    SchoolAppUtils.GetSharedParameter(AisRegistrationActivity.this,
                            Constants.USERTYPEID)));
            nameValuePairs.add(new BasicNameValuePair("organization_id",
                    SchoolAppUtils.GetSharedParameter(AisRegistrationActivity.this,
                            Constants.UORGANIZATIONID)));
            nameValuePairs
                    .add(new BasicNameValuePair("user_id", SchoolAppUtils
                            .GetSharedParameter(AisRegistrationActivity.this,
                                    Constants.USERID)));
            new GetOfflineDataStudentAsyntask().execute(nameValuePairs);

        }
    }

    // OFFLINE DATABASE INITIALIZATION
    private class GetOfflineDataStudentAsyntask extends
            AsyncTask<List<NameValuePair>, Void, Boolean> {
        String error;
        private ArrayList<DataStructureFramwork.Attendance> students;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Boolean doInBackground(List<NameValuePair>... params) {

            // Log parameters:
            students = new ArrayList<DataStructureFramwork.Attendance>();
            List<NameValuePair> nameValuePairs = params[0];
            Log.d("url extension: ", Urls.api_students_info);
            String parameters = "";
            for (NameValuePair nvp : nameValuePairs) {
                parameters += nvp.getName() + "=" + nvp.getValue() + ",";
            }
            Log.d("Parameters: ", parameters);

            JsonParser jParser = new JsonParser();
            JSONObject json = jParser.getJSONFromUrlnew(nameValuePairs,
                    Urls.api_students_info);
            try {

                if (json != null) {
                    if (json.getString("result").equalsIgnoreCase("1")) {

                        JSONArray array;
                        try {
                            array = json.getJSONArray("data");
                        } catch (Exception e) {
                            return true;
                        }
                        for (int i = 0; i < array.length(); i++) {
                            DataStructureFramwork.Attendance subject = new DataStructureFramwork.Attendance(
                                    array.getJSONObject(i));

                            students.add(subject);
                        }

                        return true;
                    } else {
                        try {
                            error = json.getString("data");
                        } catch (Exception e) {
                        }
                        return false;
                    }
                }

            } catch (JSONException e) {

            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {

            super.onPostExecute(result);


            if (students != null) {

                mDatabase.open();
                // Delete all data in database
                mDatabase.deleteAllStudents();
                for (int i = 0; i < students.size(); i++) {
                    Log.d("Insert: ", "Inserting .." + i);
                    mDatabase.addStudents(students.get(i));
                }
                mDatabase.close();
                Intent intent = new Intent(AisRegistrationActivity.this,
                        TeacherMainActivity.class);
                intent.putExtra("menu_val", menu_val);
                intent.putExtra("prompt", "1");

                startActivity(intent);

                if (dialog != null) {
                    dialog.dismiss();
                    dialog = null;
                }
                finish();

            }

        }

    }

}
