package com.example.memorix.view;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;

import com.example.memorix.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StatisticsFragment extends Fragment {
    private TextView tvTotal7Days, tvTotal30Days, tvAverage7Days, tvAverage30Days;
    private TextView tvCurrentStreak, tvLongestStreak;
    private BarChart barChart7Days;
    private LineChart lineChart30Days;

    // Database helper - sẽ thay thế bằng API service
    // private ApiService apiService;

    @Nullable
    private void updateStreakData() {
        // TODO: Thay thế bằng API call
        // Dữ liệu cứng cho streak
        int currentStreak = 15; // Streak hiện tại
        int longestStreak = 42; // Streak dài nhất

        // Cập nhật TextView
        tvCurrentStreak.setText(String.format(Locale.getDefault(), "%d ngày", currentStreak));
        tvLongestStreak.setText(String.format(Locale.getDefault(), "%d ngày", longestStreak));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        initViews(view);
        setupCharts();
        loadStatistics();

        return view;
    }

    private void initViews(View view) {
        tvTotal7Days = view.findViewById(R.id.tv_total_7_days);
        tvTotal30Days = view.findViewById(R.id.tv_total_30_days);
        tvAverage7Days = view.findViewById(R.id.tv_average_7_days);
        tvAverage30Days = view.findViewById(R.id.tv_average_30_days);
        tvCurrentStreak = view.findViewById(R.id.tv_current_streak);
        tvLongestStreak = view.findViewById(R.id.tv_longest_streak);
        barChart7Days = view.findViewById(R.id.bar_chart_7_days);
        lineChart30Days = view.findViewById(R.id.line_chart_30_days);

        // Khởi tạo API service
        // apiService = new ApiService();
    }

    private void setupCharts() {
        setupBarChart();
        setupLineChart();
    }

    private void setupBarChart() {
        barChart7Days.setDrawBarShadow(false);
        barChart7Days.setDrawValueAboveBar(true);
        barChart7Days.getDescription().setEnabled(false);
        barChart7Days.setPinchZoom(false);
        barChart7Days.setDrawGridBackground(false);

        XAxis xAxis = barChart7Days.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(7);

        YAxis leftAxis = barChart7Days.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setAxisMinimum(0f);

        YAxis rightAxis = barChart7Days.getAxisRight();
        rightAxis.setEnabled(false);

        barChart7Days.getLegend().setEnabled(false);
        barChart7Days.setExtraOffsets(10, 10, 10, 20);
    }

    private void setupLineChart() {
        lineChart30Days.getDescription().setEnabled(false);
        lineChart30Days.setTouchEnabled(true);
        lineChart30Days.setDragEnabled(true);
        lineChart30Days.setScaleEnabled(true);
        lineChart30Days.setPinchZoom(true);

        XAxis xAxis = lineChart30Days.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);

        YAxis leftAxis = lineChart30Days.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setAxisMinimum(0f);

        YAxis rightAxis = lineChart30Days.getAxisRight();
        rightAxis.setEnabled(false);

        lineChart30Days.getLegend().setEnabled(false);
        lineChart30Days.setExtraOffsets(10, 10, 10, 20);
    }

    private void loadStatistics() {
        // Lấy dữ liệu cho 7 ngày
        List<Integer> last7DaysData = getLast7DaysData();
        setupBarChartData(last7DaysData);

        // Lấy dữ liệu cho 30 ngày
        List<Integer> last30DaysData = getLast30DaysData();
        setupLineChartData(last30DaysData);

        // Tính toán và hiển thị thống kê tổng quan
        updateStatisticsText(last7DaysData, last30DaysData);

        // Cập nhật streak data
        updateStreakData();
    }

    private List<Integer> getLast7DaysData() {
        // Dữ liệu cứng cho 7 ngày qua (từ cũ đến mới)
        // TODO: Thay thế bằng API call
        List<Integer> data = new ArrayList<>();
        data.add(25); // 6 ngày trước
        data.add(18); // 5 ngày trước
        data.add(32); // 4 ngày trước
        data.add(41); // 3 ngày trước
        data.add(28); // 2 ngày trước
        data.add(35); // hôm qua
        data.add(22); // hôm nay

        return data;
    }

    private List<Integer> getLast30DaysData() {
        // Dữ liệu cứng cho 30 ngày qua
        // TODO: Thay thế bằng API call
        List<Integer> data = new ArrayList<>();

        // Dữ liệu mẫu cho 30 ngày
        int[] hardcodedData = {
                15, 22, 18, 25, 30, 28, 35, 20, 24, 38,
                42, 18, 25, 33, 28, 31, 24, 36, 29, 22,
                35, 40, 28, 32, 25, 18, 32, 41, 28, 35
        };

        for (int value : hardcodedData) {
            data.add(value);
        }

        return data;
    }

    private void setupBarChartData(List<Integer> data) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault());

        for (int i = 0; i < data.size(); i++) {
            entries.add(new BarEntry(i, data.get(i)));

            calendar.setTime(new Date());
            calendar.add(Calendar.DAY_OF_YEAR, -(6-i));
            labels.add(dayFormat.format(calendar.getTime()));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Cards Studied");

        // Sử dụng color palette của app
        int primaryColor = ContextCompat.getColor(getContext(), R.color.primary_color);
        int secondaryColor = ContextCompat.getColor(getContext(), R.color.secondary_color);
        int accentColor = ContextCompat.getColor(getContext(), R.color.accent_color);
        int primaryLightColor = ContextCompat.getColor(getContext(), R.color.primary_light_color);

        dataSet.setColors(new int[]{
                primaryColor,
                secondaryColor,
                accentColor,
                primaryLightColor,
                ContextCompat.getColor(getContext(), R.color.primary_dark_color),
                ContextCompat.getColor(getContext(), R.color.secondary_dark_color),
                ContextCompat.getColor(getContext(), R.color.accent_dark_color)
        });
        dataSet.setDrawValues(true);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(ContextCompat.getColor(getContext(), R.color.text_color));

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.8f);

        barChart7Days.setData(barData);
        barChart7Days.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChart7Days.invalidate();
    }

    private void setupLineChartData(List<Integer> data) {
        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());

        for (int i = 0; i < data.size(); i++) {
            entries.add(new Entry(i, data.get(i)));

            calendar.setTime(new Date());
            calendar.add(Calendar.DAY_OF_YEAR, -(29-i));
            labels.add(dateFormat.format(calendar.getTime()));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Cards Studied");

        // Sử dụng primary color cho line chart
        int primaryColor = ContextCompat.getColor(getContext(), R.color.primary_color);
        int primaryLightColor = ContextCompat.getColor(getContext(), R.color.primary_light_color);

        dataSet.setColor(primaryColor);
        dataSet.setCircleColor(primaryColor);
        dataSet.setLineWidth(3f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircleHole(false);
        dataSet.setValueTextSize(10f);
        dataSet.setValueTextColor(ContextCompat.getColor(getContext(), R.color.text_color));
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(primaryLightColor);
        dataSet.setFillAlpha(100);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData lineData = new LineData(dataSet);

        lineChart30Days.setData(lineData);
        lineChart30Days.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < labels.size()) {
                    // Chỉ hiện label cho mỗi 5 ngày để tránh bị chồng chéo
                    if (index % 5 == 0) {
                        return labels.get(index);
                    }
                }
                return "";
            }
        });
        lineChart30Days.invalidate();
    }

    private void updateStatisticsText(List<Integer> data7Days, List<Integer> data30Days) {
        // Tính tổng số cards học trong 7 ngày
        int total7Days = 0;
        for (int count : data7Days) {
            total7Days += count;
        }

        // Tính tổng số cards học trong 30 ngày
        int total30Days = 0;
        for (int count : data30Days) {
            total30Days += count;
        }

        // Tính trung bình
        double average7Days = (double) total7Days / 7;
        double average30Days = (double) total30Days / 30;

        // Cập nhật TextView
        tvTotal7Days.setText(String.format(Locale.getDefault(), "%d", total7Days));
        tvTotal30Days.setText(String.format(Locale.getDefault(), "%d", total30Days));
        tvAverage7Days.setText(String.format(Locale.getDefault(), "%.1f", average7Days));
        tvAverage30Days.setText(String.format(Locale.getDefault(), "%.1f", average30Days));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Cleanup nếu cần
    }
}