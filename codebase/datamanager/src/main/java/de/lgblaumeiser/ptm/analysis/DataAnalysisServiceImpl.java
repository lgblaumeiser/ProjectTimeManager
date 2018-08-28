/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 */
package de.lgblaumeiser.ptm.analysis;

import java.util.Collection;
import java.util.Map;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Maps.newHashMap;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Service implementation
 */
public class DataAnalysisServiceImpl implements DataAnalysisService {
	private final Map<String, Analysis> analysisStore = newHashMap();

	@Override
	public Collection<Collection<Object>> analyze(final String analyzerId, final Collection<String> parameter) {
		checkState(isNotBlank(analyzerId));
		checkState(parameter != null);
		Analysis analysis = analysisStore.get(analyzerId);
		checkState(analysis != null);
		return analysis.analyze(parameter);
	}

	public DataAnalysisServiceImpl addAnalysis(final String id, final Analysis analysis) {
		analysisStore.put(id, analysis);
		return this;
	}
}
