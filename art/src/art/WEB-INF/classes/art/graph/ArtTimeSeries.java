/*
 * Copyright (C)   Enrico Liboni  - enrico@computer.org
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the LGPL License as published by
 *   the Free Software Foundation;
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. *  
 */
package art.graph;

import art.utils.ArtQueryParam;
import art.utils.DrilldownQuery;
import de.laures.cewolf.ChartPostProcessor;
import de.laures.cewolf.DatasetProducer;
import de.laures.cewolf.links.XYItemLinkGenerator;
import de.laures.cewolf.tooltips.XYToolTipGenerator;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import org.apache.commons.beanutils.RowSetDynaClass;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
<b>ArtTimeSeries</b> is used to chart lines over a timeframe (TIMESTAMP sql type).
<br>
See <i>artGraph</i> interface API for description and usage example.

The x-axis is the time while the y-axis stores the value(s). <br>
<i>artTime</i> supports multiple series and PostProcessor.
<br>
There are two supported <i> resultSet</i>
<br><b>Static Series</b>
<ol>
<li>the first column must be a TIMESTAMP datatype.
</li>
<li>(optional) if setUseHyperLinks(true) is used, 
the second column must be a String (an hyperlink) named "LINK"
</li>
<li>following colums must be numeric, the column names will appear as the series names.
</li>
</ol>
<i>ResultSet  Example:</i><br>
<code>select TIMESTAMP [, HyperLink] , NUMBER1 [, NUMBER2 ...] from ...</code>
<br><b>Dynamic Series</b>
<ol>
<li>the first column must be a Date datatype.
</li>
<li>(optional) if setUseHyperLinks(true) is used, 
the second column must be a String (an hyperlink) names "LINK"
</li>
<li>the following colum is the series name
</li>
<li>the last column the series name value
</li>
</ol>
<i>ResultSet  Example:</i><br>
<code>select TIMESTAMP [, HyperLink] , SERIES, NUMBER1 from ...</code>
<br>
<i>Note:</i><br>
<ul>
<li>the <i>setSeriesName</i> method does nothing as the series labels
are set to the column names
</li>
</ul>

 * @author Enrico Liboni
 * @author Timothy Anyona
 */
public class ArtTimeSeries implements ArtGraph, DatasetProducer, XYItemLinkGenerator, XYToolTipGenerator, ChartPostProcessor, Serializable {
    //classes implementing chartpostprocessor need to be serializable to use cewolf 1.1+

    private static final long serialVersionUID = 1L;
    
    final static Logger logger = LoggerFactory.getLogger(ArtTimeSeries.class);
    
    String title = "Title";
    String xAxisLabel = "x Label";
    String yAxisLabel = "y Label";
    String seriesName = "Not Used";
    ArrayList<String> hyperLinks;
    int height = 300;
    int width = 500;
    String bgColor = "#FFFFFF";
    boolean useHyperLinks = false;
    boolean hasDrilldown = false;
    HashMap<String, String> drilldownLinks;
    boolean hasTooltips = true;
    String openDrilldownInNewWindow;
    TimeSeriesCollection dataset;
	boolean showGraphData=false;
	RowSetDynaClass graphData = null; //store graph data in disconnected, serializable object
	Map<Integer,ArtQueryParam> displayParameters=null; //to enable display of graph parameters in pdf output
    

    /**
     * 
     */
    public ArtTimeSeries() {
    }
	
	@Override
	public void setQueryType(int queryType) {
		//not used
	}
	
	@Override
	public void setDisplayParameters(Map<Integer,ArtQueryParam> value){
		displayParameters=value;
	}
	
	@Override
	public Map<Integer,ArtQueryParam> getDisplayParameters(){
		return displayParameters;
	}
	
	@Override
	public RowSetDynaClass getGraphData(){
		return graphData;
	}
	
	@Override
	public void setShowGraphData(boolean value){
		showGraphData=value;
	}
	
	@Override
	public boolean isShowGraphData(){
		return showGraphData;
	}
        
    @Override
    public String getOpenDrilldownInNewWindow() {
        return openDrilldownInNewWindow;
    }

    @Override
    public boolean getHasTooltips() {
        return hasTooltips;
    }

    @Override
    public boolean getHasDrilldown() {
        return hasDrilldown;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setXAxisLabel(String xlabel) {
        this.xAxisLabel = xlabel;
    }

    @Override
    public String getXAxisLabel() {
        return xAxisLabel;
    }

    @Override
    public void setYAxisLabel(String ylabel) {
        this.yAxisLabel = ylabel;
    }

    @Override
    public String getYAxisLabel() {
        return yAxisLabel;
    }

    @Override
    public void setSeriesName(String seriesName) {
        this.seriesName = seriesName;
    }

    @Override
    public void setWidth(int width) {
        this.width = width;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void setBgColor(String bgColor) {
        this.bgColor = bgColor;
    }

    @Override
    public String getBgColor() {
        return bgColor;
    }

    @Override
    public void setUseHyperLinks(boolean b) {
        this.useHyperLinks = b;
    }

    @Override
    public boolean getUseHyperLinks() {
        return useHyperLinks;
    }

    //overload used by exportgraph class. no drill down for scheduled charts
    @Override
    public void prepareDataset(ResultSet rs) throws SQLException {
        prepareDataset(rs, null, null, null);
    }

    //prepare graph data structures with query results
    @Override
    public void prepareDataset(ResultSet rs, Map<Integer, DrilldownQuery> drilldownQueries, Map<String, String> inlineParams, Map<String, String[]> multiParams) throws SQLException {
        int hop = 0;
        if (useHyperLinks) {
            hyperLinks = new ArrayList<String>(10);
            hop = 1;
        }

        /* discover which resultset type do we have 
        if areSeriesOnColumn is true the resultset is expected to have as many value columns
        as many series to plot, otherwise the series name is expected to be 
        in the 2nd (+hop) column (the latter allows dynamic series) 
         */
        boolean areSeriesOnColumn = false;
        ResultSetMetaData rsmd = rs.getMetaData();
        switch (rsmd.getColumnType(2 + hop)) {
            case Types.NUMERIC:
            case Types.DECIMAL:
            case Types.FLOAT:
            case Types.REAL:
            case Types.DOUBLE:
            case Types.INTEGER:
            case Types.TINYINT:
            case Types.SMALLINT:
            case Types.BIGINT:
                areSeriesOnColumn = true;
                break;
        }

        //add support for drill down queries
        DrilldownQuery drilldown = null;
        if (drilldownQueries != null && drilldownQueries.size() > 0) {
            hasDrilldown = true;
            drilldownLinks = new HashMap<String, String>();

            //only use the first drill down query
            Iterator it = drilldownQueries.entrySet().iterator();
            if (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                drilldown = (DrilldownQuery) entry.getValue();

                openDrilldownInNewWindow = drilldown.getOpenInNewWindow();
            }
        }

        String drilldownUrl;
        String outputFormat;
        int drilldownQueryId;
        List<ArtQueryParam> drilldownParams;
        String paramString;

        String category;
        double value;
        String key;

        //store parameter names so that parent parameters with the same name as in the drilldown query are omitted
        HashMap<String, String> params = new HashMap<String, String>();
        String paramLabel;
        String paramValue;

        if (areSeriesOnColumn) {
            int series = rsmd.getColumnCount() - 1 - hop;
            String[] seriesNames = new String[series];
            TimeSeries ts[] = new TimeSeries[series];
            for (int i = 0; i < series; i++) {
                seriesNames[i] = rsmd.getColumnLabel(i + 2 + hop);
                ts[i] = new TimeSeries(seriesNames[i], new Millisecond().getClass());
            }

            while (rs.next()) {
                category = rs.getString(1);
                Timestamp cts = rs.getTimestamp(1);

                for (series = 0; series < seriesNames.length; series++) {
                    value = rs.getDouble(hop + 2 + series);
                    ts[series].add(new Millisecond(new java.util.Date(cts.getTime())), value);

                    //set drill down hyperlinks
                    if (drilldown != null) {
                        drilldownQueryId = drilldown.getDrilldownQueryId();
                        outputFormat = drilldown.getOutputFormat();
                        if (outputFormat == null || outputFormat.toUpperCase().equals("ALL")) {
                            drilldownUrl = "showParams.jsp?queryId=" + drilldownQueryId;
                        } else {
                            drilldownUrl = "ExecuteQuery?queryId=" + drilldownQueryId + "&viewMode=" + outputFormat;
                        }

                        drilldownParams = drilldown.getDrilldownParams();
                        if (drilldownParams != null) {
                            for(ArtQueryParam param : drilldownParams) {
                                //drill down on col 1 = drill on data value. drill down on col 2 = category (timestamp). drill down on col 3 = series name
                                paramLabel = param.getParamLabel();
                                paramString = "&P_" + paramLabel + "=";
                                if (param.getDrilldownColumn() == 1) {
                                    paramString = paramString + value;
                                } else if (param.getDrilldownColumn() == 2) {
                                    paramValue = category;
                                    try {
                                        paramValue = URLEncoder.encode(paramValue, "UTF-8");
                                    } catch (UnsupportedEncodingException e) {
                                        logger.warn("UTF-8 encoding not supported", e);
                                    }
                                    paramString = paramString + paramValue;
                                } else {
                                    paramValue = seriesNames[series];
                                    try {
                                        paramValue = URLEncoder.encode(paramValue, "UTF-8");
                                    } catch (UnsupportedEncodingException e) {
                                        logger.warn("UTF-8 encoding not supported", e);
                                    }
                                    paramString = paramString + paramValue;
                                }
                                drilldownUrl = drilldownUrl + paramString;
                                params.put(paramLabel, paramLabel);
                            }
                        }

                        //add parameters from parent query										
                        if (inlineParams != null) {
                            for (Map.Entry<String, String> entry : inlineParams.entrySet()) {
                                paramLabel = entry.getKey();
                                paramValue = entry.getValue();
                                //add parameter only if one with a similar name doesn't already exist in the drill down parameters
                                if (!params.containsKey(paramLabel)) {
                                    try {
                                        paramValue = URLEncoder.encode(paramValue, "UTF-8");
                                    } catch (UnsupportedEncodingException e) {
                                        logger.warn("UTF-8 encoding not supported", e);
                                    }
                                    paramString = "&P_" + paramLabel + "=" + paramValue;
                                    drilldownUrl = drilldownUrl + paramString;
                                }
                            }
                        }

                        if (multiParams != null) {
                            String[] paramValues;
                            for (Map.Entry<String, String[]> entry : multiParams.entrySet()) {
                                paramLabel = entry.getKey();
                                paramValues = entry.getValue();
                                for (String param : paramValues) {
                                    try {
                                        param = URLEncoder.encode(param, "UTF-8");
                                    } catch (UnsupportedEncodingException e) {
                                        logger.warn("UTF-8 encoding not supported", e);
                                    }
                                    paramString = "&M_" + paramLabel + "=" + param;
                                    drilldownUrl = drilldownUrl + paramString;
                                }
                            }
                        }

                        //use series, data and timestamp to identify url in hashmap. to ensure correct link will be returned in generatelink. item index available in generatelink method can't be deteremined beforehand	
                        key = String.valueOf(series) + String.valueOf(value) + cts.getTime();
                        drilldownLinks.put(key, drilldownUrl);
                    }
                }

                if (useHyperLinks) {
                    hyperLinks.add(rs.getString("LINK"));
                }
            }

            dataset = new TimeSeriesCollection();
            for (series = 0; series < seriesNames.length; series++) {
                dataset.addSeries(ts[series]);
            }

        } else {
            int series = 0; // number of available series is dynamic
            int seriesId;
            String tmpSeriesName;

            TimeSeries ts[] = new TimeSeries[64]; // max of 64 series,
            // this number is reasonable and
            // we do not want to scroll all the result set...

            HashMap<String, Integer> hm = new HashMap<String, Integer>(); // stores series name and id

            while (rs.next()) {
                tmpSeriesName = rs.getString(2 + hop); // series name is in the 2nd column
                value = rs.getDouble(hop + 3);
                category = rs.getString(1);

                // has this series already appeared?
                if (hm.containsKey(tmpSeriesName)) { // item already available					
                    seriesId = (hm.get(tmpSeriesName)).intValue();
                } else { // new series 
                    seriesId = series;
                    hm.put(tmpSeriesName, Integer.valueOf(seriesId)); // map series name to array id
                    ts[seriesId] = new TimeSeries(tmpSeriesName, new Millisecond().getClass()); // initialize new timeseries
                    //insert value
                    series++;
                }

                // insert value
                Timestamp cts = rs.getTimestamp(1);
                ts[seriesId].add(new Millisecond(new java.util.Date(cts.getTime())), value);
                if (useHyperLinks) {
                    hyperLinks.add(rs.getString("LINK"));
                }

                //set drill down hyperlinks
                if (drilldown != null) {
                    drilldownQueryId = drilldown.getDrilldownQueryId();
                    outputFormat = drilldown.getOutputFormat();
                    if (outputFormat == null || outputFormat.toUpperCase().equals("ALL")) {
                        drilldownUrl = "showParams.jsp?queryId=" + drilldownQueryId;
                    } else {
                        drilldownUrl = "ExecuteQuery?queryId=" + drilldownQueryId + "&viewMode=" + outputFormat;
                    }

                    drilldownParams = drilldown.getDrilldownParams();
                    if (drilldownParams != null) {
                        for(ArtQueryParam param : drilldownParams) {
                            //drill down on col 1 = drill on data value. drill down on col 2 = category (timestamp). drill down on col 3 = series name
                            paramLabel = param.getParamLabel();
                            paramString = "&P_" + paramLabel + "=";
                            if (param.getDrilldownColumn() == 1) {
                                paramString = paramString + value;
                            } else if (param.getDrilldownColumn() == 2) {
                                paramValue = category;
                                try {
                                    paramValue = URLEncoder.encode(paramValue, "UTF-8");
                                } catch (UnsupportedEncodingException e) {
                                    logger.warn("UTF-8 encoding not supported", e);
                                }
                                paramString = paramString + paramValue;
                            } else {
                                paramValue = tmpSeriesName;
                                try {
                                    paramValue = URLEncoder.encode(paramValue, "UTF-8");
                                } catch (UnsupportedEncodingException e) {
                                    logger.warn("UTF-8 encoding not supported", e);
                                }
                                paramString = paramString + paramValue;
                            }
                            drilldownUrl = drilldownUrl + paramString;
                            params.put(paramLabel, paramLabel);
                        }
                    }

                    //add parameters from parent query										
                    if (inlineParams != null) {
                        for (Map.Entry<String, String> entry : inlineParams.entrySet()) {
                            paramLabel = entry.getKey();
                            paramValue = entry.getValue();
                            //add parameter only if one with a similar name doesn't already exist in the drill down parameters
                            if (!params.containsKey(paramLabel)) {
                                try {
                                    paramValue = URLEncoder.encode(paramValue, "UTF-8");
                                } catch (UnsupportedEncodingException e) {
                                    logger.warn("UTF-8 encoding not supported", e);
                                }
                                paramString = "&P_" + paramLabel + "=" + paramValue;
                                drilldownUrl = drilldownUrl + paramString;
                            }
                        }
                    }

                    if (multiParams != null) {
                        String[] paramValues;
                        for (Map.Entry<String, String[]> entry : multiParams.entrySet()) {
                            paramLabel = entry.getKey();
                            paramValues = entry.getValue();
                            for (String param : paramValues) {
                                try {
                                    param = URLEncoder.encode(param, "UTF-8");
                                } catch (UnsupportedEncodingException e) {
                                    logger.warn("UTF-8 encoding not supported", e);
                                }
                                paramString = "&M_" + paramLabel + "=" + param;
                                drilldownUrl = drilldownUrl + paramString;
                            }
                        }
                    }

                    //use series, data and timestamp to identify url in hashmap. to ensure correct link will be returned in generatelink. item index available in generatelink method can't be deteremined beforehand	
                    key = String.valueOf(seriesId) + String.valueOf(value) + cts.getTime();
                    drilldownLinks.put(key, drilldownUrl);
                }
            }

            dataset = new TimeSeriesCollection();
            for (int i = 0; i < series; i++) {
                dataset.addSeries(ts[i]);
            }
        }
		
		//store data for potential use in pdf output
		if (showGraphData) {
			int rsType = rs.getType();
			if (rsType == ResultSet.TYPE_SCROLL_INSENSITIVE || rsType == ResultSet.TYPE_SCROLL_SENSITIVE) {
				rs.beforeFirst();
			}
			graphData = new RowSetDynaClass(rs, false, true);
		} else {
			graphData=null;
		}
    }

    /**
     * 
     * @param params
     * @return dataset to be used for rendering the chart
     */
    @Override
    public Object produceDataset(Map params) {
        return dataset;
    }

    /**
     * 
     * @return identifier for this producer class
     */
    @Override
    public String getProducerId() {
        return "TimeDataProducer";
    }

    /**
     * 
     * @param params
     * @param since
     * @return <code>true</code> if the data for the chart has expired
     */
    @Override
    public boolean hasExpired(Map params, java.util.Date since) {
        return true;
    }

    /**
     * 
     * @param data
     * @param series
     * @param item
     * @return url of clickable link
     */
    @Override
    public String generateLink(Object data, int series, int item) {
        String link = "";
        long timestamp;
        XYDataset tmpDataset;
        double yValue;
        String key;

        if (useHyperLinks) {
            link = hyperLinks.get(item);
        } else if (hasDrilldown) {
            tmpDataset = (XYDataset) data;
            yValue = tmpDataset.getYValue(series, item);
            timestamp = (long) tmpDataset.getXValue(series, item);

            key = String.valueOf(series) + String.valueOf(yValue) + timestamp;
            link = drilldownLinks.get(key);
        }

        return link;
    }

    /**
     * 
     * @param dataset
     * @param series
     * @param index
     * @return tooltip text
     */
    @Override
    public String generateToolTip(XYDataset dataset, int series, int index) {
        //display formatted values		

        //format y value
        double yValue;
        String formattedYValue;
        DecimalFormat valueFormatter;

        yValue = dataset.getYValue(series, index);

        //format value. use numberformat factory method to set formatting according to the default locale	   		
        NumberFormat nf = NumberFormat.getInstance();
        valueFormatter = (DecimalFormat) nf;

        formattedYValue = valueFormatter.format(yValue);

        //format date value
        SimpleDateFormat dateFormatter;
        String formattedDate;
        java.util.Date dateValue;

        dateValue = new java.util.Date((long) dataset.getXValue(series, index));

        dateFormatter = new SimpleDateFormat("MMM-dd-yyyy");
        formattedDate = dateFormatter.format(dateValue);

        //return final tooltip text	   
        return formattedYValue + ", " + formattedDate;
    }

    /**
     * 
     * @param chart
     * @param params
     */
    @Override
    public void processChart(Object chart, Map params) {
        XYPlot plot = (XYPlot) ((JFreeChart) chart).getPlot();

        //set y axis range if required
		if (params.get("from") != null && params.get("to") != null) {
			Double from = (Double) params.get("from");
			Double to = (Double) params.get("to");
			NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
			rangeAxis.setRange(from, to);
		}

        //set grid lines to light grey so that they are visible with a default plot background colour of white
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);

        //allow highlighting of data points
        boolean showPoints = (Boolean) params.get("showPoints");
        if (showPoints) {
            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
            renderer.setBaseShapesVisible(true);
        }

        // Output to file if required     	  
        String outputToFile = (String) params.get("outputToFile");
        String fileName = (String) params.get("fullFileName");
        if (outputToFile.equals("pdf")) {
            PdfGraph.createPdf(chart, fileName, title, graphData, displayParameters);
        } else if (outputToFile.equals("png")) {
            //save chart as png file									            
            try {
                ChartUtilities.saveChartAsPNG(new File(fileName), (JFreeChart) chart, width, height);
            } catch (IOException e) {
                logger.error("Error",e);
            }
        }

    }
}
