package org.meveo.elresolver;

public class ELException extends Exception {

    public ELException(String message){
        super(message);
    }
    
    public ELException(String message, Exception e){
        super(message, e);
    }
}
