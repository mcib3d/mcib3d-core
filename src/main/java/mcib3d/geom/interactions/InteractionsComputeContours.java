package mcib3d.geom.interactions;

import mcib3d.geom.Object3D;
import mcib3d.geom.Objects3DPopulation;
import mcib3d.geom.Voxel3D;
import mcib3d.image3d.ImageHandler;
import mcib3d.utils.ArrayUtil;

import java.util.LinkedList;

public class InteractionsComputeContours implements InteractionsCompute {
    @Override
    public InteractionsList compute(ImageHandler image) {
        // get population from image
        Objects3DPopulation population = new Objects3DPopulation(image);
        InteractionsList interactions = new InteractionsList();
        for (Object3D object3D : population.getObjectsList()) {
            LinkedList<Voxel3D> list = object3D.getContours();
            for (Voxel3D voxel3D : list) {
                ArrayUtil util = image.getNeighborhood3x3x3(voxel3D.getRoundX(), voxel3D.getRoundY(), voxel3D.getRoundZ());
                util = util.distinctValues();
                //int c = 0;
                for (int i = 0; i < util.size(); i++) {
                    for (int j = i + 1; j < util.size(); j++) {
                        int vali = util.getValueInt(i);
                        int valj = util.getValueInt(j);
                        if ((vali > 0) && (valj > 0)) {
                            if ((vali == object3D.getValue()) || (valj == object3D.getValue())) {
                                //String key = util.getValueInt(i) + "-" + util.getValueInt(j);
                                if (!interactions.contains(vali, valj)) {
                                    //PairColocalisation pairColocalisation = new PairColocalisation(population.getObjectByValue(util.getValueInt(i)), population.getObjectByValue(util.getValueInt(j)));
                                    //interactions.put(key, pairColocalisation);
                                    interactions.addInteraction(population.getObjectByValue(vali), population.getObjectByValue(valj));
                                }
                                interactions.incrementPairVolume(vali, valj);
                                //c++;
                            }
                        }
                    }
                }
                //if (c > 1)
                //    IJ.log("Multiple point " + c + " : " + voxel3D.getRoundX() + " " + voxel3D.getRoundY() + " " + voxel3D.getRoundZ());
            }
        }

        return interactions;
    }
}
