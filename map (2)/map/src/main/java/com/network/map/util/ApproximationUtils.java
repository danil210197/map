package com.network.map.util;

import com.network.map.NetworkMap;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * Утилитный класс, содержащий методы
 * для аппроксимации поверхности
 */
public class ApproximationUtils {

    /**
     * Аппроксимировать карту с заданным параметрами
     * approximationScale и approximationPow
     *
     * @param map                Объект, содержащий сгенерированную карту
     * @param approximationScale Множитель, показывающий во сколько
     *                           раз кол-во точек на аппроксимирующей
     *                           поверхности будет больше, чем было
     *                           в сгенерированной ранее карте
     * @param approximationPow   Степень полинома
     * @return Матрица, представляющая аппроксимирующую поверхность
     */
    public static double[][] approximate(NetworkMap map, int approximationScale, int approximationPow) {
        double[][] copiedSurface = Arrays.stream(map.getSurface())
                .map(double[]::clone)
                .toArray(double[][]::new);

        double[][] surfaceApproximationCoefs = new double[copiedSurface.length][];
        double[] x = new double[copiedSurface.length];
        for (int i = 0; i < x.length; i++) {
            x[i] = i;
        }
        for (int i = 0; i < copiedSurface.length; i++) {
            GaussianMatrix gaussianMatrix = prepareGaussianMatrix(x, copiedSurface[i], approximationPow);
            surfaceApproximationCoefs[i] = resolveMatrixCoefs(gaussianMatrix.getMatrix(), gaussianMatrix.getB(), approximationPow);
        }

        for (int i = 0; i < copiedSurface.length; i++) {
            copiedSurface[i] = new double[map.getSurfaceWidth() * approximationScale];
            for (int j = 0; j < copiedSurface[i].length; j++) {
                copiedSurface[i][j] = ApproximationUtils.approximate(surfaceApproximationCoefs[i], ((double) j) / approximationScale);
            }
        }

        double[][] surfaceApproximationOrtogonalCoefs = new double[map.getSurfaceHeight() * approximationScale][];
        double[][] approximatedSurface = new double[map.getSurfaceHeight() * approximationScale][];
        for (int i = 0; i < approximatedSurface.length; i++) {
            double[] approximationSource = new double[copiedSurface.length];
            for (int j = 0; j < approximationSource.length; j++) {
                approximationSource[j] = copiedSurface[j][i];
            }
            GaussianMatrix gaussianMatrix = prepareGaussianMatrix(x, approximationSource, approximationPow);
            surfaceApproximationOrtogonalCoefs[i] = resolveMatrixCoefs(gaussianMatrix.getMatrix(), gaussianMatrix.getB(), approximationPow);
        }

        for (int i = 0; i < map.getSurfaceHeight() * approximationScale; i++) {
            approximatedSurface[i] = new double[map.getSurfaceWidth() * approximationScale];
            for (int j = 0; j < approximatedSurface[i].length; j++) {
                approximatedSurface[i][j] = ApproximationUtils.approximate(surfaceApproximationOrtogonalCoefs[i], ((double) j) / approximationScale);
            }
        }

        return mirrorMatrix(approximatedSurface);
    }

    /**
     * Получить аппроксимационное значение Y в точке X
     * с заданными коэффициентами аппроксимации approximationCoefs
     *
     * @param approximationCoefs Коэффиуиенты аппроксимации
     * @param x                  Значение X
     * @return Значение Y
     */
    public static double approximate(double[] approximationCoefs, double x) {
        double approximationResult = 0;
        for (int i = 0; i < approximationCoefs.length; i++) {
            approximationResult += approximationCoefs[i] * Math.pow(x, i);
        }
        return approximationResult;
    }

    /**
     * Подготовить матрицу для последующего нахождения
     * коэффициентов аппроксимации методом Гаусса
     *
     * @param x          Массив X значений
     * @param y          Массив Y значений
     * @param polynomPow Степень полинома
     * @return Матрица для последующего нахождения
     * коэффициентов аппроксимации методом Гаусса
     */
    private static GaussianMatrix prepareGaussianMatrix(double[] x, double[] y, int polynomPow) {
        int N = x.length;
        double[] b = new double[polynomPow + 1];
        double[][] matrix = new double[polynomPow + 1][];
        for (int i = 0; i < polynomPow + 1; i++) {
            matrix[i] = new double[polynomPow + 1];
            for (int k = 0; k < polynomPow + 1; k++) {
                matrix[i][k] = getSumOfPows(x, k + i, N);
            }
            for (int k = 0; k < N; k++) {
                b[i] += Math.pow(x[k], i) * y[k];
            }
        }
        transformToMatrixWithNotZeroDiagonalCoefs(matrix, b);
        return new GaussianMatrix(matrix, b);
    }

    /**
     * Метод, убирающий нулевые диагональные
     * элементы матрицы
     *
     * @param x Исходная матрица
     * @param b Свободные члены
     */
    public static void transformToMatrixWithNotZeroDiagonalCoefs(double[][] x, double[] b) {
        int N = b.length;

        double temp;
        for (int i = 0; i < N; i++) {
            if (x[i][i] == 0) {
                for (int j = 0; j < N; j++) {
                    if (j == i) continue;
                    if (x[j][i] != 0 && x[i][j] != 0) {
                        for (int k = 0; k < N; k++) {
                            temp = x[j][k];
                            x[j][k] = x[i][k];
                            x[i][k] = temp;
                        }
                        temp = b[j];
                        b[j] = b[i];
                        b[i] = temp;
                        break;
                    }
                }
            }
        }
    }

    /**
     * Нахождение коэффициентов аппроксимации
     * методом Гаусса
     *
     * @param x          Исходная матрица
     * @param b          Свободные члены
     * @param polynomPow Степень полинома
     * @return Коэффициенты аппроксимации
     */
    public static double[] resolveMatrixCoefs(double[][] x, double[] b, int polynomPow) {
        double[][] clonedX = new double[x.length][];
        for (int i = 0; i < clonedX.length; i++) {
            clonedX[i] = new double[x[i].length];
            for (int j = 0; j < clonedX[i].length; j++) {
                clonedX[i][j] = x[i][j];
            }
        }

        double[] coefs = new double[x.length];
        int N = b.length;
        for (int k = 0; k < polynomPow + 1; k++) {
            for (int i = k + 1; i < polynomPow + 1; i++) {
                if (clonedX[k][k] == 0) {
                    throw new RuntimeException("На диагонали найден нулевой элемент. Решений нет");
                }
                double M = clonedX[i][k] / clonedX[k][k];
                for (int j = k; j < polynomPow + 1; j++) {
                    clonedX[i][j] -= M * clonedX[k][j];
                }
                b[i] -= M * b[k];
            }
        }

        for (int i = (polynomPow + 1) - 1; i >= 0; i--) {
            double s = 0;
            for (int j = i; j < polynomPow + 1; j++) {
                s = s + clonedX[i][j] * coefs[j];
            }
            coefs[i] = (b[i] - s) / clonedX[i][i];
        }

        return coefs;
    }

    /**
     * Нахождение СКО между сгенерированной картой и
     * аппроксимирующей плоскостью
     *
     * @param generatedMatrix    Сгенерированная карта
     * @param approximatedMatrix Аппроксимирующая плоскость
     * @return СКО
     */
    public static double calcSko(double[][] generatedMatrix, double[][] approximatedMatrix) {
        int generatedWidth = generatedMatrix[0].length;
        int generatedHeight = generatedMatrix.length;
        approximatedMatrix = reduceMatrixDimensionsTo(approximatedMatrix, generatedWidth, generatedHeight);
        double sumOfDiffers = 0;
        for (int i = 0; i < generatedMatrix.length; i++) {
            for (int j = 0; j < generatedMatrix[i].length; j++) {
                double difference = generatedMatrix[i][j] - approximatedMatrix[i][j];
                sumOfDiffers += difference * difference;
            }
        }
        sumOfDiffers /= generatedWidth * generatedHeight;
        return Math.sqrt(sumOfDiffers);
    }

    /**
     * Нахождение суммы степеней
     *
     * @param x
     * @param pow
     * @param N
     * @return
     */
    private static double getSumOfPows(double[] x, int pow, int N) {
        double sum = 0;
        for (int i = 0; i < N; i++) {
            sum += Math.pow(x[i], pow);
        }
        return sum;
    }

    /**
     * Отразить матрицу зеркально относительно диагонали
     *
     * @param matrix Матрица
     * @return Отраженная матрица
     */
    private static double[][] mirrorMatrix(double[][] matrix) {
        double[][] mirroredMatrix = new double[matrix[0].length][matrix.length];
        for (int i = 0; i < mirroredMatrix.length; i++) {
            for (int j = 0; j < mirroredMatrix[i].length; j++) {
                mirroredMatrix[i][j] = matrix[j][i];
            }
        }
        return mirroredMatrix;
    }

    /**
     * Привести матрицу к нужным размерам
     *
     * @param matrix    Исходная матрица
     * @param newWidth  Новая ширина
     * @param newHeight Новая высота
     * @return Матрица с новыми размерами
     */
    private static double[][] reduceMatrixDimensionsTo(double[][] matrix, int newWidth, int newHeight) {
        int scaleX = matrix[0].length / newWidth;
        int scaleY = matrix.length / newHeight;
        if (scaleY < 1 || scaleX < 1) {
            throw new IllegalArgumentException("Размерность новой матрицы должна быть меньше размерности исходной");
        }

        double[][] reducedMatrix = new double[newHeight][newWidth];
        for (int i = 0; i < reducedMatrix.length; i++) {
            for (int j = 0; j < reducedMatrix[i].length; j++) {
                reducedMatrix[i][j] = matrix[i * scaleY][j * scaleX];
            }
        }

        return reducedMatrix;
    }

    /**
     * Класс обертка для матрицы и
     * свободных членов
     */
    @Getter
    @AllArgsConstructor
    private static class GaussianMatrix {
        private double[][] matrix;
        private double[] b;
    }
}
