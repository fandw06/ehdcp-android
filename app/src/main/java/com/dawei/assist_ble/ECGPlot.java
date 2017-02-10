package com.dawei.assist_ble;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;

import com.androidplot.util.Redrawer;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PointLabelFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.StepMode;
import com.androidplot.xy.XYPlot;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Dawei on 2/8/2017.
 */
public class ECGPlot {

    private DisplayActivity hostActivity;
    private XYPlot ecgPlot;
    private List<Number> ecgData= new LinkedList<>();
    private SimpleXYSeries ecgSeries;
    public Redrawer redrawer;
    private static final int ECG_RANGE = 400;
    private static final int ECG_PLOT_FREQ = 20;

    public ECGPlot(DisplayActivity hostActivity) {
        this.hostActivity = hostActivity;
        initializePlot();
        redrawer = new Redrawer(ecgPlot, ECG_PLOT_FREQ, true);
    }

    public void updateData(byte[] value) {
        if (value.length != 2)
            return;
        double val = calibrateECG(value);
        updateDataSeries(ecgSeries, val, ECG_RANGE);
    }

    private void updateDataSeries(SimpleXYSeries series, double data, int range){
        if (series.size() == range) {
            series.removeFirst();
        }
        series.addLast(range, data);
    }

    private void initializePlot() {
        ecgPlot = (XYPlot) this.hostActivity.findViewById(R.id.plot_ecg);
        ecgPlot.setBackgroundColor(Color.WHITE);

        ecgSeries = new SimpleXYSeries(
                ecgData,
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY,
                "ecg");
        LineAndPointFormatter ecgFormat = new LineAndPointFormatter(this.hostActivity, R.xml.ecg_line_point_formatter);

        ecgPlot.addSeries(ecgSeries, ecgFormat);
        ecgPlot.getGraph().setDrawMarkersEnabled(true);
        ecgPlot.setDomainBoundaries(0, ECG_RANGE, BoundaryMode.FIXED);
        ecgPlot.setDomainStep(StepMode.INCREMENT_BY_VAL, 50);
        ecgPlot.setRangeBoundaries(0, 3.0, BoundaryMode.FIXED);
        ecgPlot.setRangeStep(StepMode.INCREMENT_BY_VAL, 0.5);
        ecgPlot.setPlotMargins(0, 0, 0, 0);
    }

    private double calibrateECG(byte data[]) {
        int val = 0;
        if (data[0] < 0)
            val += 256*(data[0]+256);
        else
            val += 256*data[0];
        if (data[1] < 0)
            val += data[1]+256;
        else
            val += data[1];
        return (double)val * 3.0/1024.0;
    }
}
