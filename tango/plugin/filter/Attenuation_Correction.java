package tango.plugin.filter;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.*;
import ij.plugin.filter.RankFilters;

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
public class Attenuation_Correction implements PlugIn
{
	private int _openingRadius = 1;
	private int _referenceSlice = 1;
	private double _minValidValue;
	private double _maxValidValue;
	
        public Attenuation_Correction(int openingRadius, int referenceSlice, int type) {
            this._openingRadius=openingRadius;
            this._referenceSlice=referenceSlice;
            switch ( type ) {
                case ImagePlus.GRAY8:
                  _minValidValue = 0;
                  _maxValidValue = 255;
                  break;
                case ImagePlus.GRAY16:
                  _minValidValue = 0;
                  _maxValidValue = 65535;
                  break;
                case ImagePlus.GRAY32:
                  _minValidValue = -Float.MAX_VALUE;
                  _maxValidValue = Float.MAX_VALUE;
                  break;
                default: 
                  IJ.error( "This plugin cannot process color images." );
                  return;
            }
        }
        
	public void run(String arg0)
	{
	  ImagePlus imagePlus = WindowManager.getCurrentImage();

	  if ( imagePlus == null )
	  {
	    IJ.error( "There is currently no image stack to process." );
	    return;
	  }

	  switch ( imagePlus.getType() )
	  {
  	  case ImagePlus.GRAY8:
  	    _minValidValue = 0;
  	    _maxValidValue = 255;
  	    break;
  	  case ImagePlus.GRAY16:
  	    _minValidValue = 0;
  	    _maxValidValue = 65535;
  	    break;
  	  case ImagePlus.GRAY32:
  	    _minValidValue = -Float.MAX_VALUE;
  	    _maxValidValue = Float.MAX_VALUE;
  	    break;
  	  default: 
  	    IJ.error( "This plugin cannot process color images." );
  	    return;
	  }
	  
	  if ( runDialog(imagePlus) )
	  {
	    exec( imagePlus );
	  }
	}
    
	/**
	 * Runs a dialog that let the user choose the plugin parameter values.
	 * 
	 * @param imagePlus Image stack to be processed
	 * @return True if the user clicked OK, false if the user clicked Cancel.
	 */
	public boolean runDialog(ImagePlus imagePlus)
	{
	  final int numSlices = imagePlus.getNSlices();
	  GenericDialog genericDialog = new GenericDialog( "Attenuation correction" );
	  genericDialog.addNumericField( "Opening radius", 3, 1 );
	  genericDialog.addSlider( "Reference slice", 1, numSlices, 1 );
	  genericDialog.showDialog();
	  setOpeningRadius( (int)genericDialog.getNextNumber() );
	  setReferenceSlice( (int)genericDialog.getNextNumber() );
   
   return genericDialog.wasOKed();
	}
	
	/**
	 * Sets the radius of the morphological opening.
	 * 
	 * @param openingRadius Opening radius.
	 */
	public void setOpeningRadius(int openingRadius)
	{
	  _openingRadius = openingRadius;
	}
	
  /**
   * Returns the radius of the morphological opening.
   */
	public int getOpeningRadius()
	{
	  return _openingRadius;
	}

  /**
   * Sets the index of the reference slice.
   * 
   * @param referenceSlice Index of reference slice.
   */
	public void setReferenceSlice(int referenceSlice)
	{
	  _referenceSlice = referenceSlice;
	}
	
  /**
   * Returns the index of the reference slice.
   */
	public int getReferenceSlice()
	{
	  return _referenceSlice;
	}
	
	/**
	 * Performs the attenuation correction, using the current settings.
	 * 
	 * @param inputImagePlus
	 * @param newName
	 */
	public void exec(ImagePlus inputImagePlus)
	{
	  ImagePlus backgroundImagePlus;
	  ImagePlus correctedImagePlus;        
	  backgroundImagePlus = estimateBackground( inputImagePlus );
	  backgroundImagePlus.show();
	  correctedImagePlus = correctAttenuation( inputImagePlus, backgroundImagePlus );
	  correctedImagePlus.show();
	}

	/**
	 * Estimates the background of an image stack using morphological filtering.
	 * 
	 * Each slice is filtered with a grey-level opening.
	 * 
	 * @param imagePlus Input image stack
	 * @return Background image stack
	 */
	public ImagePlus estimateBackground(ImagePlus imagePlus)
	{
	  final int numSlices = imagePlus.getNSlices();
	  ImageStack inputStack = imagePlus.getStack();
	  ImageStack backgroundStack = new ImageStack( inputStack.getWidth(), inputStack.getHeight() );
	  RankFilters rankFilters = new RankFilters();

	  for (int s = 0; s < numSlices; ++s)
	  {
	    ImageProcessor inputImage = inputStack.getProcessor( s+1 );
	    ImageProcessor backgroundImage = inputImage.duplicate();
	    rankFilters.rank( backgroundImage, _openingRadius, RankFilters.MIN );
	    rankFilters.rank( backgroundImage, _openingRadius, RankFilters.MAX );
	    backgroundStack.addSlice( backgroundImage );
	    IJ.showProgress( s+1, 2*numSlices );
	  }

	  return new ImagePlus( "Background of " + imagePlus.getTitle(), backgroundStack );
	}    

	/**
	 * Performs the attenuation correction, given the estimated background image stack.
	 * 
	 * @param inputImagePlus Image stack to be corrected
	 * @param backgroundImagePlus Background image stack
	 * @return Corrected image stack
	 */
	public ImagePlus correctAttenuation(ImagePlus inputImagePlus, ImagePlus backgroundImagePlus)
	{
	  final int numSlices = inputImagePlus.getNSlices();
	  ImageStack inputStack = inputImagePlus.getStack();
	  ImageStack correctedStack = new ImageStack( inputStack.getWidth(), inputStack.getHeight() );
	  final double[] meanIntensityProfile = computeMeanIntensityProfile( backgroundImagePlus );
	  final double[] standardDeviationProfile = computeStandardDeviationProfile( backgroundImagePlus, meanIntensityProfile );
	  final double refMean = meanIntensityProfile[_referenceSlice-1];    	
	  final double refSd = standardDeviationProfile[_referenceSlice-1];    	
	  
	  for (int s = 0; s < numSlices; ++s)
	  {
	    ImageProcessor inputImage = inputStack.getProcessor( s+1 );
	    ImageProcessor correctedImage = inputImage.duplicate();
	    if ( s != _referenceSlice-1 )
	    {
	      final int pixelCount = correctedImage.getPixelCount();
	      final double average = meanIntensityProfile[s];
	      final double sd = standardDeviationProfile[s];

	      if ( sd > 0.0 )
	      {
	        for (int p = 0; p < pixelCount; ++p)
	        {
	          double initialValue = inputImage.getf( p );
	          double correctedValue = refMean + refSd * (initialValue-average)/sd;
	          if ( correctedValue < _minValidValue )
	            correctedValue = _minValidValue;
	          else if ( correctedValue > _maxValidValue )
	            correctedValue = _maxValidValue;
	          correctedImage.setf( p, (float)correctedValue );
	        }
	      }
	      else
	      {
	        IJ.log( "Warning: Attenuation correction: slice "+(s+1)+": constant background (slice ignored)" );
	      }
	    }
	    correctedStack.addSlice( correctedImage );
      IJ.showProgress( numSlices+s+1, 2*numSlices );	    
	  }

	  return new ImagePlus( "Correction of " + inputImagePlus.getTitle(), correctedStack );
	}  

	/**
	 * Computes and returns the average pixel value of an image.
	 * 
	 * @param imageProcessor Input image
	 * @return Average pixel value
	 */
	public double computeMeanIntensity(ImageProcessor imageProcessor)
	{
	  final int numPixels = imageProcessor.getPixelCount();
	  double sum = 0.0;

	  for (int i = 0; i < numPixels; ++i)
	  {
	    sum += imageProcessor.getf( i );
	  }

	  return sum / numPixels;
	}

	/**
	 * Computes and returns the standard deviation of pixel values in an image.
	 * 
	 * @param imageProcessor Input image
	 * @param mean Average pixel value of the image, computed beforehand
	 * @return Standard deviation
	 */
	public double computeStandardDeviation(
	  ImageProcessor imageProcessor,
	  double mean)
	{
	  final int numPixels = imageProcessor.getPixelCount();
	  double diff, sum = 0.0;

	  for (int i = 0; i < numPixels; ++i)
	  {
	    diff = imageProcessor.getf( i ) - mean;
	    sum += diff * diff;
	  }
	  
	  return Math.sqrt( sum / (numPixels-1) );
	}

	/**
	 * Computes the mean intensity profile of an image stack.
	 * 
	 * @param imagePlus Input image stack
	 * @return Mean intensity as a function of slice number (array)
	 */
	public double[] computeMeanIntensityProfile(ImagePlus imagePlus)
	{
	  final int numSlices = imagePlus.getNSlices();
	  double[] meanIntensityProfile = new double[numSlices];

	  for (int s = 0; s < numSlices; ++s)
	  {
	    meanIntensityProfile[s] = computeMeanIntensity( imagePlus.getStack().getProcessor(s+1) );
	  }

	  return meanIntensityProfile;
	}

	/**
	 * Computes the standard deviation profile of an image stack.
	 * 
	 * @param imagePlus Input image stack
	 * @param meanIntensityProfile Mean intensity profile of the image stack, computed beforehand
	 * @return Standard deviation as a function of slice number (array)
	 */
	public double[] computeStandardDeviationProfile(
	  ImagePlus imagePlus,
	  double[] meanIntensityProfile)
	{
	  final int numSlices = imagePlus.getNSlices();
	  double[] standardDeviationProfile = new double[numSlices];

	  for (int s = 0; s < numSlices; ++s)
	  {
	    standardDeviationProfile[s] = computeStandardDeviation( 
	      imagePlus.getStack().getProcessor(s+1),
	      meanIntensityProfile[s] );
	  }

	  return standardDeviationProfile;
	}
}