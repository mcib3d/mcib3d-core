package mcib3d.geom2.measurements;

import mcib3d.geom2.Object3D;
import mcib3d.geom2.Object3DComputation;

import java.util.HashMap;

public abstract class MeasureAbstract {
    protected final Object3DComputation computation3D;
    protected final Object3D object3D;
    protected final HashMap<String, Double> keysValues = new HashMap();

    public MeasureAbstract(Object3D object3D) {
        this.object3D = object3D;
        this.computation3D = new Object3DComputation(object3D);
    }

    public abstract String[] getNames();

    public Double getValue(String name) {
        if (keysValues.get(name) == null) computeAll();

        return keysValues.get(name);
    }

    public Double[] getValues() {
        String[] names = getNames();
        Double[] values = new Double[names.length];
        for (int i = 0; i < names.length; i++) values[i] = getValue(names[i]);

        return values;
    }

    protected abstract void computeAll();

}
