package ww10.gui;

import java.awt.Color;
import java.util.Map;
import java.util.Map.Entry;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.MultiplePiePlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.util.TableOrder;

public class ActionPanel extends ChartPanel {

	private static final long serialVersionUID = -534880050132416786L;

	private final DataModel dataModel;

	private final DefaultCategoryDataset dataset;

	private final String[] rowKeys;

	private final String[] columnKeys;

	public ActionPanel(DataModel dataModel) {
		super(null);
		this.dataModel = dataModel;

		String[] playerNames = dataModel.getPlayerNames();

		double[][] data = new double[playerNames.length][5];

		rowKeys = playerNames;
		columnKeys = new String[] { "Check", "Bet", "Fold", "Call", "Raise" };

		dataset = new DefaultCategoryDataset();
		for (int r = 0; r < data.length; r++) {
			String rowKey = rowKeys[r];
			for (int c = 0; c < data[r].length; c++) {
				String columnKey = columnKeys[c];
				dataset.addValue(data[r][c], rowKey, columnKey);
			}
		}

		final JFreeChart chart = ChartFactory.createMultiplePieChart(null, // chart title
				dataset, // dataset
				TableOrder.BY_ROW, true, // include legend
				true, false);
		final MultiplePiePlot plot = (MultiplePiePlot) chart.getPlot();
		final JFreeChart subchart = plot.getPieChart();
		final PiePlot p = (PiePlot) subchart.getPlot();

		p.setBackgroundPaint(Color.WHITE);

		//		p.setLabelGenerator(new StandardPieItemLabelGenerator("{0}"));
		//		p.setLabelFont(new Font("SansSerif", Font.PLAIN, 8));
		//		p.setInteriorGap(0.30);
		p.setBackgroundPaint(null);
		p.setOutlineStroke(null);

		setChart(chart);
	}

	public void updateActionFrequencies(Map<String, int[]> freq) {
		for (Entry<String, int[]> entry : freq.entrySet()) {
			updateActionFrequencies(entry.getKey(), entry.getValue());
		}
	}

	public void updateActionFrequencies(String player, int[] freq) {
		for (int c = 0; c < freq.length; c++) {
			String columnKey = columnKeys[c];
			dataset.addValue(freq[c], player, columnKey);
		}
	}
}
