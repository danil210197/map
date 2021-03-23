package com.network.map;

import java.io.Serializable;
import java.util.List;

public interface NetworkMap extends Serializable {
    /**
     * Сгенерировать карту с заданными параметрами
     *
     * @param mapGenerationConfig Параметры генерации карты
     * @return Объект, содержащий сгенерированную карту
     */
    NetworkMap generate(MapGenerationConfig mapGenerationConfig);

    /**
     * Получить сгенерированную карту
     *
     * @return Сгенерированная карта
     */
    double[][] getSurface();

    /**
     * Получить ширину сгенерированной карты
     *
     * @return Ширина сгенерированной карты
     */
    int getSurfaceWidth();

    /**
     * Получить высоту сгенерированной карты
     *
     * @return Высота сгенерированной карты
     */
    int getSurfaceHeight();

    /**
     * Сохранить карту в файл по заданному пути
     *
     * @param path Путь к файлу
     * @return Объект, содержащий сгенерированную карту
     */
    NetworkMap saveToFile(String path);

    /**
     * Загрузить карту из файла по заданному пути
     *
     * @param path Путь к файлу
     * @return Объект, содержащий сгенерированную карту
     */
    NetworkMap loadFromFile(String path);

    /**
     * Вывести сгенерированную карту в консоли
     *
     * @return Объект, содержащий сгенерированную карту
     */
    NetworkMap display();

    /**
     * Очистить сгенерированные ранее
     * источники сигналов
     *
     * @return Объект, содержащий сгенерированную карту
     */
    NetworkMap clearSignals();

    /**
     * Получить сгенерированные ранее
     * источники сигналов
     *
     * @return Источники сигналов
     */
    List<MapGenerationConfig.Signal> getSignals();
}
