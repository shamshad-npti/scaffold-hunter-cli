/*
 *  Modifications for Scaffold Hunter:
 *    - removed dependencies on XNap classes
 *    - reduced tab size, use our own close button icon
 *    - don't select newly created tabs automatically
 *    - added insertTab()
 *
 *  XNap Commons
 *
 *  Copyright (C) 2005  Felix Berger
 *  Copyright (C) 2005  Steffen Pingel
 *   
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package edu.udo.scaffoldhunter.gui.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.lang.reflect.Method;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JRootPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.udo.scaffoldhunter.util.Resources;


/**
 * Provides a <code>JTabbedPane</code> with close buttons  in the tab titles.
 * The buttons of the currently active tab will be visible and the button
 * of the tab the mouse if hovered over.
 * 
 * <p> 
 * If a button is clicked the tab is removed from the pane unless a CloseListener
 * has been set.
 * 
 * <p>Note: If a button is displayed on a non active tab and the mouse is
 * moved very fast out of the tabbed pane, the button may still be visible.
 * 
 * @author Felix Berger
 * @author Steffen Pingel
 */
@SuppressWarnings("all")
public class CloseableTabbedPane extends JTabbedPane {
    
//  private final static int ICON_TEXT_GAP = 10;
    // XXX: don't waste so much space  
    private final static int ICON_TEXT_GAP = 2;

    private MouseHandler mouseHandler = new MouseHandler();
    private SelectionHandler selHandler = new SelectionHandler();
    private ComponentHandler compHandler = new ComponentHandler();
    private CloseListener closeListener = null;
    private boolean useScrollHack;
    private Method getTabComponentAtMethod;
    private Method setTabComponentAtMethod;
    private boolean java6 = false;
    
    /**
     * Creates a tabbed pane with close buttons and a
     * <code>WRAP_TAB_LAYOUT</code>
     */
    public CloseableTabbedPane()
    {
        try {
            getTabComponentAtMethod = getClass().getMethod("getTabComponentAt", int.class);
            setTabComponentAtMethod = getClass().getMethod("setTabComponentAt", int.class, Component.class);
            java6 = true;
        } catch (Throwable t) {
        }
        
        setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
        addMouseMotionListener(mouseHandler);
        addMouseListener(mouseHandler);
        getModel().addChangeListener(selHandler);
        addComponentListener(compHandler);
    }
    
    /**
     * Sets the listener that is notified of user initiated close requests
     * for single tabs.
     * @param listener can be <code>null</code>, then the component
     * is simply removed
     */
    public void setCloseListener(CloseListener listener)
    {
        closeListener = listener;
    }


    // XXX
    public void insertTab(String title, Icon icon, Component component, String tip, int index) {
        insertTab(title, icon, component, tip, index, true);
    }

    // XXX
    public void insertTab(String title, Icon icon, Component component, String tip, int index, boolean closeable) {
        if (closeable) {
            if (java6) {
                super.insertTab(null, null, component, tip, index);
                TabTitleComponent tabTitle = new TabTitleComponent(title, component, icon);
                try {
                    setTabComponentAtMethod.invoke(this, index, tabTitle);
                } catch (Throwable t) {
                    throw new RuntimeException(t);
                }
            } else {
                super.insertTab(null, new TabTitleIcon(title, component, icon), component, tip, index);
            }
        } else {
            super.addTab(title, icon, component);
        }

        setButtonVisible(getSelectedIndex(), true);
    }

    /**
     * Adds a <code>compnent</code> with a tab <code>title</code> and
     * an <code>icon</code> to the tabbed pane.
     * <p>
     * The icon is displayed on the left side of the title and does not affect
     * the icon of the closing button for this tab. 
     * @param title the title of the tab, can be <code>null</code>
     * @param component 
     * @param icon can be <code>null</code>
     * @param closeable whether or not a close button should be displayed
     * for this tabbed pane.
     */
    public void addTab(String title, Component component, Icon icon, 
                       boolean closeable) 
    {
        if (closeable) {
            if (java6) {
                super.addTab(null, component);
                TabTitleComponent tabTitle = new TabTitleComponent(title, component, icon);
                try {
                    setTabComponentAtMethod.invoke(this, this.getTabCount() - 1, tabTitle);
                } catch (Throwable t) {
                    throw new RuntimeException(t);
                }
            } 
            else {
                super.addTab(null, new TabTitleIcon(title, component, icon), component);
            }
        }
        else {
            super.addTab(title, icon, component);
        }

        // XXX: don't select newly added tab
        //setSelectedComponent(component);
        setButtonVisible(getSelectedIndex(), true);
    }

    /**
     * Convenience wrapper for {@link #addTab(String, Component, Icon, boolean)
     * addTab(String, Component, Icon, true)}.
     */
    public void addTab(String title, Component component, Icon icon)
    {
        addTab(title, component, icon, true);
    }

    /**
     * Convenience wrapper for {@link #addTab(String, Component, Icon, boolean)
     * addTab(String, Component, null, true)}.
     */
    @Override
    public void addTab(String title, Component component) 
    {
        addTab(title, component, null, true);
    }

    @Override
    public void setTitleAt(int index, String newTitle)
    {
        TabTitle titleComponent = getTabTitleComponentAt(index);
        if (titleComponent != null) {
            titleComponent.setTitle(newTitle);
        }
        else {
            super.setTitleAt(index, newTitle);
        }
    }
    
    private TabTitle getTabTitleComponentAt(int index) {
        if (java6) {
            try {
                Object tabTitle = getTabComponentAtMethod.invoke(this, index);
                if (tabTitle instanceof TabTitle) {
                    return (TabTitle) tabTitle;
                }
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        } else {    
            Icon icon = getIconAt(index);
            if (icon instanceof TabTitle) {
                return (TabTitle)icon;
            }
        }
        return null;
    }
    
    /**
     * This method does not work properly for tabs that have a close
     * button.
     * <p>
     * Keep that information somewhere else if you need it.
     * @return the empty string for tabs that have a close button,
     * the title of the tab otherwise
     */
    @Override
    public String getTitleAt(int index)
    {
        return super.getTitleAt(index);
    }
    
    /**
     * Sets the tab layout policy. Currently <code>WRAP_TAB_LAYOUT</code> is
     * strongly recommended.
     * 
     * <p>
     * Explanation: <code>SCROLL_TAB_LAYOUT</code> uses a private view
     * translation for the scrolling of the tabs in
     * <code>BasicTabbedPaneUI</code>. The <code>EventIcon.paint()</code>
     * method receives coordinates from the ui view port and since it is not
     * possible to translate coordinates between the tabbed pane and the view
     * port the closing button can not be placed at the correct location.
     * 
     * <p>
     * However there is a crude work around:
     * <code>TabbedPaneUI.getTabBounds()</code> returns tabbed pane
     * coordinates whereas <code>TabTitleIcon.paint()</code> receives view
     * port coordinates. When <code>TabTitleIcon.paint()</code> is invoked it
     * can finds out its tab bounds in tabbed pane coordinates and use those
     * to set the button's bounds. The location can only be somewhat estimated
     * as it depends on the tab style of the look&feel. 
     * 
     * @param tabLayoutPolicy
     *            must be <code>JTabbedPane.WRAP_TAB_LAYOUT</code>
     * @see javax.swing.JTabbedPane#setTabLayoutPolicy(int)
     */
    @Override
    public void setTabLayoutPolicy(int tabLayoutPolicy)
    {
        useScrollHack = (tabLayoutPolicy == SCROLL_TAB_LAYOUT);
        super.setTabLayoutPolicy(tabLayoutPolicy);
    }

    @Override
    public void updateUI()
    {
        super.updateUI();
        int tabCount = getTabCount();
        for (int i = 0; i < tabCount; i++) {
            Icon icon = getIconAt(i);
            if (icon instanceof TabTitle) {
                ((TabTitle)icon).updateUI();
            }
        }
    }

    private void setButtonVisible(int index, boolean visible) 
    {
        TabTitle tabTitle = getTabTitleComponentAt(index);
        if (tabTitle != null) {
            tabTitle.setButtonVisible(visible);
        }
    }
    
    @Override
    public void removeTabAt(int index) {
        TabTitle tabTitle = getTabTitleComponentAt(index);
        super.removeTabAt(index);
        if (tabTitle != null) {
            tabTitle.disable();
        }
        if (mouseHandler.visibleIndex == index) {
            mouseHandler.visibleIndex = -1;
        }
        if (selHandler.oldIndex == index) {
            selHandler.oldIndex = -1;
        }
        int sel = getSelectedIndex();
        if (sel != -1) {
            setButtonVisible(sel, true);
        }
    }

    public static interface CloseListener
    {
        /**
         * Called when the user clicked the close button of the tab containing
         * <code>component</code>.
         * @param component that should be removed
         */
        void closeRequested(Component component);
    }
    
    public interface TabTitle {

        public abstract void updateUI();

        public abstract void setTitle(String newTitle);

        public abstract String getTitle();

        public abstract void setButtonVisible(boolean visible);

        public abstract void disable();

    }
    
//  private class DefaultCloseAction extends AbstractXNapAction
        // XXX: removed xnap dependency
        private class DefaultCloseAction extends AbstractAction
    {
        
        Component comp;
        
        public DefaultCloseAction(Component comp)
        {
            this.comp = comp;

            // XXX: we don't use this
            //putValue(AbstractXNapAction.ICON_FILENAME, "remove.png");
        }
        
        public void actionPerformed(ActionEvent e) {
            if (closeListener != null) {
                closeListener.closeRequested(comp);
            }
            else {
                remove(comp);
            }
        }
        
    }
    
    protected class TabTitleComponent extends Box implements TabTitle {

        private JLabel titleLabel;
        private TabTitleButton closeButton;
        private Component placeholder;

        public TabTitleComponent(String title, Component component,
                Icon icon) {
            super(BoxLayout.X_AXIS);
            
            titleLabel = new JLabel(title);
            titleLabel.setIcon(icon);
            titleLabel.setIconTextGap(ICON_TEXT_GAP);
            titleLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
            add(titleLabel);
            
            add(Box.createHorizontalStrut(ICON_TEXT_GAP));
            
            closeButton = new TabTitleButton(new DefaultCloseAction(component), mouseHandler);
            add(closeButton);
            
            // placeholder is displayed when the close button is not visible 
            // to avoid resizing of the component   
            placeholder = Box.createRigidArea(closeButton.getPreferredSize());

            // XXX: don't show button on newly created tabs
            setButtonVisible(false);
        }

        public void disable() {
        }

        public String getTitle() {
            return titleLabel.getText();
        }

        public void setTitle(String newTitle) {
            titleLabel.setText(newTitle);
        }

        public void setButtonVisible(boolean visible) {
            closeButton.setVisible(visible);
            if (!visible) {
                add(placeholder);
            } else {
                remove(placeholder);
            }
        }

    }
    
    /**
     * Provides an Icon that can displays a text that can have an icon to 
     * its left and an <code>EventIcon</code> to its right.
     */
    protected class TabTitleIcon implements Icon, TabTitle {

        private Icon leftIcon;
        private EventIcon closeButtonIcon;
        private String title;
        private int height = 10;

        public TabTitleIcon(String title, Component comp, Icon leftIcon)
        {
            this.title = title;
            this.leftIcon = leftIcon;

            this.closeButtonIcon = new EventIcon(new TabTitleButton(new DefaultCloseAction(comp), mouseHandler));

            height = Math.max(this.closeButtonIcon.getIconHeight(), leftIcon != null ? 
                    leftIcon.getIconHeight() : 0);
        }
    
        /* (non-Javadoc)
         * @see org.xnap.commons.gui.TabComponent#updateUI()
         */
        public void updateUI() {
            closeButtonIcon.updateUI();
        }

        public TabTitleIcon(String title, Component comp)
        {
            this(title, comp, null);
        }

        /* (non-Javadoc)
         * @see org.xnap.commons.gui.TabComponent#setTitle(java.lang.String)
         */
        public void setTitle(String newTitle)
        {
            title = newTitle;
        }

        /* (non-Javadoc)
         * @see org.xnap.commons.gui.TabComponent#getTitle()
         */
        public String getTitle()
        {
            return title;
        }
        
        /* (non-Javadoc)
         * @see org.xnap.commons.gui.TabComponent#getIconHeight()
         */
        public int getIconHeight()
        {
            return height;
        }

        public int getIconWidth()
        {
            int textWidth= SwingUtilities.computeStringWidth
                (CloseableTabbedPane.this.getFontMetrics(CloseableTabbedPane.this.getFont()), 
                 title);
            if (leftIcon != null) {
                return leftIcon.getIconWidth() + closeButtonIcon.getIconWidth()
                    + textWidth + 2 * ICON_TEXT_GAP;
            }
            else {
                return textWidth + ICON_TEXT_GAP + closeButtonIcon.getIconWidth();
            }
        }

        /**
         * Overwrites paintIcon to get hold of the coordinates of the icon.
         */
        public void paintIcon(Component c, Graphics g, int x, int y)
        {           
            if (leftIcon != null) {
                leftIcon.paintIcon(c, g, x, y + 1);
                drawTitleAndRightIcon(c, g, x, y, leftIcon.getIconWidth() + ICON_TEXT_GAP);
            }
            else {
                drawTitleAndRightIcon(c, g, x, y, 0);
            }
        }

        protected Rectangle computeTextRect(Graphics g, int x, int y) {
            // compute the correct y coordinate where to put the text
            Rectangle rect = new Rectangle(x, y, getIconWidth(), 
                                           getIconHeight());
            Rectangle iconRect = new Rectangle();
            Rectangle textRect = new Rectangle();
            SwingUtilities.layoutCompoundLabel
                (CloseableTabbedPane.this, g.getFontMetrics(),
                 title, null, SwingUtilities.CENTER,
                 SwingUtilities.CENTER,
                 SwingUtilities.CENTER,
                 SwingUtilities.TRAILING,
                 rect, iconRect, textRect,
                 UIManager.getInt("TabbedPane.textIconGap"));
            return textRect;
        }

        protected void drawTitleAndRightIcon(Component c, Graphics g, int x, int y, int offset)
        {
            Rectangle textRect = computeTextRect(g, x, y);
            
            int index = CloseableTabbedPane.this.indexOfTab(this);
            g.setColor(CloseableTabbedPane.this.getForegroundAt(index));
            g.setFont(CloseableTabbedPane.this.getFont());
            g.drawString(title, x + offset,
                     textRect.y + g.getFontMetrics().getAscent());
            
            if (useScrollHack) {
                if (index != -1) {
                    Rectangle bounds = getUI().getTabBounds(CloseableTabbedPane.this, index);
                    Insets tabInsets = UIManager.getInsets("TabbedPane.tabInsets");
                    closeButtonIcon.paintIcon
                        (c, g, bounds.x + tabInsets.left + getIconWidth()  + 3 - closeButtonIcon.getIconWidth(), bounds.y + tabInsets.top + 1);
                }
                else {
                    // will this ever happen?
                    closeButtonIcon.paintIcon
                        (c, g, x + getIconWidth() - closeButtonIcon.getIconWidth(), y + 1);
                }
            }
            else {
                closeButtonIcon.paintIcon
                    (c, g, x + getIconWidth() - closeButtonIcon.getIconWidth(), y + 1);
            }
        }
        
        public void setButtonVisible(boolean b) {
            closeButtonIcon.setVisible(b);
        }

        public void disable() {
            closeButtonIcon.disable();
        }
    
    }

    /**
     * Acts as a proxy class for the closing icon. 
     */
    private class EventIcon implements Icon {

        AbstractButton button;
        JLayeredPane pane;

        public EventIcon(AbstractButton button)
        {
            this.button = button;
            JRootPane rootPane = SwingUtilities.getRootPane(CloseableTabbedPane.this);
            if (rootPane != null) {
                pane = rootPane.getLayeredPane();
                pane.add(button, JLayeredPane.PALETTE_LAYER);
            }
        }

        public void updateUI() {
            button.updateUI();
        }

        public void disable() {
            button.setVisible(false);
            button.setEnabled(false);
            pane.remove(button);
        }

        public void setVisible(boolean b) {
            button.setVisible(b);
        }

        public int getIconHeight()
        {
            return button.getPreferredSize().height;
        }

        public int getIconWidth()
        {
            return button.getPreferredSize().width;
        }
        
        /**
         * Repositions the button.
         */
        public void paintIcon(Component c, Graphics g, int x, int y)
        {
            if (pane == null) {
                JRootPane rootPane = SwingUtilities.getRootPane(CloseableTabbedPane.this);
                pane = rootPane.getLayeredPane();
                pane.add(button, JLayeredPane.PALETTE_LAYER);
            }
            
            Point p = SwingUtilities.convertPoint(c, x, y, pane);
            button.setBounds(p.x, p.y, getIconHeight(), getIconWidth());
        }    
    }
    
    private static class TabTitleButton extends ToolBarButton
    {

        public TabTitleButton(Action action, CloseableTabbedPane.MouseHandler mouseHandler)
        {
            super(action, mouseHandler);
//          String iconName = (String)action.getValue(AbstractXNapAction.ICON_FILENAME);
//          setIcon(IconHelper.getTabTitleIcon(iconName));

            // XXX: use our own icon
            setIcon(Resources.getImageIcon("icons/misc/close_tab.png"));
            setMargin(new Insets(0, 0, 0, 0));
        }
    }
    
    class MouseHandler extends MouseAdapter implements MouseMotionListener
    {

        int visibleIndex = -1;
        
        @Override
        public void mouseEntered(MouseEvent event)
        {
            updateButton(event);
        }

        @Override
        public void mouseExited(MouseEvent event)
        {
            // FIXME can not hide the button because we may have entered it
            //updateButton(event);
            
                // XXX: yes we can
                updateButton(event);
        }

        public void mouseMoved(MouseEvent event) {
            updateButton(event);
        }

        public void mouseDragged(MouseEvent event)
        {
            updateButton(event);
        }

        void updateButton(MouseEvent event)
        {
            // we use this method to handle mouse events that have a
            // ToolBarButton as source, so convert the coordinates to
            // coordinates that are relative to the CloseableTabbedPane
            Point p = SwingUtilities.convertPoint((Component) event.getSource(), event.getX(), event.getY(),
                    CloseableTabbedPane.this);
            int index = getUI().tabForCoordinate(CloseableTabbedPane.this, p.x, p.y);
            if (index != -1 && index != getSelectedIndex()) {
                if (visibleIndex == index) {
                    return;
                }
                setButtonVisible(index, true);
                if (visibleIndex != index && visibleIndex != -1 && visibleIndex != getSelectedIndex()) {
                    setButtonVisible(visibleIndex, false);
                }
                visibleIndex = index;
            } else if (visibleIndex != -1 && visibleIndex != getSelectedIndex() && visibleIndex < getTabCount()) {
                setButtonVisible(visibleIndex, false);
                visibleIndex = -1;
            }
        }
    }

    private class SelectionHandler implements ChangeListener
    {
        int oldIndex = -1;
        
        /** 
         * Invoked when a tab is selected.
         */
        public void stateChanged(ChangeEvent e) {
            if (oldIndex != -1 && oldIndex < CloseableTabbedPane.this.getTabCount()) {
                CloseableTabbedPane.this.setButtonVisible(oldIndex, false);
            }
            oldIndex = getSelectedIndex();
            if (oldIndex != -1) {
                CloseableTabbedPane.this.setButtonVisible(oldIndex, true);
            }
        }
        
    }

    private class ComponentHandler extends ComponentAdapter
    {

        @Override
        public void componentHidden(ComponentEvent e) {
            int sel = getSelectedIndex();
            if (sel != -1) {
                setButtonVisible(sel, false);
            }
        }

        @Override
        public void componentShown(ComponentEvent e) {
            int sel = getSelectedIndex();
            if (sel != -1) {
                setButtonVisible(sel, true);
            }
        }
        
    }
}


/**
 * This class provides a toolbar button with an appropriately sized icon.
 * The border of the button is only visible when the mouse hovers over the 
 * button.
 */
@SuppressWarnings("all")
/*public*/ class ToolBarButton extends JButton {

    private boolean showBorder = false;
    private boolean showMenuHint;
    private CloseableTabbedPane.MouseHandler mouseHandler;

    public ToolBarButton(Action action, CloseableTabbedPane.MouseHandler mouseHandler)
    {
                super(action);
                this.mouseHandler = mouseHandler;
                // fixes x Button on mac
                setBorderPainted(false);
                setBackground(new Color(0, 0, 0, 0));

                setContentAreaFilled(false);
                setText(null);
                setMargin(new Insets(1, 1, 1, 1));

                putClientProperty("hideActionText", Boolean.TRUE);
    }

        /**
         * Returns true, if the mouse is currently over the button.
         */
        public boolean isMouseOver()
        {
                return showBorder;
        }

    @Override
    protected void paintBorder(Graphics g)
    {
                if (showBorder) {
                        super.paintBorder(g);
                }
    }

    @Override
        protected void processMouseEvent(MouseEvent e)
        {
                super.processMouseEvent(e);
                
                if (e.getID() == MouseEvent.MOUSE_ENTERED) {
                        showBorder = true;
                        setContentAreaFilled(true);
                        repaint();
                }
                else if (e.getID() == MouseEvent.MOUSE_EXITED) {
                        // update the button, since the mouse handler won't
                        // receive the MOUSE_EXITED event if the mouse directly
                        // leaves the tab bar
                        mouseHandler.updateButton(e);
                        showBorder = false;
                        setContentAreaFilled(false);
                        repaint();
                }
        }
        
}
