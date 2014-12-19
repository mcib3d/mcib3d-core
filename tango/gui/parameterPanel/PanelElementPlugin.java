package tango.gui.parameterPanel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JToggleButton;
import tango.gui.util.PanelElementAbstract;
import tango.util.utils;

public class PanelElementPlugin extends PanelElementAbstract implements ActionListener {

    protected JComboBox method;
    protected PanelElementPlugin template;
    protected boolean templateSet;
    protected JButton test;

    public PanelElementPlugin(ParameterPanelPlugin parameterPanel, MultiParameterPanel mpp, boolean enableRemove, boolean enableTest) {
        super(mpp, parameterPanel);
        panel = Box.createHorizontalBox();
        panel.add(Box.createHorizontalStrut(2));
        method = new JComboBox();
        int xSize = 172;
        if (parameterPanel instanceof MeasurementPanel) xSize = 250;
        method.setMaximumSize(new Dimension(xSize, 26));
        method.setMinimumSize(new Dimension(124, 26));
        method.setPreferredSize(new Dimension(xSize, 26));
        method.addItem("");
        for (String s : parameterPanel.getMethods()) {
            method.addItem(s);
        }
        if (parameterPanel.getMethod() != null) {
            method.setSelectedItem(parameterPanel.getMethod());
        }
        utils.addHorizontalScrollBar(method);
        method.addActionListener(this);
        //method.setMinimumSize(method.getPreferredSize());
        //method.setMaximumSize(method.getPreferredSize());
        panel.add(method);
        panel.add(Box.createHorizontalStrut(2));
        // TEST ICONS BUTTON THOMAS
        edit = new JToggleButton("");
        edit.setActionCommand("Edit");
        edit.setSize(16, 16);
        edit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/tango/icons/edit.png")));
        edit.setToolTipText("Edit");
        edit.addActionListener(this);
        panel.add(edit);
        panel.add(Box.createHorizontalStrut(2));
        remove = new JButton("");
        remove.setActionCommand("Remove");
        remove.setSize(16, 16);
        remove.setIcon(new javax.swing.ImageIcon(getClass().getResource("/tango/icons/remove.png")));
        remove.setToolTipText("Remove");
        panel.add(remove);
        panel.add(Box.createHorizontalStrut(2));
        if (enableRemove) {
            remove.addActionListener(this);
        } else {
            remove.setEnabled(false);
        }
        test = new JButton("");
        test.setActionCommand("Test");
        test.setSize(16, 16);
        test.setIcon(new javax.swing.ImageIcon(getClass().getResource("/tango/icons/test.png")));
        test.setToolTipText("Test");
        if (enableTest) {
            panel.add(Box.createHorizontalStrut(2));
            panel.add(test);
            panel.add(Box.createHorizontalStrut(2));
            if (enableTest) {
                test.addActionListener(this);
            }
        }
        this.label = new JLabel(parameterPanel.getMPPLabel());
        parameterPanel.setMPPLabel(label);
        panel.add(label);
        panel.add(Box.createHorizontalGlue());
        panel.setMinimumSize(panel.getPreferredSize());
        updateValidity();
    }

    public void setTemplate(PanelElementPlugin template) {
        this.template = template;
        this.templateSet = true;
        updateValidity();
    }

    public void removeTemplate() {
        this.template = null;
        this.templateSet = false;
        updateValidity();
    }

    @Override
    public void updateValidity() {
        Color col = Color.black;
        boolean b = false;
        if (template != null) {
            b = ((ParameterPanelPlugin) parameterPanel).setTemplate((ParameterPanelPlugin) template.parameterPanel);
        }
        if (!parameterPanel.checkValidity()) {
            col = Color.red;
        } else if (templateSet && !b) {
            col = Color.blue;
        }
        edit.setForeground(col);
    }

    public PanelElementPlugin(ParameterPanelPlugin parameterPanel, MultiParameterPanel mpp) {
        super(mpp, parameterPanel);
        updateValidity();
    }

    @Override
    public ParameterPanelPlugin getParameterPanel() {
        return (ParameterPanelPlugin) parameterPanel;
    }

    public void setCurrentMethod() {
        ((ParameterPanelPlugin) parameterPanel).setMethod((String) method.getSelectedItem());
        if (ml != null) {
            parameterPanel.register(ml);
        }
        if (templateSet) {
            setTemplate(template);
        }
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        String command = ae.getActionCommand();
        if (command == null) {
            return;
        } else if (command.equals("Remove")) {
            mpp.removeElement(this);
        } else if (command.equals("Edit")) {
            if (edit.isSelected()) {
                mpp.showPanel(this);
            } else {
                updateValidity();
                mpp.hidePanel();
            }
        } else if (command.equals("Test")) {
            mpp.test(this);
        } else if (ae.getSource() == method) {
            setCurrentMethod();
            mpp.showPanel(this);
        }

    }

}
