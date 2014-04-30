/*
 * Scaffold Hunter
 * Copyright (C) 2006-2008 PG504
 * Copyright (C) 2010-2011 PG552
 * See README.txt in the root directory of the Scaffold Hunter source tree
 * for details.
 *
 * Scaffold Hunter is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Scaffold Hunter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package edu.udo.scaffoldhunter.view.plot;


import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import edu.udo.scaffoldhunter.gui.GUISession;
import edu.udo.scaffoldhunter.model.GlobalConfig;
import edu.udo.scaffoldhunter.model.Selection;
import edu.udo.scaffoldhunter.model.ViewClassConfig;
import edu.udo.scaffoldhunter.model.ViewInstanceConfig;
import edu.udo.scaffoldhunter.model.ViewState;
import edu.udo.scaffoldhunter.model.db.Molecule;
import edu.udo.scaffoldhunter.model.db.Subset;
import edu.udo.scaffoldhunter.util.I18n;
import edu.udo.scaffoldhunter.util.Resources;
import edu.udo.scaffoldhunter.view.GenericView;
import edu.udo.scaffoldhunter.view.SideBarItem;

/**
 * @author Dominic Sacré
 * @author Michael Hesse
 *
 */
public class PlotView extends GenericView<PlotViewInstanceConfig, PlotViewClassConfig, PlotViewState>
        implements ActionListener, ModelChangeListener, PropertyChangeListener  {

    private JToolBar toolbar;
    private Vector<SideBarItem> sidebarComponents;
    private JMenu viewMenu;

    private DbModel model;
    private PlotPanel3D plotPanel;
    
    JButton zoomXin, zoomXnorm, zoomXout;
    JButton zoomYin, zoomYnorm, zoomYout;
    JButton zoomZin, zoomZnorm, zoomZout;
    
    JPanel dotColorChoosePanel;
    ColorButton defaultDotcolorCB, minDotcolorCB, maxDotcolorCB; 
    JPanel dotSizeChoosePanel;
    JComboBox defaultDotsizeCB, minDotsizeCB, maxDotsizeCB;
    LegendPanel legendPanel;
    
    JComboBox highlightingModeCB;
    
    PlotViewState plotViewState = null;
    boolean isPlotViewInitialized = false;
    
    /**
     * @param session
     * @param subset 
     * @param instanceConfig 
     * @param classConfig 
     * @param globalConfig 
     * @param viewState 
     */
    public PlotView(GUISession session,
                    Subset subset,
                    ViewInstanceConfig instanceConfig,
                    ViewClassConfig classConfig,
                    GlobalConfig globalConfig,
                    ViewState viewState) {
        super(session, subset, instanceConfig, classConfig, globalConfig, viewState);

        ToggleGridAction toggleGridAction = new ToggleGridAction();
        ToggleTicksAction toggleTicksAction = new ToggleTicksAction();
        //deprecated: ToggleSelectionDisplayAction toggleSelectionDisplayAction = new ToggleSelectionDisplayAction();
        NormalizeViewAction normalizeViewAction = new NormalizeViewAction();
        ZoomInAction zoomInAction = new ZoomInAction();
        ZoomOutAction zoomOutAction = new ZoomOutAction();
        ZoomToSelectionAction zoomToSelectionAction = new ZoomToSelectionAction();
        HighlightNoneAction highlightNoneAction = new HighlightNoneAction();
        HighlightSelectionAction highlightSelectionAction = new HighlightSelectionAction();
        HighlightPublicBannersAction highlightPublicBannersAction = new HighlightPublicBannersAction();
        HighlightPrivateBannersAction highlightPrivateBannersAction = new HighlightPrivateBannersAction();

        // set viewstate
        if(viewState != null)
            if(viewState instanceof PlotViewState) {
                plotViewState = (PlotViewState) viewState;
                plotViewState.setApplied(false);
            }
        
        { // init
            model = new DbModel(getDbManager(), getSelection());
            model.addModelChangeListener(this);
            model.setSubset(subset, getBannerPool());
            addPropertyChangeListener(SUBSET_PROPERTY, this);
            getSelection().addPropertyChangeListener(Selection.SELECTION_PROPERTY, model);
        }
        
        { // layout the view
          // the panel with the diagram
            plotPanel = new PlotPanel3D(this, true);
            plotPanel.setModel(model);
        }

        
        { // set up sidebar
            sidebarComponents = new Vector<SideBarItem>();

            { // add channel chooser (it's integrated in the model)
                sidebarComponents.add( new SideBarItem(I18n.get("PlotView.Mappings.AxisPropertyMapping"), Resources.getIcon("axes_property_mapping.png"), model) );
            }

            // set up cell zoom
            PlotPickPanel pickPanel = new PlotPickPanel(getDbManager());
            pickPanel.setPreferredSize(new Dimension(160, 250));
            plotPanel.addPickChangeListener(pickPanel);
            sidebarComponents.add(new SideBarItem(I18n.get("PlotView.Mappings.Detailview"), Resources.getIcon("zoom.png"), pickPanel));

            // set up hyperplanes
            HyperplanePanel hyperplanePanel = new HyperplanePanel();
            //hyperplanePanel.setPreferredSize(new Dimension(160, 50));
            sidebarComponents.add(new SideBarItem(I18n.get("PlotView.Mappings.Hyperplanes"), Resources.getIcon("hyperplanes.png"), hyperplanePanel, false));
            model.setHyperplanePanel(hyperplanePanel);
            hyperplanePanel.setModel(model);
            
            // set up legend panel
            legendPanel = new LegendPanel();
            //legendPanel.setPreferredSize(new Dimension(160, 50));
            model.addModelChangeListener(legendPanel);
            legendPanel.setStartColor( plotPanel.getMinDotColor() );
            legendPanel.setEndColor( plotPanel.getMaxDotColor() );
            legendPanel.setStartSize( plotPanel.getMinDotsize() );
            legendPanel.setStartSize( plotPanel.getMaxDotsize() );
            sidebarComponents.add(new SideBarItem(I18n.get("PlotView.Legend.Legend"), /*Resources.getIcon("view-statistics.png")*/ null, legendPanel, false));
            
            { // zoom buttons
                Box box = Box.createVerticalBox();
                Box panelX = Box.createHorizontalBox();
                panelX.setBorder( BorderFactory.createEmptyBorder(10, 10, 0, 10));
                panelX.add(new JLabel("X:"));
                panelX.add( Box.createHorizontalGlue() );
                panelX.add(zoomXin = new JButton("<"));
                panelX.add(zoomXnorm = new JButton("o"));
                panelX.add(zoomXout = new JButton(">"));
                box.add(panelX);
                Box panelY = Box.createHorizontalBox();
                panelY.setBorder( BorderFactory.createEmptyBorder(0, 10, 0, 10));
                panelY.add(new JLabel("Y:"));
                panelY.add( Box.createHorizontalGlue() );
                panelY.add(zoomYin = new JButton("<"));
                panelY.add(zoomYnorm = new JButton("o"));
                panelY.add(zoomYout = new JButton(">"));
                box.add(panelY);
                Box panelZ = Box.createHorizontalBox();
                panelZ.setBorder( BorderFactory.createEmptyBorder(0, 10, 10, 10));
                panelZ.add(new JLabel("Z:"));
                panelZ.add( Box.createHorizontalGlue() );
                panelZ.add(zoomZin = new JButton("<"));
                panelZ.add(zoomZnorm = new JButton("o"));
                panelZ.add(zoomZout = new JButton(">"));
                box.setMaximumSize( new Dimension( 100000, box.getHeight()) );
                box.add(panelZ);
                zoomXin.setActionCommand("zoomXin"); 
                zoomXnorm.setActionCommand("zoomXnorm"); 
                zoomXout.setActionCommand("zoomXout");
                zoomYin.setActionCommand("zoomYin"); 
                zoomYnorm.setActionCommand("zoomYnorm"); 
                zoomYout.setActionCommand("zoomYout");
                zoomZin.setActionCommand("zoomZin"); 
                zoomZnorm.setActionCommand("zoomZnorm"); 
                zoomZout.setActionCommand("zoomZout");
                zoomXin.addActionListener(this);
                zoomXnorm.addActionListener(this);
                zoomXout.addActionListener(this);
                zoomYin.addActionListener(this);
                zoomYnorm.addActionListener(this);
                zoomYout.addActionListener(this);
                zoomZin.addActionListener(this);
                zoomZnorm.addActionListener(this);
                zoomZout.addActionListener(this);
                SideBarItem sbi = new SideBarItem(I18n.get("PlotView.AxisScale"), Resources.getIcon("axes_resize.png"), box);
                box.setOpaque(false);
                sidebarComponents.add(sbi);
            }

        }

        
        { // set up toolbar
            toolbar = new JToolBar();
            toolbar.setFloatable(false);

            toolbar.add( toggleGridAction );
            toolbar.add( toggleTicksAction );
            //deprecated: toolbar.add( toggleSelectionDisplayAction );
            toolbar.addSeparator();
            toolbar.add( zoomInAction );
            toolbar.add( zoomOutAction );
            toolbar.add( normalizeViewAction );
            toolbar.add( zoomToSelectionAction );

            toolbar.addSeparator();

            { // combobox for highlighting dots
                JPanel panel = new JPanel();
                panel.setOpaque(false);
                Box box = Box.createHorizontalBox();
                box.add( Box.createHorizontalStrut(10));
                box.add( new JLabel(I18n.get("PlotView.Toolbar.Highlighting.Label")+":") );
                box.add( Box.createHorizontalStrut(10));
                highlightingModeCB = new JComboBox();
                highlightingModeCB.addItem(I18n.get("PlotView.Toolbar.Highlighting.None"));
                highlightingModeCB.addItem(I18n.get("PlotView.Toolbar.Highlighting.Selection"));
                highlightingModeCB.addItem(I18n.get("PlotView.Toolbar.Highlighting.PublicBanner"));
                highlightingModeCB.addItem(I18n.get("PlotView.Toolbar.Highlighting.PrivateBanner"));
                highlightingModeCB.setSelectedIndex(1);
                highlightingModeCB.setActionCommand("set highlighting mode");
                highlightingModeCB.addActionListener(this);
                box.add(highlightingModeCB);
                box.add( Box.createHorizontalStrut(10));
                panel.add(box);
                panel.doLayout();
                toolbar.add(panel);
            }

            toolbar.addSeparator();

            { // dotcolor choose panel
                dotColorChoosePanel = new JPanel();
                dotColorChoosePanel.setOpaque(false);
                dotColorChoosePanel.setLayout( new CardLayout() );
                //
                Box normal = Box.createHorizontalBox();
                normal.add( new JLabel(I18n.get("PlotView.DotColor")+": "));
                defaultDotcolorCB = new ColorButton();
                defaultDotcolorCB.setActionCommand("choose normal dot color");
                defaultDotcolorCB.addActionListener(this);
                normal.add(defaultDotcolorCB);
                normal.add( Box.createHorizontalGlue() );
                defaultDotcolorCB.setColor(plotPanel.getDefaultDotColor());
                //
                Box minmax = Box.createHorizontalBox();
                minmax.add( new JLabel(I18n.get("PlotView.DotColor")+": "));
                minDotcolorCB = new ColorButton();
                minDotcolorCB.setActionCommand("choose min dot color");
                minDotcolorCB.addActionListener(this);
                minDotcolorCB.setColor(plotPanel.getMinDotColor());
                minmax.add(minDotcolorCB);
                minmax.add(new JLabel(" - "));
                maxDotcolorCB = new ColorButton();
                maxDotcolorCB.setActionCommand("choose max dot color");
                maxDotcolorCB.addActionListener(this);
                minmax.add(maxDotcolorCB);
                minmax.add( Box.createHorizontalGlue() );
                maxDotcolorCB.setColor(plotPanel.getMaxDotColor());
                //
                dotColorChoosePanel.add(normal, "normal");
                dotColorChoosePanel.add(minmax, "minmax");
                dotColorChoosePanel.setBorder( BorderFactory.createEmptyBorder(0, 10, 0, 10));
                toolbar.add(dotColorChoosePanel);
            }
            
            toolbar.addSeparator();
            
            { // dotsize choose panel
                dotSizeChoosePanel = new JPanel();
                dotSizeChoosePanel.setOpaque(false);
                dotSizeChoosePanel.setLayout( new CardLayout() );
                //
                Box normal = Box.createHorizontalBox();
                normal.add( new JLabel(I18n.get("PlotView.DotSize")+": "));
                defaultDotsizeCB = new JComboBox();
                for(int i=0; i<20; i++)
                    defaultDotsizeCB.addItem( i+1 );
                defaultDotsizeCB.setActionCommand("choose normal dotsize");
                defaultDotsizeCB.addActionListener(this);
                defaultDotsizeCB.setSelectedIndex( plotPanel.getDefaultDotsize() -1 );
                normal.add(defaultDotsizeCB);
                normal.add( Box.createHorizontalGlue() );
                //
                Box minmax = Box.createHorizontalBox();
                minmax.add( new JLabel(I18n.get("PlotView.DotSize")+": "));
                minDotsizeCB = new JComboBox();
                for(int i=0; i<20; i++)
                    minDotsizeCB.addItem( i+1 );
                maxDotsizeCB = new JComboBox();
                for(int i=0; i<20; i++)
                    maxDotsizeCB.addItem( i+1 );
                minDotsizeCB.setActionCommand("choose min dotsize");
                minDotsizeCB.addActionListener(this);
                minDotsizeCB.setSelectedIndex( plotPanel.getMinDotsize() -1 );
                minmax.add(minDotsizeCB);
                minmax.add(new JLabel(" - "));
                maxDotsizeCB.setActionCommand("choose max dotsize");
                maxDotsizeCB.addActionListener(this);
                maxDotsizeCB.setSelectedIndex( plotPanel.getMaxDotsize() -1 );
                minmax.add(maxDotsizeCB);
                minmax.add( Box.createHorizontalGlue() );
                //
                dotSizeChoosePanel.add(normal, "normal");
                dotSizeChoosePanel.add(minmax, "minmax");
                dotSizeChoosePanel.setBorder( BorderFactory.createEmptyBorder(0, 10, 0, 10));
                toolbar.add(dotSizeChoosePanel);
            } 
        }
        
        { // set up the menu
            viewMenu = new JMenu(I18n.get("PlotView.Title"));
            viewMenu.add( zoomInAction );
            viewMenu.add( zoomOutAction );
            viewMenu.add(normalizeViewAction);
            viewMenu.add(zoomToSelectionAction);
            viewMenu.addSeparator();
            viewMenu.add(toggleGridAction);
            viewMenu.add(toggleTicksAction);
            //viewMenu.add(toggleSelectionDisplayAction);
            viewMenu.addSeparator();
            JMenu highlightMenu = new JMenu(I18n.get("PlotView.Toolbar.Highlighting.Label"));
            highlightMenu.add( highlightNoneAction );
            highlightMenu.add( highlightSelectionAction );
            highlightMenu.add( highlightPublicBannersAction );
            highlightMenu.add( highlightPrivateBannersAction );
            viewMenu.add(highlightMenu);
        }

        isPlotViewInitialized = true;
        
        // create or apply viewstate
        if(plotViewState != null) {
            if(plotViewState.isValid()) {
                // apply state
                loadState();
                plotViewState.setApplied(true);
            } else {
                // create new state
                plotViewState.setApplied(true);
                saveState();
                plotViewState.setValid(true);
            }
        }
    }
//    /**
//     * 
//     */
//    public void openKMeansDialog() {
//        StartKMeansDialog kMeansDialog = new StartKMeansDialog(getDbManager(), null, this);
//        kMeansDialog.setVisible(true);
//    }
    /**
     * 
     */
    @Override
    public JComponent getComponent() {
        return plotPanel;
    }

    /**
     * 
     */
    @Override
    public JToolBar getToolBar() {
        return toolbar;
    }

    /**
     * @return
     *  a menu with actions for the plotview
     */
    @Override
    public JMenu getMenu() {
        return viewMenu;
    }

    @Override
    public void destroy() {
        getSelection().removePropertyChangeListener(Selection.SELECTION_PROPERTY, model);
    }

    @Override
    public List<SideBarItem> getSideBarItems() {
        return sidebarComponents;
    }
    
    // =======================================================
    
    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent event) {

        if(event.getActionCommand().equals("zoomXin")) {
            plotPanel.setDotspaceScaleX( plotPanel.getDotspaceScaleX()-0.1 );
        }
        else if(event.getActionCommand().equals("zoomXnorm")) {
            plotPanel.resetDotspaceScaleX();
        }
        else if(event.getActionCommand().equals("zoomXout")) {
            plotPanel.setDotspaceScaleX( plotPanel.getDotspaceScaleX()+0.1 );
        }

        else if(event.getActionCommand().equals("zoomYin")) {
            plotPanel.setDotspaceScaleY( plotPanel.getDotspaceScaleY()-0.1 );
        }
        else if(event.getActionCommand().equals("zoomYnorm")) {
            plotPanel.resetDotspaceScaleY();
        }
        else if(event.getActionCommand().equals("zoomYout")) {
            plotPanel.setDotspaceScaleY( plotPanel.getDotspaceScaleY()+0.1 );
        }

        else if(event.getActionCommand().equals("zoomZin")) {
            plotPanel.setDotspaceScaleZ( plotPanel.getDotspaceScaleZ()-0.1 );
        }
        else if(event.getActionCommand().equals("zoomZnorm")) {
            plotPanel.resetDotspaceScaleZ();
        }
        else if(event.getActionCommand().equals("zoomZout")) {
            plotPanel.setDotspaceScaleZ( plotPanel.getDotspaceScaleZ()+0.1 );
        }

        else if(event.getActionCommand().equals("set highlighting mode")) {
            switch( highlightingModeCB.getSelectedIndex() ) {
            case 1: 
                plotPanel.setHighlightingMode( PlotPanel3D.HighlightingMode.SELECTION);
                break;
            case 2: 
                plotPanel.setHighlightingMode( PlotPanel3D.HighlightingMode.PUBLIC_BANNER);
                break;
            case 3: 
                plotPanel.setHighlightingMode( PlotPanel3D.HighlightingMode.PRIVATE_BANNER);
                break;
            default: 
                plotPanel.setHighlightingMode( PlotPanel3D.HighlightingMode.NONE);
                break;
            }
        }
        
        else if(event.getActionCommand().equals("choose normal dot color")) {
            Color newColor = JColorChooser.showDialog(
                    (JButton)event.getSource(),
                    I18n.get("PlotView.ChooseDefaultDotColorDialog.Title"),
                    ((ColorButton)event.getSource()).getColor());
            if(newColor != null) {
                ((ColorButton)event.getSource()).setColor(newColor);
                //((ColorButton)event.getSource()).repaint();
                plotPanel.setDefaultDotColor(newColor);
                if(legendPanel != null) {
                    legendPanel.setStartColor(newColor);
                    legendPanel.setEndColor(newColor);
                }
            }
        }
        else if(event.getActionCommand().equals("choose min dot color")) {
            Color newColor = JColorChooser.showDialog(
                    (JButton)event.getSource(),
                    I18n.get("PlotView.ChooseMinDotColorDialog.Title"),
                    ((ColorButton)event.getSource()).getColor());
            if(newColor != null) {
                ((ColorButton)event.getSource()).setColor(newColor);
                //((ColorButton)event.getSource()).repaint();
                plotPanel.setMinDotColor(newColor);
                if(legendPanel != null) {
                    legendPanel.setStartColor(newColor);
                }
            }
        }
        else if(event.getActionCommand().equals("choose max dot color")) {
            Color newColor = JColorChooser.showDialog(
                    (JButton)event.getSource(),
                    I18n.get("PlotView.ChooseMaxDotColorDialog.Title"),
                    ((ColorButton)event.getSource()).getColor());
            if(newColor != null) {
                ((ColorButton)event.getSource()).setColor(newColor);
                //((ColorButton)event.getSource()).repaint();
                plotPanel.setMaxDotColor(newColor);
                if(legendPanel != null) {
                    legendPanel.setEndColor(newColor);
                }
            }
        }

        else if(event.getActionCommand().equals("choose normal dotsize")) {
                int newsize = ((JComboBox) event.getSource()).getSelectedIndex() + 1;
                plotPanel.setDefaultDotsize(newsize);
                if(legendPanel != null) {
                    legendPanel.setStartSize(newsize);
                    legendPanel.setEndSize(newsize);
                }
        }

        else if(event.getActionCommand().equals("choose min dotsize")) {
            int oldMinsize = plotPanel.getMinDotsize();
            int newMinsize = minDotsizeCB.getSelectedIndex()+1;
            int maxsize = maxDotsizeCB.getSelectedIndex()+1;
            
            if( (newMinsize>=1) & (newMinsize<=19) ) {
                // apply changes
                plotPanel.setMinDotsize(newMinsize);
                if(legendPanel != null)
                    legendPanel.setStartSize(newMinsize);
                if(newMinsize >= maxsize) {
                    maxsize = newMinsize+1;
                    plotPanel.setMaxDotsize(maxsize);
                    if(legendPanel != null)
                        legendPanel.setEndSize(maxsize);
                    maxDotsizeCB.setSelectedIndex(maxsize-1);
                }
            } else {
                // restore
                minDotsizeCB.setSelectedIndex(oldMinsize-1);
            }

        }
        else if(event.getActionCommand().equals("choose max dotsize")) {
            int minsize = minDotsizeCB.getSelectedIndex()+1;
            int oldMaxsize = plotPanel.getMaxDotsize();
            int newMaxsize = maxDotsizeCB.getSelectedIndex()+1;
            
            if( (newMaxsize>=2) & (newMaxsize<=20) ) {
                // apply changes
                plotPanel.setMaxDotsize(newMaxsize);
                if(legendPanel != null)
                    legendPanel.setEndSize(newMaxsize);
                if(newMaxsize <= minsize) {
                    minsize = newMaxsize-1;
                    plotPanel.setMinDotsize(minsize);
                    minDotsizeCB.setSelectedIndex(minsize-1);
                    if(legendPanel != null) {
                        legendPanel.setStartSize(minsize);
                    }
                }
            } else {
                // restore
                maxDotsizeCB.setSelectedIndex(oldMaxsize-1);
            }
        }
     
        saveState();
    }

    /* (non-Javadoc)
     * @see edu.udo.scaffoldhunter.view.plot.ModelChangeListener#modelChanged(edu.udo.scaffoldhunter.view.plot.Model, int, boolean)
     */
    @Override
    public void modelChanged(Model model, int channel, boolean moreToCome) {
        // update display of colorbuttons
        if(dotColorChoosePanel != null) {
            if(model.hasData( PlotPanel3D.COLOR_CHANNEL) ) {
                ((CardLayout)dotColorChoosePanel.getLayout()).show(dotColorChoosePanel, "minmax");
            } else {
                ((CardLayout)dotColorChoosePanel.getLayout()).show(dotColorChoosePanel, "normal");
            }
        }
        // update display of sizebuttons
        if(dotSizeChoosePanel != null) {
            if(model.hasData( PlotPanel3D.SIZE_CHANNEL) ) {
                ((CardLayout)dotSizeChoosePanel.getLayout()).show(dotSizeChoosePanel, "minmax");
            } else {
                ((CardLayout)dotSizeChoosePanel.getLayout()).show(dotSizeChoosePanel, "normal");
            }
        }
        saveState();
    }
    

    /* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    /**
     * listens to changes of the subset
     */
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        model.setSubset((Subset) event.getNewValue(), getBannerPool());
    }
    
    
    
    
    
    
    // ======================================================
    //
    // actions for the toolbar
    //
    // ======================================================

    /**
     * 
     */
    private class ToggleGridAction extends AbstractAction {
        public ToggleGridAction() {
            super(I18n.get("PlotView.Toolbar.ToggleGridAction"));
            putValue(Action.SHORT_DESCRIPTION, I18n.get("PlotView.Toolbar.ToggleGridAction"));
            putValue(Action.LARGE_ICON_KEY, Resources.getLargeIcon("grid.png"));
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_DOWN_MASK));
        }        
        @Override
        public void actionPerformed(ActionEvent e) {
            plotPanel.setShowGrid( ! plotPanel.isShowGrid() );
            saveState();
        }
    }
    
    /**
     * 
     */
    private class ToggleTicksAction extends AbstractAction {
        public ToggleTicksAction() {
            super(I18n.get("PlotView.Toolbar.ToggleTicksAction"));
            putValue(Action.SHORT_DESCRIPTION, I18n.get("PlotView.Toolbar.ToggleTicksAction"));
            putValue(Action.LARGE_ICON_KEY, Resources.getLargeIcon("axis-labels.png"));
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK));
        }        
        @Override
        public void actionPerformed(ActionEvent e) {
            plotPanel.setShowMarking( ! plotPanel.isShowMarking() );
            saveState();
        }
    }
    
    /**
     *  deprecated
     */
    /*
    private class ToggleSelectionDisplayAction extends AbstractAction {
        public ToggleSelectionDisplayAction() {
            super(I18n.get("PlotView.Toolbar.ToggleSelectionAction"));
            putValue(Action.SHORT_DESCRIPTION, I18n.get("PlotView.Toolbar.ToggleSelectionAction"));
            putValue(Action.LARGE_ICON_KEY, Resources.getLargeIcon("plot-selection.png"));
        }        
        @Override
        public void actionPerformed(ActionEvent e) {
            plotPanel.setShowSelection( ! plotPanel.isShowSelection() );
            saveState();
        }
    }
    */
    
    /**
     * 
     */
    private class HighlightNoneAction extends AbstractAction {
        public HighlightNoneAction() {
            super(I18n.get("PlotView.Toolbar.Highlighting.None"));
            putValue(Action.SHORT_DESCRIPTION, I18n.get("PlotView.Toolbar.Highlighting.None.Long"));
        }        
        @Override
        public void actionPerformed(ActionEvent e) {
            setHighlightingMode( PlotPanel3D.HighlightingMode.NONE);
            saveState();
        }
    }
    
    /**
     * 
     */
    private class HighlightSelectionAction extends AbstractAction {
        public HighlightSelectionAction() {
            super(I18n.get("PlotView.Toolbar.Highlighting.Selection"));
            putValue(Action.SHORT_DESCRIPTION, I18n.get("PlotView.Toolbar.Highlighting.Selection.Long"));
        }        
        @Override
        public void actionPerformed(ActionEvent e) {
            setHighlightingMode( PlotPanel3D.HighlightingMode.SELECTION);
            saveState();
        }
    }
    
    /**
     * 
     */
    private class HighlightPublicBannersAction extends AbstractAction {
        public HighlightPublicBannersAction() {
            super(I18n.get("PlotView.Toolbar.Highlighting.PublicBanner"));
            putValue(Action.SHORT_DESCRIPTION, I18n.get("PlotView.Toolbar.Highlighting.PublicBanner.Long"));
        }        
        @Override
        public void actionPerformed(ActionEvent e) {
            setHighlightingMode( PlotPanel3D.HighlightingMode.PUBLIC_BANNER);
            saveState();
        }
    }
    
    /**
     * 
     */
    private class HighlightPrivateBannersAction extends AbstractAction {
        public HighlightPrivateBannersAction() {
            super(I18n.get("PlotView.Toolbar.Highlighting.PrivateBanner"));
            putValue(Action.SHORT_DESCRIPTION, I18n.get("PlotView.Toolbar.Highlighting.PrivateBanner.Long"));
        }        
        @Override
        public void actionPerformed(ActionEvent e) {
            setHighlightingMode( PlotPanel3D.HighlightingMode.PRIVATE_BANNER);
            saveState();
        }
    }
    
    
    /**
     * 
     */
    private class NormalizeViewAction extends AbstractAction {
        public NormalizeViewAction() {
            super(I18n.get("Menu.Edit.ZoomToFit"));
            putValue(Action.SHORT_DESCRIPTION, I18n.get("Menu.Edit.ZoomToFit"));
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('0'));
            putValue(Action.SMALL_ICON, Resources.getIcon("zoom-best-fit.png"));
            putValue(Action.LARGE_ICON_KEY, Resources.getLargeIcon("zoom-best-fit.png"));
        }        
        @Override
        public void actionPerformed(ActionEvent e) {
            plotPanel.resetCoSys();
            saveState();
        }
    }

    /**
     * 
     */
    private class ZoomInAction extends AbstractAction {
        public ZoomInAction() {
            super(I18n.get("Menu.Edit.Zoomin"));
            putValue(Action.SHORT_DESCRIPTION, I18n.get("Menu.Edit.Zoomin"));
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('+'));
            putValue(Action.SMALL_ICON, Resources.getIcon("zoom-in.png"));
            putValue(Action.LARGE_ICON_KEY, Resources.getLargeIcon("zoom-in.png"));
        }        
        @Override
        public void actionPerformed(ActionEvent e) {
            plotPanel.setMetaScale( plotPanel.getMetaScale() * 1.1 );
            saveState();
        }
    }

    /**
     * 
     */
    private class ZoomOutAction extends AbstractAction {
        public ZoomOutAction() {
            super(I18n.get("Menu.Edit.Zoomout"));
            putValue(Action.SHORT_DESCRIPTION, I18n.get("Menu.Edit.Zoomout"));
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('-'));
            putValue(Action.SMALL_ICON, Resources.getIcon("zoom-out.png"));
            putValue(Action.LARGE_ICON_KEY, Resources.getLargeIcon("zoom-out.png"));
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            plotPanel.setMetaScale( plotPanel.getMetaScale() / 1.1 );
            saveState();
        }
    }
    
    /**
     * 
     */
    private class ZoomToSelectionAction extends AbstractAction {
        public ZoomToSelectionAction() {
            super(I18n.get("PlotView.Toolbar.ZoomToSelection"));
            putValue(Action.SHORT_DESCRIPTION, I18n.get("PlotView.Toolbar.ZoomToSelection"));
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('s'));
            putValue(Action.SMALL_ICON, Resources.getIcon("zoom-fit-selection.png"));
            putValue(Action.LARGE_ICON_KEY, Resources.getLargeIcon("zoom-fit-selection.png"));
        }        
        @Override
        public void actionPerformed(ActionEvent e) {
            plotPanel.zoomToSelection();
            saveState();
        }
    }
    
    /**
     * sets the selected highlighting mode. needed by the plotpanel, when turning
     * the selection-highlighting on
     * 
     * @param mode
     */
    public void setHighlightingMode(PlotPanel3D.HighlightingMode mode) {
        switch(mode) {
        case NONE:
            highlightingModeCB.setSelectedIndex(0);
            break;
        case SELECTION:
            highlightingModeCB.setSelectedIndex(1);
            break;
        case PUBLIC_BANNER:
            highlightingModeCB.setSelectedIndex(2);
            break;
        case PRIVATE_BANNER:
            highlightingModeCB.setSelectedIndex(3);
            break;
        }
        saveState();
    }
    
    /**
     * saves the current state to the plotViewState object
     */
    public void saveState() {
        if(plotViewState != null) {
            if( isPlotViewInitialized ) {
                if( plotViewState.isApplied() ) {
                    plotViewState.setHighlightIndex( highlightingModeCB.getSelectedIndex() );
                    plotViewState.setDefaultDotsizeIndex( defaultDotsizeCB.getSelectedIndex() );
                    plotViewState.setMinDotsizeIndex( minDotsizeCB.getSelectedIndex() );
                    plotViewState.setMaxDotsizeIndex( maxDotsizeCB.getSelectedIndex() );
                    plotViewState.setDefaultDotcolor( defaultDotcolorCB.getColor() );
                    plotViewState.setMinDotcolor( minDotcolorCB.getColor() );
                    plotViewState.setMaxDotcolor( maxDotcolorCB.getColor() );
                    plotPanel.saveState(plotViewState);
                    model.saveState(plotViewState);
                }
            }
        }
    }
    
    /**
     * loads the state from the plotViewState object
     */
    public void loadState() {
        if(plotViewState != null) {
            if( isPlotViewInitialized ) {
                highlightingModeCB.setSelectedIndex(plotViewState.getHighlightIndex());
                plotPanel.loadState(plotViewState);
                defaultDotsizeCB.setSelectedIndex( plotViewState.getDefaultDotsizeIndex() );
                minDotsizeCB.setSelectedIndex( plotViewState.getMinDotsizeIndex() );
                maxDotsizeCB.setSelectedIndex( plotViewState.getMaxDotsizeIndex() );
                defaultDotcolorCB.setColor( plotViewState.getDefaultDotcolor() );
                minDotcolorCB.setColor( plotViewState.getMinDotcolor() );
                maxDotcolorCB.setColor( plotViewState.getMaxDotcolor() );
                model.loadState(plotViewState);
            }
        }
    }
    
    /**
     * @param kMeansMap
     */
    public void applyKMeans(HashMap<Molecule, Integer> kMeansMap) {
        plotPanel.setkMeansMap(kMeansMap); //TODO
    }
    /* (non-Javadoc)
     * @see edu.udo.scaffoldhunter.view.View#focusMolecule(edu.udo.scaffoldhunter.model.db.Molecule)
     */
    @Override
    public void focusMolecule(Molecule molecule) {
        plotPanel.zoomToMolecule(molecule);
    }
}
