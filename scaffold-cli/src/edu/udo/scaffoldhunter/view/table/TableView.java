package edu.udo.scaffoldhunter.view.table;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JToolBar;

import edu.udo.scaffoldhunter.gui.GUISession;
import edu.udo.scaffoldhunter.model.GlobalConfig;
import edu.udo.scaffoldhunter.model.ViewClassConfig;
import edu.udo.scaffoldhunter.model.ViewInstanceConfig;
import edu.udo.scaffoldhunter.model.ViewState;
import edu.udo.scaffoldhunter.model.db.Molecule;
import edu.udo.scaffoldhunter.model.db.Subset;
import edu.udo.scaffoldhunter.view.GenericView;
import edu.udo.scaffoldhunter.view.SideBarItem;


/**
 * @author Michael Hesse
 *
 */
public class TableView extends GenericView<TableViewInstanceConfig, TableViewClassConfig, TableViewState>
        implements PropertyChangeListener {

    private ViewComponent tableViewComponent;
    private JToolBar toolbar;
    //private Vector<SideBarItem> sidebarComponents;
    private List<SideBarItem> sidebarComponents;
    JMenu viewMenu;

    
    /**
     * @param session
     * @param subset
     * @param instanceConfig
     * @param classConfig
     * @param globalConfig
     * @param viewState
     */
    public TableView(GUISession session,
            Subset subset,
            ViewInstanceConfig instanceConfig,
            ViewClassConfig classConfig,
            GlobalConfig globalConfig,
            ViewState viewState ) {
        super(session, subset, instanceConfig, classConfig, globalConfig, viewState);
        
        // set up view component
        tableViewComponent = new ViewComponent(getDbManager(), getSubset(), getBannerPool(), viewState);
        tableViewComponent.setSelection(getSelection());
        
        { // set up toolbar and menu
            toolbar = tableViewComponent.getToolBar();
            viewMenu = tableViewComponent.getMenu();
        }
        
        { // set up sidebar
            sidebarComponents = tableViewComponent.getSideBar();
        }
        
        addPropertyChangeListener(SUBSET_PROPERTY, this);
    }
    
    @Override
    public JComponent getComponent() {
        return tableViewComponent;
    }
    
    @Override
    public JMenu getMenu() {
        return viewMenu;
    }

    @Override
    public JToolBar getToolBar() {
        return toolbar;
    }

    @Override
    public List<SideBarItem> getSideBarItems() {
        return sidebarComponents;
    }

    @Override
    public void destroy() {
        /*
        // destroy sidebar components first
        for(int i=0; i<sidebarComponents.size(); i++) {
            SideBarItem sbc = sidebarComponents.get(i);
            // sbc.destroy() is not part of the interface...
        }
        //Model model = tableViewComponent.getTableModel(); - should not be neccessary to destroy
        */
        tableViewComponent.destroy();
    }

    
    
    /* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    /**
     * listens to changes of the subset
     */
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        tableViewComponent.setSubset((Subset) event.getNewValue());
    }

    /* (non-Javadoc)
     * @see edu.udo.scaffoldhunter.view.View#focusMolecule(edu.udo.scaffoldhunter.model.db.Molecule)
     */
    @Override
    public void focusMolecule(Molecule molecule) {
        tableViewComponent.focusMolecule(molecule);       
    }
}

