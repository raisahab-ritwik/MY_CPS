package com.knwedu.college;

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
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.knwedu.college.utils.CollegeAppUtils;
import com.knwedu.college.utils.CollegeConstants;
import com.knwedu.college.utils.CollegeDataStructureFramwork.Assignment;
import com.knwedu.college.utils.CollegeDataStructureFramwork.Subject;
import com.knwedu.college.utils.CollegeFileUtils;
import com.knwedu.college.utils.CollegeJsonParser;
import com.knwedu.college.utils.CollegeUrls;
import com.knwedu.calcuttapublicschool.R;

public class CollegeTeacherSpecialClassActivity extends FragmentActivity {
	private TextView ttitle, title, header;
	private Button date, submite, select_file;
	private Subject subject;
	private EditText discription, titleEdt, marksEdt;/* codeEdt */
	private ProgressDialog dialog;
	AlertDialog.Builder dialoggg;
	private static final int FILE_SELECT_CODE = 0;
	private static final int GALLERY_KITKAT = 0;
	private String encrypted_file_name = null;
	int serverResponseCode = 0;
	private String dateSelected;
	//public CollegeDBAdapter mDatabase;
	private boolean internetAvailable = false;
	String path = "null";
	private String page_title = "";
	private long length;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.college_teacher_special_class_items);
		page_title = getIntent().getStringExtra(CollegeConstants.PAGE_TITLE);
		//mDatabase = ((SchoolApplication) getApplication()).getCollegeDatabase();
		internetAvailable = getIntent().getBooleanExtra(
				CollegeConstants.IS_ONLINE, false);
		initialize();
	}

	private void showFileChooser() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("*/*");
		// intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		// intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
		intent.addCategory(Intent.CATEGORY_OPENABLE);

		try {
			startActivityForResult(Intent.createChooser(intent, "Attachment"),
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

				// Get the Uri of the selected file
				Uri uri = data.getData();
				Log.d("TAG", "File Uri: " + uri.toString());

				// Get the path
				try {
					path = CollegeFileUtils.getPath(this, uri);
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (TextUtils.isEmpty(path)) {
					try {

						path = CollegeFileUtils.getDriveFileAbsolutePath(
								CollegeTeacherSpecialClassActivity.this, uri);
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

	@Override
	public void onStart() {
		super.onStart();
		Log.d("Google Analytics", "Tracking Start");
		EasyTracker.getInstance(this).activityStart(this);
		if (dialog != null) {
			dialog.dismiss();
			dialog = null;
		}
		HttpResponseCache cache = HttpResponseCache.getInstalled();
		if (cache != null) {
			cache.flush();
		}

	}

	@Override
	public void onStop() {
		super.onStop();
		Log.d("Google Analytics", "Tracking Stop");
		EasyTracker.getInstance(this).activityStop(this);
	}

	private void initialize() {
		((ImageButton) findViewById(R.id.back_btn))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						onBackPressed();
					}
				});
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
		dateSelected = CollegeAppUtils.getCurrentDate();
		date.setText(CollegeAppUtils.getDayDifferentDif(CollegeAppUtils
				.getCurrentDate()));

		header = (TextView) findViewById(R.id.header_text);
		header.setText(page_title);

		/*
		 * LocationManager locMan = (LocationManager)
		 * getSystemService(LOCATION_SERVICE); long networkTS =
		 * locMan.getLastKnownLocation
		 * (LocationManager.NETWORK_PROVIDER).getTime(); Date dt = new
		 * Date(networkTS); header.setText(dt.toString());
		 */

		if (getIntent().getExtras() != null) {
			if (internetAvailable) {
				String temp = getIntent().getExtras().getString(
						CollegeConstants.TSUBJECT);
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
			} /*else {

				findViewById(R.id.txt_offline).setVisibility(View.VISIBLE);
				// select_file.setVisibility(View.GONE);
				String subject_id = getIntent().getExtras().getString(
						CollegeConstants.OFFLINE_SUBJECT_ID);

				mDatabase.open();
				subject = mDatabase.getSubject(subject_id);
				mDatabase.close();

			}*/

		}
		if (subject != null) {
			ttitle.setText(subject.classs + " " + subject.section_name);
			title.setText(subject.sub_name);
		} else {
			ttitle.setText("");
			title.setText("");
		}

		date.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				  DialogFragment newFragment = new DatePickerFragment();
				  newFragment.show(getSupportFragmentManager(), "datePicker");
				 
				return;
			}
		});

		

		select_file.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showFileChooser();
			}
		});
		submite.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (dateSelected.toString().equalsIgnoreCase("date")) {
					Toast.makeText(CollegeTeacherSpecialClassActivity.this,
							"Select Date", Toast.LENGTH_SHORT).show();
					return;
				}
				if (titleEdt.getText().toString().length() <= 0) {
					Toast.makeText(CollegeTeacherSpecialClassActivity.this,
							"Enter Title", Toast.LENGTH_SHORT).show();
					return;
				}
				if (discription.getText().toString().length() <= 0) {
					Toast.makeText(CollegeTeacherSpecialClassActivity.this,
							"Enter Description", Toast.LENGTH_SHORT).show();
					return;
				}
				if (!internetAvailable) {
					new OfflineAnnouncementAddAsync().execute();
				} else {

					if (select_file.getText().toString()
							.equalsIgnoreCase("attachment")) {

						List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
								15);

						nameValuePairs.add(new BasicNameValuePair("id",
								CollegeAppUtils.GetSharedParameter(
										getApplicationContext(), "id")));
						nameValuePairs.add(new BasicNameValuePair(
								"subject_relation_id",
								subject.subject_relation_id));

						nameValuePairs.add(new BasicNameValuePair("title",
								titleEdt.getText().toString().trim()));
						nameValuePairs.add(new BasicNameValuePair(
								"description", discription.getText().toString()
										.trim()));
						nameValuePairs.add(new BasicNameValuePair("date", dateSelected));
						nameValuePairs.add(new BasicNameValuePair("file_name",
								""));
						nameValuePairs.add(new BasicNameValuePair("orig_name",
								""));
						nameValuePairs.add(new BasicNameValuePair("size", ""));
						Log.d("PARAMS OF SPECIAL CLASS", ""+nameValuePairs);
						new UploadAsyntask().execute(nameValuePairs);

					} else {
						dialog = ProgressDialog.show(
								CollegeTeacherSpecialClassActivity.this, "",
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
						CollegeTeacherSpecialClassActivity.this,
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
			date.setText(CollegeAppUtils.getDayDifferent(year + "/" + mon + "/"
					+ da));
			month = month + 1;
			if (month < 10) {
				mon = "0" + month;
			} else {
				mon = "" + month;
			}
		}
	}

	public void SizeFile(String sourceFileUri) {
		File file = new File(sourceFileUri);
		length = file.length();
		length = length / 1024;
		System.out.println("File Path : " + file.getPath() + ", File size : "
				+ length + " KB");
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

			Intent intent = new Intent(CollegeTeacherSpecialClassActivity.this,
					CollegeTeacherAnnouncementListActivity.class);
			intent.putExtra(CollegeConstants.TANNOUNCEMENT,
					subject.object.toString());
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
				URL url = new URL(CollegeUrls.base_url + CollegeUrls.upload_url);

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
							SizeFile(select_file.getText().toString());
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
									.equalsIgnoreCase("Select file for upload")) {
								uuuploadfile_name = "";
							}

							List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
									15);
							nameValuePairs.add(new BasicNameValuePair("id",
									CollegeAppUtils.GetSharedParameter(
											getApplicationContext(), "id")));
							nameValuePairs.add(new BasicNameValuePair(
									"subject_relation_id",
									subject.subject_relation_id));

							nameValuePairs.add(new BasicNameValuePair("title",
									titleEdt.getText().toString().trim()));
							nameValuePairs.add(new BasicNameValuePair(
									"description", discription.getText()
											.toString().trim()));
							nameValuePairs.add(new BasicNameValuePair("date",
									dateSelected));
							nameValuePairs.add(new BasicNameValuePair(
									"orig_name", encrypted_file_name));
							nameValuePairs.add(new BasicNameValuePair(
									"file_name", uuploadfile_name));
							nameValuePairs.add(new BasicNameValuePair("size",
									"" + length));
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
						Toast.makeText(CollegeTeacherSpecialClassActivity.this,
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
						Toast.makeText(CollegeTeacherSpecialClassActivity.this,
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
		String error;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = new ProgressDialog(CollegeTeacherSpecialClassActivity.this);
			dialog.setTitle(page_title);
			dialog.setMessage(getResources().getString(R.string.please_wait));
			dialog.setCanceledOnTouchOutside(false);
			dialog.setCancelable(false);
			dialog.show();
		}

		@Override
		protected Boolean doInBackground(List<NameValuePair>... params) {
			List<NameValuePair> namevaluepair = params[0];
			CollegeJsonParser jParser = new CollegeJsonParser();
			JSONObject json = jParser.getJSONFromUrlnew(namevaluepair,
					CollegeUrls.api_specialclass_create);
			try {

				if (json != null) {
					if (json.getString("result").equalsIgnoreCase("1")) {
						error = page_title + " Created.";

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
			if (dialog != null) {
				dialog.dismiss();
				dialog = null;
			}
			if (result) {
				dialoggg = new AlertDialog.Builder(
						CollegeTeacherSpecialClassActivity.this);
				dialoggg.setTitle(page_title);
				dialoggg.setMessage(error);
				dialoggg.setNeutralButton(R.string.ok,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								CollegeAppUtils
										.SetSharedBoolParameter(
												CollegeTeacherSpecialClassActivity.this,
												"update", true);
								CollegeTeacherSpecialClassActivity.this
										.finish();
							}
						});
				dialoggg.show();
			} else {
				if (error != null) {
					if (error.length() > 0) {
						CollegeAppUtils.showDialog(
								CollegeTeacherSpecialClassActivity.this,
								page_title, error);
					} else {
						CollegeAppUtils
								.showDialog(
										CollegeTeacherSpecialClassActivity.this,
										page_title,
										getResources()
												.getString(
														R.string.please_check_internet_connection));
					}
				} else {
					CollegeAppUtils.showDialog(
							CollegeTeacherSpecialClassActivity.this,
							page_title,
							getResources().getString(
									R.string.please_check_internet_connection));
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

		/*	mDatabase.open();
			Announcement announcement = new Announcement();
			announcement.date = CollegeAppUtils.getCurrentDate();
			announcement.description = discription.getText().toString().trim();
			announcement.title = titleEdt.getText().toString().trim();
			announcement.file_name = path;
			announcement.attachment = "null";
			announcement.subject_id = subject.id;
			announcement.section_id = subject.section_id;

			row_id = mDatabase.addAnnouncement(announcement, 2);
			mDatabase.close();*/

			return (row_id > 0);
		}

		@Override
		protected void onProgressUpdate(Assignment... values) {
			super.onProgressUpdate(values);
		}

		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			final AlertDialog.Builder dialog = new AlertDialog.Builder(
					CollegeTeacherSpecialClassActivity.this);
			dialog.setTitle("New " + page_title);
			// mDatabase.close();
			if (result) {
				dialog.setMessage(page_title + " Added in Offline Mode");
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
