package com.network.map.ui.wrapper;

import com.network.map.MapGenerationConfig;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Класс-обертка для источника сигнала
 * на UI-форме
 */
public class SignalSourceWrapper {
    private Map<String, JTextComponent> components = new HashMap<>();

    public SignalSourceWrapper(JPanel panel) {
        Arrays.stream(panel.getComponents()).forEach(comp -> {
            if (!StringUtils.isBlank(comp.getName()) && comp instanceof JTextComponent) {
                components.put(comp.getName(), (JTextComponent) comp);
            }
        });
    }

    /**
     * Получить координату X
     * истончика сигнала из UI-компонента
     *
     * @return Координата X источника сигнала
     */
    public Integer getX() {
        return getIntValue("x");
    }

    /**
     * Получить координату Y
     * истончика сигнала из UI-компонента
     *
     * @return Координата Y источника сигнала
     */
    public Integer getY() {
        return getIntValue("y");
    }

    /**
     * Получить значение в центре
     * истончика сигнала из UI-компонента
     *
     * @return Значение в центре источника сигнала
     */
    public double getMaxValue() {
        return getDoubleValue("signalMaxValue");
    }

    /**
     * Получить скорость спада
     * истончика сигнала из UI-компонента
     *
     * @return Скорость спада источника сигнала
     */
    public double getRecesionSpeed() {
        return getDoubleValue("signalRecesionSpeed");
    }

    /**
     * Задать координату X источника сигнала
     *
     * @param x Координата X источника сигнала
     */
    public void setX(int x) {
        setComponentText("x", String.valueOf(x));
    }

    /**
     * Задать координату Y источника сигнала
     *
     * @param y Координата Y источника сигнала
     */
    public void setY(int y) {
        setComponentText("y", String.valueOf(y));
    }

    /**
     * Задать максимальную величину
     * источника сигнала
     *
     * @param maxValue Максимальная величина источника сигнала
     */
    public void setMaxValue(double maxValue) {
        setComponentText("signalMaxValue", String.valueOf(maxValue));
    }

    /**
     * Задать скорость спада
     * источника сигнала
     *
     * @param recesionSpeed Скорость спада источника сигнала
     */
    public void setRecesionSpeed(double recesionSpeed) {
        setComponentText("signalRecesionSpeed", String.valueOf(recesionSpeed));
    }

    /**
     * Задать текст для UI-компонента
     *
     * @param componentName Название UI-компонента
     * @param text          Текст
     */
    private void setComponentText(String componentName, String text) {
        JTextComponent component = components.get(componentName);
        assert component != null : "Компонент не найден: " + componentName;

        component.setText(text);
    }

    /**
     * Получить дробное значение
     * из UI-компонента
     *
     * @param componentName Название UI-компонента
     * @return Дробное значение
     */
    private Double getDoubleValue(String componentName) {
        JTextComponent component = components.get(componentName);
        assert component != null : "Компонент не найден: " + componentName;

        Double value = null;
        try {
            value = Double.parseDouble(component.getText());
        } catch (Exception e) {
        }
        return value;
    }

    /**
     * Получить целочисленное значение
     * из UI-компонента
     *
     * @param componentName Название UI-компонента
     * @return Целочисленное значение
     */
    private Integer getIntValue(String componentName) {
        JTextComponent component = components.get(componentName);
        assert component != null : "Компонент не найден: " + componentName;

        Integer value = null;
        try {
            value = Integer.parseInt(component.getText());
        } catch (Exception e) {
        }
        return value;
    }
}
