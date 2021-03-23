package com.network.map.genstrategy;

import com.network.map.MapGenerationConfig;

import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Реализация стратегии генерации карты
 */
public class GenerateSimpleStrategy implements GenerateStrategy {
    public double[][] generate(double[][] sourceMap, MapGenerationConfig mapGenerationConfig) {
        Random random = new Random();
        mapGenerationConfig.getSignals()
                .stream()
                .forEach(signal -> {
                    generateSource(sourceMap, signal);
                });
        correctWhiteZones(sourceMap);
        return sourceMap;
    }

    /**
     * Сгенерировать и поместить на карту
     * источник сигнала
     *
     * @param sourceMap Карта, для которой генерируется сигнал
     * @param signal    Параметры источника сигнала
     */
    private void generateSource(double[][] sourceMap, MapGenerationConfig.Signal signal) {
        double maxSignalValue = signal.getPikeValue();
        sourceMap[signal.getY()][signal.getX()] = maxSignalValue;
        int mapW = sourceMap[0].length;
        int mapH = sourceMap.length;
        int iterationsCount = (int) (Math.max(mapW, mapH) * 1.5f);

        int pointsCount = 4;
        for (int i = 0; i < iterationsCount; i++) {
            int realPintsCount = 0;
            Set<Point2D> realPoints = new HashSet<>();

            for (int j = 0; j < pointsCount; j++) {
                double angle = Math.toRadians(((double) j / pointsCount) * 360d);
                double radius = i + 1;
                int pointX = (int) (Math.cos(angle) * radius) + signal.getX();
                int pointY = (int) (Math.sin(angle) * radius) + signal.getY();
                Point2D.Double point = new Point2D.Double(pointX, pointY);
                if (pointY < mapH && pointY >= 0 &&
                        pointX < mapW && pointX >= 0 &&
                        sourceMap[pointY][pointX] < maxSignalValue &&
                        !realPoints.contains(point)) {
                    realPoints.add(point);
                    sourceMap[pointY][pointX] = maxSignalValue;
                    realPintsCount++;
                }
            }
            pointsCount = realPintsCount * 3;
            maxSignalValue -= signal.getDegradationSpeed();
            if (maxSignalValue <= 0) break;
        }
    }

    /**
     * Скорректировать артефакты на сгенерированной карте
     *
     * @param sourceMap
     */
    private void correctWhiteZones(double[][] sourceMap) {
        int mapW = sourceMap[0].length;
        int mapH = sourceMap.length;
        for (int i = 0; i < mapH; i++) {
            for (int j = 0; j < mapW; j++) {
                fillAround8Of(sourceMap, j, i);
            }
        }
    }

    /**
     * Заполнить провалы (артефакты генерации)
     * вокруг точки с координатами {x;y}
     *
     * @param sourceMap Исхожная карта
     * @param x         Координата x
     * @param y         Координата y
     */
    private void fillAround8Of(double[][] sourceMap, int x, int y) {
        if (sourceMap[y][x] <= 0) return;

        int mapW = sourceMap[0].length;
        int mapH = sourceMap.length;
        if (y - 1 < mapH && y - 1 >= 0 && x < mapW && x >= 0 && sourceMap[y - 1][x] == 0) {
            sourceMap[y - 1][x] = sourceMap[y][x] - 1 > 0 ? sourceMap[y][x] - 1 : 0;
        }
        if (y - 1 < mapH && y - 1 >= 0 && x + 1 < mapW && x + 1 > 0 && sourceMap[y - 1][x + 1] == 0) {
            sourceMap[y - 1][x + 1] = sourceMap[y][x] - 1 > 0 ? sourceMap[y][x] - 1 : 0;
        }
        if (y + 1 < mapH && y + 1 > 0 && x < mapW && x >= 0 && sourceMap[y + 1][x] == 0) {
            sourceMap[y + 1][x] = sourceMap[y][x] - 1 > 0 ? sourceMap[y][x] - 1 : 0;
        }
        if (y + 1 < mapH && y + 1 > 0 && x - 1 < mapW && x - 1 >= 0 && sourceMap[y + 1][x - 1] == 0) {
            sourceMap[y + 1][x - 1] = sourceMap[y][x] - 1 > 0 ? sourceMap[y][x] - 1 : 0;
        }
        if (y < mapH && y >= 0 && x - 1 < mapW && x - 1 >= 0 && sourceMap[y][x - 1] == 0) {
            sourceMap[y][x - 1] = sourceMap[y][x] - 1 > 0 ? sourceMap[y][x] - 1 : 0;
        }
        if (y - 1 < mapH && y - 1 >= 0 && x - 1 < mapW && x - 1 >= 0 && sourceMap[y - 1][x - 1] == 0) {
            sourceMap[y - 1][x - 1] = sourceMap[y][x] - 1 > 0 ? sourceMap[y][x] - 1 : 0;
        }
        if (y < mapH && y >= 0 && x + 1 < mapW && x + 1 > 0 && sourceMap[y][x + 1] == 0) {
            sourceMap[y][x + 1] = sourceMap[y][x] - 1 > 0 ? sourceMap[y][x] - 1 : 0;
        }
        if (y + 1 < mapH && y + 1 > 0 && x + 1 < mapW && x + 1 > 0 && sourceMap[y + 1][x + 1] == 0) {
            sourceMap[y + 1][x + 1] = sourceMap[y][x] - 1 > 0 ? sourceMap[y][x] - 1 : 0;
        }

    }
}
