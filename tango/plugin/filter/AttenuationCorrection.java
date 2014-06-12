/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tango.plugin.filter;

import ij.ImagePlus;
import mcib3d.image3d.ImageHandler;
import tango.dataStructure.InputImages;
import tango.gui.Core;
import tango.parameter.ChoiceParameter;
import tango.parameter.ConditionalParameter;
import tango.parameter.IntParameter;
import tango.parameter.Parameter;
import static tango.plugin.filter.EraseSpots.methods;

/**
 * This class performs attenuation correction in 3D image stacks.
 * Copyright (C) 2012 Philippe Andrey
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License 
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 * 
 * Author: Philippe Andrey (philippe.andrey@versailles.inra.fr)
 * Co-author: Eric Biot (eric.biot@versailles.inra.fr)
 * Co-author: Souad Blila (souad.blila@versailles.inra.fr)
 * 
 * Reference: 
 * E Biot, E Crowell, H Höfte, Y Maurin, S Vernhettes & P Andrey (2008).
 * A new filter for spot extraction in N-dimensional biological imaging.
 * In Fifth IEEE International Symposium on Biomedical Imaging (ISBI'08): 
 * From Nano to Macro, pp. 975-978.
 */

public class AttenuationCorrection implements PreFilter {
    boolean verbose;
    int nCPUs=1;
    IntParameter radius = new IntParameter("Opening Radius", "rad", 3);
    static String[] methods = new String[]{"Constant Slice Index", "Middle Slice", "Maximum Intensity"};
    ChoiceParameter choice = new ChoiceParameter("Reference Slice:", "referenceSlice", methods, methods[2]);
    ConditionalParameter cond = new ConditionalParameter(choice);
    IntParameter referenceSlice = new IntParameter("Reference Slice", "slice", 1);
    Parameter[] parameters = new Parameter[]{radius, cond};
    
    public AttenuationCorrection() {
        radius.setHelp("radius of the morphological opening used to estimate background images. Hint: The radius should be as large as the radius (or thickness) of the smallest objects.", true);
        cond.setHelp("index of the slice selected as reference (this slice will not be modified). Hint: The reference slice will generally be the first (upper-most) slice containing objects. Warning: in tango, this value should be adjusted for each field", true);
        cond.setCondition(methods[0], new Parameter[]{referenceSlice});
    }
    
    @Override
    public ImageHandler runPreFilter(int currentStructureIdx, ImageHandler input, InputImages images) {
        int refSlice = getReferenceSlice(input);
        if (verbose) ij.IJ.log("Attenuation Correction:: reference slice:"+refSlice);
        Attenuation_Correction corr = new Attenuation_Correction(radius.getIntValue(3), refSlice, input.getType());
        ImagePlus backgroundImagePlus;
        ImagePlus correctedImagePlus;   
        backgroundImagePlus = corr.estimateBackground( input.getImagePlus() );
        correctedImagePlus = corr.correctAttenuation( input.getImagePlus(), backgroundImagePlus );
        if (verbose) backgroundImagePlus.show();
        else backgroundImagePlus.flush();
        ImageHandler res = ImageHandler.wrap(correctedImagePlus);
        res.setTitle(input.getTitle()+"::attenuationcorrected");
        
        return res;
    }
    
    private int getReferenceSlice(ImageHandler input) {
        if (choice.getSelectedIndex()==0) return Math.min(input.sizeZ, Math.max(referenceSlice.getIntValue(1), 1));
        else if (choice.getSelectedIndex()==1) return (int)(input.sizeZ/2);
        else if (choice.getSelectedIndex()==2) {
            // max intensity
            double max = 0;
            for (int xy=0;xy<input.sizeXY; xy++) max+=input.getPixel(xy, 0);
            int maxSlice=0;
            for (int z = 1; z<input.sizeZ; z++) {
                double sum = 0;
                for (int xy=0;xy<input.sizeXY; xy++) sum+=input.getPixel(xy, z);
                if (sum>max) {
                    max=sum;
                    maxSlice=z;
                }
            }
            return maxSlice+1;
        } else return 1;
    }

    @Override
    public Parameter[] getParameters() {
        return parameters;
    }

    @Override
    public String getHelp() {
        return "This class performs attenuation correction in 3D image stacks. \n "
                 + "Author: Philippe Andrey (philippe.andrey@versailles.inra.fr)"
 + " Co-author: Eric Biot (eric.biot@versailles.inra.fr)"
 + " Co-author: Souad Blila (souad.blila@versailles.inra.fr)"
 + " "
 + " Reference: "
 + " E Biot, E Crowell, H Höfte, Y Maurin, S Vernhettes & P Andrey (2008)."
 + " A new filter for spot extraction in N-dimensional biological imaging."
 + " In Fifth IEEE International Symposium on Biomedical Imaging (ISBI'08): "
 + " From Nano to Macro, pp. 975-978.";
    }

    @Override
    public void setVerbose(boolean verbose) {
        this.verbose=verbose;
    }

    @Override
    public void setMultithread(int nCPUs) {
        this.nCPUs=nCPUs;
    }
    
}
