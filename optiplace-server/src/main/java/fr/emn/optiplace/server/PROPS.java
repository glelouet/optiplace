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

	/** the path of the directory to find views jars in */
	VIEWS_PATH("opl.viewspath") {

		@Override
		public void apply(OptiplaceServer server, String value) {
			server.parse_viewspath(value);
		}
	},

	/** the path of the directory to find the views'configuration in */
	DATA_PATH("opl.datapath") {

		@Override
		public void apply(OptiplaceServer server, String value) {
			server.parse_datapath(value);
		}
	},

	/** the field separator for data */
	FIELDSEPARATOR("opl.fs") {

		@Override
		public void apply(OptiplaceServer server, String value) {
			server.parse_FS(value);
		}
	},

	/** set to true to disable loading of views jars */
	DISABLE_VIEW_LOADER("opl.nodynamicview") {

		@Override
		public void apply(OptiplaceServer server, String value) {
			server.getViewManager().setDisableLoading(Boolean.parseBoolean(value));
		}
	},

	/** list of views we want NOT to load */
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