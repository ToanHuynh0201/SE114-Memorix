package com.example.memorix.view;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.memorix.R;
import com.example.memorix.data.remote.dto.Share.StreakResponse;
import com.example.memorix.data.remote.dto.Statistic.StatisticsResponse;
import com.example.memorix.viewmodel.StatisticsViewModel;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
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
import java.util.Random;
import java.util.TimeZone;

public class StatisticsFragment extends Fragment {
    private TextView tvTotal7Days, tvTotal30Days, tvAverage7Days, tvAverage30Days;
    private TextView tvCurrentStreak, tvLongestStreak;
    private BarChart barChart7Days;
    private LineChart lineChart30Days;
    private ProgressBar progressBar;

    private StatisticsViewModel viewModel;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            View view = inflater.inflate(R.layout.fragment_statistics, container, false);

            initViews(view);
            initViewModel();
            setupCharts();
            setupObservers();

            // Delay loading để đảm bảo UI đã setup xong
            view.post(this::loadStatistics);

            return view;
        } catch (Exception e) {
            // Tạo view đơn giản với text để debug
            TextView errorView = new TextView(requireContext());
            errorView.setText("Error loading statistics: " + e.getMessage());
            errorView.setTextColor(android.graphics.Color.RED);
            errorView.setPadding(50, 50, 50, 50);
            return errorView;
        }
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
            progressBar = view.findViewById(R.id.progress_bar);
    }

    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(StatisticsViewModel.class);
    }

    private void setupObservers() {
        try {
            // Observe statistics data
            viewModel.getStatisticsLiveData().observe(getViewLifecycleOwner(), statisticsResponse -> {
                    if (statisticsResponse != null) {
                        viewModel.processStatisticsData(statisticsResponse);
                        updateStatisticsText(statisticsResponse);
                    } else {
                        // Show demo data message when no API data
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Đang hiển thị dữ liệu demo", Toast.LENGTH_SHORT).show();
                        }
                        viewModel.processStatisticsData(null);
                    }
            });

            // Observe streak data
            viewModel.getStreakLiveData().observe(getViewLifecycleOwner(), streakResponse -> {
                try {
                    if (streakResponse != null) {
                        viewModel.processStreakData(streakResponse);
                        updateStreakText(streakResponse);
                    } else {
                        // Show demo data message when no API data
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Đang hiển thị dữ liệu streak demo", Toast.LENGTH_SHORT).show();
                        }
                        viewModel.processStreakData(null);
                    }
                } catch (Exception e) {
//                    e.printStackTrace();
                }
            });

            // Observe 7 days data for bar chart
            viewModel.getLast7DaysDataLiveData().observe(getViewLifecycleOwner(), data -> {
                try {
                    if (data != null && barChart7Days != null) {
                        setupBarChartData(data);
                    }
                } catch (Exception e) {
//                    e.printStackTrace();
                }
            });

            // Observe 30 days data for line chart
            viewModel.getLast30DaysDataLiveData().observe(getViewLifecycleOwner(), data -> {
                try {
                    if (data != null && lineChart30Days != null) {
                        setupLineChartData(data);
                    }
                } catch (Exception e) {
//                    e.printStackTrace();
                }
            });

            // Observe streaks from ViewModel (for fallback data)
            viewModel.getCurrentStreakLiveData().observe(getViewLifecycleOwner(), currentStreak -> {
                try {
                    if (currentStreak != null && tvCurrentStreak != null) {
                        tvCurrentStreak.setText(String.format(Locale.getDefault(), "%d ngày", currentStreak));
                    }
                } catch (Exception e) {
//                    e.printStackTrace();
                }
            });

            viewModel.getLongestStreakLiveData().observe(getViewLifecycleOwner(), longestStreak -> {
                try {
                    if (longestStreak != null && tvLongestStreak != null) {
                        tvLongestStreak.setText(String.format(Locale.getDefault(), "%d ngày", longestStreak));
                    }
                } catch (Exception e) {
//                    e.printStackTrace();
                }
            });

            // Observe loading state
            viewModel.getLoadingLiveData().observe(getViewLifecycleOwner(), isLoading -> {
                try {
                    if (progressBar != null) {
                        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                    }
                } catch (Exception e) {
//                    e.printStackTrace();
                }
            });

            // Observe errors
            viewModel.getErrorLiveData().observe(getViewLifecycleOwner(), errorMessage -> {
                try {
                    if (errorMessage != null && !errorMessage.isEmpty() && getContext() != null) {
                        Toast.makeText(getContext(), errorMessage + " - Hiển thị dữ liệu demo", Toast.LENGTH_LONG).show();
                        // Process with null to show demo data
                        viewModel.processStatisticsData(null);
                        viewModel.processStreakData(null);
                    }
                } catch (Exception e) {
//                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
//            e.printStackTrace();
        }
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
        viewModel.loadStatistics();
    }

    private void updateStreakText(StreakResponse streakResponse) {
        try {
            if (streakResponse == null) {
                return;
            }

            if (tvCurrentStreak != null) {
                tvCurrentStreak.setText(String.format(Locale.getDefault(), "%d ngày", streakResponse.getCurrentStreak()));
            }

            if (tvLongestStreak != null) {
                tvLongestStreak.setText(String.format(Locale.getDefault(), "%d ngày", streakResponse.getMaxStreak()));
            }
        } catch (Exception e) {
//            e.printStackTrace();
        }
    }

    private void setupBarChartData(List<Integer> data) {
        try {
            if (data == null || data.isEmpty() || barChart7Days == null || getContext() == null) {
                return;
            }

            ArrayList<BarEntry> entries = new ArrayList<>();
            ArrayList<String> labels = new ArrayList<>();

            // Use Vietnam timezone for chart labels
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+7"));
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault());
            dayFormat.setTimeZone(TimeZone.getTimeZone("GMT+7"));

            for (int i = 0; i < data.size(); i++) {
                entries.add(new BarEntry(i, data.get(i)));

                // Calculate date in local timezone
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

            dataSet.setColors(primaryColor,
                    secondaryColor,
                    accentColor,
                    primaryLightColor,
                    ContextCompat.getColor(getContext(), R.color.primary_dark_color),
                    ContextCompat.getColor(getContext(), R.color.secondary_dark_color),
                    ContextCompat.getColor(getContext(), R.color.accent_dark_color));
            dataSet.setDrawValues(true);
            dataSet.setValueTextSize(12f);
            dataSet.setValueTextColor(ContextCompat.getColor(getContext(), R.color.text_color));

            BarData barData = new BarData(dataSet);
            barData.setBarWidth(0.8f);

            barChart7Days.setData(barData);
            barChart7Days.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
            barChart7Days.invalidate();
        } catch (Exception e) {
//            e.printStackTrace();
        }

    }

    private void setupLineChartData(List<Integer> data) {
        try {
            if (data == null || data.isEmpty() || lineChart30Days == null || getContext() == null) {
                return;
            }

            ArrayList<Entry> entries = new ArrayList<>();
            ArrayList<String> labels = new ArrayList<>();

            // Use Vietnam timezone for chart labels
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+7"));
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+7"));

            for (int i = 0; i < data.size(); i++) {
                entries.add(new Entry(i, data.get(i)));

                // Calculate date in local timezone
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
                    try {
                        int index = (int) value;
                        if (index >= 0 && index < labels.size()) {
                            // Chỉ hiện label cho mỗi 5 ngày để tránh bị chồng chéo
                            if (index % 5 == 0) {
                                return labels.get(index);
                            }
                        }
                        return "";
                    } catch (Exception e) {
//                        e.printStackTrace();
                        return "";
                    }
                }
            });
            lineChart30Days.invalidate();
        } catch (Exception e) {
//            e.printStackTrace();
        }
    }

    private void updateStatisticsText(StatisticsResponse response) {
        try {
            if (response == null) {
                // Generate random statistics for demo
                Random random = new Random();

                int total7Days = random.nextInt(150) + 50; // 50-200 cards
                int total30Days = random.nextInt(800) + 400; // 400-1200 cards
                double average7Days = total7Days / 7.0;
                double average30Days = total30Days / 30.0;

                if (tvTotal7Days != null) {
                    tvTotal7Days.setText(String.format(Locale.getDefault(), "%d", total7Days));
                }
                if (tvTotal30Days != null) {
                    tvTotal30Days.setText(String.format(Locale.getDefault(), "%d", total30Days));
                }
                if (tvAverage7Days != null) {
                    tvAverage7Days.setText(String.format(Locale.getDefault(), "%.1f", average7Days));
                }
                if (tvAverage30Days != null) {
                    tvAverage30Days.setText(String.format(Locale.getDefault(), "%.1f", average30Days));
                }
                return;
            }

            // Cập nhật dữ liệu 7 ngày
            if (response.getLast7Days() != null) {
                if (tvTotal7Days != null) {
                    tvTotal7Days.setText(String.format(Locale.getDefault(), "%d", response.getLast7Days().getTotal()));
                }
                if (tvAverage7Days != null) {
                    tvAverage7Days.setText(String.format(Locale.getDefault(), "%.1f", response.getLast7Days().getAverage()));
                }
            }

            // Cập nhật dữ liệu 30 ngày
            if (response.getLast30Days() != null) {
                if (tvTotal30Days != null) {
                    tvTotal30Days.setText(String.format(Locale.getDefault(), "%d", response.getLast30Days().getTotal()));
                }
                if (tvAverage30Days != null) {
                    tvAverage30Days.setText(String.format(Locale.getDefault(), "%.1f", response.getLast30Days().getAverage()));
                }
            }
        } catch (Exception e) {
//            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            // Reload statistics when fragment becomes visible again
            if (viewModel != null) {
                loadStatistics();
            }
        } catch (Exception e) {
//            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Cleanup nếu cần
    }
}