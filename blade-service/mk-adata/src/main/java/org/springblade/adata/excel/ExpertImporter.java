package org.springblade.adata.excel;


import lombok.RequiredArgsConstructor;
import org.springblade.adata.service.IExpertService;
import org.springblade.core.excel.support.ExcelImporter;

import java.util.List;

@RequiredArgsConstructor
public class ExpertImporter implements ExcelImporter<ExpertExcel> {

	private final IExpertService service;
	private final Boolean isCovered;

	@Override
	public void save(List<ExpertExcel> data) {
		service.importUser(data, isCovered);
	}
}
