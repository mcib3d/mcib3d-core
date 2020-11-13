/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mcib3d.image3d.distanceMap3d;

import mcib3d.image3d.ImageFloat;
import mcib3d.image3d.ImageHandler;

/**
 *
 * @author thomasb
 */
public class EDT_1D {

    ImageHandler input;

    public EDT_1D(ImageHandler input) {
        this.input = input;
    }

    private void EDTAxisXPos(ImageFloat res, int y, int z) {
        float dist;
        int x = 0;
        int wi = input.sizeX;
        // start with first non-zero
        while ((x < wi) && (input.getPixel(x, y, z) == 0)) {
            x++;
        }
        while (x < wi) {
            while ((x < wi) && (input.getPixel(x, y, z) > 0)) {
                x++;
            }
            dist = 1;
            while ((x < wi) && (input.getPixel(x, y, z) == 0)) {
                res.setPixel(x, y, z, dist);
                x++;
                dist++;
            }
        }
    }

    private void EDTAxisXNeg(ImageFloat res, int y, int z) {
        float dist;
        int wi = input.sizeX;
        int x = wi - 1;
        // start with first non-zero
        while ((x >= 0) && (input.getPixel(x, y, z) == 0)) {
            x--;
        }
        while (x >= 0) {
            while ((x >= 0) && (input.getPixel(x, y, z) > 0)) {
                x--;
            }
            dist = -1;
            while ((x >= 0) && (input.getPixel(x, y, z) == 0)) {
                if (res.getPixel(x, y, z) == 0) {
                    res.setPixel(x, y, z, dist);
                } else {
                    res.setPixel(x, y, z, 0);
                }
                x--;
                dist--;
            }
        }
    }

    public ImageFloat process1D() {
        ImageFloat res = new ImageFloat("EDT_1D", input.sizeX, input.sizeY, input.sizeZ);
        for (int z = 0; z < input.sizeZ; z++) {
            for (int y = 0; y < input.sizeY; y++) {
                EDTAxisXPos(res, y, z);
                EDTAxisXNeg(res, y, z);
            }
        }
        return res;
    }

}
