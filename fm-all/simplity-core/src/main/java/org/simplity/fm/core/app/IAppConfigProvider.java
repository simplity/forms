package org.simplity.fm.core.app;

/**
 * Class that knows how to get an instance of AppConfigINfo. Designed for use in
 * bootstrapping with a class-name
 *
 */
public interface IAppConfigProvider {

	/**
	 *
	 * @return config info that is used to cnfigure an App
	 */
	AppConfigInfo getConfigInfo();

}
