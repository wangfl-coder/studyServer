package org.springblade.adata.excel;


import lombok.RequiredArgsConstructor;
import org.springblade.adata.service.IExpertOriginService;
import org.springblade.adata.service.IExpertService;
import org.springblade.core.excel.support.ExcelImporter;

import java.util.List;

public class ExpertImporter implements ExcelImporter<ExpertExcel> {

	private final IExpertService expertService;
	private final IExpertOriginService expertOriginService;
	private final Boolean isCovered;

	public ExpertImporter(IExpertService service, Boolean isCovered) {
		this.expertService = service;
		this.isCovered = isCovered;
		this.expertOriginService = null;
	}

	public ExpertImporter(IExpertOriginService service, Boolean isCovered) {
		this.expertOriginService = service;
		this.isCovered = isCovered;
		this.expertService = null;
	}

	@Override
	public void save(List<ExpertExcel> data) {
		expertService.importExpert(data, isCovered);
	}
}
