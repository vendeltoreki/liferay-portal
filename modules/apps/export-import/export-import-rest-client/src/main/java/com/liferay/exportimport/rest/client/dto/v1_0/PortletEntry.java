/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.exportimport.rest.client.dto.v1_0;

import com.liferay.exportimport.rest.client.function.UnsafeSupplier;
import com.liferay.exportimport.rest.client.serdes.v1_0.PortletEntrySerDes;

import jakarta.annotation.Generated;

import java.io.Serializable;

import java.util.Objects;

/**
 * @author Petteri Karttunen
 * @generated
 */
@Generated("")
public class PortletEntry implements Cloneable, Serializable {

	public static PortletEntry toDTO(String json) {
		return PortletEntrySerDes.toDTO(json);
	}

	public Long getCount() {
		return count;
	}

	public void setCount(Long count) {
		this.count = count;
	}

	public void setCount(UnsafeSupplier<Long, Exception> countUnsafeSupplier) {
		try {
			count = countUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Long count;

	public String getPortletDescription() {
		return portletDescription;
	}

	public void setPortletDescription(String portletDescription) {
		this.portletDescription = portletDescription;
	}

	public void setPortletDescription(
		UnsafeSupplier<String, Exception> portletDescriptionUnsafeSupplier) {

		try {
			portletDescription = portletDescriptionUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String portletDescription;

	public String getPortletId() {
		return portletId;
	}

	public void setPortletId(String portletId) {
		this.portletId = portletId;
	}

	public void setPortletId(
		UnsafeSupplier<String, Exception> portletIdUnsafeSupplier) {

		try {
			portletId = portletIdUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String portletId;

	public String getPortletTitle() {
		return portletTitle;
	}

	public void setPortletTitle(String portletTitle) {
		this.portletTitle = portletTitle;
	}

	public void setPortletTitle(
		UnsafeSupplier<String, Exception> portletTitleUnsafeSupplier) {

		try {
			portletTitle = portletTitleUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String portletTitle;

	@Override
	public PortletEntry clone() throws CloneNotSupportedException {
		return (PortletEntry)super.clone();
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof PortletEntry)) {
			return false;
		}

		PortletEntry portletEntry = (PortletEntry)object;

		return Objects.equals(toString(), portletEntry.toString());
	}

	@Override
	public int hashCode() {
		String string = toString();

		return string.hashCode();
	}

	public String toString() {
		return PortletEntrySerDes.toJSON(this);
	}

}