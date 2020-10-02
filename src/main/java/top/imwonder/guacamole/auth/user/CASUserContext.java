/*
 * @Author: Wonder2019 
 * @Date: 2020-10-02 08:52:27 
 * @Last Modified by:   Wonder2019 
 * @Last Modified time: 2020-10-02 08:52:27 
 */
package top.imwonder.guacamole.auth.user;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.environment.Environment;
import org.apache.guacamole.environment.LocalEnvironment;
import org.apache.guacamole.net.auth.AuthenticatedUser;
import org.apache.guacamole.net.auth.AuthenticationProvider;
import org.apache.guacamole.net.auth.simple.SimpleUserContext;
import org.apache.guacamole.protocol.GuacamoleConfiguration;

import top.imwonder.guacamole.auth.conf.WCASGuacamoleProperties;

public class CASUserContext extends SimpleUserContext {

    private static final String CLASS_NAME;
    private static final String DB_URL;
    private static final String DB_USNAME;
    private static final String DB_PWD;
    private static final String SQL;
    private static final int ID_INDEX;
    private static final int DES_INDEX;
    private static final Boolean IS_INIT;

    static {
        String className = null;
        String dbUrl = null;
        String dbUsname = null;
        String dbPwd = null;
        String prefix = null;
        String tbName = null;
        String colUsname = null;
        String colId = null;
        String colProtocol = null;
        String colHostname = null;
        String colPort = null;
        String colPwd = null;
        String colDes = null;
        Boolean isInit = false;
        try {
            Environment env = new LocalEnvironment();
            className = env.getRequiredProperty(WCASGuacamoleProperties.WCAS_DATABASE_DRIVER_CLASS_NAME);
            dbUrl = env.getRequiredProperty(WCASGuacamoleProperties.WCAS_DATABASE_URL);
            dbUsname = env.getRequiredProperty(WCASGuacamoleProperties.WCAS_DATABASE_USERNAME);
            dbPwd = env.getRequiredProperty(WCASGuacamoleProperties.WCAS_DATABASE_PASSWORD);
            prefix = env.getProperty(WCASGuacamoleProperties.WCAS_DATABASE_PREFIX);
            tbName = env.getProperty(WCASGuacamoleProperties.WCAS_DATABASE_TABLE_NAME);
            tbName = tbName == null ? "user_connection_info" : tbName;
            colId = env.getProperty(WCASGuacamoleProperties.WCAS_DATABASE_COLUMN_ID);
            colUsname = env.getProperty(WCASGuacamoleProperties.WCAS_DATABASE_COLUMN_USERNAME);
            colUsname = colUsname == null ? "username" : colUsname;
            colProtocol = env.getProperty(WCASGuacamoleProperties.WCAS_DATABASE_COLUMN_PROTOCOL);
            colProtocol = colProtocol == null ? "protocol" : colProtocol;
            colHostname = env.getProperty(WCASGuacamoleProperties.WCAS_DATABASE_COLUMN_HOSTNAME);
            colHostname = colHostname == null ? "hostname" : colHostname;
            colPort = env.getProperty(WCASGuacamoleProperties.WCAS_DATABASE_COLUMN_PORT);
            colPort = colPort == null ? "port" : colPort;
            colPwd = env.getProperty(WCASGuacamoleProperties.WCAS_DATABASE_COLUMN_PASSWORD);
            colPwd = colPwd == null ? "password" : colPwd;
            colDes = env.getProperty(WCASGuacamoleProperties.WCAS_DATABASE_COLUMN_DESCRIPTION);
            isInit = true;

        } catch (Exception e) {
        }
        if (isInit) {
            CLASS_NAME = className;
            DB_URL = dbUrl;
            DB_USNAME = dbUsname;
            DB_PWD = dbPwd;
            if (prefix != null) {
                tbName = prefix + tbName;
                colId = prefix + colId;
                colUsname = prefix + colUsname;
                colProtocol = prefix + colProtocol;
                colHostname = prefix + colHostname;
                colPort = prefix + colPort;
                colPwd = prefix + colPwd;
                colDes = prefix + colDes;
            }
            StringBuffer sqlBuffer = new StringBuffer();
            sqlBuffer.append(String.format("select %s, %s, %s, %s", colProtocol, colHostname, colPort, colPwd));
            int count = 4;
            if (colId != null) {
                sqlBuffer.append(", ");
                sqlBuffer.append(colId);
                ID_INDEX = ++count;
            } else {
                ID_INDEX = 0;
            }
            if (colDes != null) {
                sqlBuffer.append(", ");
                sqlBuffer.append(colDes);
                DES_INDEX = ++count;
            } else {
                DES_INDEX = 0;
            }
            sqlBuffer.append(String.format(" from %s where %s=?", tbName, colUsname));
            SQL = sqlBuffer.toString();
        } else {
            CLASS_NAME = null;
            DB_URL = null;
            DB_USNAME = null;
            DB_PWD = null;
            SQL = null;
            DES_INDEX = 0;
            ID_INDEX = 0;
        }
        IS_INIT = isInit;
    }

    protected CASUserContext(AuthenticationProvider authProvider, String username,
            Map<String, GuacamoleConfiguration> configs) {
        super(authProvider, username, configs);
    }

    public static CASUserContext loadUserContext(AuthenticationProvider authProvider,
            AuthenticatedUser authenticatedUser) throws GuacamoleException {
        if (!IS_INIT) {
            throw new GuacamoleException("canot init configures");
        }
        String username = authenticatedUser.getIdentifier();
        try {
            Class.forName(CLASS_NAME);
        } catch (ClassNotFoundException e) {
            throw new GuacamoleException("Can Not Load Database Driver!");
        }

        Map<String, GuacamoleConfiguration> configs = new HashMap<>();

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USNAME, DB_PWD);
                PreparedStatement ptmt = conn.prepareStatement(SQL)) {
            ptmt.setString(1, username);
            try (ResultSet rs = ptmt.executeQuery()) {
                while (rs.next()) {
                    GuacamoleConfiguration gc = new GuacamoleConfiguration();
                    gc.setProtocol(rs.getString(1));
                    gc.setParameter("hostname", rs.getString(2));
                    rs.getString(1);
                    gc.setParameter("port", rs.getString(3));
                    gc.setParameter("password", rs.getString(4));
                    configs.put(genConnectionId(rs), gc);
                }
            }

        } catch (Exception e) {
            throw new GuacamoleException("can not get user info from database");
        }
        return new CASUserContext(authProvider, username, configs);
    }

    private static String genConnectionId(ResultSet rs) throws SQLException {
        StringBuffer idBuffer = new StringBuffer("Wonder CAS Connection (");
        idBuffer.append(ID_INDEX == 0 ? rs.getRow() : rs.getString(ID_INDEX));
        idBuffer.append(")$");
        idBuffer.append(String.format("%s://%s:%s", rs.getString(1), rs.getString(2), rs.getString(3)));
        idBuffer.append(DES_INDEX == 0 ? "" : " - " + rs.getString(DES_INDEX));
        return idBuffer.toString();
    }

}
