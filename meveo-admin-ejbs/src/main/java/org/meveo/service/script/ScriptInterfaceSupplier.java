package org.meveo.service.script;

@FunctionalInterface
public interface ScriptInterfaceSupplier {
	
	ScriptInterface getScriptInterface() throws Exception;

}
