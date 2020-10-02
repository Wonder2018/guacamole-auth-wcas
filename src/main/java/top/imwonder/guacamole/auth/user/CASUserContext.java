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

    protected CASUserContext(AuthenticationProvider authProvider, String username,
            Map<String, GuacamoleConfiguration> configs) {
        super(authProvider, username, configs);
    }

    public static CASUserContext loadUserContext(AuthenticationProvider authProvider, AuthenticatedUser authenticatedUser) throws GuacamoleException {
        String username = authenticatedUser.getIdentifier();
        Environment env = new LocalEnvironment();
        String className = env.getRequiredProperty(WCASGuacamoleProperties.WCAS_DATABASE_DRIVER_CLASS_NAME);
        String dburl = env.getRequiredProperty(WCASGuacamoleProperties.WCAS_DATABASE_URL);
        String dbUsername = env.getRequiredProperty(WCASGuacamoleProperties.WCAS_DATABASE_USERNAME);
        String dbPassword = env.getRequiredProperty(WCASGuacamoleProperties.WCAS_DATABASE_PASSWORD);
        String prefix = env.getRequiredProperty(WCASGuacamoleProperties.WCAS_DATABASE_PREFIX);
        String sqlFormat = "select %sprotocol, %shostname, %sport, %spassword from %suser_connection_info where %susername=? and %sdisable=?";
        String sql = String.format(sqlFormat, prefix, prefix, prefix, prefix, prefix, prefix, prefix);
        try {
            Class.forName(className);
        } catch (ClassNotFoundException e) {
            System.out.println("Can Not Load Database Driver!");
            return null;
        }

        Map<String, GuacamoleConfiguration> configs = new HashMap<>();

        try (Connection conn = DriverManager.getConnection(dburl, dbUsername, dbPassword);
                PreparedStatement ptmt = conn.prepareStatement(sql)) {
            ptmt.setString(1, username);
            ptmt.setInt(2, 0);
            try (ResultSet rs = ptmt.executeQuery()) {
                while (rs.next()) {
                    GuacamoleConfiguration gc = new GuacamoleConfiguration();
                    gc.setProtocol(rs.getString(1));
                    gc.setParameter("hostname", rs.getString(2));
                    gc.setParameter("port", rs.getString(3));
                    gc.setParameter("password", rs.getString(4));
                    configs.put(String.format("Wonder CAS Connection %d", rs.getRow()), gc);
                }
            }

        } catch (Exception e) {
            System.out.println("can not get user info from database");
            return null;
        }
        return new CASUserContext(authProvider, username, configs);
    }

}
