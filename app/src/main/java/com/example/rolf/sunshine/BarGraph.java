package com.example.rolf.sunshine;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.Collections;


public class BarGraph {
    int n;

    private XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
    private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
    private XYSeriesRenderer renderer = new XYSeriesRenderer();

    //Contructor with Parameters
    public BarGraph(int[] tempArray) {
        refreshBarGraph(tempArray);
    }

    public void refreshBarGraph(int[] tempArray) {
        int n = tempArray.length;
        //CategorySeries series1 = new CategorySeries("Binomialverteilung");
        //XYSeries series1 = new XYSeries("Binomialverteilung");
        TimeSeries series1 = new TimeSeries("Binomialverteilung");
        //endlich, so geht's und so stimmen die (k, B(n,k,p))-Paare
        for( int k = 0; k < n; k++) {
            series1.add(k,tempArray[k]);
        }

        dataset.clear();
        //dataset.addSeries(series1.toXYSeries());
        dataset.addSeries(series1);

        renderer.setDisplayChartValues(true);
        renderer.setChartValuesTextSize(20.0f);
        //renderer.setDisplayChartValuesDistance(150);

        renderer.setChartValuesSpacing((float) 0.5);

        //renderer.setColor(Color.rgb(200, 128, 0));
        renderer.setColor(Color.rgb(255, 200, 0));

        mRenderer.addSeriesRenderer(renderer);

        mRenderer.setBarSpacing(0.5);
        mRenderer.setXAxisMin(0);
        mRenderer.setXAxisMax(n);
        mRenderer.setXLabels(n);
        int max = 0;
        int min = 99;
        for (int i = 0; i < n; i++) {
            min = (tempArray[i] < min) ? tempArray[i] : min;
            max = (tempArray[i] > max) ? tempArray[i] : max;
        }
        mRenderer.setYAxisMin(min-2);
        mRenderer.setYAxisMax(max+2);

        mRenderer.setZoomEnabled(false, false);
        mRenderer.setPanEnabled(false, false);

        mRenderer.setApplyBackgroundColor(true);
        mRenderer.setBackgroundColor(Color.WHITE);
        mRenderer.setMarginsColor(Color.WHITE);

        mRenderer.setXLabelsColor(Color.BLACK);
        mRenderer.setYLabelsColor(0,Color.BLACK);
        mRenderer.setYLabelsAlign(Paint.Align.RIGHT);
        mRenderer.setYLabelsPadding(3.f);
        mRenderer.setAxesColor(Color.BLACK);

        mRenderer.setShowLegend(false);
        mRenderer.setLabelsTextSize(20.0f);

        mRenderer.setLegendTextSize(30f);
        mRenderer.setInScroll(true);
    }

    public GraphicalView getView(Context context){
          return ChartFactory.getBarChartView(context, dataset, mRenderer, BarChart.Type.DEFAULT);
    }
}
