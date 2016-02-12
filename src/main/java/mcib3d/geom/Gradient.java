
package mcib3d.geom;

/**
Copyright (C) Thomas Boudier

License:
This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *

/**
 *
 * @author dimitri
 */
public class Gradient {

    /**
     * Transition linéaire suivant la fonction f(x) = x
     */
    public static final int TRANSITION_LINEAR = 0;
    /**
     * Transition circulaire entre 6H et 9H : f(x) = 1 - sqrt(1 - (x-1)^2)
     */
    public static final int TRANSITION_ARC_69 = 1;
    /**
     * Transition circulaire entre 9H et 12H : f(x) = sqrt(1 - (x-1)^2)
     */
    public static final int TRANSITION_ARC_90 = 2;
    /**
     * Transition circulaire entre 0H et 3H : f(x) = sqrt(1 - x^2)
     */
    public static final int TRANSITION_ARC_03 = 3;
    /**
     * Transition circulaire entre 3H et 6H : f(x) = 1 - sqrt(1 - x^2)
     */
    public static final int TRANSITION_ARC_36 = 4;
    /**
     * Transition suivant une loi gaussienne [f(1) = 0.99] : f(x) = 1 - e^(-4.6 * x^2)
     */
    public static final int TRANSITION_GAUSSIAN_01 = 5;
    /**
     * Transition suivant une loi gaussienne [f(1) = 0.999] : f(x) = 1 - e^(-6.9 * x^2)
     */
    public static final int TRANSITION_GAUSSIAN_001 = 6;
    /**
     * Transition suivant une loi gaussienne [f(1) = 0.9999] : f(x) = 1 - e^(-9.21 * x^2)
     */
    public static final int TRANSITION_GAUSSIAN_0001 = 7;
    /**
     * Axe de rotations
     */
    public static final Vector3D X_ROTATION = new Vector3D(1, 0, 0);
    /**
     * Y rotation
     */
    public static final Vector3D Y_ROTATION = new Vector3D(0, 1, 0);
    /**
     * Z rotation
     */
    public static final Vector3D Z_ROTATION = new Vector3D(0, 0, 1);
    int transition;
    int[] colors;
    double[] ratios;
    double[] alphas;
    // Matrice de transformation
    GeomTransform3D matrix = new GeomTransform3D();
    // Centre de rotation de l'image
    Vector3D center = new Vector3D(0.5, 0.5, 0.5);
    // Valeur alpha du dernier point calculé
    double alpha = 1;

    /**
     * Constructeur de l'objet Gradient
     */
    public Gradient() {
    }

    /**
     * Constructeur de l'objet Gradient
     *
     * @param c Valeur des pixels limites
     * @param a Opacité des pixels limites [0-1]
     * @param r Ratios des pixels limites [0-1]
     */
    public Gradient(int[] c, double[] a, double[] r) {
        init(c, a, r, TRANSITION_LINEAR);
    }

    /**
     * Constructeur de l'objet Gradient
     *
     * @param c Valeur des pixels limites
     * @param a Opacité des pixels limites [0-1]
     * @param r Ratios des pixels limites [0-1]
     * @param t Type de transition
     */
    public Gradient(int[] c, double[] a, double[] r, int t) {
        init(c, a, r, t);
    }

    private void init(int[] c, double[] a, double[] r, int t) {
        this.setValues(c);
        this.setAlphas(a);
        this.setRatios(r);
        this.setTransition(t);
    }

    /**
     *
     * @param pcentX pourcentage du gradient sur X
     * @param pcentY pourcentage du gradient sur Y
     * @param pcentZ pourcentage du gradient sur Z
     * @return valeur du pixel d'après le gradient, la direction, etc...
     */
    public int getLinearPixelAt(double pcentX, double pcentY, double pcentZ) {
        Vector3D positions = matrix.getVectorTransformed(new Vector3D(pcentX, pcentY, pcentZ), center);

        return getValue(positions.getX());
    }

    /**
     * Get the pixel value at ???
     * @param pcentX
     * @param pcentY
     * @param pcentZ
     * @return
     */
    public int getRadialPixelAt(double pcentX, double pcentY, double pcentZ) {
        Vector3D positions = matrix.getVectorTransformed(new Vector3D(pcentX, pcentY, pcentZ), center);
        positions.setX(2 * (positions.getX() - center.getX()));
        positions.setY(2 * (positions.getY() - center.getY()));
        positions.setZ(2 * (positions.getZ() - center.getZ()));

        return getValue(positions.getLength());
    }

    private int getValue(double local) {
        if (local <= 0) {
            alpha = alphas[0];
            return colors[0];
        } else if (local >= 1) {
            alpha = alphas[alphas.length - 1];
            return colors[colors.length - 1];
        }

        local = evaluate(local);

        int[] localColors = copy(colors[0], colors, colors[colors.length - 1]);
        double[] localAlphas = copy(alphas[0], alphas, alphas[alphas.length - 1]);
        double[] localRatios = copy(0, ratios, 1);

        int value = -1;

        int lr = localRatios.length - 1;
        for (int i = 0; i < lr; i++) {
            if (localRatios[i] <= local && localRatios[i + 1] > local) {
                double localPcent = (local - localRatios[i]) / (localRatios[i + 1] - localRatios[i]);

                alpha = (localAlphas[i + 1] - localAlphas[i]) * localPcent + localAlphas[i];
                value = (int) ((localColors[i + 1] - localColors[i]) * localPcent + localColors[i]);

                break;
            }
        }

        return value;
    }

    /**
     *
     * @return
     */
    public double getLastAlpha() {
        return alpha;
    }

    /**
     * Reprend tout à zéro
     *
     */
    public void reset() {
        alpha = 1;
    }

    /**
     * Définir la valeur des pixels limites.
     *
     * @param c Tableau donnant la valeur des pixels intermédiaires
     */
    public void setValues(int[] c) {
        colors = c.clone();
    }

    /**
     * Définir la transparence des pixels limites
     * 
     * @param a Tableau donnant la valeur de l'opacité des pixels intermédiaires
     */
    public void setAlphas(double[] a) {
        alphas = a.clone();
    }

    /**
     * Définir le ratio des pixels limites
     *
     * @param r Valeur comprise entre 0 et 1 indiquant la position sur le dégradé en pourcent
     */
    public void setRatios(double[] r) {
        ratios = r.clone();
    }

    /**
     * Définir la matrice de transformation
     *
     * @param gt
     */
    public void setTransformation(GeomTransform3D gt) {
        matrix = gt;
    }

    /**
     * Définir le centre du gradient. Permet de définir le centre de rotation,
     * mais permet aussi de déplacer le centre des gradients sphériques (aucun
     * effet sur les gradients cubiques)
     *
     * @param c Les coordonées du centre (0 = 0% et 1 = 100%)
     */
    public void setCenter(Vector3D c) {
        center.setCoord(c.getX(), c.getY(), c.getZ());
    }

    /**
     * Définir le centre du gradient. Permet de définir le centre de rotation,
     * mais permet aussi de déplacer le centre des gradients sphériques (aucun
     * effet sur les gradients cubiques)
     *
     * @param p Les coordonées du centre (0 = 0% et 1 = 100%)
     */
    public void setCenter(Point3D p) {
        center.setCoord(p.getX(), p.getY(), p.getZ());
    }

    /**
     * Ajoute ensuite une transformation de type translation à la matrice
     *
     * @param tx Translation selon X
     * @param ty Translation selon Y
     * @param tz Translation selon Z
     */
    public void appendTranslation(double tx, double ty, double tz) {
        matrix.setTranslation(-tx, -ty, -tz);
    }

    /**
     * Ajout ensuite une transformation de type homothétie à la matrice
     *
     * @param sx Valeur de multiplication de l'échelle sur X
     * @param sy Valeur de multiplication de l'échelle sur Y
     * @param sz Valeur de multiplication de l'échelle sur Z
     */
    public void appendScale(double sx, double sy, double sz) {
        matrix.setScale(1 / sx, 1 / sy, 1 / sz);
    }

    /**
     * Ajoute ensuite une transformation de type rotation à la matrice
     * selon un axe quelconque
     *
     * @param angle Angle de rotation (radian)
     * @param axe Axe de rotation (quelconque)
     */
    public void appendRotation(double angle, Vector3D axe) {
        matrix.setRotation(axe, angle);
    }

    /**
     * Définir le type de transition
     *
     * @param t Type de transition
     */
    public void setTransition(int t) {
        transition = t;
    }

    /**
     * Copie un tableau en ajoutant une valeur avant et après
     *
     * @param a Valeur à ajouter au début du tableau
     * @param b Tableau
     * @param c Valeur à ajouter en fin de tableau
     *
     * @return
     */
    private int[] copy(int a, int[] b, int c) {
        int l = b.length;
        int[] r = new int[l + 2];

        r[0] = a;
        for (int i = 0; i < l; i++) {
            r[i + 1] = b[i];
        }
        r[l + 1] = c;

        return r;
    }

    private double[] copy(double a, double[] b, double c) {
        int l = b.length;
        double[] r = new double[l + 2];

        r[0] = a;
        for (int i = 0; i < l; i++) {
            r[i + 1] = b[i];
        }
        r[l + 1] = c;

        return r;
    }

    /**
     * Corrige la valeur du pourcentage en fonction de la loi à suivre
     *
     * @param x Valeur à corriger (entre 0 et 1)
     * @return
     */
    protected double evaluate(double x) {
        double y = x;
        switch (transition) {
            case TRANSITION_LINEAR:
                break;
            case TRANSITION_ARC_03:
                y = Math.sqrt(1 - x * x);
                break;
            case TRANSITION_ARC_36:
                y = 1 - Math.sqrt(1 - x * x);
                break;
            case TRANSITION_ARC_69:
                y = x - 1;
                y = 1 - Math.sqrt(1 - y * y);
                break;
            case TRANSITION_ARC_90:
                y = x - 1;
                y = Math.sqrt(1 - y * y);
                break;
            case TRANSITION_GAUSSIAN_01:
                y = 1 - Math.exp(-4.6 * x * x);
                break;
            case TRANSITION_GAUSSIAN_001:
                y = 1 - Math.exp(-6.9 * x * x);
                break;
            case TRANSITION_GAUSSIAN_0001:
                y = 1 - Math.exp(-9.21 * x * x);
                break;
        }

        return y;
    }
}
