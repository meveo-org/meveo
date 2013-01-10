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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.hibernate.ejb.HibernateEntityManager;
import org.manaty.telecom.mediation.cache.TransactionalMagicNumberCache;
import org.manaty.telecom.mediation.cache.TransactionalMagicNumberCache.CacheTransaction;
import org.manaty.utils.DateUtil;
import org.manaty.utils.FileUtil;
import org.manaty.utils.SQLUtil;
import org.meveo.commons.utils.FileFormat;
import org.meveo.model.mediation.CDRFile;

/**
 * Runnable mediation task.
 * 
 * @author Donatas Remeika
 * @created Mar 2, 2009
 */
public class MediatorTask implements Runnable {

    private static final Logger logger = Logger.getLogger(MediatorTask.class);

    private final Format errorFileExtensionFormat = new SimpleDateFormat(MedinaConfig.getErrorFileExtension());

    private final Format ignoredFileExtensionFormat = new SimpleDateFormat(MedinaConfig.getIgnoredFileExtension());

    public void run() {
        File ticketsFile = FileUtil.getFileForParsing(MedinaConfig.getSourceFilesDirectory(), MedinaConfig
                .getFileExtensions());
        if (ticketsFile != null) {
            execute(ticketsFile);
        }
    }

    protected void execute(File ticketsFile) {
        long start = System.currentTimeMillis();
        File lockedFile = null;
        FileProcessingResult result = null;
        FileProcessingContext context = new FileProcessingContext();
        CacheTransaction cacheTransaction = TransactionalMagicNumberCache.getInstance().getTransaction();
        CDRFileProcessor fileProcessor = null;
        try {
            MedinaPersistence.getEntityManager().getTransaction().begin();
//            TransactionalCellCache.getInstance().beginTransaction();
            logger.info(String.format("Processing '%s'", ticketsFile.getName()));
            String originalName = ticketsFile.getName();
            
            long lastModified = ticketsFile.lastModified();
            // Avoid file of being taken by other threads by changing it's
            // extension
            lockedFile = FileUtil.addExtension(ticketsFile, MedinaConfig.getFileProcessingExtension());
            if (lockedFile == null) {
                logger.info(String.format("File '%s' could not be renamed. Another thread might have taken it first",
                        originalName));
                return;
            }
            boolean rejected = checkFileAndRejectIfNeeded(lockedFile, originalName, MedinaConfig
                    .getRejectedFilesDirectory());
            if (rejected) {
                logger.debug(String.format("File '%s' was rejected as already been processed.", originalName));
                return;
            }
            Date startDate = new Date();
            CDRFile cdrFile = createInitialCDRFile(originalName, FileUtil.replaceFilenameExtension(originalName,
                    errorFileExtensionFormat.format(startDate)), FileUtil.replaceFilenameExtension(originalName,
                    ignoredFileExtensionFormat.format(startDate)), startDate, new Date(lastModified));
            FileFormat format = FileUtil.getFileFormatByExtension(originalName);
            
            fileProcessor = new CDRFileProcessor(null,lockedFile.getAbsolutePath(), cacheTransaction);
            //context.setFileName(lockedFile.getAbsolutePath());
            result = fileProcessor.process(context);
            result.setTotalDuration(System.currentTimeMillis() - start);
            updateCDRFile(cdrFile, result);
            handleFiles(cdrFile, result, originalName);
            if (logger.isDebugEnabled()) {
                logger.debug("Committing transaction...");
            }
            TransactionalMagicNumberCache.getInstance().persistCacheTransaction(cacheTransaction);
//            TransactionalCellCache.getInstance().commitTransaction();
            MedinaPersistence.getEntityManager().getTransaction().commit();
            cacheTransaction.commit();
        } catch (Throwable e) {
            logger.error("Exception occured. Transaction rolled back.", e);
            // workaround to show stacktrace
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String stacktrace = sw.toString();
            logger.error(stacktrace);
            
            MedinaPersistence.getEntityManager().getTransaction().rollback();
            cacheTransaction.rollback();
            FileUtil.addExtension(lockedFile, MedinaConfig.getFileProcessingFailedExtension());
        } finally {
        	//checkAndInsertMissingPaMathingsIfNeeded(context);
            MedinaPersistence.closeEntityManager();
            if (lockedFile != null) {
                logger.info(String.format("Mediator finished processing file %s in %s ms.", lockedFile.getName(),
                        System.currentTimeMillis() - start));
            }
        }
        
        
    }
    

    /**
     * Move files to their final destinations after processing is finished.
     * 
     * @param cdrFile
     *            CDRFile entity.
     * @param result
     *            File processing data.
     */
    private void handleFiles(CDRFile cdrFile, FileProcessingResult result, String ticketFileName) {
        String acceptedDir = MedinaConfig.getAcceptedFilesDirectory();
        FileUtil.moveFile(acceptedDir, new File(result.getParsedFile()), cdrFile
                .getFilename());
        if (result.getIgnoredCount() > 0) {
            FileUtil.moveFile(MedinaConfig.getIgnoredTicketFilesDirectory(), new File(result.getIgnoredTicketsFile()),
                    cdrFile.getIgnoredFilename());
            if (logger.isDebugEnabled()) {
                logger.debug(String.format("%s ignored tickets from %s moved to %s", result.getIgnoredCount(), cdrFile
                        .getFilename(), cdrFile.getIgnoredFilename()));
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug(String.format("No ignored tickets in %s", cdrFile.getFilename()));
            }
        }
        String acceptedTicketsFile = result.getAcceptedTicketsFile(); 
        FileUtil.moveFile(MedinaConfig.getOutputFilesDirectory(), new File(acceptedTicketsFile), cdrFile.getFilename());
        Date timestamp = DateUtil.getCurrentDateWithUniqueSeconds();
                logger.debug(String.format("EDRs  from %s moved to %s", cdrFile.getFilename(),
                		acceptedTicketsFile));
        
    }

    /**
     * Create CDRFile entity with initial data.
     * 
     * @param filename
     *            Filename of the file being processed.
     * @param errorFilename
     *            File name of the file where rejected tickets are stored.
     * @param startDate
     *            Processing start time.
     * @return Created UsageFile entity.
     */
    protected CDRFile createInitialCDRFile(String filename, String errorFilename, String ignoredFilename, Date startDate, Date fileCreated) {
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Creating CDRFile entity for source file %s", filename));
        }
        EntityManager em = MedinaPersistence.getEntityManager();
        CDRFile cdrFile = new CDRFile();
        cdrFile.setFilename(filename);
        cdrFile.setErrorFilename(errorFilename);
        cdrFile.setIgnoredFilename(ignoredFilename);
        cdrFile.setAnalysisStartDate(startDate);
        cdrFile.setFileDate(fileCreated);
        cdrFile.setRejectedDATAVolume(0L);
        cdrFile.setRejectedSMSVolume(0L);
        cdrFile.setRejectedVOICEVolume(0L);
        cdrFile.setUsageDATAVolume(0L);
        cdrFile.setUsageSMSVolume(0L);
        cdrFile.setUsageVOICEVolume(0L);
        em.persist(cdrFile);
        em.flush();
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("CDRFile entity for source file %s created successfuly", filename));
        }
        return cdrFile;
    }

    /**
     * Update CDRFile with results of file processing.
     * 
     * @param cdrFile
     *            CDRFile entity.
     * @param result
     *            File processing data.
     * @return true if update was successful, false otherwise.
     */
    protected boolean updateCDRFile(CDRFile cdrFile, FileProcessingResult result) {
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Updating CDRFile entity for source file %s", cdrFile.getFilename()));
        }
        EntityManager em = MedinaPersistence.getEntityManager();
        cdrFile.setParsedCDRs(result.getParsedCount());
        cdrFile.setRejectedCDRs(result.getRejectedCount());
        cdrFile.setSuccessedCDRs(result.getAcceptedCount());
        cdrFile.setIgnoredCDRs(result.getIgnoredCount());
        cdrFile.setAnalysisEndDate(new Date());
        cdrFile.setUsageDATAVolume(result.getUsageDATAVolume());
        cdrFile.setUsageVOICEVolume(result.getUsageVOICEVolume());
        cdrFile.setUsageSMSVolume(result.getUsageSMSVolume());
        cdrFile.setRejectedDATAVolume(result.getRejectedDATAVolume());
        cdrFile.setRejectedVOICEVolume(result.getRejectedVOICEVolume());
        cdrFile.setRejectedSMSVolume(result.getRejectedSMSVolume());
        
        cdrFile.setAccessStepAverageDuration(result.getAccessStepAverageDuration());
        cdrFile.setAccessStepExecutionCount(result.getAccessStepExecutionCount());
        cdrFile.setAccessStepTotalDuration(result.getAccessStepTotalDuration());
        
        cdrFile.setUniquenessStepAverageDuration(result.getUniquenessStepAverageDuration());
        cdrFile.setUniquenessStepExecutionCount(result.getUniquenessStepExecutionCount());
        cdrFile.setUniquenessStepTotalDuration(result.getUniquenessStepTotalDuration());
        
        cdrFile.setZonningStepAverageDuration(result.getZonningStepAverageDuration());
        cdrFile.setZonningStepExecutionCount(result.getZonningStepExecutionCount());
        cdrFile.setZonningStepTotalDuration(result.getZonningStepTotalDuration());
        
        cdrFile.setProvissioningStepAverageDuration(result.getProvissioningStepAverageDuration());
        cdrFile.setProvissioningStepExecutionCount(result.getProvissioningStepExecutionCount());
        cdrFile.setProvissioningStepTotalDuration(result.getProvissioningStepTotalDuration());
        
        cdrFile.setTotalDuration(result.getTotalDuration());
        cdrFile.setCommitDuration(result.getCommitDuration());
        cdrFile.setProcessDuration(result.getProcessDuration());
        
        
        em.merge(cdrFile);
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Updating CDRFile entity for source file %s completed successfuly", cdrFile
                    .getFilename()));
        }
        return true;
    }

    /**
     * Check if file is not already processed.
     * 
     * @param ticketsFile
     *            File to check.
     * @param filename
     *            Original filename.
     * @return true if file was rejected, false otherwise.
     */
    private boolean checkFileAndRejectIfNeeded(File ticketsFile, String filename, String rejectedFilesDirectory) {
        Statement statement = null;
        try {
            EntityManager em = MedinaPersistence.getEntityManager();
            @SuppressWarnings("deprecation")
            Connection connection = ((HibernateEntityManager) em).getSession().connection();
            statement = connection.createStatement();
            StringBuilder query = new StringBuilder(128);
            query.append("SELECT COUNT(*) FROM MEDINA_CDR_FILE WHERE FILENAME = '").append(filename).append("'");
            Integer count = SQLUtil.getIntegerAndCloseResultSet(statement.executeQuery(query.toString()));
            if (count > 0) {
                FileUtil.moveFile(rejectedFilesDirectory, ticketsFile, filename);
                return true;
            }
            return false;
        } catch (SQLException e) {
            throw new ConfigurationException("Could not access database", e);
        } finally {
            SQLUtil.closeStatements(statement);
        }

    }
    
}
