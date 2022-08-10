package org.meveo.endpoint;

import java.util.Map;

import org.meveo.service.script.Script;
import org.meveo.admin.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScriptForEndointTest extends Script {
  
	private static final Logger log = LoggerFactory.getLogger(ScriptForEndointTest.class);
  
	private String param1;
  
  	private String param2;
  
    public void setParam1(String param1){
      this.param1=param1;
    }
  
    public void setParam2(String param2){
      this.param2=param2;
    }
  
	@Override
	public void execute(Map<String, Object> parameters) throws BusinessException {
      log.info("param1 :{}",param1);
      log.info("param2 :{}",param2);
      log.info("parameters :{}",parameters);
		
	}
	
}