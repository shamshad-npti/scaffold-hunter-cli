/*
 * Scaffold Hunter
 * Copyright (C) 2006-2008 PG504
 * Copyright (C) 2010-2011 PG552
 * Copyright (C) 2012-2014 LS11
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

package edu.udo.scaffoldhunter.cli.args;

import java.io.File;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.converters.FileConverter;

/**
 * Holds arguments required to generate scaffolds
 * tree or network from database and file
 * @author Shamshad Alam
 * 
 */
@Parameters(commandNames = {"generate"}, commandDescriptionKey = "CLI.CommandHelp.Generate")
public class ScaffoldGeneratorArgs extends AbstractArgs{
    
    /**
     * Input sdf file to generate scaffolds
     */
    @Parameter(names = {"-i", "--input-file"}, converter = FileConverter.class, 
            descriptionKey = "", description="Input file location")
    public File sourceFile;
    
    /**
     * Output sdf file to save generated tree or network
     */
    @Parameter(names = {"-o", "--output-file"}, 
            descriptionKey = "", description = "Output file location")
    public String destinationFile;
    
    /**
     * True value indicate that you want to generate scaffold network
     */
    @Parameter(names = {"-n", "--network"}, 
            description = "Generate scaffold network, In absence ot this parameter scaffold tree is generated", descriptionKey = "")
    public boolean network;
    
    /**
     * Max ring size after which scaffolds are discarded
     */
    @Parameter(names = {"-m", "--max-ring-size"}, 
            descriptionKey= "", description="Maximum size of scaffold ring included in output")
    public int maxRingSize = 10;
    
    /**
     * Connection name to be used to retrieve molecules
     */
    @Parameter(names = {"-c", "--connection-name"}, description = "Name of the connection to connect to database", descriptionKey = "")
    public String connectionName;
    
    /**
     * Dataset name to be used to retrieve molecules
     */
    @Parameter(names = {"-d", "--dataset-name"}, description = "Name of the dataset which is used to generate scaffold tree", descriptionKey = "")
    public String datasetName;
    
}
