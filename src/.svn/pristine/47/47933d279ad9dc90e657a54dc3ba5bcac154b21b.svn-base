package mcib3d.utils;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Plot;
import ij.measure.CurveFitter;
import ij.process.ByteProcessor;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Copyright (C) Thomas Boudier
 *
 * License: This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * class allowing : <BR> storage of arrays with util function such as statistics
 * and sorting
 *
 * @author Cedric MESSAOUDI & Thomas BOUDIER
 * @created 17 septembre 2003
 */
public class ArrayUtil {

    private double values[];
    private int size;
    private boolean sorted;
    private boolean debug = false;

    /**
     * constructeur
     *
     * @param size number of elements
     */
    public ArrayUtil(int size) {
        this.size = size;
        values = new double[size];
        for (int i = 0; i < size; i++) {
            values[i] = 0;
        }
        sorted = false;
    }

    /**
     * constructeur
     *
     * @param data double array
     */
    public ArrayUtil(double[] data) {
        this.size = data.length;
        sorted = false;
        values = data;
    }

    public ArrayUtil(int[] data) {
        this.size = data.length;
        sorted = false;
        values = new double[size];
        for (int i = 0; i < size; i++) {
            values[i] = data[i];
        }
    }

    /**
     * put a value to a index
     *
     * @param pos position in the array
     * @param value value to put
     * @return false if position does not exist
     */
    public boolean putValue(int pos, double value) {
        if (pos < size) {
            values[pos] = value;
            sorted = false;
            return true;
        } else {
            return false;
        }
    }

    /**
     * get value at an index
     *
     * @param pos position in the array
     * @return the value, or NaN if does not exist
     */
    public double getValue(int pos) {
        if (pos < size) {
            return values[pos];
        } else {
            return Double.NaN;
        }
    }

    /**
     * get the number of elements
     *
     * @return number of elements
     */
    public int getSize() {
        return size;
    }

    /**
     * Gets the array attribute of the ArrayUtil object
     *
     * @return The array value
     */
    public double[] getArray() {
        return values;
    }

    /**
     * new size of the array (can incresase size of array)
     *
     * @param size The new size value
     */
    public void setSize(int size) {
        if (size > this.size) {
            double[] temp = new double[size];
            System.arraycopy(values, 0, temp, 0, this.size);
            values = temp;
        }
        this.size = size;
        sorted = false;
    }

    /**
     *
     * @param val
     */
    public void fillValue(double val) {
        for (int i = 0; i < size; i++) {
            this.putValue(i, val);
        }
        sorted = false;
    }

    /**
     * The maximum value
     *
     * @return max value
     */
    public double getMaximum() {
        double max = values[0];
        for (int i = 1; i < size; i++) {
            if (values[i] > max) {
                max = values[i];
            }
        }

        return max;
    }

    /**
     * The maximum value
     *
     * @return max value
     */
    public double[] getMaximumAbove(double th) {
        double max = Double.MIN_VALUE;
        double maxIdx = -1;
        for (int i = 0; i < size; i++) {
            if ((values[i] > max) && (values[i] > th)) {
                max = values[i];
                maxIdx = i;
            }
        }

        return new double[]{max, maxIdx};
    }

    public double[] getMaximumStarting(int th) {
        double max = Double.MIN_VALUE;
        double maxIdx = -1;
        for (int i = th; i < size; i++) {
            if (values[i] > max) {
                max = values[i];
                maxIdx = i;
            }
        }

        return new double[]{max, maxIdx};
    }

    /**
     * The maximum value
     *
     * @return max value
     */
    public int getMaximumIndex() {
        double max = values[0];
        int imax = 0;
        for (int i = 1; i < size; i++) {
            if (values[i] > max) {
                max = values[i];
                imax = i;
            }
        }

        return imax;
    }

    public int getFirstLocalMaxima(double threshold) {
        for (int i = 1; i < size - 1; i++) {
            if (values[i] >= threshold) {
                double v0 = values[i - 1];
                double v1 = values[i];
                double v2 = values[i + 1];
                if ((v1 >= v0) && (v1 >= v2)) {
                    return i;
                }
            }
        }
        return -1;
    }

    public int getFirstLocalMinima(double threshold) {
        for (int i = 1; i < size - 1; i++) {
            if (values[i] <= threshold) {
                double v0 = values[i - 1];
                double v1 = values[i];
                double v2 = values[i + 1];
                if ((v1 <= v0) && (v1 <= v2)) {
                    return i;
                }
            }
        }
        return -1;
    }

    public int getFirstLocalExtrema(double thresholdMax, double thresholdMin) {
        for (int i = 1; i < size - 1; i++) {
            if (values[i] >= thresholdMax) {
                double v0 = values[i - 1];
                double v1 = values[i];
                double v2 = values[i + 1];
                if ((v1 >= v0) && (v1 >= v2)) {
                    return i;
                }
            }
            if (values[i] <= thresholdMin) {
                double v0 = values[i - 1];
                double v1 = values[i];
                double v2 = values[i + 1];
                if ((v1 <= v0) && (v1 <= v2)) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     *
     * @param val
     * @return
     */
    public boolean isMaximum(double val) {
        int i = 0;
        boolean maxok = true;
        while ((i < size) && (values[i] <= val)) {
            i++;
        }
        if (i < size) {
            maxok = false;
        }

        return maxok;
    }

    /**
     *
     * @param th
     * @return
     */
    public double getMaximumBelow(double th) {
        double max = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < size; i++) {
            if ((values[i] > max) && (values[i] < th)) {
                max = values[i];
            }
        }

        return max;
    }

    /**
     * Last index whose value is not zero
     *
     * @return The limitSup value
     */
    public int getLimitSup() {
        int i = size - 1;
        while ((i >= 0) && (values[i] == 0)) {
            i--;
        }

        return i;
    }

    /**
     * First encoutered value not zero, starting from th
     *
     * @param th minimum index
     * @return The limitInf value
     */
    public int getLimitInf(int th) {
        int i = th;
        while ((i < size) && (values[i] == 0)) {
            i++;
        }

        return i;
    }

    /**
     * The minimum value
     *
     * @return min value
     */
    public double getMinimum() {
        double min = values[0];
        for (int i = 1; i < size; i++) {
            if (values[i] < min) {
                min = values[i];
            }
        }

        return min;
    }

    public int getMinimumIndex() {
        double min = values[0];
        int imin = 0;
        for (int i = 1; i < size; i++) {
            if (values[i] < min) {
                min = values[i];
                imin = i;
            }
        }

        return imin;
    }

    /**
     * Gets the minimum value above a threshold
     *
     * @param th the threshold
     * @return The minimumAbove value
     */
    public double getMinimumAbove(double th) {
        double min = Double.MAX_VALUE;
        for (int i = 0; i < size; i++) {
            if (values[i] > th) {
                if (values[i] < min) {
                    min = values[i];
                }
            }
        }
        if (min < Double.MAX_VALUE) {
            return min;
        } else {
            return th;
        }
    }

    /**
     * Average value
     *
     * @return average value
     */
    public double getMean() {
        double total = 0;
        for (int i = 0; i < size; i++) {
            total += values[i];
        }

        return total / (double) size;
    }

    /**
     * sum of all elements
     *
     * @return sum
     */
    public double getSum() {
        double total = 0;
        for (int i = 0; i < size; i++) {
            total += values[i];
        }

        return total;
    }

    /**
     * standard deviation
     *
     * @return std dev
     */
    public double getStdDev() {
        double var = getVariance();
        return Math.sqrt(var);
    }

    /**
     * skewness, order 3
     *
     * @return skewness value (- for right distribution, 0 symmetric, + left
     * distribution)
     */
    public double getSkewness() {
        double mu = getMean();
        double si = getStdDev();
        double diff;

        double total = 0;
        for (int i = 0; i < size; i++) {
            diff = (values[i] - mu) / si;
            total += diff * diff * diff;
            //System.out.println(tab[i]);
        }

        if (size > 1) {
            {
                double fsize = (double) size;
                total *= fsize / ((fsize - 1) * (fsize - 2));
            }
        } else {
            total = (double) 0.0;
        }
        return total;
    }

    /**
     * Kurtosis, order 4
     *
     * @return kurtosis (peakiness)
     */
    public double getKurtosis() {
        double mu = getMean();
        double var = getVariance();
        double diff;

        double total = 0;
        for (int i = 0; i < size; i++) {
            diff = values[i] - mu;
            total += diff * diff * diff * diff;
        }

        if (size > 1) {
            double fsize = (double) size;
            total *= ((fsize) * (fsize + 1)) / ((fsize - 1) * (fsize - 2) * (fsize - 3) * var * var);
            total -= 3 * (fsize - 1) * (fsize - 1) / ((fsize - 2) * (fsize - 3));

        } else {
            total = 0;
        }

        return total;
    }

    /**
     * Variance value (not biased)
     *
     * @return variance
     */
    public double getVariance() {
        if (size == 1) {
            return 0;
        }

        double mean = this.getMean();
        double total = 0;
        for (int i = 0; i < size; i++) {
            total += (values[i] - mean) * (values[i] - mean);
        }

        if (size > 1) {
            total /= (size - 1);
        } else {
            total = 0;
        }

        return total;
    }

    /**
     * Variance value (not biased) fast version
     *
     * @return variance
     */
    public double getVariance2() {
        if (size == 1) {
            return 0;
        }

        double total = 0;
        double total2 = 0;

        for (int i = 0; i < size; i++) {
            total += values[i];
            total2 += values[i] * values[i];
        }

        double var = (double) ((total2 - (total * total / size)) / (size - 1));
        return var;
    }

    /**
     *
     * @param hmean
     * @return
     */
    public double getMaxAbsDifference(ArrayUtil hmean) {
        double diffmax = 0;
        double diff;
        for (int b = 0; b < size; b++) {
            diff = Math.abs(values[b] - hmean.getValue(b));
            if (diff > diffmax) {
                diffmax = diff;
            }
        }
        return diffmax;
    }

    public int countValue(double val) {
        int c = 0;

        for (int b = 0; b < size; b++) {
            if (values[b] == val) {
                c++;
            }
        }

        return c;
    }

    public int countValueAbove(double val) {
        int c = 0;

        for (int b = 0; b < size; b++) {
            if (values[b] > val) {
                c++;
            }
        }

        return c;
    }

    /**
     * Array with only distinct values (sorted)
     *
     * @return
     */
    public ArrayUtil distinctValues() {
        this.sort();
        ArrayUtil V = new ArrayUtil(this.getSize());
        int s = 0;
        double tmp = this.getValue(0);
        V.addValue(0, tmp);
        s++;
        int p = 1;
        int si = this.getSize();
        while (p < si) {
            while ((p < si) && (this.getValue(p) == tmp)) {
                p++;
            }
            if (p < si) {
                tmp = this.getValue(p);
                V.addValue(s, tmp);
                s++;
                p++;
            }
        }

        return V.getSubTabUtil(0, s);
    }

    /**
     * local mean filtering
     *
     * @param rad radius for filtering
     * @return filtered array
     */
    public ArrayUtil localMean(int rad) {
        ArrayUtil tab2 = new ArrayUtil(size);
        for (int i = 0; i < size; i++) {
            if (i > rad && i < size - rad) {
                int total = 0;
                int nombre = 0;
                for (int j = i - rad; j <= i + rad; j++) {
                    total += values[j];
                    nombre++;
                }
                tab2.putValue(i, total / nombre);
            } else {
                tab2.putValue(i, values[i]);
            }
        }

        return tab2;
    }

    /**
     * Addition a value at a index
     *
     * @param position the position index
     * @param value the value to add
     * @return true if position < size
     */
    public boolean addValue(int position, double value) {
        if (position < size) {
            values[position] += value;
            sorted = false;
            return true;
        }
        return false;
    }

    /**
     *
     * @param value
     */
    public void divideAll(double value) {
        for (int i = 0; i < size; ++i) {
            values[i] /= value;
        }
        if (value < 0) {
            sorted = false;
        }
    }

    /**
     *
     * @return
     */
    public boolean isSorted() {
        return sorted;
    }

    /**
     *
     */
    public void sortJava() {
        if (size < values.length) {
            double[] tosort = new double[size];
            System.arraycopy(values, 0, tosort, 0, size);
            Arrays.sort(tosort);
            System.arraycopy(tosort, 0, values, 0, size);
        } else {
            Arrays.sort(values);
        }
        sorted = true;
    }

    /**
     *
     */
    public void sort() {
        sortJava();
    }

    /**
     * Sort the array (Shell-Meitzner algorithm) increasing
     */
    public void sortShellMeitzner() {
        double aux;
        for (int ecart = size / 2; ecart > 0; ecart /= 2) {
            int i = 0;
            int pos_mem = 0;
            while (i < (size - ecart)) {
                if (values[i] > values[i + ecart]) {
                    aux = values[i];
                    values[i] = values[i + ecart];
                    values[i + ecart] = aux;
                    if (i - ecart >= 0) {
                        i -= ecart;
                    } else {
                        i = ++pos_mem;
                    }

                } else {
                    i = ++pos_mem;
                }
            }
        }
        sorted = true;
    }

    /**
     * Get the indexes of sorted array (Shell-Meitzner algorithm) do not sort
     * the array
     *
     * @return the sorted index
     */
    public int[] sortIndexShellMeitzner() {
        int td[] = new int[size];
        for (int i = 0; i < size; i++) {
            td[i] = i;
        }
        int aux;
        for (int ecart = size / 2; ecart > 0; ecart /= 2) {
            int i = 0;
            int pos_mem = 0;
            while (i < (size - ecart)) {
                if (values[td[i]] > values[td[i + ecart]]) {
                    aux = td[i];
                    td[i] = td[i + ecart];
                    td[i + ecart] = aux;
                    if (i - ecart >= 0) {
                        i -= ecart;
                    } else {
                        i = ++pos_mem;
                    }

                } else {
                    i = ++pos_mem;
                }
            }
        }
        return td;
    }

    /**
     * The median (sorted array)
     *
     * @return mediane
     */
    public double medianSort() {
        if (!sorted) {
            sort();
        }
        if (size % 2 == 1) {
            return values[size / 2];
        } else {
            return (0.5f * (values[size / 2 - 1] + values[size / 2]));
        }
    }

    /**
     * Find median. Modified algorithm according to
     * http://www.geocities.com/zabrodskyvlada/3alg.html Contributed by
     * HeinzKlar. (copied form ij.plugin.filter.rankfilters
     *
     * @return Median value
     */
    public double median() {
        // pb with even values ?
        final int nValues = size;
        final int nv1b2 = (nValues - 1) / 2;
        int i;
        int j;
        int l = 0;
        int m = nValues - 1;
        double med = values[nv1b2];
        double dum;

        while (l < m) {
            i = l;
            j = m;
            do {
                while (values[i] < med) {
                    i++;
                }
                while (med < values[j]) {
                    j--;
                }
                dum = values[j];
                values[j] = values[i];
                values[i] = dum;
                i++;
                j--;
            } while ((j >= nv1b2) && (i <= nv1b2));
            if (j < nv1b2) {
                l = i;
            }
            if (nv1b2 < i) {
                m = j;
            }
            med = values[nv1b2];
        }
        return med;
    }

    /**
     * Convolution with a kernel
     *
     * @param kernel the kernel
     * @param D the divisor
     * @return the convolved value
     */
    public double convolve(double[] kernel, double D) {
        double sum;
        if (size != kernel.length) {
            return -1;
        } else {
            sum = 0;
            for (int i = 0; i < size; i++) {
                sum += values[i] * kernel[i];
            }
            sum /= D;
        }
        return sum;
    }

    /**
     *
     * @param value
     * @return
     */
    public int indexOf(double value) {
        int i = 0;
        while ((i < size) && (values[i] != value)) {
            i++;
        }
        if (i == size) {
            i = -1;
        }
        return i;
    }

    /**
     * Index to get x % of the values
     *
     * @param percent percentage(1.0=100; 0.5=50% ...)
     * @return index in the array
     */
    public int indexOfSumPercent(double percent) {
        int total = 0;
        int sum = 0;
        for (int i = 0; i < size; i++) {
            total += values[i];
        }

        for (int i = 0; i < size; i++) {
            sum += values[i];
            if ((double) sum >= percent * total) {
                return (i > 0 ? i - 1 : 0);
            }
        }
        return size - 1;
    }

    /**
     * The value is in the array ?
     *
     * @param val the value
     * @return true if value in the array
     */
    public boolean hasValue(int val) {
        int i = 0;
        boolean in = false;
        while ((!in) && (i < size)) {
            if (values[i] == val) {
                in = true;
            }

            i++;
        }
        return in;
    }

    /**
     * One of the values are in the array ?
     *
     * @param val the values
     * @return true if one of the values is in the array
     */
    public boolean hasOneValue(ArrayList vals) {
        int i = 0;
        boolean in = false;
        while ((!in) && (i < size)) {
            //IJ.log("" + i + " " + values[i] + " " + vals.contains(values[i]));
            if (vals.contains(values[i])) {
                in = true;
            }
            i++;
        }
        return in;
    }

    /**
     * One of the values are in the array ? For int values (does not work if
     * ArrayList is int and values is double)
     *
     * @param val the values
     * @return true if one of the values is in the array
     */
    public boolean hasOneValueInt(ArrayList vals) {
        int i = 0;
        boolean in = false;
        while ((!in) && (i < size)) {
            //IJ.log("" + i + " " + values[i] + " " + vals.contains(values[i]));
            if (vals.contains((int) values[i])) {
                in = true;
            }
            i++;
        }
        return in;
    }

    /**
     * test if array has only one particular value
     *
     * @param val the value
     * @return true if only this value is inside the array
     */
    public boolean hasOnlyValue(int val) {
        int i = 0;
        while (i < size) {
            if (values[i] != val) {
                return false;
            }
            i++;
        }
        return true;
    }

    /**
     * test if array has only particular values
     *
     * @param val the value
     * @return true if only this value is inside the array
     */
    public boolean hasOnlyValuesInt(ArrayList vals) {
        int i = 0;
        while (i < size) {
            if (!vals.contains((int) values[i])) {
                return false;
            }
            i++;
        }
        return true;
    }

    /**
     * isodata algorithm (for histogram)
     *
     * @return the computed isodata value
     */
    public int IsoData() {
        int sup = this.getLimitSup();
        int m1 = sup / 2;
        int m0 = 0;
        double md;
        double mg;
        double nombre_pixels;
        int nb = 0;

        while ((Math.abs(m0 - m1) > 2) && (nb < size)) {
            nb++;
            m0 = m1;
            md = 0;
            nombre_pixels = 0;
            if (debug) {
                IJ.log("size=" + size + " m0=" + m0);
            }

            for (int i = 0; i < m0; i++) {
                nombre_pixels = nombre_pixels + values[i];
                md = md + values[i] * i;
            }
            if (debug) {
                IJ.log("md=" + md + " nb=" + nombre_pixels);
            }

            md = md / nombre_pixels;
            mg = 0;
            nombre_pixels = 0;
            for (int i = m0; i <= sup; i++) {
                nombre_pixels = nombre_pixels + values[i];
                mg = mg + values[i] * i;
            }
            if (debug) {
                IJ.log("mg=" + mg + " nb=" + nombre_pixels);
            }

            mg = mg / nombre_pixels;
            m1 = (int) ((md + mg) / 2);
            if (debug) {
                IJ.log("m0=" + m0 + " m1=" + m1);
            }
        }
        if (nb == size) {
            if (debug) {
                System.out.println("Pb convergence moyenne intermediaire");
            }
        }
        return m1;
    }

    /**
     * drawing of the array as an image
     *
     * @param scale scale value
     */
    public ImagePlus drawBar(int scale) {
        int haut = 256;
        double val;
        double max = getMaximum();
        ByteProcessor ip = new ByteProcessor(size, haut);
        ip.setColor(255);
        for (int i = 0; i < size; i++) {
            ip.moveTo(i, haut - 1);
            if (scale == -1) {
                val = (haut * values[i] / max);
            } else if (values[i] > scale) {
                val = haut - 1;
            } else {
                val = (haut * values[i] / scale);
            }

            ip.lineTo(i, (int) (haut - val));
        }
        return new ImagePlus("tab", ip);
    }

    /**
     * random mix of the values
     */
    public void randomize() {
        sorted = false;
        for (int i = 0; i < size - 1; i++) {
            int pos = (int) Math.round(Math.random() * (size - 1 - i) + i);
            double aux = values[i];
            values[i] = values[pos];
            values[pos] = aux;
        }
    }

    /**
     * put values inside an array
     *
     * @param tmp the array of values to put
     */
    public void setValues(ArrayUtil tmp) {
        if (tmp.getSize() > values.length) {
            values = new double[tmp.getSize()];
        }

        this.setSize(tmp.getSize());
        for (int i = 0; i < size; i++) {
            values[i] = tmp.getValue(i);
        }
        sorted = false;
    }

    public void insertValues(int pos, ArrayUtil tmp) {
        if (tmp.getSize() + pos > values.length) {
            double[] values2 = new double[tmp.getSize() + pos];
            System.arraycopy(values, 0, values2, 0, pos);
            values = values2;
            size = values.length;
        }
        System.arraycopy(tmp.getArray(), 0, values, pos, tmp.getSize());
        size = Math.max(size, pos + tmp.getSize());
        sorted = false;
    }

    /**
     * test if two arrays are identical
     *
     * @param other the other array
     * @return true if arrays are equal
     */
    public boolean isEqual(ArrayUtil other) {
        if (other.getSize() != this.getSize()) {
            return false;
        }
        return isEqual(other, 0, this.getSize() - 1);
    }

    /**
     * test if two arrays are identical between two indexes
     *
     * @param other the other array
     * @param begin start index
     * @param end end index
     * @return The equal value
     */
    public boolean isEqual(ArrayUtil other, int begin, int end) {
        boolean result = true;
        for (int i = begin; i <= end; i++) {
            if (this.values[i] != other.values[i]) {
                result = false;
            }
        }

        return result;
    }

    /**
     * Gets the copy of the ArrayUtil object
     *
     * @return The copy array
     */
    public ArrayUtil getCopy() {
        ArrayUtil result = new ArrayUtil(this.size);
        System.arraycopy(this.values, 0, result.values, 0, this.size);
        return result;
    }

    /**
     * information to be displayed
     *
     * @return text
     */
    @Override
    public String toString() {
        String str = "{" + values[0];
        for (int i = 1; i < size; i++) {
            str = str + ", " + values[i];
        }
        return str + "}";
    }

    /**
     * remove a value at a position
     *
     * @param index the position index
     */
    public void removeValueAt(int index) {
        size--;
        for (int i = index; i < size; i++) {
            values[i] = values[i + 1];
        }
        // does not change sorted state
    }

    /**
     *
     * @param tabToAdd
     */
    public void addValueArray(ArrayUtil tabToAdd) {
        // tester tailles identiques
        if (size != tabToAdd.size) {
            return;
        }

        for (int i = 0; i < size; ++i) {
            values[i] += tabToAdd.values[i];
        }
        sorted = false;
    }

    public Plot getPlot() {
        double[] xVal = new double[size];
        for (int i = 0; i < xVal.length; i++) {
            xVal[i] = i;
        }
        Plot plot = new Plot("Plot", "indices", "y values", xVal, values);
        plot.draw();

        return plot;
    }

    /**
     * concat an array to the end
     *
     * @param tabToAdd the array to concat
     */
    public void concat(ArrayUtil tabToAdd) {
        int newsize = size + tabToAdd.size;
        double[] tmp = new double[newsize];
        System.arraycopy(values, 0, tmp, 0, size);
        System.arraycopy(tabToAdd.values, 0, tmp, size, tabToAdd.size);
        this.values = tmp;
        size = newsize;
        sorted = false;
    }

    public void reverse() {
        for (int i = 0; i < size / 2; i++) {
            double tmp = values[i];
            values[i] = values[size - 1 - i];
            values[size - 1 - i] = tmp;
        }
    }

    /**
     * extract a sub-array from this array
     *
     * @param startindex start index
     * @param newsize new size
     * @return The subTabUtil value
     */
    public ArrayUtil getSubTabUtil(int startindex, int newsize) {

        ArrayUtil tmp = new ArrayUtil(newsize);
        for (int i = 0; i < newsize; i++, startindex++) {
            tmp.putValue(i, getValue(startindex));
        }
        return tmp;
    }

    public ArrayUtil getDifferenceNext() {
        ArrayUtil diff = new ArrayUtil(getSize() - 1);
        for (int i = 0; i < diff.getSize(); i++) {
            diff.putValue(i, getValue(i + 1) - getValue(i));
        }
        return diff;
    }

    public ArrayUtil getDifferenceNextAbs() {
        ArrayUtil diff = new ArrayUtil(getSize() - 1);
        for (int i = 0; i < diff.getSize(); i++) {
            diff.putValue(i, Math.abs(getValue(i + 1) - getValue(i)));
        }
        return diff;
    }

    /**
     *
     * @return
     */
    public ArrayUtil getIntegerHistogram() {
        double[] ynumber = new double[(int) getMaximum() + 1];
        int nbins = ynumber.length;
        double val;
        int bi;
        int si = this.getSize();
        for (int i = 0; i < nbins; i++) {
            ynumber[i] = 0;
        }
        for (int i = 0; i < si; i++) {
            val = this.getValue(i);
            bi = (int) (val);
            if (bi >= nbins) {
                bi = nbins - 1;
            }
            if (bi < 0) {
                bi = 0;
            }
            ynumber[bi]++;
        }

        return new ArrayUtil(ynumber);
    }

    public int getMode() {
        return getIntegerHistogram().getMaximumIndex();
    }

    public int getModeNonZero() {
        return (int) getIntegerHistogram().getMaximumStarting(1)[1];
    }

    /**
     * Fit a gaussian to the values (radial distribution)
     *
     * @param values to fit
     * @param initSD
     * @param maxR maximum interval
     * @return the gaussian fitted values
     */
    public static double[] fitGaussian(double[] values, double initSD, int maxR) {
        //IJ.log("\nGaussian Fitting ");
        // find extrem NaN
        int cm = 0;
        while (Double.isNaN(values[cm])) {
            cm++;
        }
        // copy
        if (cm > 0) {
            maxR -= cm;
            double[] vv = new double[values.length - 2 * cm];
            for (int i = 0; i < vv.length; i++) {
                vv[i] = values[i + cm];
            }
            values = vv;
        }
        double[] id = new double[values.length];
        int c = 0;
        double minVal = values[0];
        double maxVal = values[0];
        double val;
        for (int i = -maxR; i <= maxR; i++) {
            val = values[c];
            if (val > maxVal) {
                maxVal = val;
            }
            if (val < minVal) {
                minVal = val;
            }
            id[c] = i;
            c++;
        }
        //IJ.log("min=" + minVal + " max=" + maxVal);
        // fitting by a gaussian
        CurveFitter fit = new CurveFitter(id, values);
        double[] params = new double[4];
        params[0] = minVal;
        params[1] = maxVal;
        params[2] = 0;
        params[3] = initSD;
        //IJ.log("res=" + params[0] + " " + params[1] + " " + params[2] + " " + params[3]);

        fit.setInitialParameters(params);
        fit.setMaxIterations(10000);
        fit.setRestarts(1000);
        fit.doFit(CurveFitter.GAUSSIAN);
        params = fit.getParams();
        //IJ.log("res=" + params[0] + " " + params[1] + " " + params[2] + " " + params[3]);
        //IJ.log("error=" + fit.getRSquared());
        if (Double.isNaN(params[0])) {
            return null;
        }

        return params;
    }

    /**
     * COPIED FROM ICY (A. DUFOUR) KMeans classification algorithm, optimized
     * for 1D histogram data. The algorithm is initialized by spacing the class
     * centers equally
     *
     * @param histogram the histogram to classify
     * @param nbClasses the number of classes to extract
     * @return the optimal class centers after convergence
     */
    public static int[] kMeans_Histogram1D(int[] histogram, int nbClasses, int thmin) {
        int[] centers = new int[nbClasses];
        double[] sums = new double[nbClasses];
        double[] nbElements = new double[nbClasses];

        // get first and last non-zero
        ArrayUtil tab = new ArrayUtil(histogram);
        int start = tab.getLimitInf(0);
        int end = tab.getLimitSup();
        if ((thmin > start) && (thmin < end)) {
            start = thmin;
        }
        double step = (end - start) / (nbClasses);

        for (int i = 0; i < nbClasses; i++) {
            centers[i] = (int) (start + (i + 1) * step);
        }

        // basic class initialization : regularly divide the space
//        for (int i = 0; i < nbClasses; i++) {
//            centers[i] = (int) ((histogram.length - 1f) * (i + 1f) / (nbClasses + 1f));
//        }
        // main loop
        boolean convergence = false;
        int count = 1;
        while (!convergence) {
            IJ.showStatus("K-means " + count);
            count++;
            // assume the convergence is reached
            // (invalidate this assumption later if class means move)
            convergence = true;
            java.util.Arrays.fill(nbElements, 0);
            java.util.Arrays.fill(sums, 0);

            for (int i = thmin; i < histogram.length; i++) {
                if (histogram[i] == 0) {
                    continue;
                }
                int closestClass = 0;
                double minDistance = Double.MAX_VALUE;
                // compute the shortest distance from the current bin
                // to a class center and assign the bin to that class
                for (int k = 0; k < nbClasses; k++) {
                    double distance = Math.abs(i - centers[k]);
                    if (distance < minDistance) {
                        minDistance = distance;
                        closestClass = k;
                    }
                }

                // to update each class center to the mean,
                // accumulate both number and sum per class
                double nbElemInCurrentBin = histogram[i];
                sums[closestClass] += i * nbElemInCurrentBin;
                nbElements[closestClass] += nbElemInCurrentBin;
            }

            // once all bins have been assigned to a class,
            // the class centers can be moved toward the new means
            for (int k = 0; k < nbClasses; k++) {
                int oldCenter = centers[k];
                int newCenter = (int) (sums[k] / nbElements[k]);
                convergence &= (oldCenter == newCenter);
                centers[k] = newCenter;
            }
        }

        return centers;
    }
}
