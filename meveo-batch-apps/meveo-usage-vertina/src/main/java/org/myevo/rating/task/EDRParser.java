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
package org.myevo.rating.task;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;
import org.meveo.core.parser.Parser;
import org.meveo.core.parser.ParserException;
import org.myevo.rating.model.EDR;
import org.myevo.rating.ticket.EDRTicket;

public class EDRParser implements Parser<EDRTicket> {

    private static final Logger logger = Logger.getLogger(EDRParser.class);
    private final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");

    private BufferedReader reader = null;

    public void close() {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
            }
        }
    }

    public EDRTicket next() throws ParserException {
        EDRTicket edrTicket = null;
        try {
            String currentLine = reader.readLine();
            if (currentLine != null) {
                try {
                    edrTicket = new EDRTicket(parseToEDR(currentLine), currentLine);
                } catch (Exception e) {
                    logger.error("Failed to parse the following EDR line: " + currentLine, e);
                }
            }

        } catch (Exception e) {
            throw new ParserException(e);
        }

        return edrTicket;
    }

    public void setParsingFile(String fileName) throws ParserException {
        try {
            reader = new BufferedReader(new FileReader(fileName));
        } catch (Exception e) {
            throw new ParserException(e);
        }
    }

    private EDR parseToEDR(String currentLine) throws ParseException {
        String[] values = currentLine.split(";");

        EDR edr = new EDR();

        edr.setAPid(values[0].replaceAll("\"", ""));
        edr.setServiceId(values[1].replaceAll("\"", ""));
        edr.setId(values[2].replaceAll("\"", ""));
        edr.setConsumptionDate(DATE_FORMAT.parse(values[3].replaceAll("\"", "")));
        edr.setAccessPointId(values[4].replaceAll("\"", ""));
        edr.setIMSI(values[5].replaceAll("\"", ""));
        edr.setMSISDN(values[6].replaceAll("\"", ""));

        try {
            edr.setDownloadVolume(Long.parseLong(values[7].replaceAll("\"", "")));
        } catch (Exception e) {
            // Ignore in case an empty value was passed instead of 0
            edr.setDownloadVolume(0L);
        }
        try {
            edr.setUploadVolume(Long.parseLong(values[8].replaceAll("\"", "")));
        } catch (Exception e) {
            // Ignore in case an empty value was passed instead of 0
            edr.setUploadVolume(0L);
        }
        try {
            edr.setDuration(Long.parseLong(values[9].replaceAll("\"", "")));
        } catch (Exception e) {
            // Ignore in case an empty value was passed instead of 0
            edr.setDuration(0L);
        }

        // if ("OUT_ZONE".equalsIgnoreCase(values[10])) {
        edr.setOriginZone(values[10].replaceAll("\"", ""));
        // }
        edr.setAccessPointNameNI(values[11].replaceAll("\"", ""));
        edr.setPlmn(values[12].replaceAll("\"", ""));
        edr.setPlmnFromTicket(values[13].replaceAll("\"", ""));
        edr.setRoaming("1".equals(values[14]) ? true : false);

        return edr;
    }
}