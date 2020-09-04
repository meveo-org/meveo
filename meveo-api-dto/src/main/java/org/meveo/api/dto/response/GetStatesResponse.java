package org.meveo.api.dto.response;

import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@XmlRootElement(name = "GetStatesResponse")
@XmlAccessorType(XmlAccessType.FIELD)
public class GetStatesResponse extends BaseResponse {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1262341691039525086L;

    /** The states. */
    @ApiModelProperty("The states of CET")
    private Map<String, List<String>> states = new HashMap<>();

    /**
     * Gets the states.
     *
     * @return the states
     */
    public Map<String, List<String>> getStates() {
        return states;
    }

    /**
     * Sets the states.
     *
     * @param states the new states
     */
    public void setStates(Map<String, List<String>> states) {
        this.states = states;
    }
}
