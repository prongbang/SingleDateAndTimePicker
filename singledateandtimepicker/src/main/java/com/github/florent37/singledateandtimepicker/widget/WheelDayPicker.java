package com.github.florent37.singledateandtimepicker.widget;

import android.content.Context;
import android.util.AttributeSet;

import com.github.florent37.singledateandtimepicker.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;

import static com.github.florent37.singledateandtimepicker.widget.SingleDateAndTimeConstants.DAYS_PADDING;

public class WheelDayPicker extends WheelPicker<DateWithLabel> {

    private static final String DAY_FORMAT_PATTERN = "EEE d MMM";

    private SimpleDateFormat simpleDateFormat;
    private SimpleDateFormat customDateFormat;
    private int dayCount = DAYS_PADDING;

    private OnDaySelectedListener onDaySelectedListener;

    public WheelDayPicker(Context context) {
        super(context);
    }

    public WheelDayPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void init() {
        simpleDateFormat = new SimpleDateFormat(DAY_FORMAT_PATTERN, getCurrentLocale());
        simpleDateFormat.setTimeZone(dateHelper.getTimeZone());
    }

    @Override
    public void setCustomLocale(Locale customLocale) {
        super.setCustomLocale(customLocale);
        simpleDateFormat = new SimpleDateFormat(DAY_FORMAT_PATTERN, getCurrentLocale());
        simpleDateFormat.setTimeZone(dateHelper.getTimeZone());
    }

    @Override
    protected DateWithLabel initDefault() {
        Date today = new Date();
        return new DateWithLabel(getFormattedValue(today), today, true);
    }

    @Override
    protected void onItemSelected(int position, DateWithLabel item) {
        if (onDaySelectedListener != null) {
            onDaySelectedListener.onDaySelected(this, position, item.label, item.date);
        }
    }

    public void setDayCount(int dayCount) {
        this.dayCount = dayCount;
    }

    @Override
    protected List<DateWithLabel> generateAdapterValues(boolean showOnlyFutureDates) {
        final List<DateWithLabel> days = new ArrayList<>();

        Calendar instance = Calendar.getInstance();
        instance.setTimeZone(dateHelper.getTimeZone());
        int startDayOffset = showOnlyFutureDates ? 0 : -1 * dayCount;
        instance.add(Calendar.DATE, startDayOffset - 1);
        for (int i = startDayOffset; i < 0; ++i) {
            instance.add(Calendar.DAY_OF_MONTH, 1);
            Date date = instance.getTime();
            days.add(new DateWithLabel(getFormattedValue(date), date, false));
        }

        //today
        Date today = new Date();
        days.add(new DateWithLabel(getFormattedValue(today), today, true));

        instance = Calendar.getInstance();
        instance.setTimeZone(dateHelper.getTimeZone());

        if (showOnlyFutureDates) {
            for (int i = 0; i < dayCount; ++i) {
                instance.add(Calendar.DATE, 1);
                Date date = instance.getTime();
                days.add(new DateWithLabel(getFormattedValue(date), date, false));
            }
        }

        return days;
    }

    protected String getFormattedValue(Object value) {
        return getDateFormat().format(value);
    }

    public WheelDayPicker setDayFormatter(SimpleDateFormat simpleDateFormat) {
        simpleDateFormat.setTimeZone(dateHelper.getTimeZone());
        this.customDateFormat = simpleDateFormat;
        updateAdapter();
        return this;
    }

    public void setOnDaySelectedListener(OnDaySelectedListener onDaySelectedListener) {
        this.onDaySelectedListener = onDaySelectedListener;
    }

    public Date getCurrentDate() {
        return convertItemToDate(super.getCurrentItemPosition());
    }

    private SimpleDateFormat getDateFormat() {
        if (customDateFormat != null) {
            return customDateFormat;
        }
        return simpleDateFormat;
    }

    private Date convertItemToDate(int itemPosition) {
        Date date;
        final String itemText = adapter.getItemText(itemPosition);
        final Calendar todayCalendar = Calendar.getInstance();
        todayCalendar.setTimeZone(dateHelper.getTimeZone());

        int todayPosition = -1;
        final List<DateWithLabel> data = adapter.getData();

        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).isToday) {
                todayPosition = i;
                break;
            }
        }

        if (todayPosition <= -1 || !data.get(todayPosition).label.equals(itemText)) {
            todayCalendar.add(Calendar.DAY_OF_YEAR, (itemPosition - todayPosition));
        }
        date = todayCalendar.getTime();

        return date;
    }

    public void setTodayText(DateWithLabel today) {
        final List<DateWithLabel> data = adapter.getData();
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).isToday) {
                adapter.getData().set(i, today);
                notifyDatasetChanged();
            }
        }
    }

    public interface OnDaySelectedListener {
        void onDaySelected(WheelDayPicker picker, int position, String name, Date date);
    }
}