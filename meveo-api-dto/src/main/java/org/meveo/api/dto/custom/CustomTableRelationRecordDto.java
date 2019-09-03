package org.meveo.api.dto.custom;

public class CustomTableRelationRecordDto extends CustomTableRecordDto {

	private static final long serialVersionUID = 956795158210014050L;
    /**
	 * UUID of the source entity if record is a relation
	 */
	private String startUuid;
	
	/**
	 * UUID of the target entity if record is a relation
	 */
	private String endUuid;

	public String getStartUuid() {
		return startUuid;
	}

	public void setStartUuid(String startUuid) {
		this.startUuid = startUuid;
	}

	public String getEndUuid() {
		return endUuid;
	}

	public void setEndUuid(String endUuid) {
		this.endUuid = endUuid;
	}
	
	
}
