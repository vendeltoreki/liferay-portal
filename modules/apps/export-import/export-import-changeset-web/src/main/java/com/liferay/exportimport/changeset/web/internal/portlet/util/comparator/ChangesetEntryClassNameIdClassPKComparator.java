/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.exportimport.changeset.web.internal.portlet.util.comparator;

import com.liferay.changeset.model.ChangesetEntry;
import com.liferay.portal.kernel.util.OrderByComparator;

/**
 * @author Jürgen Kappler
 */
public class ChangesetEntryClassNameIdClassPKComparator
	extends OrderByComparator<ChangesetEntry> {

	public static final String ORDER_BY_ASC =
		"ChangesetEntry.classNameId ASC, ChangesetEntry.classPK ASC";

	public static final String ORDER_BY_DESC =
		"ChangesetEntry.classNameId DESC, ChangesetEntry.classPK DESC";

	public static final String[] ORDER_BY_FIELDS = {"classNameId", "classPK"};

	public ChangesetEntryClassNameIdClassPKComparator() {
		this(false);
	}

	public ChangesetEntryClassNameIdClassPKComparator(boolean ascending) {
		_ascending = ascending;
	}

	@Override
	public int compare(
		ChangesetEntry changesetEntry1, ChangesetEntry changesetEntry2) {

		int value = 0;

		if (changesetEntry1.getClassNameId() !=
				changesetEntry2.getClassNameId()) {

			value = Long.compare(
				changesetEntry1.getClassNameId(),
				changesetEntry2.getClassNameId());
		}

		if (value == 0) {
			value = Long.compare(
				changesetEntry1.getClassPK(), changesetEntry2.getClassPK());
		}

		if (_ascending) {
			return value;
		}

		return -value;
	}

	@Override
	public String getOrderBy() {
		if (_ascending) {
			return ORDER_BY_ASC;
		}

		return ORDER_BY_DESC;
	}

	@Override
	public String[] getOrderByFields() {
		return ORDER_BY_FIELDS;
	}

	@Override
	public boolean isAscending() {
		return _ascending;
	}

	private final boolean _ascending;

}