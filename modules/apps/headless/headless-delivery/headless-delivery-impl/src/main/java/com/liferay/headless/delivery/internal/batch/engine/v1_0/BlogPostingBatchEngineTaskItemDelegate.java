package com.liferay.headless.delivery.internal.batch.engine.v1_0;

import com.liferay.batch.engine.BaseBatchEngineTaskItemDelegate;
import com.liferay.batch.engine.BatchEngineTaskItemDelegate;
import com.liferay.batch.engine.pagination.Page;
import com.liferay.batch.engine.pagination.Pagination;
import com.liferay.blogs.service.BlogsEntryService;
import com.liferay.blogs.service.BlogsEntryServiceUtil;
import com.liferay.headless.delivery.dto.v1_0.BlogPosting;
import com.liferay.headless.delivery.dto.v1_0.Image;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.search.filter.Filter;
import com.liferay.portal.vulcan.util.LocalDateTimeUtil;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(service = BatchEngineTaskItemDelegate.class)
public class BlogPostingBatchEngineTaskItemDelegate extends BaseBatchEngineTaskItemDelegate<BlogPosting> {

	@Override
	public Page<BlogPosting> read(Filter filter, Pagination pagination,
		Sort[] sorts, Map<String, Serializable> parameters, String search)
		throws Exception {

		return null;
	}

	@Override
	public BlogPosting createItem(
		BlogPosting blogPosting,
			Map<String, Serializable> parameters)
		throws Exception {

		String externalReferenceCode = null;

		long groupId = 0;
		
		LocalDateTime localDateTime = LocalDateTimeUtil.toLocalDateTime(
			blogPosting.getDatePublished());
		Image image = blogPosting.getImage();

		
		_blogsEntryService.addEntry(
			externalReferenceCode, blogPosting.getHeadline(),
			blogPosting.getAlternativeHeadline(),
			blogPosting.getFriendlyUrlPath(), blogPosting.getDescription(),
			blogPosting.getArticleBody(), localDateTime.getMonthValue() - 1,
			localDateTime.getDayOfMonth(), localDateTime.getYear(),
			localDateTime.getHour(), localDateTime.getMinute(), true, true,
			new String[0], null, null,
			null, null);
		
		return null;
	}
	
	@Reference
	private BlogsEntryService _blogsEntryService;
	
}
