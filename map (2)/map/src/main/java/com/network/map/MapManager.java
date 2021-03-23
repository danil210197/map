package com.network.map;

import com.network.map.ui.SurfaceGraphics;
import com.network.map.util.ApproximationUtils;
import lombok.Getter;
import lombok.SneakyThrows;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Сервис, представляющий собой прослойку
 * между объектом карты и UI
 */
@Getter
public class MapManager {
    private static final int POLYNOM_POW_EXPERIMENT_APPROXIMATION_SCALE = 1;
    private static final String MAP_SAVE_FILE_PATH = "D:/map.txt";
    private NetworkMap map;
    private double[][] generatedSurface;
    private double[][] approximatedSurface;

    public MapManager() {
        map = new NetworkMapImpl();
    }

    /**
     * Сгенерировать карту с заданными параметрами
     *
     * @param mapGenerationConfig
     */
    public void generate(MapGenerationConfig mapGenerationConfig) {
        map.generate(mapGenerationConfig);
        generatedSurface = Arrays.stream(map.getSurface())
                .map(double[]::clone)
                .toArray(double[][]::new);
    }

    /**
     * Отобразить поверхность сгенерированной карты
     * в новом окне
     *
     * @throws Exception
     */
    public void displayGeneratedSurface() throws Exception {
        assert generatedSurface != null : "Плоскость должна быть сгенерирована";
        new SurfaceGraphics(generatedSurface, 0, generatedSurface.length);
    }

    /**
     * Аппроксимировать сгенерированную ранее карту
     * и отобразить полученную аппроксимирующую поверхность
     * в новом окне
     *
     * @param approximationScale Множитель, показывающий во сколько
     *                           раз кол-во точек на аппроксимирующей
     *                           поверхности будет больше, чем было
     *                           в сгенерированной ранее карте
     * @param approximationPow   Степень полинома
     * @return СКО
     */
    public double approximate(int approximationScale, int approximationPow) {
        approximatedSurface = ApproximationUtils.approximate(map, approximationScale, approximationPow);
        return ApproximationUtils.calcSko(map.getSurface(), approximatedSurface);
    }

    /**
     * Отобразить аппроксимирующую поверхность
     * в новом окне
     *
     * @throws Exception
     */
    public void displayApproximatedSurface() throws Exception {
        assert generatedSurface != null : "Плоскость должна быть сгенерирована";
        new SurfaceGraphics(approximatedSurface, 0, generatedSurface.length);
    }

    /**
     * Очистить источники сигналов
     */
    public void clearSignals() {
        map.clearSignals();
    }

    /**
     * Сохранить карту в файл
     */
    public void saveToFile() {
        map.saveToFile(MAP_SAVE_FILE_PATH);
    }

    /**
     * Загрузить карту из файла
     */
    public void loadFromFile() {
        map.loadFromFile(MAP_SAVE_FILE_PATH);
    }

    /**
     * Получить источники сигналов
     *
     * @return Источники сигналов
     */
    public List<MapGenerationConfig.Signal> getSignals() {
        return map.getSignals();
    }

    /**
     * Провести эксперимент
     *
     * @param experimentType      Тип эксперимента
     * @param params              Параметры эксперимента
     * @param mapGenerationConfig Параметры генерации карты
     */
    public void startExperiment(ExperimentType experimentType, Map<String, Object> params, MapGenerationConfig mapGenerationConfig) {
        generate(mapGenerationConfig);
        switch (experimentType) {
            case SKO_BY_POLYNOM_POW:
                startPolynomPowExperiment((int) params.get("polynomPowFrom"), (int) params.get("polynomPowTo"));
                break;
        }
    }

    /**
     * Провести эксперимент для нахождения
     * зависимости погрешности (СКО) от
     * степени полинома
     *
     * @param polynomPowFrom Начальная степень полинома
     * @param polynomPowTo   Конечная степень полинома
     */
    @SneakyThrows
    private void startPolynomPowExperiment(int polynomPowFrom, int polynomPowTo) {
        System.out.println("Вычисление зависимости погрешности от степени полинома: ");
        System.out.println("========================");

        double[] values = new double[polynomPowTo - polynomPowFrom + 1];
        for (int i = polynomPowFrom; i <= polynomPowTo; i++) {
            double sko = approximate(POLYNOM_POW_EXPERIMENT_APPROXIMATION_SCALE, i);
            values[i - polynomPowFrom] = sko;
            System.out.println(String.format("Степень полинома/Погрешность: %s/%s ", i, sko));
        }
        System.out.println();

        createExperimentGraphic(values, polynomPowFrom, "Степень полинома", "Погрешность (СКО)");
    }

    /**
     * Отобразить 2D график в отдельном окне
     *
     * @param y          Значения по оси Y
     * @param xStart     Начальное значение по оси X
     * @param xAxisTitle Название оси X
     * @param yAxisTitle Название оси Y
     */
    private void createExperimentGraphic(double[] y, int xStart, String xAxisTitle, String yAxisTitle) {
        new Thread(() -> {
            double[] x = new double[y.length];
            for (int i = 0; i < x.length; i++) {
                x[i] = xStart + i;
            }
            XYChart chart = QuickChart.getChart(
                    String.format("График зависисимости \"%s от %s\"", xAxisTitle, yAxisTitle),
                    xAxisTitle,
                    yAxisTitle,
                    "График",
                    x,
                    y
            );
            SwingWrapper swingWrapper = new SwingWrapper(chart);
            swingWrapper.displayChart();
        }).start();
    }

    /**
     * Типы экспериментов
     */
    public enum ExperimentType {
        SKO_BY_POLYNOM_POW
    }
}
