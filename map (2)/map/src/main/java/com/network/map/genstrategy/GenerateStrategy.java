package com.network.map.genstrategy;

import com.network.map.MapGenerationConfig;

import java.io.Serializable;

/**
 * Стратегия генерации карты
 */
public interface GenerateStrategy extends Serializable {
    /**
     * Сгенерировать карту с заданными параметрами
     *
     * @param sourceMap           Исходная карта, для которой необходимо
     *                            сгенерировать новые источники сигналов
     * @param mapGenerationConfig Параметры генерации
     * @return Сгенерированная карта
     */
    double[][] generate(double[][] sourceMap, MapGenerationConfig mapGenerationConfig);
}
