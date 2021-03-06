package com.knwedu.com.knwedu.calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.knwedu.com.knwedu.calendar.CalendarPickerView.SelectionMode;
import com.knwedu.calcuttapublicschool.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

public class SampleTimesSquareActivity extends Activity {
	private static final String TAG = "SampleTimesSquareActivi";
	private CalendarPickerView calendar;
	private AlertDialog theDialog;
	private CalendarPickerView dialogView;
	private final Set<Button> modeButtons = new LinkedHashSet<Button>();
	private ArrayList<Date> dates = new ArrayList<Date>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sample_calendar_picker);

		final Calendar nextYear = Calendar.getInstance();
		nextYear.add(Calendar.YEAR, 1);

		Calendar todayDate = Calendar.getInstance();
		System.out.println("today : " + todayDate.getTime());
		// final Calendar lastYear = Calendar.getInstance();
		// lastYear.add(Calendar.YEAR, -1);

		calendar = (CalendarPickerView) findViewById(R.id.calendar_view);
		// calendar.init(today.getTime(), nextYear.getTime()) //
		// .inMode(SelectionMode.SINGLE) //
		// .withSelectedDate(new Date());

		calendar.setCustomDayView(new DefaultDayViewAdapter());

		// today.add(Calendar.DATE, 3);
		Calendar startDate = Calendar.getInstance();
		startDate.set(2016, 05 - 01, 01);
		dates.add(startDate.getTime());
		Calendar endDate = Calendar.getInstance();
		endDate.set(2016, 05 - 01, 10);
		// today.add(Calendar.DATE, 20);
		// dates.add(today.getTime());
		dates.add(endDate.getTime());
		calendar.setDecorators(Collections.<CalendarCellDecorator> emptyList());
		calendar.init(todayDate.getTime(), nextYear.getTime()) //
				.inMode(SelectionMode.RANGE) //
				.withSelectedDates(dates);

		calendar.setCellClickInterceptor(new CalendarPickerView.CellClickInterceptor() {
			@Override
			public boolean onCellClicked(Date date) {
				if (dates.get(0).compareTo(date) * date.compareTo(dates.get(1)) > 0) {
//					Intent intent = new Intent(SampleTimesSquareActivity.this, DetailsActivity.class);
//					intent.putExtra("Title", "Title : Event Title");
//					intent.putExtra("Desc", "Description : Event Description");
//					intent.putExtra("SD", "Start Date : " + dates.get(0).getDate() + "/" + dates.get(0).getMonth() + "/"
//							+ dates.get(0).getYear());
//					intent.putExtra("ED", "End Date : " + dates.get(1).getDate() + "/" + dates.get(1).getMonth() + "/"
//							+ dates.get(1).getYear());
//					startActivity(intent);
				}
				return true;

			}
		});

		// initButtonListeners(nextYear, today);
	}

	private void initButtonListeners(final Calendar nextYear, final Calendar currentYear) {
		final Button single = (Button) findViewById(R.id.button_single);
		final Button multi = (Button) findViewById(R.id.button_multi);
		final Button range = (Button) findViewById(R.id.button_range);
		final Button displayOnly = (Button) findViewById(R.id.button_display_only);
		final Button dialog = (Button) findViewById(R.id.button_dialog);
		final Button customized = (Button) findViewById(R.id.button_customized);
		final Button decorator = (Button) findViewById(R.id.button_decorator);
		final Button hebrew = (Button) findViewById(R.id.button_hebrew);
		final Button arabic = (Button) findViewById(R.id.button_arabic);
		final Button customView = (Button) findViewById(R.id.button_custom_view);

		modeButtons.addAll(Arrays.asList(single, multi, range, displayOnly, decorator, customView));

		single.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setButtonsEnabled(single);

				calendar.setCustomDayView(new DefaultDayViewAdapter());
				calendar.setDecorators(Collections.<CalendarCellDecorator> emptyList());
				calendar.init(currentYear.getTime(), nextYear.getTime()) //
						.inMode(SelectionMode.SINGLE) //
						.withSelectedDate(new Date());
			}
		});

		multi.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setButtonsEnabled(multi);

				calendar.setCustomDayView(new DefaultDayViewAdapter());
				Calendar today = Calendar.getInstance();
				ArrayList<Date> dates = new ArrayList<Date>();
				for (int i = 0; i < 5; i++) {
					today.add(Calendar.DAY_OF_MONTH, 3);
					dates.add(today.getTime());
				}
				calendar.setDecorators(Collections.<CalendarCellDecorator> emptyList());
				calendar.init(new Date(), nextYear.getTime()) //
						.inMode(SelectionMode.MULTIPLE) //
						.withSelectedDates(dates);
			}
		});

		range.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setButtonsEnabled(range);

				calendar.setCustomDayView(new DefaultDayViewAdapter());
				Calendar today = Calendar.getInstance();
				ArrayList<Date> dates = new ArrayList<Date>();
				// today.add(Calendar.DATE, 3);
				today.set(2016, 05 - 01, 01);
				System.out.println("today : " + today.getTime());
				dates.add(today.getTime());
				today.set(2016, 05, 10);
				// today.add(Calendar.DATE, 20);
				// dates.add(today.getTime());
				dates.add(today.getTime());
				calendar.setDecorators(Collections.<CalendarCellDecorator> emptyList());
				calendar.init(new Date(), nextYear.getTime()) //
						.inMode(SelectionMode.RANGE) //
						.withSelectedDates(dates);
			}
		});

		displayOnly.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setButtonsEnabled(displayOnly);

				calendar.setCustomDayView(new DefaultDayViewAdapter());
				calendar.setDecorators(Collections.<CalendarCellDecorator> emptyList());
				calendar.init(new Date(), nextYear.getTime()) //
						.inMode(SelectionMode.SINGLE) //
						.withSelectedDate(new Date()) //
						.displayOnly();
			}
		});

		dialog.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				String title = "I'm a dialog!";
				showCalendarInDialog(title, R.layout.dialog);
				dialogView.init(currentYear.getTime(), nextYear.getTime()) //
						.withSelectedDate(new Date());
			}
		});

		customized.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				showCalendarInDialog("Pimp my calendar!", R.layout.dialog_customized);
				dialogView.init(currentYear.getTime(), nextYear.getTime()).withSelectedDate(new Date());
			}
		});

		decorator.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setButtonsEnabled(decorator);

				calendar.setCustomDayView(new DefaultDayViewAdapter());
				calendar.setDecorators(Arrays.<CalendarCellDecorator> asList(new SampleDecorator()));
				calendar.init(currentYear.getTime(), nextYear.getTime()) //
						.inMode(SelectionMode.SINGLE) //
						.withSelectedDate(new Date());
			}
		});

		hebrew.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				showCalendarInDialog("I'm Hebrew!", R.layout.dialog);
				dialogView.init(currentYear.getTime(), nextYear.getTime(), new Locale("iw", "IL")) //
						.withSelectedDate(new Date());
			}
		});

		arabic.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				showCalendarInDialog("I'm Arabic!", R.layout.dialog);
				dialogView.init(currentYear.getTime(), nextYear.getTime(), new Locale("ar", "EG")) //
						.withSelectedDate(new Date());
			}
		});

		customView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				setButtonsEnabled(customView);

				calendar.setDecorators(Collections.<CalendarCellDecorator> emptyList());
				calendar.setCustomDayView(new SampleDayViewAdapter());
				calendar.init(currentYear.getTime(), nextYear.getTime()).inMode(SelectionMode.SINGLE)
						.withSelectedDate(new Date());
			}
		});

//		findViewById(R.id.done_button).setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View view) {
//				Log.d(TAG, "Selected time in millis: " + calendar.getSelectedDate().getTime());
//				String toast = "Selected: " + calendar.getSelectedDate().getTime();
//				Toast.makeText(SampleTimesSquareActivity.this, toast, LENGTH_SHORT).show();
//			}
//		});
	}

	private void showCalendarInDialog(String title, int layoutResId) {
		dialogView = (CalendarPickerView) getLayoutInflater().inflate(layoutResId, null, false);
		theDialog = new AlertDialog.Builder(this) //
				.setTitle(title).setView(dialogView).setNeutralButton("Dismiss", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						dialogInterface.dismiss();
					}
				}).create();
		theDialog.setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(DialogInterface dialogInterface) {
				Log.d(TAG, "onShow: fix the dimens!");
				dialogView.fixDialogDimens();
			}
		});
		theDialog.show();
	}

	private void setButtonsEnabled(Button currentButton) {
		for (Button modeButton : modeButtons) {
			modeButton.setEnabled(modeButton != currentButton);
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		boolean applyFixes = theDialog != null && theDialog.isShowing();
		if (applyFixes) {
			Log.d(TAG, "Config change: unfix the dimens so I'll get remeasured!");
			dialogView.unfixDialogDimens();
		}
		super.onConfigurationChanged(newConfig);
		if (applyFixes) {
			dialogView.post(new Runnable() {
				@Override
				public void run() {
					Log.d(TAG, "Config change done: re-fix the dimens!");
					dialogView.fixDialogDimens();
				}
			});
		}
	}
}
