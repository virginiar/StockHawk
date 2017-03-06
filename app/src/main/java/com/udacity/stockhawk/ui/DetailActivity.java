package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /* Tag for intent extra data*/
    public static final String EXTRA_SYMBOL = "EXTRA_SYMBOL";

    private static final int DETAIL_LOADER = 1;
    final private static int DATE = 0;
    final private static int STOCK = 1;
    @BindView(R.id.chart)
    LineChart lineChart;
    private String mSymbol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ButterKnife.bind(this);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_SYMBOL)) {
            mSymbol = intent.getStringExtra(EXTRA_SYMBOL);
            Timber.d("symbol: %s", mSymbol);
            getSupportLoaderManager().initLoader(DETAIL_LOADER, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                Contract.Quote.makeUriForStock(mSymbol),
                null,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Non Valid data available
        if (data == null || !data.moveToFirst()) {
            return;
        }
        String history = data.getString(Contract.Quote.POSITION_HISTORY);
        setData(history);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void setData(String history) {
        List<Entry> entries = new ArrayList<>();
        String[] historyArray = history.split("\n");
        final List<Long> dates = new ArrayList<>();
        int count = 0;
        // It is necessary to reverse the dates
        for (int i = historyArray.length - 1; i > 0; i--) {
            String[] value = historyArray[i].split(",");
            Long date = Long.valueOf(value[DATE]);
            dates.add(date);
            Float stock = Float.valueOf(value[STOCK]);
            // Add data in natural order
            entries.add(new Entry(count++, stock));
        }

        LineDataSet dataSet = new LineDataSet(entries, mSymbol);
        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        lineChart.invalidate();

        lineChart.getLegend().setEnabled(false);
        lineChart.setExtraOffsets(5f, 15f, 5f, 15f);
        //lineChart.getDescription().setEnabled(false);

        Description description = new Description();
        description.setText(getString(R.string.chart_description, mSymbol));
        lineChart.setDescription(description);
        lineChart.setContentDescription(getString(R.string.chart_description, mSymbol));
        description.setTextColor(Color.WHITE);
        description.setTextSize(15f);

        lineData.setValueTextColor(Color.WHITE);
        lineData.setValueTextSize(10f);
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setTextColor(Color.WHITE);
        xAxis.setTextSize(10f);
        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setTextSize(10f);
        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setTextColor(Color.WHITE);
        rightAxis.setTextSize(10f);


        xAxis.setValueFormatter(new IAxisValueFormatter() {
            private SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM", Locale.US);

            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return mFormat.format(dates.get((int) value));
            }
        });


    }
}
