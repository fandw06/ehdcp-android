package com.dawei.scdpm.plot;

import android.util.Log;

import com.androidplot.util.Redrawer;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.StepMode;
import com.androidplot.xy.XYPlot;
import com.dawei.scdpm.DisplayActivity;
import com.dawei.scdpm.calibrate.Calibrate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Dawei on 2/15/2017.
 */
public class DataPlot {

    private PlotConfig config;
    private DisplayActivity hostActivity;
    private XYPlot xyPlot;
    private List<Number> dataList[];
    private SimpleXYSeries dataSeries[];
    public Redrawer redrawer;

    public DataPlot(DisplayActivity hostActivity, PlotConfig config) {
        this.hostActivity = hostActivity;
        this.config = config;
        initializePlot();
        redrawer = new Redrawer(xyPlot, config.REDRAW_FREQ, true);
    }

    public void updateDataSeries(double val[]){
        int samples = val.length;
        if (samples % config.NUMBER_OF_SERIES != 0)
            return;
        int points = samples / config.NUMBER_OF_SERIES;
        for (int i = 0; i < points; i++) {
            for (int j = 0; j < config.NUMBER_OF_SERIES; j++) {
                double data = val[i * config.NUMBER_OF_SERIES + j];
                if (dataSeries[j].size() == config.DOMAIN_BOUNDARY[1])
                    dataSeries[j].removeFirst();
                dataSeries[j].addLast(config.DOMAIN_BOUNDARY[1], data);
            }
        }
    }

    public void clear() {
        xyPlot.clear();
    }

    private void initializePlot() {
        xyPlot = (XYPlot) this.hostActivity.findViewById(config.RES_ID);
        dataSeries = new SimpleXYSeries[config.NUMBER_OF_SERIES];
        dataList = new List[config.NUMBER_OF_SERIES];
        for (int i = 0; i< config.NUMBER_OF_SERIES; i++) {
            dataList[i] = new ArrayList<>();
            dataSeries[i] = new SimpleXYSeries(
                    dataList[i],
                    SimpleXYSeries.ArrayFormat.Y_VALS_ONLY,
                    config.NAME[i]);
            Log.d("DEBUG", String.valueOf(config.XML_ID[i]));
            LineAndPointFormatter format = new LineAndPointFormatter(this.hostActivity, config.XML_ID[i]);
            xyPlot.addSeries(dataSeries[i], format);
        }
        xyPlot.getGraph().setDrawMarkersEnabled(true);
        xyPlot.setDomainBoundaries(config.DOMAIN_BOUNDARY[0], config.DOMAIN_BOUNDARY[1], BoundaryMode.FIXED);
        xyPlot.setDomainStep(StepMode.INCREMENT_BY_VAL, config.DOMAIN_INC);
        xyPlot.setRangeBoundaries(config.RANGE_BOUNDARY[0], config.RANGE_BOUNDARY[1], BoundaryMode.FIXED);
        xyPlot.setRangeStep(StepMode.INCREMENT_BY_VAL, config.RANGE_INC);
        xyPlot.setPlotMargins(0, 0, 0, 0);
    }
}
