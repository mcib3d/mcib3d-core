package mcib3d.geom.interactions;

import mcib3d.geom.Objects3DPopulation;
import mcib3d.image3d.ImageHandler;
import mcib3d.utils.ArrayUtil;

public class InteractionsComputeDamLines implements InteractionsCompute {

    @Override
    public InteractionsList compute(ImageHandler image) {
        // get population from image
        Objects3DPopulation population = new Objects3DPopulation(image);
        InteractionsList interactions = new InteractionsList();
        for (int z = 0; z < image.sizeZ; z++) {
            for (int x = 0; x < image.sizeX; x++) {
                for (int y = 0; y < image.sizeY; y++) {
                    if (image.getPixel(x, y, z) == 0) {
                        ArrayUtil util = image.getNeighborhood3x3x3(x, y, z); // 26 neighbors
                        util = util.distinctValues();
                        //int c = 0;
                        for (int i = 0; i < util.size(); i++) {
                            for (int j = i + 1; j < util.size(); j++) {
                                int vali = util.getValueInt(i);
                                int valj = util.getValueInt(j);
                                if ((vali > 0) && (valj > 0)) {
                                    if (!interactions.contains(vali, valj)) {
                                        interactions.addInteraction(population.getObjectByValue(vali), population.getObjectByValue(valj));
                                        //PairColocalisation pairColocalisation = new PairColocalisation(population.getObjectByValue(vali), population.getObjectByValue(valj));
                                        //interactions.addInteraction(pairColocalisation);
                                    }
                                    interactions.incrementPairVolume(vali, valj);
                                    // c++;
                                }
                            }
                        }
                        //if (c > 1) IJ.log("Multiple point " + c + " : " + x + " " + y + " " + z);
                    }
                }
            }
        }

        return interactions;
    }
}
