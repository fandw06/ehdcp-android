package com.dawei.assist_ble;

import android.graphics.Color;

import com.androidplot.util.Redrawer;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.StepMode;
import com.androidplot.xy.XYPlot;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Dawei on 2/8/2017.
 */
public class VolPlot {

    private DisplayActivity hostActivity;
    private XYPlot volPlot;
    private List<Number> volData= new LinkedList<>();
    private SimpleXYSeries volSeries;
    public Redrawer redrawer;
    private static final int VOL_RANGE = 200;
    private static final int VOL_PLOT_FREQ = 20;

    public VolPlot(DisplayActivity hostActivity) {
        this.hostActivity = hostActivity;
        initializePlot();
        redrawer = new Redrawer(volPlot, VOL_PLOT_FREQ, true);
    }

    public void updateData(byte[] value) {
        if (value.length != 2)
            return;
        double val = calibrateVol(value);
        updateDataSeries(volSeries, val, VOL_RANGE);
    }

    private void updateDataSeries(SimpleXYSeries series, double data, int range){
        if (series.size() == range) {
            series.removeFirst();
        }
        series.addLast(range, data);
    }

    private void initializePlot() {
        volPlot = (XYPlot) this.hostActivity.findViewById(R.id.plot_vol);
        volPlot.setBackgroundColor(Color.WHITE);

        volSeries = new SimpleXYSeries(
                volData,
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY,
                "ecg");
        LineAndPointFormatter volFormat = new LineAndPointFormatter(this.hostActivity, R.xml.vol_line_point_formatter);

        volPlot.addSeries(volSeries, volFormat);
        volPlot.getGraph().setDrawMarkersEnabled(true);
        volPlot.setDomainBoundaries(0, VOL_RANGE, BoundaryMode.FIXED);
        volPlot.setDomainStep(StepMode.INCREMENT_BY_VAL, 50);
        volPlot.setRangeBoundaries(0, 3.0, BoundaryMode.FIXED);
        volPlot.setRangeStep(StepMode.INCREMENT_BY_VAL, 0.5);
        volPlot.setPlotMargins(0, 0, 0, 0);
    }

    private double calibrateVol(byte data[]) {
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
