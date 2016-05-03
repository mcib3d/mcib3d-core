package mcib3d.utils;

import ij.IJ;
import ij.plugin.BrowserLauncher;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Description of the Class
 *
 * @author thomas @created 23 octobre 2007
 */
public class AboutMCIB extends JFrame {

    private static String name;
    private static final double VERSION = 3.74;

    /**
     * Constructor for the AboutWindow object
     *
     * @param na
     */
    public AboutMCIB(String na) {
        name = na;
        Container top = this.getContentPane();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBackground(Color.WHITE);
        top.add(version());
        top.add(licence());
        top.add(authors());
        top.add(institutions());
        top.add(contact());
    }

    /**
     * the version of the plugin
     *
     * @return the label with the version
     */
    private JLabel version() {
        JLabel label = new JLabel(name + " (MCIB V" + VERSION+")");
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    /**
     * creates the licence label
     *
     * @return the licence label
     */
    private JLabel licence() {
        JLabel lic = new JLabel("distributed under the Licence Cecill");
        lic.setAlignmentX(Component.CENTER_ALIGNMENT);
        lic.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lic.addMouseListener(
                new MouseAdapter() {

                    @Override
                    public void mouseClicked(MouseEvent me) {
                        String url = "http://www.cecill.info/licences/Licence_CeCILL_V2-en.html";
                        try {
                            BrowserLauncher.openURL(url);
                        } catch (IOException ioe) {
                            IJ.log("cannot open the url " + url + "\n" + ioe);
                        }
                    }
                });
        return lic;
    }

    /**
     * creates the authors label
     *
     * @return the authors label
     */
    private JLabel authors() {
        JLabel curie = new JLabel("T. BOUDIER and J. OLLION");
        curie.setAlignmentX(Component.CENTER_ALIGNMENT);
        return curie;
    }

    /**
     * creates the contact label
     *
     * @return the contact label
     */
    private JLabel contact() {
        JLabel cont = new JLabel("contact : thomas.boudier at upmc.fr");
        cont.setAlignmentX(Component.CENTER_ALIGNMENT);
        cont.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cont.addMouseListener(
                new MouseAdapter() {

                    @Override
                    public void mouseClicked(MouseEvent me) {
                        try {
                            BrowserLauncher.openURL("http://imagejdocu.tudor.lu/doku.php?id=plugin:stacks:3d_ij_suite:start");
                        } catch (IOException ioe) {
                            IJ.log("cannot open link\n" + ioe);
                        }
                    }
                });
        return cont;
    }

    /**
     * creates the institutions label
     *
     * @return the institutions label
     */
    private JPanel institutions() {
        JPanel inst = new JPanel();
        inst.setLayout(new BoxLayout(inst, BoxLayout.X_AXIS));
        URL url = getClass().getResource("/icons/institut_curie.gif");
        Image image = Toolkit.getDefaultToolkit().getImage(url);
        ImageIcon icon = new ImageIcon(image);
        JLabel curie = new JLabel(icon, JLabel.CENTER);
        curie.setCursor(new Cursor(Cursor.HAND_CURSOR));

        curie.addMouseListener(
                new MouseAdapter() {

                    @Override
                    public void mouseClicked(MouseEvent me) {
                        try {
                            BrowserLauncher.openURL("http://www.curie.fr");
                        } catch (IOException ioe) {
                            IJ.log("cannot open url http://www.curie.fr\n" + ioe);
                        }
                    }
                });
        JLabel upmc;
        url = getClass().getResource("/icons/upmc.gif");
        image = Toolkit.getDefaultToolkit().getImage(url);
        icon = new ImageIcon(image);
        upmc = new JLabel(icon, JLabel.CENTER);
        upmc.setCursor(new Cursor(Cursor.HAND_CURSOR));
        upmc.addMouseListener(
                new MouseAdapter() {

                    @Override
                    public void mouseClicked(MouseEvent me) {
                        try {
                            BrowserLauncher.openURL("http://www.upmc.fr");
                        } catch (IOException ioe) {
                            IJ.log("cannot open url http://www.upmc.fr\n" + ioe);
                        }
                    }
                });

        url = getClass().getResource("/icons/cnrs.gif");
        image = Toolkit.getDefaultToolkit().getImage(url);
        icon = new ImageIcon(image);
        JLabel cnrs = new JLabel(icon, JLabel.CENTER);
        cnrs.setCursor(new Cursor(Cursor.HAND_CURSOR));
        //JLabel cnrs = new JLabel(" CNRS ");
        cnrs.addMouseListener(
                new MouseAdapter() {

                    @Override
                    public void mouseClicked(MouseEvent me) {
                        try {
                            BrowserLauncher.openURL("http://www.cnrs.fr");
                        } catch (IOException ioe) {
                            IJ.log("cannot open url http://www.cnrs.fr\n" + ioe);
                        }
                    }
                });

        url = getClass().getResource("/icons/inserm.gif");
        image = Toolkit.getDefaultToolkit().getImage(url);
        icon = new ImageIcon(image);
        JLabel inserm = new JLabel(icon, JLabel.CENTER);
        inserm.setCursor(new Cursor(Cursor.HAND_CURSOR));
        //JLabel cnrs = new JLabel(" CNRS ");
        inserm.addMouseListener(
                new MouseAdapter() {

                    @Override
                    public void mouseClicked(MouseEvent me) {
                        try {
                            BrowserLauncher.openURL("http://www.inserm.fr/fr/home.html");
                        } catch (IOException ioe) {
                            IJ.log("cannot open url http://www.inserm.fr\n" + ioe);
                        }
                    }
                });
        
        JLabel suite;
        url = getClass().getResource("/icons/suite.png");
        image = Toolkit.getDefaultToolkit().getImage(url);
        icon = new ImageIcon(image);
        suite = new JLabel(icon, JLabel.CENTER);
        suite.setCursor(new Cursor(Cursor.HAND_CURSOR));
        suite.addMouseListener(
                new MouseAdapter() {

                    @Override
                    public void mouseClicked(MouseEvent me) {
                        try {
                            BrowserLauncher.openURL("http://imagejdocu.tudor.lu/doku.php?id=plugin:stacks:3d_ij_suite:start");
                        } catch (IOException ioe) {
                            IJ.log("cannot open url http://imagejdocu.tudor.lu/doku.php?id=plugin:stacks:3d_ij_suite:start\n" + ioe);
                        }
                    }
                });
        
        
        //inst.add(curie);
        inst.add(upmc);
        inst.add(suite);
        //inst.add(cnrs);
        //inst.add(inserm);

        return inst;
    }

    /**
     * draw the window
     */
    public void drawAbout() {
        int sizeX = 400;
        Container top = this.getContentPane();
        int nbcomp = top.getComponentCount();
        for (int i = 0; i < nbcomp; i++) {
            Component tmp = top.getComponent(i);
            Dimension dim = tmp.getMinimumSize();
            tmp.setSize(sizeX, (int) dim.getHeight());
        }
        setSize(sizeX, 160);
        setResizable(false);
        setVisible(true);
    }
}
