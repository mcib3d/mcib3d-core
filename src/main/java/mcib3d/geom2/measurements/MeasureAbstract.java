package mcib3d.geom2.measurements;

import mcib3d.geom2.Object3DInt;
import mcib3d.geom2.Object3DComputation;

import java.util.HashMap;

public abstract class MeasureAbstract {
    protected static final String VALUE = "Value";
    protected final Object3DComputation computation3D;
    protected final Object3DInt object3DInt;
    protected final HashMap<String, Double> keysValues = new HashMap();

    public MeasureAbstract(Object3DInt object3DInt) {
        this.object3DInt = object3DInt;
        this.computation3D = new Object3DComputation(object3DInt);
        keysValues.put(VALUE, Double.valueOf(object3DInt.getValue()));
    }

    protected abstract String[] getNames();

    public String[] getNamesMeasurement(){
        String[] names = getNames();
        String[] namesMeasurement = new String[names.length+1];
        namesMeasurement[0]=VALUE;
        for (int i = 0; i < names.length; i++) namesMeasurement[i+1] = new String(names[i]);

        return namesMeasurement;
    }

    public Double getValueMeasurement(String name) {
        if (keysValues.get(name) == null) computeAll();

        return keysValues.get(name);
    }

    public Double[] getValuesMeasurement() {
        String[] names = getNamesMeasurement();
        Double[] values = new Double[names.length];
        for (int i = 0; i < names.length; i++) values[i] = getValueMeasurement(names[i]);

        return values;
    }

    protected abstract void computeAll();

}
