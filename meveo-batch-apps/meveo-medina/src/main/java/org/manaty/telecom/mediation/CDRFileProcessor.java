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
package org.manaty.telecom.mediation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.meveo.commons.utils.FileFormat;
import org.meveo.model.mediation.RejectedCDR;
import org.meveo.model.mediation.RejectedCDR.RejectedCDRFlag;
import org.manaty.model.telecom.mediation.cdr.CDR;
import org.manaty.model.telecom.mediation.cdr.CDRStatus;
import org.manaty.model.telecom.mediation.edr.EDRFileBuilder;
import org.manaty.telecom.mediation.cache.TransactionalMagicNumberCache.CacheTransaction;
import org.manaty.telecom.mediation.context.MediationContext;
import org.manaty.telecom.mediation.parser.Parser;
import org.manaty.telecom.mediation.parser.ParserFactory;
import org.manaty.telecom.mediation.process.AbstractProcessStep;
import org.manaty.telecom.mediation.process.CommitResult;
import org.manaty.telecom.mediation.process.Processor;
import org.manaty.utils.MagicNumberConverter;

/**
 * CDR file processor. Responsible for parsing file, validating tickets, gathering statistics, transforming to EDR format.
 * 
 * @author Donatas Remeika
 * @created Mar 4, 2009
 */
public class CDRFileProcessor {

    private static final Logger logger = Logger.getLogger(CDRFileProcessor.class);

    private String eventType;
    private String filename;
    private String errorOutputFilename;
    private String ignoredOutputFilename;
    private PrintWriter errorWriter;
    private PrintWriter ignoredWriter;
    private File sourceFile;
    protected File tempDir = new File(MedinaConfig.getTempFilesDirectory());
    String outputFilename;
    EDRFileBuilder outputBuilder;
    private CacheTransaction cacheTransaction;

    public CDRFileProcessor(String eventType,String filename, CacheTransaction cacheTransaction) {
        this.filename = filename;
        this.eventType = eventType;
        this.cacheTransaction = cacheTransaction;
        sourceFile = new File(filename);
    }

    /**
     * Process file.
     * 
     * @return FileProcessingResult bean with data.
     * @throws Exception
     */
    public FileProcessingResult process(FileProcessingContext fileProcessingContext) throws Exception {
        long parsedCDRs = 0;
        long acceptedCDRs = 0;
        long rejectedCDRs = 0;
        long ignoredCDRs = 0;
        Parser parser = null;
        Processor processor = null;
        CommitResult commitResult = null;
        long rejectedDATAVolume = 0L;
        long rejectedVOICEVolume = 0L;
        long rejectedSMSVolume = 0L;
        long savedTicketsForAggregation = 0L;

        MediationContext context = null;
        try {
            parser = ParserFactory.getParser(filename, eventType,"MEVEO");
            //TODO : get processor from DB ?
            //processor = new CDRProcessor(cacheTransaction);
            CDR cdr = null;
            long processStart = System.currentTimeMillis();
            while ((cdr = parser.next()) != null) {
                parsedCDRs++;
                context = processor.process(cdr, eventType);
                //if (context.getNotInsertedPaMatch() != null) {
                //    fileProcessingContext.getPAMatchesToRetryInsert().add(context.getNotInsertedPaMatch());
                //}
                if (context.isAccepted()) {
                    acceptedCDRs++;
                    outputFilename="EDR_"+filename;
                    if(outputBuilder==null){
                    	outputBuilder = new  EDRFileBuilder(outputFilename);//EDRUtils.getEDRBuilder(new EDROutputSortingKey(cdrType, channel), outputBuilders, outputFilenames);
                    }
                    outputBuilder.append(context.getEdr());
                    // if retried rejected ticket was processed successfully, change rejected ticket flag in database to PROCESSED
                    if (cdr.getRetryRejectedID() != null) {
                        fileProcessingContext.getProcessedRejectedTicketsIds().add(cdr.getRetryRejectedID());
                    }
                    //addCellAndPDPInfoToFileProcessingContext(fileProcessingContext, context);
                } else {
                    CDRStatus status = context.getStatus();
                    if (status == CDRStatus.IGNORED) {
                        ignoredCDRs++;
                        writeIgnoredTicket(cdr);
                        //addCellAndPDPInfoToFileProcessingContext(fileProcessingContext, context);
                    } else if (status == CDRStatus.AGGREGATED) {
                        savedTicketsForAggregation++;
                       // fileProcessingContext.getPartialCDRs().add(context.getCDR());
                        // if retried rejected ticket was processed successfully, change rejected ticket flag in database to PROCESSED
                        if (cdr.getRetryRejectedID() != null) {
                            fileProcessingContext.getProcessedRejectedTicketsIds().add(cdr.getRetryRejectedID());
                        }
                    } else {
                        System.out.println("OOO " + context.getStatus());
                        rejectedCDRs++;
                        //if (cdrType.getCDRSubType() == CDRSubtype.DATA) {
                         //   rejectedDATAVolume += (cdr.getDownloadedDataVolume() != null ? cdr.getDownloadedDataVolume() : 0L)
                         //           + (cdr.getUploadedDataVolume() != null ? cdr.getUploadedDataVolume() : 0L);
                        //} else if (cdrType.getCDRSubType() == CDRSubtype.SMS) {
                        //    rejectedSMSVolume += 1;
                        //} else if (cdrType.getCDRSubType() == CDRSubtype.VOICE) {
                            rejectedVOICEVolume += cdr.getVolumeTotal().longValue();
                        //}
                        // CDRs with downloaded volume (with dest ip address) do not reject them to db
                        /*boolean saveRejectedTicketToDB = !(context.getStatus() == CDRStatus.NO_ACCESS && cdr.getDownloadedDataVolume() != null);
                        System.out.println("OOO " + saveRejectedTicketToDB + " " + cdr.getIPBinV4Address());
                        if (saveRejectedTicketToDB) {
                            // if its not retry ticket then save rejected ticket to database
                            if (cdr.getRetryRejectedID() == null) {
                                System.out.println("FFF " + filename + " " + cdr.getIPBinV4Address());
                                RejectedCDR rejectedCDR = new RejectedCDR();
                                rejectedCDR.setDate(new Date());
                                rejectedCDR.setFileName(filename);
                                String reason = null;
                                if (CDRStatus.DUPLICATE == status) {
                                    reason = status.name() + "_" + MagicNumberConverter.convertToString(cdr.getMagicNumber());
                                } else {
                                    reason = status.name();
                                }
                                rejectedCDR.setRejectedFlag(RejectedCDRFlag.REJECTED_FOR_RETRY);
                                rejectedCDR.setRejectionReason(reason);
                                rejectedCDR.setTicketData(cdr.getSource().toString());
                                fileProcessingContext.getRejectedCDRs().add(rejectedCDR);
                            } else {
                                // if ticket was rejected (was already in MEDINA_REJECTED_CDR) and after re processing it was rejected again
                                // then we have to update its REASON because it might be that it has changed.
                                fileProcessingContext.getFailedRejectedTicketsIds().put(cdr.getRetryRejectedID(), context.getStatus());
                            }
                        }*/
                    }
                }
            }
            long processDuration = System.currentTimeMillis() - processStart;
            logger.info("Processor process took: " + processDuration);

            AbstractProcessStep uniquenessStep = processor.getUniquenessStep();
            AbstractProcessStep accessStep = processor.getAccessStep();
            AbstractProcessStep zonningStep = processor.getZonningStep();
           
            double uniquenessAvgDuration = uniquenessStep.getExecutionCount() != 0 ? (uniquenessStep.getExecutionTime()) / uniquenessStep.getExecutionCount() : 0;
            logger.info(String.format("Uniqueness: %s times, %4.2f average, total time - %s ms", uniquenessStep.getExecutionCount(), uniquenessAvgDuration, uniquenessStep
                .getExecutionTime()));

            double accessAvgDuration = accessStep.getExecutionCount() != 0 ? (accessStep.getExecutionTime()) / accessStep.getExecutionCount() : 0;
            logger.info(String.format("Access: %s times, %4.2f average, total time - %s ms", accessStep.getExecutionCount(), accessAvgDuration, accessStep.getExecutionTime()));

            double zonningAvgDuration = zonningStep.getExecutionCount() != 0 ? (zonningStep.getExecutionTime()) / zonningStep.getExecutionCount() : 0;
            logger.info(String.format("Zonning: %s times, %4.2f average, total time - %s ms", zonningStep.getExecutionCount(), zonningAvgDuration, zonningStep.getExecutionTime()));

         

            long commitStart = System.currentTimeMillis();
            Date ticketDate = context.getCdr().getStartDate();
            commitResult = processor.commit(ticketDate, fileProcessingContext);
            long commitDuration = System.currentTimeMillis() - commitStart;
            logger.info("Processor commit took: " + commitDuration);

            FileProcessingResult result = new FileProcessingResult();
            result.setParsedFile(filename);
            result.setRejectedTicketsFile(errorOutputFilename);
            result.setAcceptedTicketsFile(outputFilename);
            result.setIgnoredTicketsFile(ignoredOutputFilename);
            result.setIgnoredCount(ignoredCDRs);
            result.setParsedCount(parsedCDRs);
            result.setAcceptedCount(acceptedCDRs);
            result.setRejectedCount(rejectedCDRs);
            result.setSavedPendingTickets(savedTicketsForAggregation);
            result.setUsageDATAVolume(commitResult != null ? commitResult.getUsageCountDATA() : 0L);
            result.setUsageVOICEVolume(commitResult != null ? commitResult.getUsageCountVOICE() : 0L);
            result.setUsageSMSVolume(commitResult != null ? commitResult.getUsageCountSMS() : 0L);
            result.setRejectedDATAVolume(rejectedDATAVolume);
            result.setRejectedVOICEVolume(rejectedVOICEVolume);
            result.setRejectedSMSVolume(rejectedSMSVolume);

            result.setCommitDuration(commitDuration);
            result.setProcessDuration(processDuration);

            result.setUniquenessStepExecutionCount(uniquenessStep.getExecutionCount());
            result.setUniquenessStepTotalDuration(uniquenessStep.getExecutionTime());
            result.setUniquenessStepAverageDuration(uniquenessAvgDuration);

            result.setAccessStepExecutionCount(accessStep.getExecutionCount());
            result.setAccessStepTotalDuration(accessStep.getExecutionTime());
            result.setAccessStepAverageDuration(accessAvgDuration);

            result.setZonningStepExecutionCount(zonningStep.getExecutionCount());
            result.setZonningStepTotalDuration(zonningStep.getExecutionTime());
            result.setZonningStepAverageDuration(zonningAvgDuration);

            return result;
        } finally {
            if (parser != null) {
                parser.close();
            }
            closeOpenFiles();
        }
    }


    /**
     * Write ignored ticket to error output.
     * 
     * @param cdr
     *        Rejected CDR.
     * @param status
     *        Rejection status.
     * @param format
     *        Source file format.
     */
    private void writeIgnoredTicket(CDR cdr) {
        try {
            if (ignoredOutputFilename == null) {
                try {
                    File ignoredOutputFile = File.createTempFile(sourceFile.getName(), String.valueOf(System.currentTimeMillis()), tempDir);
                    ignoredOutputFilename = ignoredOutputFile.getAbsolutePath();
                } catch (IOException e) {
                    throw new ConfigurationException("Could not set up parsing environment.", e);
                }
            }
            if (ignoredWriter == null) {
                ignoredWriter = new PrintWriter(new File(ignoredOutputFilename));
            }
        } catch (FileNotFoundException e) {
            logger.error("Could not open ignored tickets output file", e);
            throw new ConfigurationException("Could not write to ignored output", e);
        }
    }

    /**
     * Close all open files.
     */
    private void closeOpenFiles() {
        outputBuilder.close();
        
        if (errorWriter != null) {
            errorWriter.close();
        }
        if (ignoredWriter != null) {
            ignoredWriter.close();
        }
    }
/*
    public static class EDROutputSortingKey {
        private String cdrType;
        private String channel;

        public EDROutputSortingKey(String cdrType, String channel) {
            super();
            this.cdrType = cdrType;
            this.channel = channel;
        }

        public String getCdrType() {
            return cdrType;
        }

        public String getChannel() {
            return channel;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((cdrType == null) ? 0 : cdrType.hashCode());
            result = prime * result + ((channel == null) ? 0 : channel.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            EDROutputSortingKey other = (EDROutputSortingKey) obj;
            if (cdrType != other.cdrType)
                return false;
            if (channel == null) {
                if (other.channel != null)
                    return false;
            } else if (!channel.equals(other.channel))
                return false;
            return true;
        }

    }
*/
}
