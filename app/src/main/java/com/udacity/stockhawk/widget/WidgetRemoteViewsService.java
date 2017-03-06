package com.udacity.stockhawk.widget;


import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.ui.DetailActivity;

import timber.log.Timber;

public class WidgetRemoteViewsService extends RemoteViewsService {

    private static final String[] STOCK_COLUMNS = {
            Contract.Quote._ID,
            Contract.Quote.COLUMN_SYMBOL,
            Contract.Quote.COLUMN_ABSOLUTE_CHANGE
    };

    private static final int INDEX_ID = 0;
    private static final int INDEX_SYMBOL = 1;
    private static final int INDEX_ABS_CHANGE = 2;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {

            private Cursor cursor = null;

            @Override
            public void onCreate() {
            }

            @Override
            public void onDataSetChanged() {
                if (cursor != null) {
                    cursor.close();
                }

                final long identityToken = Binder.clearCallingIdentity();
                cursor = getContentResolver().
                        query(Contract.Quote.URI, STOCK_COLUMNS, null, null, null);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (cursor != null) {
                    cursor.close();
                    cursor = null;
                }
            }

            @Override
            public int getCount() {
                return cursor == null ? 0 : cursor.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        cursor == null ||
                        !cursor.moveToPosition(position)) {
                    return null;
                }

                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_list_item);

                String symbol = cursor.getString(INDEX_SYMBOL);
                Double abs = cursor.getDouble(INDEX_ABS_CHANGE);
                Timber.d("Symbol: %s %s", symbol, abs);

                // Add the data
                views.setTextViewText(R.id.widget_symbol, symbol);
                if (abs >= 0) {
                    views.setTextViewCompoundDrawablesRelative(R.id.widget_symbol, 0, 0, R.drawable.ic_arrow_up, 0);
                } else {
                    views.setTextViewCompoundDrawablesRelative(R.id.widget_symbol, 0, 0, R.drawable.ic_arrow_down, 0);
                }

                final Intent intent = new Intent();
                intent.putExtra(DetailActivity.EXTRA_SYMBOL, symbol);
                views.setOnClickFillInIntent(R.id.widget_list_item, intent);

                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (cursor.moveToPosition(position)) {
                    return cursor.getLong(INDEX_ID);
                }
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}


