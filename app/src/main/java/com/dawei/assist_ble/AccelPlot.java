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
public class AccelPlot {

    private DisplayActivity hostActivity;
    private XYPlot accPlot;
    private List<Number> accXData= new LinkedList<>();
    private List<Number> accYData= new LinkedList<>();
    private List<Number> accZData= new LinkedList<>();
    private SimpleXYSeries accXSeries;
    private SimpleXYSeries accYSeries;
    private SimpleXYSeries accZSeries;
    public Redrawer redrawer;
    private static final int ACC_RANGE = 200;
    private static final int ACC_PLOT_FREQ = 20;

    public AccelPlot(DisplayActivity hostActivity) {
        this.hostActivity = hostActivity;
        initializePlot();
        redrawer = new Redrawer(accPlot, ACC_PLOT_FREQ, true);
    }

    private void initializePlot() {
        accPlot = (XYPlot) this.hostActivity.findViewById(R.id.plot_accel);
        accPlot.setBackgroundColor(Color.WHITE);

        // Create acceleration series
        accXSeries = new SimpleXYSeries(
                accXData,
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY,
                "x");
        LineAndPointFormatter accXFormat = new LineAndPointFormatter(this.hostActivity, R.xml.accel_x_line_point_formatter);

        accYSeries = new SimpleXYSeries(
                accYData,
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY,
                "y");
        LineAndPointFormatter accYFormat = new LineAndPointFormatter(this.hostActivity, R.xml.accel_y_line_point_formatter);

        accZSeries = new SimpleXYSeries(
                accZData,
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY,
                "z");
        LineAndPointFormatter accZFormat = new LineAndPointFormatter(this.hostActivity, R.xml.accel_z_line_point_formatter);

        accPlot.addSeries(accXSeries, accXFormat);
        accPlot.addSeries(accYSeries, accYFormat);
        accPlot.addSeries(accZSeries, accZFormat);

        accPlot.getGraph().setDrawMarkersEnabled(true);
        accPlot.setDomainBoundaries(0, ACC_RANGE, BoundaryMode.FIXED);
        accPlot.setDomainStep(StepMode.INCREMENT_BY_VAL, 50);
        accPlot.setRangeBoundaries(-2, 2, BoundaryMode.FIXED);
        accPlot.setRangeStep(StepMode.INCREMENT_BY_VAL, 1);
        accPlot.setPlotMargins(0, 0, 0, 0);
    }

    public void updateData(byte[] value) {
        if (value.length != 3)
            return;
        double val[] = calibrateAccelArray(value);
        updateDataSeries(accXSeries, val[0], ACC_RANGE);
        updateDataSeries(accYSeries, val[1], ACC_RANGE);
        updateDataSeries(accZSeries, val[2], ACC_RANGE);
    }

    private void updateDataSeries(SimpleXYSeries series, double data, int range){
        if (series.size() == range) {
            series.removeFirst();
        }
        series.addLast(range, data);
    }

    private double calibrateAccel(byte data) {
        // The range is -2g ~ 2g
        // Unit is g
        return (double)data * 2.0/128.0;
    }

    private double[] calibrateAccelArray(byte data[]) {
        double c[] = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            c[i] = calibrateAccel(data[i]);
        }
        return c;
    }
}
