package org.example.dividend.scraper;

import org.example.dividend.model.Company;
import org.example.dividend.model.ScrapedResult;

public interface Scraper {
    Company scrapCompanyByTicker(String ticker);

    ScrapedResult scrap(Company company);
}
