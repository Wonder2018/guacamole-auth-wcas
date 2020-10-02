/*
 * @Author: Wonder2020 
 * @Date: 2020-10-01 15:53:30 
 * @Last Modified by: Wonder2020
 * @Last Modified time: 2020-10-01 19:26:43
 */
package top.imwonder.guacamole.auth;

import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.auth.cas.CASAuthenticationProvider;
import org.apache.guacamole.net.auth.AuthenticatedUser;
import org.apache.guacamole.net.auth.UserContext;

import top.imwonder.guacamole.auth.user.CASUserContext;

public class WCASAuthenticationProvider extends CASAuthenticationProvider {

    public WCASAuthenticationProvider() throws GuacamoleException {
        super();
    }

    @Override
    public String getIdentifier() {
        return "wcas";
    }

    @Override
    public UserContext getUserContext(AuthenticatedUser authenticatedUser) throws GuacamoleException {
        return CASUserContext.loadUserContext(this, authenticatedUser);
    }
}
