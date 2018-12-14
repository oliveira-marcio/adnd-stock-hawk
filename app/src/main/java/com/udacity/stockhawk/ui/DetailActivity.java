package com.udacity.stockhawk.ui;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.IMarker;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.MPPointF;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.udacity.stockhawk.R.id.chart;

public class DetailActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>,
        OnChartValueSelectedListener {

    @SuppressWarnings("WeakerAccess")
    @BindView(chart)
    public LineChart mLineChart;

    private static final int QUOTE_LOADER_ID = 1;
    private Uri mUri;
    private boolean mIsRTL;

    private static final String[] MAIN_PROJECTION = {
            Contract.Quote.COLUMN_SYMBOL,
            Contract.Quote.COLUMN_HISTORY
    };

    // Armazena as datas como strings para serem usadas como labels do gráfico
    private final List<String> mLabels = new ArrayList<String>();

    // Variáveis necessárias para compor o Content Description do gráfico
    private String mFirstDate;
    private String mLastDate;
    private String mStockSymbol;

    // Guarda a seleção no gráfico feita pelo usuário.
    private Highlight mHighLight;
    private final String HIGHLIGHT_SAVE_X = "highlight_x";
    private final String HIGHLIGHT_SAVE_Y = "highlight_y";

    private final String REGISTER_SEPARATOR = "\n";
    private final String VALUE_SEPARATOR = ",";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        mUri = getIntent().getData();
        if (mUri == null) finish();

        if (savedInstanceState != null) {
            float posX = savedInstanceState.getFloat(HIGHLIGHT_SAVE_X, -1f);
            float posY = savedInstanceState.getFloat(HIGHLIGHT_SAVE_Y, -1f);
            if (posX > -1f || posY > -1f) {
                mHighLight = new Highlight(posX, posY, 0);
            }
        }

        mIsRTL = getResources().getBoolean(R.bool.is_right_to_left);

        getSupportLoaderManager().initLoader(QUOTE_LOADER_ID, null, this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mHighLight != null) {
            outState.putFloat(HIGHLIGHT_SAVE_X, mHighLight.getX());
            outState.putFloat(HIGHLIGHT_SAVE_Y, mHighLight.getY());
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                mUri,
                MAIN_PROJECTION,
                null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!plotGraph(data)) {
            Toast.makeText(this, getString(R.string.toast_plot_data_invalid),
                    Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private String getReadableDateString(long timeInMillis) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMillis);
        return formatter.format(calendar.getTime());
    }

    private String getFormattedPrice(double price) {
        DecimalFormat formatter = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        return formatter.format(price);
    }

    private boolean plotGraph(Cursor data) {
        if (data == null || !data.moveToFirst()) return false;

        mStockSymbol = data.getString(0);
        String historyString = data.getString(1);

        String[] historyLines;
        if (historyString.contains(REGISTER_SEPARATOR)) {
            historyLines = historyString.split(REGISTER_SEPARATOR);
        } else {
            historyLines = new String[]{historyString};
        }

        List<Entry> entries = new ArrayList<Entry>();

        // Em caso de RTL, o DataSet do gráfico é criado com as datas mais recentes primeiro, com
        // isso o gráfico será plotado de forma invertida.
        if (mIsRTL) {
            for (int x = 0; x < historyLines.length; x++) {
                String line = historyLines[x];
                if (!line.contains(VALUE_SEPARATOR)) return false;

                String[] lineArray = line.split(VALUE_SEPARATOR);

                float date = (float) x;
                float price = Float.parseFloat(lineArray[1]);
                mLabels.add(getReadableDateString(Long.parseLong(lineArray[0])));

                entries.add(new Entry(date, price));
            }

            mFirstDate = mLabels.get(mLabels.size() - 1);
            mLastDate = mLabels.get(0);
        } else {
            int y = 0;
            for (int x = historyLines.length - 1; x >= 0; x--) {
                String line = historyLines[x];
                if (!line.contains(VALUE_SEPARATOR)) return false;

                String[] lineArray = line.split(VALUE_SEPARATOR);

                float date = (float) y++;
                float price = Float.parseFloat(lineArray[1]);
                mLabels.add(getReadableDateString(Long.parseLong(lineArray[0])));

                entries.add(new Entry(date, price));
            }

            mFirstDate = mLabels.get(0);
            mLastDate = mLabels.get(mLabels.size() - 1);
        }

        LineDataSet dataSet = new LineDataSet(entries, mStockSymbol);
        dataSet.setAxisDependency(mIsRTL ? YAxis.AxisDependency.RIGHT : YAxis.AxisDependency.LEFT);
        dataSet.setDrawValues(false);

        // Formatador para o eixo horizontal (datas)
        IAxisValueFormatter xFormatter = new IAxisValueFormatter() {

            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return mLabels.get((int) value);
            }
        };

        XAxis xAxis = mLineChart.getXAxis();
        xAxis.setTextColor(getResources().getColor(R.color.graph_label));
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(xFormatter);

        // Formatador para o eixo vertical (preço)
        IAxisValueFormatter yFormatter = new IAxisValueFormatter() {

            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return getFormattedPrice(value);
            }
        };

        YAxis leftAxis = mLineChart.getAxisLeft();
        leftAxis.setTextColor(getResources().getColor(R.color.graph_label));
        leftAxis.setValueFormatter(yFormatter);

        YAxis rightAxis = mLineChart.getAxisRight();
        rightAxis.setTextColor(getResources().getColor(R.color.graph_label));
        rightAxis.setValueFormatter(yFormatter);

        Legend legend = mLineChart.getLegend();
        legend.setTextColor(getResources().getColor(R.color.graph_label));

        LineData lineData = new LineData(dataSet);
        mLineChart.setData(lineData);

        mLineChart.setTouchEnabled(true);

        IMarker markerView = new CustomMarkerView(this, R.layout.marker_view);
        mLineChart.setMarker(markerView);

        mLineChart.setOnChartValueSelectedListener(this);
        mLineChart.setPinchZoom(true);
        mLineChart.setDescription(null);
        mLineChart.invalidate();

        // Cria o Content Description para o gráfico quando não há valores selecionados.
        String chartDescription = getString(R.string.a11y_stock_graph1, mStockSymbol) +
                getString(R.string.a11y_stock_graph2, mFirstDate) +
                getString(R.string.a11y_stock_graph3, mLastDate);

        mLineChart.setContentDescription(chartDescription);

        if (mHighLight != null) mLineChart.highlightValue(mHighLight, true);

        return true;
    }

    // Altera o Content Description do gráfico para os valores do ponto selecionado.
    @Override
    public void onValueSelected(Entry e, Highlight h) {
        mHighLight = h;
        String chartDescription = getString(R.string.a11y_stock_graph_marker,
                getFormattedPrice(e.getY())) + mLabels.get((int) e.getX());
        mLineChart.setContentDescription(chartDescription);
    }

    // Altera o Content Description do gráfico para o padrão, quando não há valores selecionados.
    @Override
    public void onNothingSelected() {
        mHighLight = null;
        String chartDescription = getString(R.string.a11y_stock_graph1, mStockSymbol) +
                getString(R.string.a11y_stock_graph2, mFirstDate) +
                getString(R.string.a11y_stock_graph3, mLastDate);

        mLineChart.setContentDescription(chartDescription);
    }

    // Classe necessária para contruir e posicionar o pop-up de um ponto selecionado no gráfico
    private class CustomMarkerView extends MarkerView {

        private TextView tvDate;
        private TextView tvPrice;

        public CustomMarkerView(Context context, int layoutResource) {
            super(context, layoutResource);
            tvDate = (TextView) findViewById(R.id.tvDate);
            tvPrice = (TextView) findViewById(R.id.tvPrice);
        }

        @Override
        public void refreshContent(Entry e, Highlight highlight) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.gravity = mIsRTL ? Gravity.RIGHT : Gravity.LEFT;

            tvDate.setText(mLabels.get((int) e.getX()));
            tvDate.setLayoutParams(params);
            tvPrice.setText(getFormattedPrice(e.getY()));
            tvPrice.setLayoutParams(params);
            super.refreshContent(e, highlight);
        }

        private MPPointF mOffset;

        @Override
        public MPPointF getOffset() {

            if (mOffset == null) {
                // Centraliza o pop-up horizontalmente e verticalmente
                mOffset = new MPPointF(-(getWidth() / 2), -getHeight());
            }

            return mOffset;
        }
    }
}
