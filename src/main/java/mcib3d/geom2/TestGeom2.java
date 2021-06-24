package mcib3d.geom2;

import ij.ImagePlus;
import mcib3d.geom2.measurements.MeasureAbstract;
import mcib3d.geom2.measurements.MeasureDistancesCenter;
import mcib3d.geom2.measurements.MeasureEllipsoid;
import mcib3d.geom2.measurements.MeasureIntensity;
import mcib3d.image3d.ImageHandler;

import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.List;

public class TestGeom2 {

    public static void main(String[] args) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        // ImagePlus plus = new ImagePlus("/home/thomas/Shape3D.tif");
        // ImagePlus plus = new ImagePlus("/home/thomas/bat-cochlea-volume.tif");
        ImagePlus plus = new ImagePlus("/home/thomas/Download/draw.zip");
        //ImagePlus plus = new ImagePlus("/home/thomas/lymphNode.tif");
        ImageHandler handler = ImageHandler.wrap(plus);

        Instant instant0 = Instant.now();
        Objects3DIntPopulation population = new Objects3DIntPopulation(handler);
        Object3DInt object3DInt = population.getFirstObject();
        int value = (int) object3DInt.getValue();
        System.out.println("First object " + value);
        MeasureAbstract distancesCenter = new MeasureEllipsoid(object3DInt);
        for (String name : distancesCenter.getNamesMeasurement()) {
            System.out.println(name + " " + distancesCenter.getValueMeasurement(name));
        }

        //
        List<Double[]> list = population.getMeasurements(MeasureEllipsoid.class);
        System.out.println("List " + list.size());
        for (Double[] doubles : list) {
            for (Double v : doubles) {
                System.out.println(v);
            }
        }

        list = population.getMeasurementsIntensity(handler);
        System.out.println("List " + list.size());
        for (Double[] doubles : list) {
            for (Double v : doubles) {
                System.out.println(v);
            }
        }


    }


}
