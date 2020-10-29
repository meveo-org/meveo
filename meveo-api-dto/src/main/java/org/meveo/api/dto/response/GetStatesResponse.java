package org.meveo.api.dto.response;

import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "StatesResponse")
@XmlAccessorType(XmlAccessType.FIELD)
public class GetStatesResponse extends BaseResponse {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1262341691039525086L;

    /** The states. */
    @ApiModelProperty("The states")
    private List<String> states = new ArrayList<>();

    /**
     * Gets the states.
     *
     * @return the states
     */
    public List<String> getStates() {
        return states;
    }

    /**
     * Sets the states.
     *
     * @param states the new states
     */
    public void setStates(List<String> states) {
        this.states = states;
    }
}
