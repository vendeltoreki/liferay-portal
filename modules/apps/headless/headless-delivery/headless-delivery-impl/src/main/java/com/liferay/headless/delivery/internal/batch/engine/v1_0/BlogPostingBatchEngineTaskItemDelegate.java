package com.liferay.headless.delivery.internal.batch.engine.v1_0;

import com.liferay.batch.engine.BaseBatchEngineTaskItemDelegate;
import com.liferay.batch.engine.BatchEngineTaskItemDelegate;
import com.liferay.batch.engine.pagination.Page;
import com.liferay.batch.engine.pagination.Pagination;
import com.liferay.headless.delivery.dto.v1_0.BlogPosting;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.search.filter.Filter;

import java.io.Serializable;
import java.util.Map;

import org.osgi.service.component.annotations.Component;

@Component(service = BatchEngineTaskItemDelegate.class)
public class BlogPostingBatchEngineTaskItemDelegate extends BaseBatchEngineTaskItemDelegate<BlogPosting> {

	@Override
	public Page<BlogPosting> read(Filter filter, Pagination pagination,
		Sort[] sorts, Map<String, Serializable> parameters, String search)
		throws Exception {

		return null;
	}

}
