package mcib3d.image3d.processing;

import ij.IJ;
import ij.util.ThreadUtil;
import mcib3d.geom.Objects3DPopulation;
import mcib3d.image3d.ImageFloat;
import mcib3d.image3d.ImageHandler;

import java.util.concurrent.atomic.AtomicInteger;

public class Density3D {
    private int neighbours;
    private double sigma;

    public Density3D(int neighbours, double sigma) {
        this.neighbours = neighbours;
        this.sigma = sigma;
    }

    public ImageHandler computeDensity(ImageHandler handler, boolean multi) {
        final ImageHandler img = handler;
        ImageHandler res = new ImageFloat("density", img.sizeX, img.sizeY, img.sizeZ);
        Objects3DPopulation population = new Objects3DPopulation(img);
        population.createKDTreeCenters();
        // non parallel version
        if (!multi) {
            densityProcess(img, population, res, 0, handler.sizeZ, neighbours, sigma);
        } else { // parallel version
            // PARALLEL, need to duplicate populations
            int neighbours2 = Math.min(neighbours, population.getNbObjects());
            final AtomicInteger ai = new AtomicInteger(0);
            final int n_cpus = ThreadUtil.getNbCpus();
            final int dec = (int) Math.ceil((double) handler.sizeZ / (double) n_cpus);
            Thread[] threads = ThreadUtil.createThreadArray(n_cpus);
            int bound = threads.length;
            for (int iThread = 0; iThread < bound; iThread++) {
                threads[iThread] = new Thread(() -> {
                    for (int k = ai.getAndIncrement(); k < n_cpus; k = ai.getAndIncrement()) {
                        Objects3DPopulation pop = new Objects3DPopulation(img);
                        densityProcess(img, pop, res, dec * k, dec * (k + 1), neighbours2, sigma);
                    }
                });
            }
            ThreadUtil.startAndJoin(threads);
        }

        res.setScale(img);
        
        return res;
    }

    private void densityProcess(ImageHandler in, Objects3DPopulation population, ImageHandler out, int zmin, int zmax, int nk, double sigma) {
        zmax = Math.min(zmax, in.sizeZ);
        double coeff = 1.0 / (2.0 * sigma * sigma);
        for (int z = zmin; z < zmax; z++) {
            IJ.showStatus("Density slice " + z);
            for (int x = 0; x < in.sizeX; x++) {
                for (int y = 0; y < in.sizeY; y++) {
                    double[] dists = population.kClosestDistancesSquared(x, y, z, nk);
                    double density = 0;
                    for (int i = 0; i < nk; i++) {
                        density += Math.exp(-dists[i] * coeff);
                    }
                    out.setPixel(x, y, z, (float) density);
                }
            }
        }
    }
}
