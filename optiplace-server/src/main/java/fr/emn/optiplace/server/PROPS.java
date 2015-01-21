/**
 *
 */
package fr.emn.optiplace.server;

import java.util.Properties;

/**
 * enum of the properties used to configure the OPL algorithm. Those properties
 * can be set before invoking the first constructor.<br />
 * each of this enum consists in a key to be searched in a properties, and a way
 * to apply the value associated to that key to an OPLServer.
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 *
 */
public enum PROPS {

    VIEWS_PATH("opl.viewspath") {
        @Override
        public void apply(OptiplaceServer server, String value) {
    	server.parse_viewspath(value);
        }
    },
    DATA_PATH("opl.datapath") {
        @Override
        public void apply(OptiplaceServer server, String value) {
    	server.parse_datapath(value);
        }
    },
    FIELDSEPARATOR("opl.fs") {
        @Override
        public void apply(OptiplaceServer server, String value) {
    	server.parse_FS(value);
        }
    },
    DISABLE_VIEW_LOADER("opl.nodynamicview") {
        @Override
        public void apply(OptiplaceServer server, String value) {
    	server.getViewManager().setDisableLoading(Boolean.parseBoolean(value));
        }
    },
    BAN_VIEWS("opl.banviews") {
        @Override
        public void apply(OptiplaceServer server, String value) {
    	server.parse_banViews(value);
        }
    };
    public final String key;

    PROPS(String key) {
        this.key = key;
    }

    public abstract void apply(OptiplaceServer server, String value);

    public void apply(Properties props, OptiplaceServer server) {
        if (props.containsKey(key)) {
    	apply(server, props.getProperty(key));
        }
    }
}