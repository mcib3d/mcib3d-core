package tango.helper;

/**
 *
 **
 * /**
 * Copyright (C) 2012 Jean Ollion
 *
 *
 *
 * This file is part of tango
 *
 * tango is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Jean Ollion
 */

// On définit la page (variable path) puis l'id du bouton (variable fragment) et on récupère la portion de texte (variable result)
import ij.IJ;
import net.htmlparser.jericho.*;
import java.util.*;
import java.net.*;
import java.io.*;
import mcib3d.utils.exceptionPrinter;

public class RetrieveHelp {

    public final static String connectPage = "/tango/Connect";
    public final static String editXPPage = "/tango/Experiment";
    public final static String editPSPage = "/tango/Processing+Chain";
    public final static String fieldPage = "/tango/Fields";
    public final static String cellPage = "/tango/Cells";
    public final static String objectPage = "/tango/Objects";
    public final static String manualNucPage = "/tango/Manual+Nucleus+Segmentation";
    private HashMap<String, Source> sources;
    public static String scheme = "http";
    public static String webSite = "biophysique.mnhn.fr";

    public void retrieveFromWeb() {
        sources = new HashMap<String, Source>();
        retrievePage(connectPage);
        retrievePage(editXPPage);
        retrievePage(editPSPage);
        retrievePage(fieldPage);
        retrievePage(cellPage);
        retrievePage(objectPage);
        retrievePage(manualNucPage);
    }

    public String getHelp(ID id) {
        System.out.println("getHelp: " + id);
        Source src = sources.get(id.container);
        if (src == null) {
            return null;
        }
        System.out.println("src ok");
        List<? extends Segment> segments = src.getAllElements(HTMLElementName.H2);
        if (segments != null) {
            System.out.println("segments ok");
            int start = 0;
            int end = 0;
            boolean follow = false;
            for (Segment segment : segments) {
               if(follow == true){
                   end = segment.getBegin();
                   follow = false;
               }
               if(segment.getFirstStartTag().getAttributeValue("id").equals(id.element)){
                   start = segment.getBegin();
                   follow = true;
               }
           }
           segments = src.getAllElements(HTMLElementName.H1);
           if (segments!=null) {
            for (Segment segment : segments) {
                int idx = segment.getBegin();
                if (idx>start && idx<=end) {
                    System.out.println("cut segment h1:"+segment.toString());
                    end=idx;
                    break;
                }
            }
           }
           //System.out.println("Start:"+start+ " End:"+end);
           if (start>0 && end>0 && end>start && end<src.length()) {
               CharSequence result = src.subSequence(start,end);
                System.out.println("result: "+result);
                //String linkLess = result.toString().replaceAll("(\\s)*(?i)href(\\s)*=(\\s)*(\"([^\"]*\")|'[^']*'|([^'\">\\s]+))","$3");
                String linkLess = result.toString().replaceAll("<a class=\"wiki external\" href=\"(.*)\"\\s(.*)>(.*)</a>","$1"); 
                linkLess = linkLess.replaceAll("<a class=\"wiki\" href=\"(.*)\"\\s(.*)>(.*)</a>","$2");
                String iconLess = linkLess.replaceAll("<img[^']*?class=\"icon\"[^']*?>","");
                String imageLess = iconLess.replaceAll("<img[^']*?src=\"([^']*?)\"[^']*?>",""); //( see image : $2 )
                return imageLess;
           } else return null;
           
        } else return null;
    }

    private void retrievePage(String page) {
        URI uri = null;
        URL url = null;
        try {
            uri = new URI(scheme, webSite, page, null, null);
            url = uri.toURL();
            System.out.println("retrieve Page: "+url.toString());
            Source src = new Source(url);
            sources.put(page, src);
        } catch (Exception e) {
            exceptionPrinter.print(e, "retrieve help error", true);
        }
    }
}
