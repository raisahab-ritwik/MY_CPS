package com.knwedu.ourschool;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.net.http.HttpResponseCache;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.knwedu.calcuttapublicschool.R;
import com.knwedu.ourschool.db.DatabaseAdapter;
import com.knwedu.ourschool.db.SchoolApplication;
import com.knwedu.ourschool.utils.Constants;
import com.knwedu.ourschool.utils.DataStructureFramwork.Announcement;
import com.knwedu.ourschool.utils.DataStructureFramwork.Assignment;
import com.knwedu.ourschool.utils.DataStructureFramwork.Subject;
import com.knwedu.ourschool.utils.FileUtils;
import com.knwedu.ourschool.utils.JsonParser;
import com.knwedu.ourschool.utils.SchoolAppUtils;
import com.knwedu.ourschool.utils.Urls;

public class TeacherClassWorkActivity extends AppCompatActivity {
    private TextView ttitle, title, header;
    private Button date, submite, select_file;
    private Subject subject;
    private EditText discription, titleEdt, marksEdt;/* codeEdt */
    private ProgressDialog dialog;
    AlertDialog.Builder dialoggg;
    private static final int FILE_SELECT_CODE = 0;
    private String encrypted_file_name = null;
    int serverResponseCode = 0;
    public DatabaseAdapter mDatabase;
    private boolean internetAvailable = false;
    String path = "null";
    private String page_title = "";
    TextView dateText;
    private CheckBox chk_marks;

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_assignment);
        page_title = getIntent().getStringExtra(Constants.PAGE_TITLE);
        SchoolAppUtils.loadAppLogo(TeacherClassWorkActivity.this, (ImageButton) findViewById(R.id.app_logo));

        mDatabase = ((SchoolApplication) getApplication()).getDatabase();
        internetAvailable = getIntent().getBooleanExtra(Constants.IS_ONLINE, false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setTitle("Publish");
        getSupportActionBar().setTitle("" + page_title);
        dateText = (TextView) findViewById(R.id.date_text);
        dateText.setText("Date");
        ttitle = (TextView) findViewById(R.id.ttitle_txt);
        title = (TextView) findViewById(R.id.title_txt);
        date = (Button) findViewById(R.id.date_btns);
        select_file = (Button) findViewById(R.id.select_file_for_upload);
        submite = (Button) findViewById(R.id.submit_btn);
        submite.setText("Create " + page_title);
        discription = (EditText) findViewById(R.id.description_edt);
        titleEdt = (EditText) findViewById(R.id.title_edt);
        marksEdt = (EditText) findViewById(R.id.marks_edt);
        marksEdt.setVisibility(View.GONE);
        chk_marks = (CheckBox) findViewById(R.id.chk_marks);
        chk_marks.setVisibility(View.GONE);
        date.setText(SchoolAppUtils.getDayDifferentDif(SchoolAppUtils.getCurrentDate()));
        findViewById(R.id.layout_spinner).setVisibility(View.GONE);

        header = (TextView) findViewById(R.id.header_text);
        header.setVisibility(View.GONE);
        initialize();
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("Google Analytics", "Tracking Start");
        EasyTracker.getInstance(this).activityStart(this);

    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("Google Analytics", "Tracking Stop");
        EasyTracker.getInstance(this).activityStop(this);
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
        HttpResponseCache cache = HttpResponseCache.getInstalled();
        if (cache != null) {
            cache.flush();
        }
    }

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a File to Upload"),
                    FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(this, "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    Uri selectedUri = data.getData();
                    if (selectedUri == null) {
                        return;
                    }

                    try {
                        path = FileUtils.getPath(this, selectedUri);
                    } catch (URISyntaxException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    if (TextUtils.isEmpty(path)) {
                        try {

                            path = FileUtils.getDriveFileAbsolutePath(TeacherClassWorkActivity.this, selectedUri);
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }


                    Log.d("TAG", "File Path: " + path);
                    select_file.setText(path);

                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initialize() {


        if (getIntent().getExtras() != null) {
            if (internetAvailable) {
                String temp = getIntent().getExtras().getString(
                        Constants.TSUBJECT);
                if (temp != null) {
                    JSONObject object = null;
                    try {
                        object = new JSONObject(temp);
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    if (object != null) {
                        subject = new Subject(object);
                    }
                }
            } else {

                findViewById(R.id.txt_offline).setVisibility(View.VISIBLE);
                String subject_id = getIntent().getExtras().getString(
                        Constants.OFFLINE_SUBJECT_ID);
                mDatabase.open();
                subject = mDatabase.getSubject(subject_id);
                mDatabase.close();
            }

        }
        if (subject != null) {
            ttitle.setText("Class: " + subject.classs + " " + subject.section_name);
            title.setText("" + subject.sub_name);
        } else {
            ttitle.setText("");
            title.setText("");
        }

        date.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                /*
                 * DialogFragment newFragment = new DatePickerFragment();
				 * newFragment.show(getSupportFragmentManager(), "datePicker");
				 */
                return;
            }
        });

        date.setBackgroundResource(0);

        select_file.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                showFileChooser();
            }
        });
        submite.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (titleEdt.getText().toString().length() <= 0) {
                    Toast.makeText(TeacherClassWorkActivity.this,
                            "Enter Title", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (discription.getText().toString().length() <= 0) {
                    Toast.makeText(TeacherClassWorkActivity.this,
                            "Enter Description", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!internetAvailable) {
                    new OfflineAnnouncementAddAsync().execute();
                } else {

                    if (select_file.getText().toString()
                            .equalsIgnoreCase(getString(R.string.attachment))) {

                        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
                                15);
                        nameValuePairs.add(new BasicNameValuePair(
                                "user_type_id", SchoolAppUtils
                                .GetSharedParameter(
                                        getApplicationContext(),
                                        Constants.USERTYPEID)));
                        nameValuePairs.add(new BasicNameValuePair(
                                "organization_id", SchoolAppUtils
                                .GetSharedParameter(
                                        getApplicationContext(),
                                        Constants.UORGANIZATIONID)));
                        nameValuePairs.add(new BasicNameValuePair("user_id",
                                SchoolAppUtils.GetSharedParameter(
                                        getApplicationContext(),
                                        Constants.USERID)));
                        nameValuePairs.add(new BasicNameValuePair("subject_id",
                                subject.id));
                        nameValuePairs.add(new BasicNameValuePair("section_id",
                                subject.section_id));
                        nameValuePairs.add(new BasicNameValuePair("cl_title",
                                titleEdt.getText().toString()));
                        nameValuePairs
                                .add(new BasicNameValuePair("description",
                                        discription.getText().toString()));
                        nameValuePairs
                                .add(new BasicNameValuePair("a_type", "2"));
                        nameValuePairs.add(new BasicNameValuePair("type", "1"));
                        nameValuePairs.add(new BasicNameValuePair("file_name",
                                ""));
                        nameValuePairs.add(new BasicNameValuePair("orig_name",
                                ""));
                        new UploadAsyntask().execute(nameValuePairs);

                    } else {
                        dialog = ProgressDialog.show(
                                TeacherClassWorkActivity.this, "",
                                "Uploading file...", true);

                        new Thread(new Runnable() {
                            @Override
                            public void run() {

                                uploadFile(select_file.getText().toString());

                            }
                        }).start();
                    }
                }

            }
        });
    }

    @SuppressLint("ValidFragment")
    private class DatePickerFragment extends DialogFragment implements
            DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        @Override
        public void onDateSet(DatePicker view, int year, int month, int day) {

            Calendar newDate = Calendar.getInstance();
            newDate.set(year, month, day);
            Calendar minDate = Calendar.getInstance();
            if (minDate != null && minDate.after(newDate)) {
                Toast.makeText(
                        TeacherClassWorkActivity.this,
                        "Please set todays or future dates," + " not past date",
                        Toast.LENGTH_LONG).show();
                onCreateDialog(getArguments());
                return;
            }

            String mon, da;
            if (month < 10) {
                mon = "0" + month;
            } else {
                mon = "" + month;
            }
            if (day < 10) {
                da = "0" + day;
            } else {
                da = "" + day;
            }
            date.setText(SchoolAppUtils.getDayDifferent(year + "/" + mon + "/"
                    + da));
            month = month + 1;
            if (month < 10) {
                mon = "0" + month;
            } else {
                mon = "" + month;
            }
        }
    }

    public int uploadFile(String sourceFileUri) {

        String fileName = sourceFileUri;

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(sourceFileUri);

        if (!sourceFile.isFile()) {

            dialog.dismiss();

            Intent intent = new Intent(TeacherClassWorkActivity.this,
                    TeacherClassWorkListActivity.class);
            intent.putExtra(Constants.TANNOUNCEMENT, subject.object.toString());
            startActivity(intent);
            Log.e("uploadFile", "Source File not exist :"
                    + select_file.getText().toString());

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    select_file.setText("Source File not exist :"
                            + select_file.getText().toString());
                }
            });

            return 0;

        } else {
            try {

                // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream(
                        sourceFile);
                URL url = new URL(Urls.upload_announcement_classwork_upload);

                // Open a HTTP connection to the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type",
                        "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_file", fileName);

                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                        + fileName + "\"" + lineEnd);

                dos.writeBytes(lineEnd);

                // create a buffer of maximum size
                bytesAvailable = fileInputStream.available();

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {

                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                }

                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();
                InputStream stream = conn.getInputStream();
                InputStreamReader isReader = new InputStreamReader(stream);

                // put output stream into a string
                BufferedReader br = new BufferedReader(isReader);

                String result = null;
                String line;
                while ((line = br.readLine()) != null) {
                    System.out.println(line);
                    encrypted_file_name = line;
                }

                System.out.println(encrypted_file_name);
                br.close();
                Log.i("uploadFile", "HTTP Response is : " + result + ": "
                        + serverResponseCode);

                if (serverResponseCode == 200) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
							/*
							 * Toast.makeText(TeacherAssignmentAddActivity.this,
							 * "File Upload Complete.", Toast.LENGTH_SHORT)
							 * .show();
							 */
                            String title = null, descp = null, code = null;
                            title = titleEdt.getText().toString();
                            descp = discription.getText().toString();
                            // code = codeEdt.getText().toString();
                            String uuploadfile_name = null;
                            String uuuploadfile_name;

                            final String[] tokens = select_file.getText()
                                    .toString().split("/");
                            for (String s : tokens) {
                                System.out.println(s);
                                uuploadfile_name = s;
                            }
                            uuuploadfile_name = uuploadfile_name;
                            if (uuuploadfile_name
                                    .equalsIgnoreCase(getString(R.string.attachment))) {
                                uuuploadfile_name = "";
                            }

                            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
                                    15);
                            nameValuePairs.add(new BasicNameValuePair(
                                    "user_type_id", SchoolAppUtils
                                    .GetSharedParameter(
                                            getApplicationContext(),
                                            Constants.USERTYPEID)));
                            nameValuePairs
                                    .add(new BasicNameValuePair(
                                            "organization_id",
                                            SchoolAppUtils.GetSharedParameter(
                                                    getApplicationContext(),
                                                    Constants.UORGANIZATIONID)));
                            nameValuePairs.add(new BasicNameValuePair(
                                    "user_id", SchoolAppUtils
                                    .GetSharedParameter(
                                            getApplicationContext(),
                                            Constants.USERID)));
                            nameValuePairs.add(new BasicNameValuePair(
                                    "subject_id", subject.id));
                            nameValuePairs.add(new BasicNameValuePair(
                                    "section_id", subject.section_id));
                            nameValuePairs.add(new BasicNameValuePair(
                                    "cl_title", titleEdt.getText().toString()));
                            nameValuePairs.add(new BasicNameValuePair(
                                    "description", discription.getText()
                                    .toString()));
                            nameValuePairs.add(new BasicNameValuePair("a_type",
                                    "2"));
                            nameValuePairs.add(new BasicNameValuePair("type",
                                    "1"));
                            nameValuePairs.add(new BasicNameValuePair(
                                    "file_name", encrypted_file_name));
                            nameValuePairs.add(new BasicNameValuePair(
                                    "orig_name", uuploadfile_name));
                            new UploadAsyntask().execute(nameValuePairs);

                        }
                    });
                }

                // close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();

            } catch (MalformedURLException ex) {

                dialog.dismiss();

                ex.printStackTrace();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        select_file
                                .setText("MalformedURLException Exception : check script url.");
                        Toast.makeText(TeacherClassWorkActivity.this,
                                "MalformedURLException", Toast.LENGTH_SHORT)
                                .show();
                    }
                });

                Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
            } catch (Exception e) {

                dialog.dismiss();

                e.printStackTrace();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        select_file.setText("Got Exception : see logcat ");
                        Toast.makeText(TeacherClassWorkActivity.this,
                                "Got Exception : see logcat ",
                                Toast.LENGTH_SHORT).show();
                    }
                });
                Log.e("Upload file to server Exception",
                        "Exception : " + e.getMessage(), e);
            }
            dialog.dismiss();

            return serverResponseCode;

        } // End else block
    }

    private class UploadAsyntask extends
            AsyncTask<List<NameValuePair>, Void, Boolean> {
        String error = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(TeacherClassWorkActivity.this);
            dialog.setTitle("Creating " + page_title);
            dialog.setMessage(getResources().getString(R.string.please_wait));
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected Boolean doInBackground(List<NameValuePair>... params) {
            List<NameValuePair> namevaluepair = params[0];
            JsonParser jParser = new JsonParser();
            JSONObject json = jParser.getJSONFromUrlnew(namevaluepair,
                    Urls.api_announcement_create);
            try {

                if (json != null) {
                    if (json.getString("result").equalsIgnoreCase("1")) {
                        error = page_title + " created successfully";

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
                dialoggg = new AlertDialog.Builder(
                        TeacherClassWorkActivity.this);
                dialoggg.setTitle(page_title);
                dialoggg.setMessage(error);
                dialoggg.setNeutralButton(R.string.ok,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                SchoolAppUtils.SetSharedBoolParameter(
                                        TeacherClassWorkActivity.this,
                                        "update", true);
                                TeacherClassWorkActivity.this.finish();
                            }
                        });
                dialoggg.show();
            } else {
                if (error.length() > 0) {
                    SchoolAppUtils.showDialog(TeacherClassWorkActivity.this, page_title, error);
                } else {
                    SchoolAppUtils.showDialog(TeacherClassWorkActivity.this, page_title,
                            getResources().getString(R.string.error));
                }

            }
        }

    }

    class OfflineAnnouncementAddAsync extends
            AsyncTask<String, Assignment, Boolean> {

        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected Boolean doInBackground(String... params) {
            long row_id = 0;
            mDatabase.open();
            Announcement announcement = new Announcement();
            announcement.date = SchoolAppUtils.getCurrentDate();
            announcement.description = discription.getText().toString().trim();
            announcement.title = titleEdt.getText().toString().trim();
            announcement.file_name = path;
            announcement.attachment = "null";
            announcement.subject_id = subject.id;
            announcement.section_id = subject.section_id;

            row_id = mDatabase.addAnnouncement(announcement, 1);
            mDatabase.close();

            return (row_id > 0);
        }

        @Override
        protected void onProgressUpdate(Assignment... values) {
            super.onProgressUpdate(values);
        }

        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            final AlertDialog.Builder dialog = new AlertDialog.Builder(
                    TeacherClassWorkActivity.this);
            dialog.setTitle("New " + page_title);
            // mDatabase.close();
            if (result) {
                dialog.setMessage(page_title + " created in Offline Mode");
            } else {
                dialog.setMessage("Error in inserting Data");
            }

            dialog.setNegativeButton("OK",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
            dialog.show();
        }

    }
}
