package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /* Tag for intent extra data*/
    static final String EXTRA_SYMBOL = "EXTRA_SYMBOL";

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
            //errorView.setText(symbol);
            //errorView.setVisibility(View.VISIBLE);

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

        for (int i = 0; i < historyArray.length; i++) {
            String[] value = historyArray[i].split(",");
            Float stock = Float.valueOf(value[STOCK]);
            entries.add(new Entry(i, stock));
        }

        LineDataSet dataSet = new LineDataSet(entries, "label");
        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        lineChart.invalidate();
    }
}
