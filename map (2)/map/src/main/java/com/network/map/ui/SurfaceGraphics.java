package com.network.map.ui;

import org.jzy3d.analysis.AbstractAnalysis;
import org.jzy3d.analysis.AnalysisLauncher;
import org.jzy3d.chart.factories.AWTChartComponentFactory;
import org.jzy3d.colors.Color;
import org.jzy3d.colors.ColorMapper;
import org.jzy3d.colors.colormaps.ColorMapRainbow;
import org.jzy3d.maths.Range;
import org.jzy3d.plot3d.builder.Builder;
import org.jzy3d.plot3d.builder.Mapper;
import org.jzy3d.plot3d.builder.concrete.OrthonormalGrid;
import org.jzy3d.plot3d.primitives.Shape;
import org.jzy3d.plot3d.rendering.canvas.Quality;

/**
 * Класс, отвечающий за отображение поверхности
 * в отдельном окне
 */
public class SurfaceGraphics extends AbstractAnalysis {
    private double[][] surface;
    private int rangeMinValue;
    private int rangeMaxValue;

    public SurfaceGraphics(double[][] surface, int rangeMinValue, int rangeMaxValue) throws Exception {
        super();
        this.rangeMinValue = rangeMinValue;
        this.rangeMaxValue = rangeMaxValue;
        this.surface = surface;
        AnalysisLauncher.open(this);
    }

    public void init() {
//        final int steps = (rangeMaxValue - rangeMinValue) / surface.length;
        final int steps = 80;
        Shape approximatedShape = prepareSurfaceForRendering(surface, steps);
        chart = AWTChartComponentFactory.chart(Quality.Advanced, getCanvasType());
        chart.getScene().getGraph().add(approximatedShape);
    }

    /**
     * Подготовить поверхность для отрисовки
     *
     * @param map   Поверхность в виде матрицы
     * @param steps Шаг отрисовки
     * @return Подготовленная поверхность
     */
    private Shape prepareSurfaceForRendering(double[][] map, int steps) {
        final double scaleCoef = (double) surface.length / (rangeMaxValue - rangeMinValue);
        Mapper mapper = new Mapper() {
            @Override
            public double f(double x, double y) {
                int i = (int) (y * scaleCoef);
                int j = (int) (x * scaleCoef);
                return i < map.length && j < map[i].length
                        ? map[i][j]
                        : 0;
            }
        };
//        Range range = new Range(0, Math.max(map.length, map[0].length));
        Range range = new Range(rangeMinValue, rangeMaxValue);
        final Shape surface = Builder.buildOrthonormal(new OrthonormalGrid(range, steps, range, steps), mapper);
        surface.setColorMapper(new ColorMapper(new ColorMapRainbow(), surface.getBounds().getZmin(), surface.getBounds().getZmax(), new Color(1, 1, 1, .5f)));
        surface.setFaceDisplayed(true);
        surface.setWireframeDisplayed(false);

        return surface;
    }
}
