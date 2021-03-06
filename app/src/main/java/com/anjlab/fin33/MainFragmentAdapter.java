package com.anjlab.fin33;

import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.anjlab.fin33.model.AppState;
import com.anjlab.fin33.model.Bank;
import com.anjlab.fin33.model.ExchangeRate;
import com.anjlab.fin33.model.GraphUpdateListener;
import com.anjlab.fin33.model.TimeSeries;
import com.anjlab.fin33.model.Value;
import com.anjlab.fin33.view.ExchangeRateView;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainFragmentAdapter extends RecyclerView.Adapter<MainFragmentAdapter.ViewHolder> implements GraphUpdateListener{

    private ExchangeRate.Currency[] currencies;
    private List<Bank> banks;
    View v;
    public MainFragmentAdapter(List<Bank> banks, ExchangeRate.Currency[] currencies) {
        this.banks = banks;
        this.currencies = currencies;
    }

    @Override
    public MainFragmentAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        v = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.card_view, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ExchangeRate.Currency currency = currencies[position];// ExchangeRate.Currency.USD;
        holder.mTextView.setText(getTitle(currency));
        Bank bank = new Bank();
        holder.ervBuy.setExchangeRate(Bank.findBestRate(banks, currency, ExchangeRate.Kind.BUY));
        ExchangeRate exchange = Bank.findBestRate(banks, currency, ExchangeRate.Kind.BUY);
        if (exchange != null) {
            bank = exchange.getBank();
            holder.textViewDateBuy.setText("" + exchange.getFormattedDate());
            holder.textView3.setText(bank.getName());
            holder.ervSell.setExchangeRate(Bank.findBestRate(banks, currency, ExchangeRate.Kind.SELL));
            exchange = Bank.findBestRate(banks, currency, ExchangeRate.Kind.SELL);
            holder.textViewDateSell.setText("" + exchange.getFormattedDate());
            bank = exchange.getBank();
            holder.textView4.setText(bank.getName());
        }
        AppState.getInstance().subscribeGraph(this);
        //onParseDone(AppState.getInstance().getGraph());
        drawGrapg(holder,AppState.getInstance().getGraph(), position);
    }

    public void drawGrapg(ViewHolder holder, Map<Integer, List<TimeSeries>> map, int position){
        List<Value> values;
        LineGraphSeries<DataPoint> series = null;
        PointsGraphSeries<DataPoint> seriesP = null;
        int i = 0;
        for (Map.Entry entry : map.entrySet()) {
            List<TimeSeries> timeSeriesList = new ArrayList<>();
            timeSeriesList = (List<TimeSeries>) entry.getValue();
            if (position == (int)entry.getKey()) {
                DataPoint[] data = new DataPoint[14];
                for (TimeSeries timeSeries : timeSeriesList) {
                    values = timeSeries.getValues();
                    String col;
                    for (Value value : values) {
                        data[i] = new DataPoint(value.getDate(), value.getValue().doubleValue());
                        i++;
                    }
                    i = 0;
                    series = new LineGraphSeries<DataPoint>(data);
                    series.setDrawDataPoints(true);
                    series.setDataPointsRadius(6);
                    series.setThickness(5);
                    holder.graph.addSeries(series);
                    series.setColor(timeSeries.getColor());
                    series.setTitle(timeSeries.getTitle());
                    holder.graph.getLegendRenderer().setVisible(true);
                    Color color = new Color();
                    int colorRGB = color.argb(255,124,124,124);
                    GridLabelRenderer gridLabel = holder.graph.getGridLabelRenderer();
                    gridLabel.setHorizontalAxisTitle("Дата");
                    gridLabel.setHorizontalAxisTitleColor(colorRGB);
                    gridLabel.setHorizontalAxisTitleTextSize(25f);
                    holder.graph.getGridLabelRenderer().setTextSize(20f);
                    holder.graph.getGridLabelRenderer().setHorizontalLabelsColor(colorRGB);
                    holder.graph.getGridLabelRenderer().setVerticalLabelsColor(colorRGB);
                    holder.graph.getGridLabelRenderer().setHumanRounding(false);
                    holder.graph.getViewport().setXAxisBoundsManual(true);
                    holder.graph.getGridLabelRenderer().setNumHorizontalLabels(data.length / 2);
                    holder.graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
                    holder.graph.getGridLabelRenderer().setLabelFormatter(
                            new DateAsXAxisLabelFormatter(v.getContext(), new SimpleDateFormat("dd.MM")));
                }
            }
        }
    }

    private String getTitle(ExchangeRate.Currency currency) {
        switch (currency) {
            case USD:
                return " USD " ;
            case EUR:
                return " EUR ";
            default:
                return "?";
        }
    }

    @Override
    public int getItemCount() {
        return currencies.length;
    }

    @Override
    public void onParseDone(Map<Integer, List<TimeSeries>> map) {
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextView;
        public TextView textView4;
        public TextView textView3;
        public ExchangeRateView ervBuy;
        public ExchangeRateView ervSell;
        private TextView textViewDateBuy;
        private TextView textViewDateSell;
        private GraphView graph;
        public ViewHolder(View v) {
            super(v);
            mTextView = (TextView) v.findViewById(R.id.currency);
            textView4 = (TextView) v.findViewById(R.id.textView4);
            textView3 = (TextView) v.findViewById(R.id.textView3);
            textViewDateBuy = (TextView) v.findViewById(R.id.textViewDateBuy);
            textViewDateSell = (TextView) v.findViewById(R.id.textViewDateSell);
            ervBuy = (ExchangeRateView) v.findViewById(R.id.ervBuy);
            ervSell = (ExchangeRateView) v.findViewById(R.id.ervSell);
            graph = (GraphView) v.findViewById(R.id.graph);

        }
    }
}