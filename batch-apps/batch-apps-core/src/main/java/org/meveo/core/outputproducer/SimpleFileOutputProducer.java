/*
* (C) Copyright 2009-2013 Manaty SARL (http://manaty.net/) and contributors.
*
* Licensed under the GNU Public Licence, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.gnu.org/licenses/gpl-2.0.txt
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.meveo.core.outputproducer;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

import org.apache.log4j.Logger;
import org.meveo.core.output.Output;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * File output producer, that just simply prints all outputs to file, with with
 * each ticket printed in new line.
 * 
 * @author Ignas Lelys
 * @created Sep 20, 2010
 * 
 */
public class SimpleFileOutputProducer implements OutputProducer {
    
    private static final Logger logger = Logger.getLogger(SimpleFileOutputProducer.class);

    private PrintWriter writer;
    private String name;

    @Inject
    public SimpleFileOutputProducer(@Named("OutputName") String filename) {
        try {
            writer = new PrintWriter(filename);
            this.name = filename;
        } catch (FileNotFoundException e) {
            logger.error("Could not open file for writing", e);
            throw new RuntimeException("SimpleFileOutputProducer can not be created", e);
        }
    }

    public String produceOutput(List<Output> outputs) {
        for (Output output : outputs) {
            writer.println(output.getTicketOutput());
        }
        writer.flush();
        writer.close();
        
        return name;
    }

}
