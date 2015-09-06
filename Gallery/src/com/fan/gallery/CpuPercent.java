package com.fan.gallery;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.chart.AbstractChart;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;

public class CpuPercent extends AbstractChart {
	private double[] arrValues;
	private double[] arrx;
	public CpuPercent(double[] arrValues,double[] arrx){
		this.arrValues = arrValues;
		this.arrx = arrx;
	}

	public String getName() {
		return "Cpu Percent";
	}

	public String getDesc() {
		return "The average temperature in 4 Greek islands (line chart)";
	}

	/**
	 * Executes the chart demo.
	 * 
	 * @param context
	 *            the context
	 * @return the built intent
	 */
	public Intent execute(Context context) {
		// 每个item的title
		String[] titles = new String[] { "cpu usage",};
		// x轴的值
/*		double[] arrValues = Utils.getCpuPercent(Utils.CPU_FILE_NAME);
		double[] arrx = new double[arrValues.length];
		arrx[0] = 0;
		for (int i = 1; i < arrx.length; i++) {
			arrx[i] = arrx[i - 1] + Utils.DELAYTIME;
		}*/
		List<double[]> x = new ArrayList<double[]>();
		for (int i = 0; i < titles.length; i++) {
			x.add(arrx);
		}
		// y轴的值
		List<double[]> values = new ArrayList<double[]>();
		values.add(arrValues);
		
		int[] colors = new int[] { Color.BLUE,};
		// 点的样式
		PointStyle[] styles = new PointStyle[] { PointStyle.CIRCLE,};
		XYMultipleSeriesRenderer renderer = buildRenderer(colors, styles);
		int length = renderer.getSeriesRendererCount();
		// 点是空心还是实心
		for (int i = 0; i < length; i++) {
			((XYSeriesRenderer) renderer.getSeriesRendererAt(i))
					.setFillPoints(true);
		}

		// 图表部分的背景颜色
		renderer.setBackgroundColor(Color.parseColor("#f3f3f3"));
		renderer.setApplyBackgroundColor(true);
		// 图表与屏幕四边的间距颜色
		renderer.setMarginsColor(Color.argb(0, 0xF3, 0xF3, 0xF3));
		renderer.setChartTitleTextSize(30);
		renderer.setAxisTitleTextSize(25);
		// renderer.setLegendHeight(50);
		// 图例文字的大小
		renderer.setLegendTextSize(20);
		renderer.setMargins(new int[] { 50, 50, 50, 30 });
		// x、y轴上刻度颜色
		renderer.setXLabelsColor(Color.BLACK);
		renderer.setYLabelsColor(0, Color.BLACK);

		// 最后两个参数代表轴的颜色和轴标签的颜色
		setChartSettings(renderer, "Cpu Percent", "time/min",
				"cpu usage/%", 1, 12.5, 1, 40, Color.BLACK, Color.BLACK);
		// 轴上数字的数量
		renderer.setXLabels(12);
		renderer.setYLabels(10);
		// 是否显示网格
		renderer.setShowGrid(true);
		// x或y轴上数字的方向，相反的。
		renderer.setXLabelsAlign(Align.RIGHT);
		renderer.setYLabelsAlign(Align.RIGHT);
		// 有问题的方法
		// renderer.setZoomButtonsVisible(true);
		// renderer.setPanLimits(new double[] { -10, 20, -10, 40 });
		// renderer.setZoomLimits(new double[] { -10, 20, -10, 40 });

		Intent intent = ChartFactory.getLineChartIntent(context,
				buildDataset(titles, x, values), renderer,
				"Cpu Percent");
		return intent;
	}

	  protected XYMultipleSeriesDataset buildDataset(String[] titles, List<double[]> xValues,
		      List<double[]> yValues) {
		    XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		    addXYSeries(dataset, titles, xValues, yValues, 0);
		    return dataset;
		  }
	  
	  public void addXYSeries(XYMultipleSeriesDataset dataset, String[] titles, List<double[]> xValues,
		      List<double[]> yValues, int scale) {
		    int length = titles.length;
		    for (int i = 0; i < length; i++) {
		      XYSeries series = new XYSeries(titles[i], scale);
		      double[] xV = xValues.get(i);
		      double[] yV = yValues.get(i);
		      int seriesLength = xV.length;
		      for (int k = 0; k < seriesLength; k++) {
		        series.add(xV[k], yV[k]);
		      }
		      dataset.addSeries(series);
		    }
		  }
	  protected void setRenderer(XYMultipleSeriesRenderer renderer, int[] colors, PointStyle[] styles) {
		    renderer.setAxisTitleTextSize(16);
		    renderer.setChartTitleTextSize(20);
		    renderer.setLabelsTextSize(15);
		    renderer.setLegendTextSize(15);
		    renderer.setPointSize(5f);
		    renderer.setMargins(new int[] { 20, 30, 15, 20 });
		    int length = colors.length;
		    for (int i = 0; i < length; i++) {
		      XYSeriesRenderer r = new XYSeriesRenderer();
		      r.setColor(colors[i]);
		      r.setPointStyle(styles[i]);
		      renderer.addSeriesRenderer(r);
		    }
		  }

	  protected XYMultipleSeriesRenderer buildRenderer(int[] colors, PointStyle[] styles) {
		    XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
		    setRenderer(renderer, colors, styles);
		    return renderer;
		  }

	@Override
	public void draw(Canvas arg0, int arg1, int arg2, int arg3, int arg4,
			Paint arg5) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void drawLegendShape(Canvas arg0, SimpleSeriesRenderer arg1,
			float arg2, float arg3, int arg4, Paint arg5) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getLegendShapeWidth(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}
	  protected XYMultipleSeriesDataset buildDateDataset(String[] titles, List<Date[]> xValues,
		      List<double[]> yValues) {
		    XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		    int length = titles.length;
		    for (int i = 0; i < length; i++) {
		      TimeSeries series = new TimeSeries(titles[i]);
		      Date[] xV = xValues.get(i);
		      double[] yV = yValues.get(i);
		      int seriesLength = xV.length;
		      for (int k = 0; k < seriesLength; k++) {
		        series.add(xV[k], yV[k]);
		      }
		      dataset.addSeries(series);
		    }
		    return dataset;
		  }
	  protected void setChartSettings(XYMultipleSeriesRenderer renderer, String title, String xTitle,
		      String yTitle, double xMin, double xMax, double yMin, double yMax, int axesColor,
		      int labelsColor) {
		    renderer.setChartTitle(title);
		    renderer.setXTitle(xTitle);
		    renderer.setYTitle(yTitle);
		    renderer.setXAxisMin(xMin);
		    renderer.setXAxisMax(xMax);
		    renderer.setYAxisMin(yMin);
		    renderer.setYAxisMax(yMax);
		    renderer.setAxesColor(axesColor);
		    renderer.setLabelsColor(labelsColor);
		  }
}



