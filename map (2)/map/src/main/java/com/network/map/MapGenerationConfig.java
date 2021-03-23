package com.network.map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Параметры генерации карты
 */
@Data
@Builder
public class MapGenerationConfig implements Serializable {
    private int surfaceWidth;
    private int surfaceHeight;
    private List<Signal> signals = new ArrayList();

    /**
     * Источник сигнала
     */
    @Data
    @AllArgsConstructor
    public static class Signal implements Serializable {
        private static final double BORDER_OF_MAX_PIKE_VALUE = 5;
        private static final double BORDER_OF_MAX_DEGRADATION_SPEED = 0.5;

        private final Random random = new Random();

        private Integer x, y;
        /**
         * Максимальное значение в вершине источника сигнала
         */
        private double pikeValue;
        /**
         * Скорость спада сигнала
         */
        private double degradationSpeed;

        public Signal() {
            randomizePikeValueAndDegradationSpeed(
                    BORDER_OF_MAX_PIKE_VALUE,
                    BORDER_OF_MAX_DEGRADATION_SPEED
            );
        }

        public Signal(double pikeValue, double degradationSpeed) {
            this.pikeValue = pikeValue;
            this.degradationSpeed = degradationSpeed;
        }

        public Signal randomizePikeValueAndDegradationSpeed(double borderOfMaxPikeValue, double borderOfMaxDegradationSpeed) {
            pikeValue = ((int) (random.nextDouble() * borderOfMaxPikeValue * 100)) / 100d;
            degradationSpeed = ((int) (random.nextDouble() * borderOfMaxDegradationSpeed * 100)) / 100d;
            return this;
        }
    }
}
