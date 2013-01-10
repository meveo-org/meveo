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

import java.util.Map;


/**
 * Bean for transfering file processing data.
 * 
 * @author Donatas Remeika
 * @created Mar 5, 2009
 */
public class FileProcessingResult {

    private String parsedFile;

    private String rejectedTicketsFile;

    private String ignoredTicketsFile;

    private String acceptedTicketsFile;
    
    private long parsedCount;
    private long rejectedCount;
    private long acceptedCount;
    private long ignoredCount;
    private long usageDATAVolume;
    private long usageVOICEVolume;
    private long usageSMSVolume;
    private long rejectedDATAVolume;
    private long rejectedVOICEVolume;
    private long rejectedSMSVolume;
    private long savedPendingTickets;
    
    private long uniquenessStepTotalDuration;
    private long uniquenessStepExecutionCount;
    private double uniquenessStepAverageDuration;
    
    private long accessStepTotalDuration;
    private long accessStepExecutionCount;
    private double accessStepAverageDuration;
    
    private long zonningStepTotalDuration;
    private long zonningStepExecutionCount;
    private double zonningStepAverageDuration;
    
    private long provissioningStepTotalDuration;
    private long provissioningStepExecutionCount;
    private double provissioningStepAverageDuration;
    
    private long processDuration;
    private long commitDuration;
    private long totalDuration;

    public String getAcceptedTicketsFile() {
        return acceptedTicketsFile;
    }

    public void setAcceptedTicketsFile(String acceptedTicketsFile) {
        this.acceptedTicketsFile = acceptedTicketsFile;
    }
    
    public String getParsedFile() {
        return parsedFile;
    }

    public void setParsedFile(String parsedFile) {
        this.parsedFile = parsedFile;
    }

    public String getRejectedTicketsFile() {
        return rejectedTicketsFile;
    }

    public void setRejectedTicketsFile(String rejectedTicketsFile) {
        this.rejectedTicketsFile = rejectedTicketsFile;
    }

    public long getParsedCount() {
        return parsedCount;
    }

    public void setParsedCount(long parsedCount) {
        this.parsedCount = parsedCount;
    }

    public long getRejectedCount() {
        return rejectedCount;
    }

    public void setRejectedCount(long rejectedCount) {
        this.rejectedCount = rejectedCount;
    }

    public long getAcceptedCount() {
        return acceptedCount;
    }

    public void setAcceptedCount(long acceptedCount) {
        this.acceptedCount = acceptedCount;
    }

    public String getIgnoredTicketsFile() {
        return ignoredTicketsFile;
    }

    public void setIgnoredTicketsFile(String ignoredTicketsFile) {
        this.ignoredTicketsFile = ignoredTicketsFile;
    }

    public long getIgnoredCount() {
        return ignoredCount;
    }

    public void setIgnoredCount(long ignoredCount) {
        this.ignoredCount = ignoredCount;
    }

    public long getUsageDATAVolume() {
        return usageDATAVolume;
    }

    public void setUsageDATAVolume(long usageDATAVolume) {
        this.usageDATAVolume = usageDATAVolume;
    }

    public long getUsageVOICEVolume() {
        return usageVOICEVolume;
    }

    public void setUsageVOICEVolume(long usageVOICEVolume) {
        this.usageVOICEVolume = usageVOICEVolume;
    }

    public long getUsageSMSVolume() {
        return usageSMSVolume;
    }

    public void setUsageSMSVolume(long usageSMSVolume) {
        this.usageSMSVolume = usageSMSVolume;
    }

    public long getRejectedDATAVolume() {
        return rejectedDATAVolume;
    }

    public void setRejectedDATAVolume(long rejectedDATAVolume) {
        this.rejectedDATAVolume = rejectedDATAVolume;
    }

    public long getRejectedVOICEVolume() {
        return rejectedVOICEVolume;
    }

    public void setRejectedVOICEVolume(long rejectedVOICEVolume) {
        this.rejectedVOICEVolume = rejectedVOICEVolume;
    }

    public long getRejectedSMSVolume() {
        return rejectedSMSVolume;
    }

    public void setRejectedSMSVolume(long rejectedSMSVolume) {
        this.rejectedSMSVolume = rejectedSMSVolume;
    }

    public long getSavedPendingTickets() {
        return savedPendingTickets;
    }

    public void setSavedPendingTickets(long savedPendingTickets) {
        this.savedPendingTickets = savedPendingTickets;
    }

	public long getUniquenessStepTotalDuration() {
		return uniquenessStepTotalDuration;
	}

	public void setUniquenessStepTotalDuration(long uniquenessStepTotalDuration) {
		this.uniquenessStepTotalDuration = uniquenessStepTotalDuration;
	}

	public long getUniquenessStepExecutionCount() {
		return uniquenessStepExecutionCount;
	}

	public void setUniquenessStepExecutionCount(long uniquenessStepExecutionCount) {
		this.uniquenessStepExecutionCount = uniquenessStepExecutionCount;
	}

	public double getUniquenessStepAverageDuration() {
		return uniquenessStepAverageDuration;
	}

	public void setUniquenessStepAverageDuration(
			double uniquenessStepAverageDuration) {
		this.uniquenessStepAverageDuration = uniquenessStepAverageDuration;
	}

	public long getAccessStepTotalDuration() {
		return accessStepTotalDuration;
	}

	public void setAccessStepTotalDuration(long accessStepTotalDuration) {
		this.accessStepTotalDuration = accessStepTotalDuration;
	}

	public long getAccessStepExecutionCount() {
		return accessStepExecutionCount;
	}

	public void setAccessStepExecutionCount(long accessStepExecutionCount) {
		this.accessStepExecutionCount = accessStepExecutionCount;
	}

	public double getAccessStepAverageDuration() {
		return accessStepAverageDuration;
	}

	public void setAccessStepAverageDuration(double accessStepAverageDuration) {
		this.accessStepAverageDuration = accessStepAverageDuration;
	}

	public long getZonningStepTotalDuration() {
		return zonningStepTotalDuration;
	}

	public void setZonningStepTotalDuration(long zonningStepTotalDuration) {
		this.zonningStepTotalDuration = zonningStepTotalDuration;
	}

	public long getZonningStepExecutionCount() {
		return zonningStepExecutionCount;
	}

	public void setZonningStepExecutionCount(long zonningStepExecutionCount) {
		this.zonningStepExecutionCount = zonningStepExecutionCount;
	}

	public double getZonningStepAverageDuration() {
		return zonningStepAverageDuration;
	}

	public void setZonningStepAverageDuration(double zonningStepAverageDuration) {
		this.zonningStepAverageDuration = zonningStepAverageDuration;
	}

	public long getProvissioningStepTotalDuration() {
		return provissioningStepTotalDuration;
	}

	public void setProvissioningStepTotalDuration(
			long provissioningStepTotalDuration) {
		this.provissioningStepTotalDuration = provissioningStepTotalDuration;
	}

	public long getProvissioningStepExecutionCount() {
		return provissioningStepExecutionCount;
	}

	public void setProvissioningStepExecutionCount(
			long provissioningStepExecutionCount) {
		this.provissioningStepExecutionCount = provissioningStepExecutionCount;
	}

	public double getProvissioningStepAverageDuration() {
		return provissioningStepAverageDuration;
	}

	public void setProvissioningStepAverageDuration(
			double provissioningStepAverageDuration) {
		this.provissioningStepAverageDuration = provissioningStepAverageDuration;
	}

	public long getProcessDuration() {
		return processDuration;
	}

	public void setProcessDuration(long processDuration) {
		this.processDuration = processDuration;
	}

	public long getCommitDuration() {
		return commitDuration;
	}

	public void setCommitDuration(long commitDuration) {
		this.commitDuration = commitDuration;
	}

	public long getTotalDuration() {
		return totalDuration;
	}

	public void setTotalDuration(long totalDuration) {
		this.totalDuration = totalDuration;
	}
    
}
