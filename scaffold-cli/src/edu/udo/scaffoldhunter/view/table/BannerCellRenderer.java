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

package edu.udo.scaffoldhunter.view.table;

import java.awt.Color;
import java.awt.Component;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import edu.udo.scaffoldhunter.view.table.BannerManager.BannerState;

/**
 * @author Michael Hesse
 *
 * this renderer shows currently only checkboxes as banners
 */
public class BannerCellRenderer extends DefaultTableCellRenderer {
    
    Icon publicBannerIcon, privateBannerIcon;
    Color background;
    
    
    /**
     * 
     */
    public BannerCellRenderer() {
        super();
        publicBannerIcon = new ImageIcon(getClass().getClassLoader().getResource("edu/udo/scaffoldhunter/resources/images/banner_public.png"));
        privateBannerIcon = new ImageIcon(getClass().getClassLoader().getResource("edu/udo/scaffoldhunter/resources/images/banner_private.png"));
        setHorizontalAlignment(CENTER);
        setVerticalAlignment(CENTER);
        background = getBackground();
    }
    
    /* (non-Javadoc)
     * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
     */
    @Override
    public Component getTableCellRendererComponent(
            JTable jTable, Object value,
            boolean isSelected, boolean hasFocus, 
            int rowIndex, int columnIndex) {

        super.getTableCellRendererComponent ( jTable, value, isSelected, hasFocus, rowIndex, columnIndex);
        setOpaque(true);
        if( ! isSelected ) {
            setBackground( (Color) ((rowIndex & 0x01) == 0x01 ? jTable.getClientProperty("zebracolor") : background ) );
        }
        
        BannerState bannerState = (BannerState) value;
        
        if( bannerState == BannerState.PUBLIC ) {
            setIcon(publicBannerIcon);
            //setText(I18n.get("Banner.Public"));
        }
        else if( bannerState == BannerState.PRIVATE ) {
            setIcon(privateBannerIcon);
            //setText(I18n.get("Banner.Private"));
        }
        else {
            setIcon(null);
            //setText("");
        }
        setText("");

        return this;
    }
}
