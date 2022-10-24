/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.exportimport.kernel.lar;

import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMap;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMapFactory;
import com.liferay.portal.kernel.model.Portlet;
import com.liferay.portal.kernel.module.util.SystemBundleUtil;

import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.BundleContext;

/**
 * Provides a utility facade to the staged model data handler registry
 * framework.
 *
 * @author Máté Thurzó
 * @author Brian Wing Shun Chan
 * @since  6.2
 */
public class PortletDataHandlerRegistryUtil {

	/**
	 * Returns the registered staged model data handler with the class name.
	 *
	 * @param  className the name of the staged model class
	 * @return the registered staged model data handler with the class name, or
	 *         <code>null</code> if none are registered with the class name
	 */
	public static PortletDataHandler getPortletDataHandler(
		String className) {

		return _portletDataHandlers.getService(className);
	}

	public static PortletDataHandler getPortlet(
		String className) {

		return _portletDataHandlers.getService(className);
	}
	
	/**
	 * Returns all the registered staged model data handlers.
	 *
	 * @return the registered staged model data handlers
	 */
	public static List<PortletDataHandler> getPortletDataHandlers() {
		return new ArrayList<>(_portletDataHandlers.values());
	}

	private PortletDataHandlerRegistryUtil() {
	}

	private static final BundleContext _bundleContext =
		SystemBundleUtil.getBundleContext();

	private static final ServiceTrackerMap<String, PortletDataHandler>
		_portletDataHandlers = ServiceTrackerMapFactory.openSingleValueMap(
			_bundleContext,
			(Class<PortletDataHandler>)
				(Class<?>)PortletDataHandler.class,
			null,
			(serviceReference, emitter) -> {
				System.out.println(" --serviceReference="+serviceReference);
				PortletDataHandler portletDataHandler =
					_bundleContext.getService(serviceReference);

				System.out.println(" --portletDataHandler="+portletDataHandler);
				
				if (portletDataHandler != null && portletDataHandler.getClassNames() != null) {
					for (String className :
						portletDataHandler.getClassNames()) {
						System.out.println(" ----className="+className);
						emitter.emit(className);
					}
				}
				_bundleContext.ungetService(serviceReference);
			});

	private static final ServiceTrackerMap<String, Portlet>
	_portlets = ServiceTrackerMapFactory.openSingleValueMap(
		_bundleContext,
		(Class<Portlet>)
			(Class<?>)Portlet.class,
		null,
		(serviceReference, emitter) -> {
			System.out.println(" --serviceReference="+serviceReference);
			Portlet portlet =
				_bundleContext.getService(serviceReference);

			if (portlet != null) {
			
				PortletDataHandler portletDataHandler =
					portlet.getPortletDataHandlerInstance();
				
				System.out.println(" --portletDataHandler="+portletDataHandler);
				
				if (portletDataHandler != null && portletDataHandler.getClassNames() != null) {
					for (String className :
						portletDataHandler.getClassNames()) {
						System.out.println(" ----className="+className);
						emitter.emit(className);
					}
				}
			}
			_bundleContext.ungetService(serviceReference);
		});
	
}