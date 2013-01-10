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
package org.meveo.core.parser;

import java.io.CharArrayReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Scanner;

import org.apache.log4j.Logger;
import org.meveo.config.MeveoFileConfig;

import com.google.inject.Inject;

/**
 * Abstract text file (like .csv or .txt) Parser class. Implements
 * setParsingFile() and close() methods and adds abstract method to get ticket
 * delimiter from concrete parser implementation.
 * 
 * @author Ignas Lelys
 * @created Dec 21, 2010
 * 
 */
public abstract class AbstractTextParser<T> extends AbstractParser<T> {

    private static final Logger logger = Logger.getLogger(AbstractTextParser.class);
    
    @Inject
    private MeveoFileConfig config;

    protected String fileName;

    protected FileChannel channel;

    protected Scanner scanner;

    protected long time = 0L;

    /**
     * @see org.meveo.core.parser.Parser#setParsingFile(java.lang.String)
     */
    public void setParsingFile(String fileName) throws ParserException {
        this.fileName = fileName;
        try {
            CharsetDecoder decoder = Charset.defaultCharset().newDecoder();
            channel = new FileInputStream(fileName).getChannel();
            long size = channel.size();
            MappedByteBuffer buffer = channel.map(MapMode.READ_ONLY, 0, size);
            buffer = buffer.load();
            CharBuffer cb = decoder.decode(buffer);
            scanner = new Scanner(new CharArrayReader(cb.array()));
            scanner.useDelimiter(config.getTicketSeparator());
        } catch (FileNotFoundException e) {
            throw new ParserException("File not found", e);
        } catch (NumberFormatException e) {
            throw new ParserException("Wrong buffer size value", e);
        } catch (CharacterCodingException e) {
            throw new ParserException("Wrong character encoding", e);
        } catch (IOException e) {
            throw new ParserException("Could not read file to buffer", e);
        }
    }

    /**
     * @see org.meveo.core.parser.Parser#close()
     */
    public void close() {
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Closing channel from '%s'", fileName));
            logger.debug(String.format("Parser took: %sms", time));
        }
        try {
            if (channel != null) {
                channel.close();
            }
            if (scanner != null) {
                scanner.close();
            }
        } catch (Exception e) {
            logger.error(String.format("Error while closing channel from '%s'", fileName), e);
        }
    }

    /**
     * @see org.meveo.core.parser.Parser#next()
     */
    public abstract T next() throws ParserException;
}
