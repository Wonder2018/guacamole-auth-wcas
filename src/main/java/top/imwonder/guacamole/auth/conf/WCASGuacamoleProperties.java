/*
 * @Author: Wonder2020 
 * @Date: 2020-10-01 17:52:18 
 * @Last Modified by: Wonder2020
 * @Last Modified time: 2020-10-01 19:04:00
 */

package top.imwonder.guacamole.auth.conf;

import org.apache.guacamole.properties.StringGuacamoleProperty;

public class WCASGuacamoleProperties {

    private WCASGuacamoleProperties() {
    }

    public static final StringGuacamoleProperty WCAS_DATABASE_URL = new StringGuacamoleProperty() {

        @Override
        public String getName() {
            return "wcas-database-url";
        }

    };
    public static final StringGuacamoleProperty WCAS_DATABASE_USERNAME = new StringGuacamoleProperty() {

        @Override
        public String getName() {
            return "wcas-database-username";
        }

    };
    public static final StringGuacamoleProperty WCAS_DATABASE_PASSWORD = new StringGuacamoleProperty() {

        @Override
        public String getName() {
            return "wcas-database-password";
        }

    };
    public static final StringGuacamoleProperty WCAS_DATABASE_DRIVER_CLASS_NAME = new StringGuacamoleProperty() {

        @Override
        public String getName() {
            return "wcas-database-driver-class-name";
        }

    };
    public static final StringGuacamoleProperty WCAS_DATABASE_PREFIX = new StringGuacamoleProperty() {

        @Override
        public String getName() {
            return "wcas-database-prefix";
        }

    };

}
