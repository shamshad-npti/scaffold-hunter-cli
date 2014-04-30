/*
 * Scaffold Hunter
 * Copyright (C) 2006-2008 PG504
 * Copyright (C) 2010-2011 PG552
 * See the file README.txt in the root directory of the Scaffold Hunter
 * source tree for details.
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

package edu.udo.scaffoldhunter.plugins.dataimport.impl.dummy;

/**
 * @author Bernhard Dick
 *
 */
public class DummyImportPluginArguments {
    private boolean generateError;
    private String errorMessage;
    
    /**
     * @param generateError 
     * @param errorMessage 
     * 
     */
    public DummyImportPluginArguments(boolean generateError, String errorMessage) {
        this.setGenerateError(generateError);
        this.setErrorMessage(errorMessage);
    }

    /**
     * @param errorMessage the errorMessage to set
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * @return the errorMessage
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * @param generateError the generateError to set
     */
    public void setGenerateError(boolean generateError) {
        this.generateError = generateError;
    }

    /**
     * @return the generateError
     */
    public boolean isGenerateError() {
        return generateError;
    }
}
