package com.network.map;

import com.network.map.genstrategy.GenerateSimpleStrategy;
import com.network.map.genstrategy.GenerateStrategy;
import lombok.Data;

import java.io.*;
import java.util.List;

@Data
public class NetworkMapImpl implements NetworkMap {

    private double[][] map;
    private GenerateStrategy generateStrategy;
    private MapGenerationConfig mapGenerationConfig;

    public NetworkMapImpl() {
        map = new double[0][0];
        generateStrategy = new GenerateSimpleStrategy();
    }

    @Override
    public NetworkMap generate(MapGenerationConfig mapGenerationConfig) {
        this.mapGenerationConfig = mapGenerationConfig;
        map = createWhiteMap(mapGenerationConfig.getSurfaceWidth(), mapGenerationConfig.getSurfaceHeight());
        generateStrategy.generate(map, mapGenerationConfig);
        return this;
    }

    @Override
    public double[][] getSurface() {
        return map;
    }

    @Override
    public int getSurfaceWidth() {
        if (mapGenerationConfig == null) {
            throw new RuntimeException("Ширина карты не задана. Необходимо сгенерировать карту");
        }
        return mapGenerationConfig.getSurfaceWidth();
    }

    @Override
    public int getSurfaceHeight() {
        if (mapGenerationConfig == null) {
            throw new RuntimeException("Высота карты не задана. Необходимо сгенерировать карту");
        }
        return mapGenerationConfig.getSurfaceHeight();
    }

    @Override
    public NetworkMap loadFromFile(String path) {
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(path));
            NetworkMapImpl networkMap = (NetworkMapImpl) objectInputStream.readObject();
            this.map = networkMap.getMap();
            this.mapGenerationConfig = networkMap.getMapGenerationConfig();
            this.generateStrategy = networkMap.getGenerateStrategy();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return this;
    }

    @Override
    public NetworkMap saveToFile(String path) {
        try {
            File file = new File(path);
            file.createNewFile(); // if file already exists will do nothing
            FileOutputStream fileOutputStream = new FileOutputStream(file, false);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    /**
     * /**
     * Сгенерировать пустую карту (уровень сигнала во всех точках = 0)
     *
     * @param width  Ширина карты
     * @param height Высота карты
     * @return Сгенерированная пуста карта {@link double[][]}
     */
    private double[][] createWhiteMap(int width, int height) {
        double[][] map = new double[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                map[i][j] = 0;
            }
        }
        return map;
    }

    public NetworkMap display() {
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                String value = String.format("%1$" + 5 + "s", map[i][j]);
                System.out.print(value + " ");
            }
            System.out.println();
        }
        return this;
    }

    @Override
    public NetworkMap clearSignals() {
        if (mapGenerationConfig != null) {
            this.mapGenerationConfig.getSignals().clear();
        }
        return this;
    }

    @Override
    public List<MapGenerationConfig.Signal> getSignals() {
        if (mapGenerationConfig == null) {
            throw new RuntimeException("Невозможно получить источники сигналов. Необходимо сгенерировать карту");
        }
        return this.mapGenerationConfig.getSignals();
    }
}
