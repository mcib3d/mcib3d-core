package mcib3d.geom2;

import mcib3d.geom2.measurements.MeasureAbstract;
import mcib3d.geom2.measurements.MeasureIntensity;
import mcib3d.image3d.ImageHandler;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Objects3DIntPopulation {
    private List<Object3DInt> objects3DInt = null;
    private double resXY = 1.0;
    private double resZ = 1.0;

    Map<Float, Object3DInt> objectsByValue;

    public Objects3DIntPopulation(ImageHandler handler) {
        resXY = handler.getScaleXY();
        resZ = handler.getScaleZ();

        objects3DInt = new LinkedList<>();
        objectsByValue = new HashMap<>();
        buildObjects(handler);
    }

    public Object3DInt getFirstObject() {
        return objects3DInt.get(0);
    }

    public Object3DInt getObjectByValue(float val) {
        return objectsByValue.get(val);
    }

    private void buildObjects(ImageHandler handler) {
        HashMap<Float, Object3DInt> objects = new HashMap<>();
        for (int z = 0; z < handler.sizeZ; z++) {
            HashMap<Float, List<VoxelInt>> map = buildObjectsPlane(handler, z);
            for (Float val : map.keySet()) {
                if (objects.containsKey(val)) {
                    // System.out.println("adding plane "+z+" to object "+val+" with size "+map.get(val).size());
                    objects.get(val).addPlane(new Object3DPlane(map.get(val), z));
                } else { // new Object3D
                    // System.out.println("New object found at "+z+" with value "+val+" with size "+map.get(val).size());
                    Object3DInt object3DInt = new Object3DInt(val);
                    object3DInt.addPlane(new Object3DPlane(map.get(val), z));
                    objects.put(val, object3DInt);
                    objectsByValue.put(val, object3DInt);
                }
            }
        }
        objects3DInt.addAll(objects.values());
    }

    public List<Double[]> getMeasurements(Class classe) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        List<Double[]> measures = new LinkedList<>();
        Constructor<MeasureAbstract> constructor = classe.getDeclaredConstructor(Object3DInt.class);

        for (Object3DInt object3DInt : objects3DInt) {
            MeasureAbstract measureAbstract = measureAbstract = constructor.newInstance(object3DInt);
            Double[] values = measureAbstract.getValuesMeasurement();
            measures.add(values);
        }

        return measures;
    }

    public List<Double[]> getMeasurementsIntensity(ImageHandler image){
        List<Double[]> measures = new LinkedList<>();

        for (Object3DInt object3DInt : objects3DInt) {
            MeasureIntensity intensity = new MeasureIntensity(object3DInt);
            intensity.setIntensityImage(image);
            Double[] values = intensity.getValuesMeasurement();
            measures.add(values);
        }

        return measures;
    }



    private HashMap<Float, List<VoxelInt>> buildObjectsPlane(ImageHandler handler, int z) {
        HashMap<Float, List<VoxelInt>> map = new HashMap<>();

        for (int i = 0; i < handler.sizeX; i++) {
            for (int j = 0; j < handler.sizeY; j++) {
                float pix = handler.getPixel(i, j, z);
                if (pix > 0) {
                    if (map.containsKey(pix)) {
                        map.get(pix).add(new VoxelInt(i, j, z, pix));
                    } else {
                        List<VoxelInt> list = new LinkedList<>();
                        list.add(new VoxelInt(i, j, z, pix));
                        map.put(pix, list);
                    }
                }
            }
        }

        return map;
    }

}
