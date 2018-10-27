package com.atguigu.gmall1129.list.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall1129.bean.SkuInfo;
import com.atguigu.gmall1129.bean.SkuInfoEs;
import com.atguigu.gmall1129.bean.SkuInfoEsParam;
import com.atguigu.gmall1129.bean.SkuInfoEsResult;
import com.atguigu.gmall1129.service.ListService;
import com.atguigu.gmall1129.utils.RedisUtil;
import io.searchbox.action.Action;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.Update;
import io.searchbox.core.search.aggregation.MetricAggregation;
import io.searchbox.core.search.aggregation.TermsAggregation;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 三级分类  和  搜索  即ES 来进入 List页面
 */
@Service
public class ListServiceImpl  implements ListService {

    @Autowired
    JestClient jestClient;

    @Autowired
    RedisUtil redisUtil;


    public void saveSkuInfoEs(SkuInfoEs skuInfoEs){
        Index index = new Index.Builder (skuInfoEs).index ("gmall").type ("SkuInfo").id (skuInfoEs.getId ()).build ();
        try {
            jestClient.execute(index);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //根据查询条件组合query串
    private  String makeQueryStringForSearch(SkuInfoEsParam skuInfoParam){
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //过滤
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        if (skuInfoParam.getCatalog3Id()!=null){
            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("catalog3Id", skuInfoParam.getCatalog3Id());
            boolQueryBuilder.filter(termQueryBuilder);
        }




        if(skuInfoParam.getValueIdList()!=null&&skuInfoParam.getValueIdList().size()>0){
            List<String> valueIdListForSearch = skuInfoParam.getValueIdList();
            for (String valueIdForSearch : valueIdListForSearch) {
                TermQueryBuilder termQueryBuilderValueId = new TermQueryBuilder("skuAttrValueListEs.valueId", valueIdForSearch);
                boolQueryBuilder.filter(termQueryBuilderValueId);
            }
        }


        //匹配
        if(skuInfoParam.getKeyword()!=null&&skuInfoParam.getKeyword().length()>0){
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName", skuInfoParam.getKeyword());
            boolQueryBuilder.must(matchQueryBuilder);

            //高亮
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuName");
            highlightBuilder.preTags("<span style='color:red'>");
            highlightBuilder.postTags("</span>");

            searchSourceBuilder.highlight(highlightBuilder);

        }


        searchSourceBuilder.query(boolQueryBuilder);


        //分页
        searchSourceBuilder.size(skuInfoParam.getPageSize());
        int fromRow=(skuInfoParam.getPageNo()-1)*skuInfoParam.getPageSize();

        searchSourceBuilder.from(fromRow);

        //排序
        searchSourceBuilder.sort("hotScore", SortOrder.DESC);


        //聚合
        TermsBuilder termsBuilder= AggregationBuilders.terms("groupby_valueId").field("skuAttrValueListEs.valueId");

        searchSourceBuilder.aggregation(termsBuilder);

        String query = searchSourceBuilder.toString();

        System.out.println("query = " + query);

        return query;
    }

    private  SkuInfoEsResult makeResultForSearch( SearchResult searchResult,SkuInfoEsParam skuInfoEsParam){

        SkuInfoEsResult skuInfoEsResult = new SkuInfoEsResult();
        //获取商品列表
        List<SearchResult.Hit<SkuInfoEs, Void>> hits = searchResult.getHits(SkuInfoEs.class);
        List<SkuInfoEs> skuInfoEslist=new ArrayList<>(hits.size());
        for (SearchResult.Hit<SkuInfoEs, Void> hit : hits) {
            SkuInfoEs skuInfoEs = hit.source;
            if(hit.highlight!=null) {
                List<String> skuNameHlList = hit.highlight.get("skuName");
                String skuNameHl = skuNameHlList.get(0);
                skuInfoEs.setSkuName(skuNameHl);
            }
            skuInfoEslist.add(skuInfoEs);
        }

        System.out.println("skuInfoEslist = " + skuInfoEslist);
        skuInfoEsResult.setSkuInfoEsList(skuInfoEslist);

        //获取平台属性值列表
        MetricAggregation aggregations = searchResult.getAggregations();
        TermsAggregation groupby_valueId = aggregations.getTermsAggregation("groupby_valueId");

        List<TermsAggregation.Entry> buckets = groupby_valueId.getBuckets();

        List<String> attrValueList=new ArrayList<>(buckets.size());
        for (TermsAggregation.Entry bucket : buckets) {
            String attrValueId = bucket.getKey();
            attrValueList.add(attrValueId);
        }

        System.out.println("attrValueList = " + attrValueList);
        skuInfoEsResult.setValueIdList(attrValueList);


        Long total = searchResult.getTotal();


        skuInfoEsResult.setTotal( total.intValue() );

        long totalPage = (total + skuInfoEsParam.getPageSize() - 1) / skuInfoEsParam.getPageSize();

        skuInfoEsResult.setTotalPage(Integer.parseInt(""+totalPage));

        return skuInfoEsResult;

    }


    public SkuInfoEsResult searchSkuInfoList(SkuInfoEsParam skuInfoParam){
        String query = makeQueryStringForSearch(skuInfoParam);


        Search search = new Search.Builder(query).addIndex("gmall").addType("SkuInfo").build();
        SearchResult searchResult=null;
        try {
              searchResult = jestClient.execute(search);
        } catch (IOException e) {
            e.printStackTrace();
        }

        SkuInfoEsResult skuInfoEsResult = makeResultForSearch(searchResult,skuInfoParam);
        return  skuInfoEsResult;

    }


    /** redis string list set  键值类型hash zset 计数器zset多,可以排序
     * redis 内存计数器  每点击量积累到  1000次 更新一次ES 热度
     *
     * 因为ES不应 高频次 更新它的值 比如hotscore 应该借助redis
     * 服务在商品详情页里 item*/
    public void countHotScore(String skuId){
        Jedis jedis = redisUtil.getJedis();
        String key="hotScoreSet";
        Double curCount = jedis.zincrby(key, 1, skuId);
        if(curCount%10==0){
            updateHotScore(skuId,curCount.longValue());
        }

    }


    public void updateHotScore(String id,Long hotScore){
        String query="{\n" +
                "  \"doc\": {\n" +
                "    \"hotScore\":"+hotScore+"\n" +
                "  }\n" +
                "}";


        Update update = new Update.Builder(query).index("gmall").type("SkuInfo").id(id).build();
        try {
            jestClient.execute(update);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
