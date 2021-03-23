package com.network.map.ui.form;

import com.network.map.MapGenerationConfig;
import com.network.map.MapManager;
import com.network.map.ui.wrapper.SignalSourceWrapper;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * UI
 */
public class MainForm extends JFrame {
    private List<SignalSourceWrapper> signalSourceWrappers = new ArrayList();
    private MapManager mapManager;

    private JPanel contentPane;
    private JTabbedPane tabbedPane1;
    private JButton btnApproximateMap;
    private JPanel signalSourceParams;
    private JButton brnAddSignalSource;
    private JPanel genSignalsGroup;
    private JButton generateMap;
    private JButton randomizeSignals;
    private JTextField inputMapWidth;
    private JTextField inputMapHeight;
    private JTextField inputPolynomPow;
    private JTextField inputApproximationScale;
    private JTextArea inputApproximationLog;
    private JTextField inputMaxValue;
    private JTextField inputMaxRecesionSpeed;
    private JTextField inputMinPolynomPow;
    private JTextField inputMaxPolynomPow;
    private JButton btnStartPolynomPowExperiment;
    private JButton btnLoadFromFile;
    private JButton btnSaveToFile;
    private JButton buttonOK;


    public MainForm() {
        setContentPane(contentPane);
        pack();
        createUIComponents();
        initEventListeners();

        mapManager = new MapManager();

        setVisible(true);
    }

    /**
     * Инициализации обработчков нажатий на кнопки
     */
    private void initEventListeners() {
        brnAddSignalSource.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                SignalSourceWrapper signalSourceWrapper = addNewSignal(genSignalsGroup);
                signalSourceWrappers.add(signalSourceWrapper);
                genSignalsGroup.updateUI();
            }
        });
        generateMap.addMouseListener(new MouseAdapter() {
            @SneakyThrows
            @Override
            public void mouseClicked(MouseEvent e) {
                MapGenerationConfig mapGenerationConfig = prepareMapGenerationConfig();
                mapManager.generate(mapGenerationConfig);
                mapManager.displayGeneratedSurface();
            }
        });
        btnApproximateMap.addMouseListener(new MouseAdapter() {
            @SneakyThrows
            @Override
            public void mouseClicked(MouseEvent e) {
                int approximationScale = Integer.parseInt(!StringUtils.isBlank(inputApproximationScale.getText()) ? inputApproximationScale.getText() : "1");
                int polynomPow = Integer.parseInt(!StringUtils.isBlank(inputPolynomPow.getText()) ? inputPolynomPow.getText() : "1");

                double sko = mapManager.approximate(approximationScale, polynomPow);
                mapManager.displayApproximatedSurface();
                inputApproximationLog.setText(String.format("СКО: %1$,.5f", sko));
            }
        });
        randomizeSignals.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Random random = new Random();
                int width = Integer.parseInt(!StringUtils.isBlank(inputMapWidth.getText()) ? inputMapWidth.getText() : "0");
                int height = Integer.parseInt(!StringUtils.isBlank(inputMapHeight.getText()) ? inputMapHeight.getText() : "0");
                double maxRecesionSpeed = Double.parseDouble(!StringUtils.isBlank(inputMaxRecesionSpeed.getText()) ? inputMaxRecesionSpeed.getText() : "1");
                double maxValue = Double.parseDouble(!StringUtils.isBlank(inputMaxValue.getText()) ? inputMaxValue.getText() : "10");
                signalSourceWrappers.stream()
                        .forEach(signalWrapper -> {
                            MapGenerationConfig.Signal signal = new MapGenerationConfig.Signal()
                                    .randomizePikeValueAndDegradationSpeed(maxValue, maxRecesionSpeed);
                            signalWrapper.setX(random.nextInt(width));
                            signalWrapper.setY(random.nextInt(height));
                            signalWrapper.setMaxValue(signal.getPikeValue());
                            signalWrapper.setRecesionSpeed(signal.getDegradationSpeed());
                        });
            }
        });
        btnStartPolynomPowExperiment.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                MapGenerationConfig mapGenerationConfig = prepareMapGenerationConfig();
                Map<String, Object> params = Map.of(
                        "polynomPowFrom", Integer.parseInt(!StringUtils.isBlank(inputMinPolynomPow.getText()) ? inputMinPolynomPow.getText() : "2"),
                        "polynomPowTo", Integer.parseInt(!StringUtils.isBlank(inputMaxPolynomPow.getText()) ? inputMaxPolynomPow.getText() : "10")
                );
                mapManager.startExperiment(MapManager.ExperimentType.SKO_BY_POLYNOM_POW, params, mapGenerationConfig);
            }
        });
        btnSaveToFile.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                MapGenerationConfig mapGenerationConfig = prepareMapGenerationConfig();
                mapManager.generate(mapGenerationConfig);
                mapManager.saveToFile();
                System.out.println("Карта сохранена в файл");
            }
        });
        btnLoadFromFile.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                clearSignals();
                mapManager.loadFromFile();
                List<MapGenerationConfig.Signal> signals = mapManager.getSignals();
                signalSourceWrappers = signals.stream()
                        .map(signal -> addNewSignal(genSignalsGroup, signal))
                        .collect(Collectors.toList());
                inputMapWidth.setText(mapManager.getMap().getSurfaceWidth() + "");
                inputMapHeight.setText(mapManager.getMap().getSurfaceHeight() + "");
                genSignalsGroup.updateUI();
                System.out.println("Карта загружена из файла");
            }
        });
    }

    /**
     * Собрать данные с формы и сформировать
     * объект с параметрами генерации карты
     *
     * @return
     */
    private MapGenerationConfig prepareMapGenerationConfig() {
        String width = !StringUtils.isBlank(inputMapWidth.getText()) ? inputMapWidth.getText() : "0";
        String height = !StringUtils.isBlank(inputMapHeight.getText()) ? inputMapHeight.getText() : "0";
        return MapGenerationConfig.builder()
                .surfaceWidth(Integer.parseInt(width))
                .surfaceHeight(Integer.parseInt(height))
                .signals(signalSourceWrappers.stream()
                        .map(signal -> new MapGenerationConfig.Signal(signal.getX(), signal.getY(), signal.getMaxValue(), signal.getRecesionSpeed()))
                        .collect(Collectors.toList())
                ).build();
    }

    private void createUIComponents() {
    }

    /**
     * Очистить источники сигналов
     * на форме
     */
    private void clearSignals() {
        signalSourceWrappers = new ArrayList<>();
        mapManager.clearSignals();
        genSignalsGroup.removeAll();
    }

    /**
     * Добавить новый источник сигнала
     * на форму
     *
     * @param panel Родительская панель для
     *              источников сигнала на форме
     * @return Обертка для источника сигнала
     */
    private SignalSourceWrapper addNewSignal(JPanel panel) {
        return addNewSignal(panel, null);
    }

    /**
     * Добавить новый источник сигнала
     * на форму
     *
     * @param signal Параметры источника сигнала
     * @param panel  Родительская панель для
     *               источников сигнала на форме
     * @return Обертка для источника сигнала
     */
    private SignalSourceWrapper addNewSignal(JPanel panel, MapGenerationConfig.Signal signal) {
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JPanel signalSourceParams = new JPanel();
        signalSourceParams.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 9, new Insets(0, 0, 0, 0), -1, -1));
        panel.add(signalSourceParams);

        JLabel labelX = new JLabel();
        labelX.setText("x:");
        signalSourceParams.add(labelX, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        JTextField signalX = new JTextField();
        signalX.setName("x");
        signalX.setText(signal != null ? signal.getX() + "" : "");
        signalSourceParams.add(signalX, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(50, -1), null, 0, false));

        JLabel labelY = new JLabel();
        labelY.setText("y:");
        signalSourceParams.add(labelY, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        JTextField signalY = new JTextField();
        signalY.setName("y");
        signalY.setText(signal != null ? signal.getY() + "" : "");
        signalSourceParams.add(signalY, new com.intellij.uiDesigner.core.GridConstraints(0, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(50, -1), null, 0, false));

        JLabel labelMaxValue = new JLabel();
        labelMaxValue.setText("Макс. в-на:");
        signalSourceParams.add(labelMaxValue, new com.intellij.uiDesigner.core.GridConstraints(0, 4, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        JTextField signalMaxValue = new JTextField();
        signalMaxValue.setName("signalMaxValue");
        signalMaxValue.setText(signal != null ? signal.getPikeValue() + "" : "");
        signalSourceParams.add(signalMaxValue, new com.intellij.uiDesigner.core.GridConstraints(0, 5, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(50, -1), null, 0, false));

        JLabel labelRecesionSpeed = new JLabel();
        labelRecesionSpeed.setText("Ск-сть. пад-я:");
        signalSourceParams.add(labelRecesionSpeed, new com.intellij.uiDesigner.core.GridConstraints(0, 6, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        JTextField signalRecesionSpeed = new JTextField();
        signalRecesionSpeed.setName("signalRecesionSpeed");
        signalRecesionSpeed.setText(signal != null ? signal.getDegradationSpeed() + "" : "");
        signalSourceParams.add(signalRecesionSpeed, new com.intellij.uiDesigner.core.GridConstraints(0, 7, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(50, -1), null, 0, false));

        JButton removeSignalSource = new JButton();
        removeSignalSource.setText("-");
        signalSourceParams.add(removeSignalSource, new com.intellij.uiDesigner.core.GridConstraints(0, 8, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        SignalSourceWrapper signalSourceWrapper = new SignalSourceWrapper(signalSourceParams);
        removeSignalSource.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                panel.remove(signalSourceParams);
                signalSourceWrappers.remove(signalSourceWrapper);
                genSignalsGroup.updateUI();
            }
        });

        return signalSourceWrapper;
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        contentPane = new JPanel();
        contentPane.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 1, new Insets(10, 10, 10, 10), -1, -1));
        tabbedPane1 = new JTabbedPane();
        tabbedPane1.putClientProperty("html.disable", Boolean.FALSE);
        contentPane.add(tabbedPane1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(16, 7, new Insets(10, 10, 10, 10), -1, -1));
        tabbedPane1.addTab("Генерация", panel1);
        final com.intellij.uiDesigner.core.Spacer spacer1 = new com.intellij.uiDesigner.core.Spacer();
        panel1.add(spacer1, new com.intellij.uiDesigner.core.GridConstraints(1, 2, 1, 5, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_NORTH, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        brnAddSignalSource = new JButton();
        brnAddSignalSource.setText("Добавить источник сигнала");
        panel1.add(brnAddSignalSource, new com.intellij.uiDesigner.core.GridConstraints(15, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_SOUTH, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        generateMap = new JButton();
        generateMap.setText("Сгенерировать");
        panel1.add(generateMap, new com.intellij.uiDesigner.core.GridConstraints(15, 6, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_SOUTHEAST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        genSignalsGroup = new JPanel();
        genSignalsGroup.setLayout(new BorderLayout(0, 0));
        panel1.add(genSignalsGroup, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 12, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_NORTH, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        randomizeSignals = new JButton();
        randomizeSignals.setText("Случайные значения");
        panel1.add(randomizeSignals, new com.intellij.uiDesigner.core.GridConstraints(14, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_SOUTH, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Источники сигнала");
        panel1.add(label1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Параметры генерации");
        panel1.add(label2, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 5, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new BorderLayout(0, 0));
        panel2.setBackground(new Color(-16777216));
        panel2.setEnabled(true);
        panel1.add(panel2, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 16, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(1, -1), new Dimension(1, -1), new Dimension(1, -1), 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridBagLayout());
        panel1.add(panel3, new com.intellij.uiDesigner.core.GridConstraints(2, 2, 1, 5, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_NORTHWEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Ширина поля");
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel3.add(label3, gbc);
        inputMapWidth = new JTextField();
        inputMapWidth.setText("50");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.ipadx = 20;
        panel3.add(inputMapWidth, gbc);
        final JLabel label4 = new JLabel();
        label4.setText("Высота поля");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel3.add(label4, gbc);
        inputMapHeight = new JTextField();
        inputMapHeight.setText("50");
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.ipadx = 20;
        panel3.add(inputMapHeight, gbc);
        final JPanel spacer2 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel3.add(spacer2, gbc);
        final JPanel spacer3 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel3.add(spacer3, gbc);
        final JPanel spacer4 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel3.add(spacer4, gbc);
        final JLabel label5 = new JLabel();
        label5.setText("Параметры случайной генерации сигналов");
        panel1.add(label5, new com.intellij.uiDesigner.core.GridConstraints(4, 2, 1, 5, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_NORTHWEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridBagLayout());
        panel1.add(panel4, new com.intellij.uiDesigner.core.GridConstraints(5, 2, 1, 5, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_NORTHWEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Макс. знач-е");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel4.add(label6, gbc);
        inputMaxValue = new JTextField();
        inputMaxValue.setText("10");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.ipadx = 20;
        panel4.add(inputMaxValue, gbc);
        final JLabel label7 = new JLabel();
        label7.setText("Макс ск-сть  пад-я");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel4.add(label7, gbc);
        inputMaxRecesionSpeed = new JTextField();
        inputMaxRecesionSpeed.setText("1");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.ipadx = 20;
        panel4.add(inputMaxRecesionSpeed, gbc);
        final JPanel spacer5 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel4.add(spacer5, gbc);
        final com.intellij.uiDesigner.core.Spacer spacer6 = new com.intellij.uiDesigner.core.Spacer();
        panel1.add(spacer6, new com.intellij.uiDesigner.core.GridConstraints(6, 2, 1, 5, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new BorderLayout(0, 0));
        panel1.add(panel5, new com.intellij.uiDesigner.core.GridConstraints(3, 2, 1, 5, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(-1, 10), new Dimension(-1, 10), 0, false));
        btnLoadFromFile = new JButton();
        btnLoadFromFile.setText("Загрузить");
        panel1.add(btnLoadFromFile, new com.intellij.uiDesigner.core.GridConstraints(15, 5, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_SOUTHEAST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnSaveToFile = new JButton();
        btnSaveToFile.setText("Сохранить");
        panel1.add(btnSaveToFile, new com.intellij.uiDesigner.core.GridConstraints(15, 4, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_SOUTHEAST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer7 = new com.intellij.uiDesigner.core.Spacer();
        panel1.add(spacer7, new com.intellij.uiDesigner.core.GridConstraints(15, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 5, new Insets(10, 10, 10, 10), -1, -1));
        tabbedPane1.addTab("Аппроксимация", panel6);
        final com.intellij.uiDesigner.core.Spacer spacer8 = new com.intellij.uiDesigner.core.Spacer();
        panel6.add(spacer8, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 4, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        btnApproximateMap = new JButton();
        btnApproximateMap.setText("Аппроксимировать");
        panel6.add(btnApproximateMap, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_SOUTH, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new GridBagLayout());
        panel6.add(panel7, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_NORTHWEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel spacer9 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel7.add(spacer9, gbc);
        final JPanel spacer10 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.VERTICAL;
        panel7.add(spacer10, gbc);
        inputPolynomPow = new JTextField();
        inputPolynomPow.setText("10");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.ipadx = 20;
        panel7.add(inputPolynomPow, gbc);
        final JLabel label8 = new JLabel();
        label8.setText("Размерность полинома");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel7.add(label8, gbc);
        final JLabel label9 = new JLabel();
        label9.setText("Множитель кол-ва новых точек");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel7.add(label9, gbc);
        inputApproximationScale = new JTextField();
        inputApproximationScale.setText("2");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.ipadx = 20;
        panel7.add(inputApproximationScale, gbc);
        final JPanel spacer11 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel7.add(spacer11, gbc);
        final JPanel panel8 = new JPanel();
        panel8.setLayout(new BorderLayout(0, 0));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.BOTH;
        panel7.add(panel8, gbc);
        inputApproximationLog = new JTextArea();
        inputApproximationLog.setAutoscrolls(true);
        inputApproximationLog.setRequestFocusEnabled(true);
        inputApproximationLog.setRows(10);
        inputApproximationLog.setText("");
        panel6.add(inputApproximationLog, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), new Dimension(-1, 300), 0, false));
        final JPanel panel9 = new JPanel();
        panel9.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane1.addTab("Опыты", panel9);
        final JPanel panel10 = new JPanel();
        panel10.setLayout(new GridBagLayout());
        panel9.add(panel10, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_NORTHWEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel10.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JLabel label10 = new JLabel();
        label10.setText("Начальное значение ст. полинома:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel10.add(label10, gbc);
        final JPanel spacer12 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel10.add(spacer12, gbc);
        inputMinPolynomPow = new JTextField();
        inputMinPolynomPow.setMaximumSize(new Dimension(2147483647, 30));
        inputMinPolynomPow.setMinimumSize(new Dimension(20, 30));
        inputMinPolynomPow.setPreferredSize(new Dimension(20, 30));
        inputMinPolynomPow.setText("2");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel10.add(inputMinPolynomPow, gbc);
        final JLabel label11 = new JLabel();
        label11.setText("Конечное значение ст. полинома:");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel10.add(label11, gbc);
        inputMaxPolynomPow = new JTextField();
        inputMaxPolynomPow.setMaximumSize(new Dimension(2147483647, 30));
        inputMaxPolynomPow.setMinimumSize(new Dimension(20, 30));
        inputMaxPolynomPow.setPreferredSize(new Dimension(20, 30));
        inputMaxPolynomPow.setText("20");
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel10.add(inputMaxPolynomPow, gbc);
        final JPanel spacer13 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel10.add(spacer13, gbc);
        final JPanel spacer14 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel10.add(spacer14, gbc);
        final JPanel spacer15 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 7;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel10.add(spacer15, gbc);
        btnStartPolynomPowExperiment = new JButton();
        btnStartPolynomPowExperiment.setText("Начать");
        gbc = new GridBagConstraints();
        gbc.gridx = 8;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel10.add(btnStartPolynomPowExperiment, gbc);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

}
