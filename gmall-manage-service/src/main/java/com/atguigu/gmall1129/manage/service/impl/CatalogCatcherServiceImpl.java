package com.atguigu.gmall1129.manage.service.impl;

import com.atguigu.gmall1129.bean.BaseCatalog1;
import com.atguigu.gmall1129.bean.BaseCatalog2;
import com.atguigu.gmall1129.bean.BaseCatalog3;
import com.atguigu.gmall1129.manage.mapper.BaseCatalog1Mapper;
import com.atguigu.gmall1129.manage.mapper.BaseCatalog2Mapper;
import com.atguigu.gmall1129.manage.mapper.BaseCatalog3Mapper;
import com.atguigu.gmall1129.utils.HttpClientUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @param
 * @return
 */


@Service
public class CatalogCatcherServiceImpl {

       String url="https://www.jd.com/allSort.aspx";


    @Autowired
    BaseCatalog1Mapper baseCatalog1Mapper;
    @Autowired
    BaseCatalog2Mapper baseCatalog2Mapper;
    @Autowired
    BaseCatalog3Mapper baseCatalog3Mapper;

       @Test
        public void catchCatalog(){
            String html = HttpClientUtil.doGet(url);

            Document document = Jsoup.parse(html);

            Elements elements = document.select("div[class='category-item m']");
            for (Element element : elements) {
                String catalog1Name = element.select(".item-title span").text();
                System.out.println("catalog1Name = " + catalog1Name);
                BaseCatalog1 baseCatalog1 =new BaseCatalog1();

                baseCatalog1.setName(catalog1Name);
                baseCatalog1Mapper.insertSelective(baseCatalog1);


                Elements catalog2s = element.select(".items .clearfix");
                for (Element catalog2 : catalog2s) {
                    String catalog2Name = catalog2.select("dt a").text();
                    System.out.println("-------catalog2Name = " + catalog2Name);

                    BaseCatalog2 baseCatalog2 =new BaseCatalog2();
                    baseCatalog2.setName(catalog2Name);
                    baseCatalog2.setCatalog1Id(baseCatalog1.getId());
                    baseCatalog2Mapper.insertSelective(baseCatalog2);


                    Elements catalog3s = catalog2.select("dd a");
                    for (Element catalog3 : catalog3s) {
                        String catalog3Name = catalog3.text();
                        System.out.println("------------------catalog3Name = " + catalog3Name);

                        BaseCatalog3 baseCatalog3 =new BaseCatalog3();
                        baseCatalog3.setCatalog2Id(baseCatalog2.getId());
                        baseCatalog3.setName(catalog3Name);
                        baseCatalog3Mapper.insertSelective(baseCatalog3);

                    }
                }
            }

        }


}
