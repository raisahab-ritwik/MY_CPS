package com.knwedu.ourschool;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.net.http.HttpResponseCache;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.knwedu.calcuttapublicschool.R;
import com.knwedu.ourschool.utils.Constants;
import com.knwedu.ourschool.utils.FileUtils;
import com.knwedu.ourschool.utils.JsonParser;
import com.knwedu.ourschool.utils.SchoolAppUtils;
import com.knwedu.ourschool.utils.Urls;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import java.util.List;

public class CreateDailyDairyActivity extends AppCompatActivity {

    private ProgressDialog dialog;
    private RadioGroup radioGroupType;
    private String page_title = "";
    private TextView header_text;
    private EditText editTextDesc, editTextTitle;
    private TextView course_txt;
    private CheckBox checkBoxParentReply;
    private Button buttonCreate, buttonAttachment;
    private ImageView imageViewNext;
    private static final int FILE_SELECT_CODE = 0;
    private String path, isComment, encrypted_file_name;
    private int serverResponseCode = 0;
    private int type;
    private JSONArray students;
    private String filename;

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
        setContentView(R.layout.create_daily_view);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Create Daily Diary");
        header_text = (TextView) findViewById(R.id.header_text);
        header_text.setVisibility(View.GONE);
        initializeElements();
    }

    private void initializeElements() {
        radioGroupType = (RadioGroup) findViewById(R.id.radioGroupType);
        editTextTitle = (EditText) findViewById(R.id.editTextTitle);
        editTextDesc = (EditText) findViewById(R.id.editTextDesc);
        course_txt = (TextView) findViewById(R.id.course_txt);
        checkBoxParentReply = (CheckBox) findViewById(R.id.checkBoxParentReply);
        buttonCreate = (Button) findViewById(R.id.buttonCreate);
        buttonAttachment = (Button) findViewById(R.id.buttonAttachment);
        imageViewNext = (ImageView) findViewById(R.id.imageViewNext);

        radioGroupType.check(R.id.rbStudentWise);
        type = 1;

        buttonCreate.setVisibility(View.VISIBLE);
        imageViewNext.setVisibility(View.GONE);

        buttonCreate.setText("Next");

        course_txt.setText("Class : " + getIntent().getStringExtra("class") + getIntent().getStringExtra("section"));

        radioGroupType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rbStudentWise) {
                    type = 1;
//                    imageViewNext.setVisibility(View.VISIBLE);
//                    buttonCreate.setVisibility(View.GONE);
                    buttonCreate.setText("Next");
                } else {
                    type = 2;
//                    imageViewNext.setVisibility(View.GONE);
//                    buttonCreate.setVisibility(View.VISIBLE);
                    buttonCreate.setText("Create Daily Diary");
                }
            }

        });

        buttonAttachment.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                showFileChooser();
            }
        });

        buttonCreate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editTextTitle.getText().toString().trim().isEmpty() || editTextTitle.getText().toString().trim().isEmpty()) {
                    Toast.makeText(CreateDailyDairyActivity.this, "Fields cannot be blank", Toast.LENGTH_LONG).show();
                } else {
                    if (path != null && path.length() > 0) {
                        new UploadFileAsyntask().execute();
                    } else {
                        requetsForCreatingDaily("");
                    }
                }
            }
        });
    }

    private class UploadFileAsyntask extends AsyncTask<Void, Void, Void> {
        String error;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(CreateDailyDairyActivity.this);
            dialog.setTitle("Updating Daily " + page_title);
            dialog.setMessage(getResources().getString(R.string.please_wait));
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected void onPostExecute(Void result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            if (dialog != null) {
                dialog.dismiss();
                dialog = null;
            }
            if (serverResponseCode == 200) {

                String uuploadfile_name = null;
                String uuuploadfile_name;

                final String[] tokens = path
                        .split("/");
                for (String s : tokens) {
                    System.out.println(s);
                    uuploadfile_name = s;
                }
                uuuploadfile_name = uuploadfile_name;
                if (uuuploadfile_name
                        .equalsIgnoreCase(getString(R.string.attachment))) {
                    uuuploadfile_name = "";
                }

                requetsForCreatingDaily(uuuploadfile_name);

            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            // TODO Auto-generated method stub
            uploadFile(path);
            return null;
        }

    }

    private void requetsForCreatingDaily(String file_name) {
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(11);
        nameValuePairs.add(
                new BasicNameValuePair("section_id", getIntent().getStringExtra("section_id")));
        nameValuePairs.add(new BasicNameValuePair("organization_id",
                SchoolAppUtils.GetSharedParameter(this, Constants.UORGANIZATIONID)));
        nameValuePairs
                .add(new BasicNameValuePair("user_id", SchoolAppUtils.GetSharedParameter(this, Constants.USERID)));

        nameValuePairs.add(
                new BasicNameValuePair("user_type_id", SchoolAppUtils.GetSharedParameter(this, Constants.USERTYPEID)));

        nameValuePairs.add(
                new BasicNameValuePair("title", editTextTitle.getText().toString().trim()));
        nameValuePairs.add(
                new BasicNameValuePair("description", editTextDesc.getText().toString().trim()));
        if (checkBoxParentReply.isChecked()) {
            isComment = "Y";
        } else {
            isComment = "N";
        }

        nameValuePairs.add(
                new BasicNameValuePair("is_comment", isComment));
        nameValuePairs.add(
                new BasicNameValuePair("type", type + ""));

        if (file_name.isEmpty()) {
            nameValuePairs.add(
                    new BasicNameValuePair("attachment", ""));
            nameValuePairs.add(
                    new BasicNameValuePair("file_name", ""));
        } else {
            nameValuePairs.add(
                    new BasicNameValuePair("attachment", encrypted_file_name));
            nameValuePairs.add(
                    new BasicNameValuePair("file_name", file_name));
        }


        new CreateDailyDiaryAsynTask().execute(nameValuePairs);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @SuppressLint("NewApi")
    @Override
    public void onStop() {
        super.onStop();
        if (dialog != null) {
            if (dialog.isShowing()) {
                dialog.dismiss();
                dialog = null;
            }
        }
        HttpResponseCache cache = HttpResponseCache.getInstalled();
        if (cache != null) {
            cache.flush();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    ;

    private class CreateDailyDiaryAsynTask extends AsyncTask<List<NameValuePair>, Void, Boolean> {
        String error = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(CreateDailyDairyActivity.this);
            dialog.setTitle(page_title);
            dialog.setMessage(getResources().getString(R.string.please_wait));
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected Boolean doInBackground(List<NameValuePair>... params) {
            List<NameValuePair> nameValuePairs = params[0];
            // Log parameters:
            Log.d("url extension: ", Urls.api_create_daily_diary);
            String parameters = "";
            for (NameValuePair nvp : nameValuePairs) {
                parameters += nvp.getName() + "=" + nvp.getValue() + ",";
            }
            Log.d("Parameters: ", parameters);
            JsonParser jParser = new JsonParser();
            JSONObject json = jParser.getJSONFromUrlnew(nameValuePairs, Urls.api_create_daily_diary);
            try {
                if (json != null) {
                    if (json.getString("result").equalsIgnoreCase("1")) {
                        Log.d("type", type + "");
                        if (type == 2) {
                            error = json.getString("data");
                        } else {
                            students = json.getJSONArray("data");
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
                if (type == 1) {
                    Intent intent = new Intent(CreateDailyDairyActivity.this, CreateDailyDairyStudentWiseActivity.class);
                    intent.putExtra("jsonarrayStudent", students.toString());
                    intent.putExtra("section_id", getIntent().getStringExtra("section_id"));
                    intent.putExtra("organization_id", SchoolAppUtils.GetSharedParameter(CreateDailyDairyActivity.this, Constants.UORGANIZATIONID));
                    intent.putExtra("user_id", SchoolAppUtils.GetSharedParameter(CreateDailyDairyActivity.this, Constants.USERID));
                    intent.putExtra("user_type_id", SchoolAppUtils.GetSharedParameter(CreateDailyDairyActivity.this, Constants.USERTYPEID));
                    intent.putExtra("student_id", "");
                    intent.putExtra("title", editTextTitle.getText().toString().trim());
                    intent.putExtra("description", editTextDesc.getText().toString().trim());
                    if (checkBoxParentReply.isChecked()) {
                        isComment = "Y";
                    } else {
                        isComment = "N";
                    }
                    intent.putExtra("is_comment", isComment);
                    intent.putExtra("type", type);
                    intent.putExtra("file_name", filename);
                    intent.putExtra("attachment", encrypted_file_name);
                    startActivity(intent);
                } else {
                    Toast.makeText(CreateDailyDairyActivity.this, error, Toast.LENGTH_LONG).show();
                    finish();
                }

            } else {
                if (error.length() > 0) {
                    SchoolAppUtils.showDialog(CreateDailyDairyActivity.this, page_title, error);
                } else {
                    SchoolAppUtils.showDialog(CreateDailyDairyActivity.this, page_title,
                            getResources().getString(R.string.error));
                }

            }
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
            Toast.makeText(CreateDailyDairyActivity.this, "Please install a File Manager.",
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

                            path = FileUtils.getDriveFileAbsolutePath(
                                    CreateDailyDairyActivity.this, selectedUri);
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }

                    Log.d("TAG", "File Path: " + path);
                    buttonAttachment.setText("File Attached");

                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);


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

//            dialog.dismiss();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    buttonAttachment.setText("Source File not exist :"
                            + buttonAttachment.getText().toString());
                }
            });

            return 0;

        } else {
            try {

                // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream(
                        sourceFile);
                URL url = new URL(Urls.upLoadFileFromDailyDiary);
                Log.d("test", "test");
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
                Log.d("test", "test");
                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                        + fileName + "\"" + lineEnd);

                dos.writeBytes(lineEnd);
                Log.d("test", "test");
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

                Log.i("uploadFile", "HTTP Response is : "
                        + serverResponseMessage + ": " + serverResponseCode);

                // close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();

            } catch (MalformedURLException ex) {

                //  dialog.dismiss();

                ex.printStackTrace();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        buttonAttachment
                                .setText("MalformedURLException Exception : check script url.");
                        Toast.makeText(CreateDailyDairyActivity.this,
                                "MalformedURLException", Toast.LENGTH_SHORT)
                                .show();
                    }
                });

                Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
            } catch (Exception e) {

                //  dialog.dismiss();

                e.printStackTrace();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        buttonAttachment.setText("Got Exception : see logcat ");
                        Toast.makeText(CreateDailyDairyActivity.this,
                                "Got Exception : see logcat ",
                                Toast.LENGTH_SHORT).show();
                    }
                });
                Log.e("Upload Exception", "Exception : " + e.getMessage(), e);
            }
//            dialog.dismiss();

            return serverResponseCode;

        } // End else block
    }
}
