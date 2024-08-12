package org.example.dividend.scheduler;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dividend.model.Company;
import org.example.dividend.model.ScrapedResult;
import org.example.dividend.model.constants.CacheKey;
import org.example.dividend.persist.CompanyRepository;
import org.example.dividend.persist.DividendRepository;
import org.example.dividend.persist.entity.CompanyEntity;
import org.example.dividend.persist.entity.DividendEntity;
import org.example.dividend.scraper.Scraper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@EnableCaching
@AllArgsConstructor
public class ScraperScheduler {

    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;
    private final Scraper yahooFinanceScraper;


//    일정 주기마다 수행
    @CacheEvict(value = CacheKey.KEY_FINANCE, allEntries = true)
    @Scheduled(cron = "${scheduler.scrap.yahoo:0 0/5 * * * *}" )
    public void yahooFinanceScheduling() {
        log.info("scraping scheduler is started");
//        저장된 회사 목록을 조회
        List<CompanyEntity> companies = this.companyRepository.findAll();

//        회사마다 배당금 정보를 새로 스크래핑
        for (var company : companies) {
            log.info("scraping scheduler is started ->" + company.getName());
            ScrapedResult scrapedResult = this.yahooFinanceScraper.scrap(
                                        new Company(company.getTicker(), company.getName()));
            //        스크래핑된 배당금 정보 중 데이터베이스에 없는 값은 저장
            scrapedResult.getDividends().stream()
//                    dividend 모델을 dividend entity로 매핑
                    .map(e -> new DividendEntity(company.getId(), e))
//                    엘리먼트를 하나씩 dividend 레포지토리에 삽입
                    .forEach(e -> {
                        boolean exists = this.dividendRepository.existsByCompanyIdAndDate(e.getCompanyId(), e.getDate());
                        if (!exists) {
                            this.dividendRepository.save(e);
                            log.info("insert new dividend -> " + e.toString());

                        }
                    });

//            연속적으로 스크래핑 대상 사이트 서버에 요청을 날리지 않도록 일시정지
            try {
                Thread.sleep(3000);  //3 seconds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
