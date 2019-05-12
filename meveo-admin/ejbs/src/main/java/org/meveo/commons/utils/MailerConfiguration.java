
package org.meveo.commons.utils;

import org.meveo.model.BusinessEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

/**
 * Created by HienBach
 */
public class MailerConfiguration extends BusinessEntity {

    private static final Logger log = LoggerFactory.getLogger(ParamBean.class);

    private String _propertyFile;

    /**
     * True if read file is ok.
     */
    private boolean valid = false;

    /**
     * Save properties imported from the file.
     */
    private Properties properties = new Properties();

    private static final char[] hexDigit = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    /**
     * @param nibble input int
     * @return hex output
     */
    private static char toHex(int nibble) {
        return hexDigit[(nibble & 0xF)];
    }

    /**
     * Map of categories.
     */
    private HashMap<String, String> categories = new HashMap<String, String>();

    /**
     * sender
     */
    private String sender;

    /**
     * receiver
     */
    private String receiver;

    /**
     * subject
     */
    private String subject;

    /**
     * body
     */
    private String body;

    /**
     * host
     */
    private String host;

    /**
     * port
     */
    private String port;

    /**
     * userName
     */
    private String userName;

    /**
     * passWord
     */
    private String passWord;

    /**
     * tls
     */
    private String transportLayerSecurity;

    /**
     * Reload application configuration properties file.
     */
    private static boolean reload = false;

    /**
     * Configuration instance
     */
    private static MailerConfiguration instance = null;

    /**
     * Loads application configuration properties from file
     *
     * @param name System property containing a file name, or a relative filename in Wildfly server's configuration directory
     */
    private MailerConfiguration(String name) {
        super();
        if (System.getProperty(name) != null) {
            _propertyFile = System.getProperty(name);
        } else {
            // https://docs.jboss.org/author/display/AS7/Command+line+parameters
            // http://www.jboss.org/jdf/migrations/war-stories/2012/07/18/jack_wang/
            if (System.getProperty("jboss.server.config.dir") == null) {
                _propertyFile = ResourceUtils.getFileFromClasspathResource(name).getAbsolutePath();
            } else {
                _propertyFile = System.getProperty("jboss.server.config.dir") + File.separator + name;
            }
        }
        log.info("Created mailer configuration for file:" + _propertyFile);
        initialize();
    }

    /**
     * Initialize/load application configuration property file
     *
     */
    private void initialize() {
        log.debug("Initialize  from file :" + _propertyFile + "...");
        if (_propertyFile.startsWith("file:")) {
            _propertyFile = _propertyFile.substring(5);
        }

        boolean result = false;
        FileInputStream propertyFile = null;
        Properties pr = new Properties();
        File file = new File(_propertyFile);
        try {
            if (file.createNewFile()) {
                setProperties(pr);
                saveProperties(file);
                result = true;
            } else {
                pr.load(new FileInputStream(_propertyFile));
                setProperties(pr);
                result = true;
            }
        } catch (IOException e1) {
            log.error("Impossible to create :" + _propertyFile);
        } finally {
            if (propertyFile != null) {
                try {
                    propertyFile.close();
                } catch (Exception e) {
                    log.error("FileInputStream error", e);
                }
            }
        }
        // log.debug("-Fin initialize , result:" + result
        // + ", portability.defaultDelay="
        // + getProperty("portability.defaultDelay"));
        valid = result;
    }

    /**
     * Set properties
     *
     * @param new_properties New properties to set
     */
    public void setProperties(Properties new_properties) {
        properties = new_properties;
    }

    /**
     * Save application configuration properties to a given file
     *
     * @param file File to save to
     * @return True if file was saved successfully.
     */
    public boolean saveProperties(File file) {
        boolean result = false;
        String fileName = file.getAbsolutePath();
        log.info("saveProperties to " + fileName);
        OutputStream propertyFile = null;
        BufferedWriter bw = null;
        try {
            propertyFile = new FileOutputStream(file);
            bw = new BufferedWriter(new OutputStreamWriter(propertyFile));
            bw.write("#" + new Date().toString());
            bw.newLine();
            String lastCategory = "";
            synchronized (this) {
                List<String> keys = new ArrayList<String>();
                Enumeration<Object> keysEnum = properties.keys();
                while (keysEnum.hasMoreElements()) {
                    keys.add((String) keysEnum.nextElement());
                }
                Collections.sort(keys);
                for (String key : keys) {
                    key = saveConvert(key, true, true);
                    String val = saveConvert((String) properties.get(key), true, true);
                    if (categories.containsKey(key)) {
                        if (!lastCategory.equals(categories.get(key))) {
                            lastCategory = categories.get(key);
                            bw.newLine();
                            bw.write("#" + lastCategory);
                            bw.newLine();
                        }
                    }
                    bw.write(key + "=" + val);
                    bw.newLine();
                }
            }
            bw.flush();
            result = true;
        } catch (Exception e) {
            log.error("failed to save properties ", e);
        } finally {
            if (propertyFile != null) {
                try {
                    propertyFile.close();
                } catch (Exception e) {
                    log.error("outputStream error ", e);
                }
            }
            if (bw != null) {
                try {
                    bw.close();
                } catch (Exception e) {
                    log.error("BufferedWriter error", e);
                }
            }
        }
        // setInstance(new ParamBean(fileName));
        // log.info("-Fin saveProperties , result:" + result);
        return result;
    }

    /**
     * @param theString input string
     * @param escapeSpace true if escape spacce
     * @param escapeUnicode true if escape unicode
     * @return escaped string.
     */
    private String saveConvert(String theString, boolean escapeSpace, boolean escapeUnicode) {
        int len = theString.length();
        int bufLen = len * 2;
        if (bufLen < 0) {
            bufLen = Integer.MAX_VALUE;
        }
        StringBuffer outBuffer = new StringBuffer(bufLen);

        for (int x = 0; x < len; x++) {
            char aChar = theString.charAt(x);
            // Handle common case first, selecting largest block that
            // avoids the specials below
            if ((aChar > 61) && (aChar < 127)) {
                if (aChar == '\\') {
                    outBuffer.append('\\');
                    outBuffer.append('\\');
                    continue;
                }
                outBuffer.append(aChar);
                continue;
            }
            switch (aChar) {
                case ' ':
                    if (x == 0 || escapeSpace)
                        outBuffer.append('\\');
                    outBuffer.append(' ');
                    break;
                case '\t':
                    outBuffer.append('\\');
                    outBuffer.append('t');
                    break;
                case '\n':
                    outBuffer.append('\\');
                    outBuffer.append('n');
                    break;
                case '\r':
                    outBuffer.append('\\');
                    outBuffer.append('r');
                    break;
                case '\f':
                    outBuffer.append('\\');
                    outBuffer.append('f');
                    break;
                case '=': // Fall through
                case ':': // Fall through
                case '#': // Fall through
                case '!':
                    outBuffer.append('\\');
                    outBuffer.append(aChar);
                    break;
                default:
                    if (((aChar < 0x0020) || (aChar > 0x007e)) & escapeUnicode) {
                        outBuffer.append('\\');
                        outBuffer.append('u');
                        outBuffer.append(toHex((aChar >> 12) & 0xF));
                        outBuffer.append(toHex((aChar >> 8) & 0xF));
                        outBuffer.append(toHex((aChar >> 4) & 0xF));
                        outBuffer.append(toHex(aChar & 0xF));
                    } else {
                        outBuffer.append(aChar);
                    }
            }
        }
        return outBuffer.toString();
    }

    /**
     * Get an application configuration instance from a given file.
     *
     * @param propertiesName System property containing a file name, or a relative filename in Wildfly server's configuration directory
     * @return Application configuration instance
     */
    public static MailerConfiguration getInstance(String propertiesName) {
        if (reload) {
            instance = new MailerConfiguration(propertiesName);
        } else if (instance == null) {
            instance = new MailerConfiguration(propertiesName);
        }

        return instance;
    }

    /**
     * Get an application configuration instance from a default file.
     *
     * @return Application configuration instance
     */
    public static MailerConfiguration getInstance() {
        try {
            return getInstance("meveo-admin.properties");
        } catch (Exception e) {
            log.error("Failed to initialize meveo-admin.properties file.", e);
            throw new RuntimeException(e);
        }
    }

    public static Logger getLog() {
        return log;
    }

    public String get_propertyFile() {
        return _propertyFile;
    }

    public void set_propertyFile(String _propertyFile) {
        this._propertyFile = _propertyFile;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public Properties getProperties() {
        return properties;
    }

    public static char[] getHexDigit() {
        return hexDigit;
    }

    public HashMap<String, String> getCategories() {
        return categories;
    }

    public void setCategories(HashMap<String, String> categories) {
        this.categories = categories;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getHost() {
        host = properties.getProperty("mailSmtpHost");
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        port = properties.getProperty("mailSmtpPort");
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getUserName() {
        userName = properties.getProperty("mailSmtpUsername");
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassWord()  {
         passWord = properties.getProperty("mailSmtpPassword");
         return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    public String getTransportLayerSecurity() {
        transportLayerSecurity = properties.getProperty("mailSmtpTls");
        return transportLayerSecurity;
    }

    public void setTransportLayerSecurity(String transportLayerSecurity) {
        this.transportLayerSecurity = transportLayerSecurity;
    }
}
