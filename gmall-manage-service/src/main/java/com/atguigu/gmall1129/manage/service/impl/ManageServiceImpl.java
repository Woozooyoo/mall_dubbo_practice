package com.atguigu.gmall1129.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall1129.bean.*;
import com.atguigu.gmall1129.manage.constant.RedisConst;
import com.atguigu.gmall1129.manage.mapper.*;
import com.atguigu.gmall1129.service.ListService;
import com.atguigu.gmall1129.service.ManageService;
import com.atguigu.gmall1129.utils.RedisUtil;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * 只做了 新增 没做编辑逻辑
 * 
 */

@Service
public class ManageServiceImpl implements ManageService {
	@Autowired
	BaseCatalog1Mapper baseCatalog1Mapper;
	@Autowired
	BaseCatalog2Mapper baseCatalog2Mapper;
	@Autowired
	BaseCatalog3Mapper baseCatalog3Mapper;
	@Autowired
	BaseAttrInfoMapper baseAttrInfoMapper;
	@Autowired
	BaseAttrValueMapper baseAttrValueMapper;

	@Autowired
	SpuInfoMapper spuInfoMapper;
	@Autowired
	BaseSaleAttrMapper baseSaleAttrMapper;
	@Autowired
	SpuSaleAttrMapper spuSaleAttrMapper;
	@Autowired
	SpuSaleAttrValueMapper spuSaleAttrValueMapper;

	@Autowired
	SpuImageMapper spuImageMapper;
	@Autowired
	SkuAttrValueMapper skuAttrValueMapper;
	@Autowired
	SkuImageMapper skuImageMapper;
	@Autowired
	SkuInfoMapper skuInfoMapper;
	@Autowired
	SkuSaleAttrValueMapper skuSaleAttrValueMapper;

	// 配置类@Configuration的 RedisConfig 的@Bean RedisUtil getRedisUtil可以直接自动注入
	@Autowired
	RedisUtil redisUtil;

	@Reference
	ListService listService;

	Map ooMaper = new HashMap<> ();

	public List<BaseCatalog1> getCataLog1List() {
		List<BaseCatalog1> baseCatalog1List = baseCatalog1Mapper.selectAll ();

		for (int i = 0; i < 50000; i++) {
			ooMaper.put (new Random ().nextInt (10000000), baseCatalog1List);
		}

		return baseCatalog1List;

	}

	public List<BaseCatalog2> getCataLog2List(String catalog1Id) {
		BaseCatalog2 baseCatalog2Query = new BaseCatalog2 ();
		baseCatalog2Query.setCatalog1Id (catalog1Id);

		List<BaseCatalog2> baseCatalog2List = baseCatalog2Mapper.select (baseCatalog2Query);
		return baseCatalog2List;

	}

	public List<BaseCatalog3> getCataLog3List(String catalog2Id) {
		BaseCatalog3 baseCatalog3Query = new BaseCatalog3 ();
		baseCatalog3Query.setCatalog2Id (catalog2Id);

		List<BaseCatalog3> baseCatalog3List = baseCatalog3Mapper.select (baseCatalog3Query);
		return baseCatalog3List;

	}

	public List<BaseAttrInfo> getAttrInfoList(String catalog3Id) {
		BaseAttrInfo baseAttrInfo = new BaseAttrInfo ();

		baseAttrInfo.setCatalog3Id (catalog3Id);

//		List<BaseAttrInfo> baseAttrInfoList = baseAttrInfoMapper.select (baseAttrInfo);/*错误 应该使用 mybatis的select*/
		List<BaseAttrInfo> baseAttrInfoList = baseAttrInfoMapper.selectAttrInfoList (Long.parseLong (catalog3Id));
		//1循环遍历  连接次数 连接 操作消耗

		return baseAttrInfoList;
	}

	public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
		baseAttrInfoMapper.insertSelective (baseAttrInfo);

		//应该线程池
		List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList ();
		for (BaseAttrValue baseAttrValue : attrValueList) {
			baseAttrValue.setAttrId (baseAttrInfo.getId ());
			baseAttrValueMapper.insertSelective (baseAttrValue);
		}

	}

	public List<SpuInfo> getSpuList(String catalog3Id) {
		SpuInfo spuInfoQuery = new SpuInfo ();
		spuInfoQuery.setCatalog3Id (catalog3Id);

		List<SpuInfo> spuInfoList = spuInfoMapper.select (spuInfoQuery);

		return spuInfoList;

	}

	public List<BaseSaleAttr> getBaseSaleAttrList() {
		List<BaseSaleAttr> baseSaleAttrList = baseSaleAttrMapper.selectAll ();
		return baseSaleAttrList;
	}

	public void saveSpuInfo(SpuInfo spuInfo) {
		//自动生成主键 如果有主键 update 没主键 insert
		spuInfoMapper.insertSelective (spuInfo);

		//保存前先清空原来  假如是update的图片 原来有10张,删了3张,又加了2张
		SpuImage spuImageDel = new SpuImage ();
		String spuInfoId = spuInfo.getId ();
		spuImageDel.setSpuId (spuInfoId);
		spuImageMapper.delete (spuImageDel);

		//一对多
		List<SpuImage> spuImageList = spuInfo.getSpuImageList ();
		for (SpuImage spuImage : spuImageList) {
			spuImage.setSpuId (spuInfoId);
			spuImageMapper.insertSelective (spuImage);
		}

		SpuSaleAttr spuSaleAttrDel = new SpuSaleAttr ();
		spuSaleAttrDel.setSpuId (spuInfoId);
		spuSaleAttrMapper.delete (spuSaleAttrDel);

		SpuSaleAttrValue spuSaleAttrValueDel = new SpuSaleAttrValue ();
		spuSaleAttrValueDel.setSpuId (spuInfoId);
		spuSaleAttrValueMapper.delete (spuSaleAttrValueDel);

		List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList ();
		for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {
			spuSaleAttr.setSpuId (spuInfoId);
			spuSaleAttrMapper.insertSelective (spuSaleAttr);

			List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList ();
			for (SpuSaleAttrValue spuSaleAttrValue : spuSaleAttrValueList) {
				spuSaleAttrValue.setSpuId (spuInfoId);
				spuSaleAttrValueMapper.insertSelective (spuSaleAttrValue);
			}

		}
	}

	// 增加sku  的销售属性列表
	public List<SpuSaleAttr> getSaleAttrList(String spuId) {
		List<SpuSaleAttr> spuSaleAttrList = spuSaleAttrMapper.selectSaleAttrInfoList (Long.parseLong (spuId));
		return spuSaleAttrList;
	}

	/** 通过sku 查询销售属性    可以优化 用redis来跳转*/
	public List<SpuSaleAttr> getSaleAttrListBySku(String spuId, String skuId) {
		List<SpuSaleAttr> spuSaleAttrList = spuSaleAttrMapper.selectSaleAttrInfoListBySku (Long.parseLong (spuId), Long.parseLong (skuId));
		return spuSaleAttrList;
	}

	// 增加sku  的销售属性列表
	public List<SpuImage> getSpuImageList(String spuId) {
		SpuImage spuImageQuery = new SpuImage ();
		spuImageQuery.setSpuId (spuId);
		List<SpuImage> spuImageList = spuImageMapper.select (spuImageQuery);
		return spuImageList;
	}

	// 增加sku  的 保存sku
	public void saveSkuInfo(SkuInfo skuInfo) {
		skuInfoMapper.insertSelective (skuInfo);

		String skuInfoId = skuInfo.getId ();

		//编辑第二次保存的时候删除掉以前的 首次保存无所谓
		SkuImage skuImageDel = new SkuImage ();
		skuImageDel.setSkuId (skuInfoId);
		skuImageMapper.delete (skuImageDel);

		List<SkuImage> skuImageList = skuInfo.getSkuImageList ();
		for (SkuImage skuImage : skuImageList) {
			skuImage.setSkuId (skuInfoId);
			skuImageMapper.insertSelective (skuImage);
		}

		List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList ();
		for (SkuAttrValue skuAttrValue : skuAttrValueList) {
			skuAttrValue.setSkuId (skuInfoId);
			skuAttrValueMapper.insertSelective (skuAttrValue);
		}

		List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList ();
		for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
			skuSaleAttrValue.setSkuId (skuInfoId);
			skuSaleAttrValueMapper.insertSelective (skuSaleAttrValue);
		}

	}


	/** 以下是item-web 调用的serviceImpl 方法------------------------------------------------------------------*/
	// getSkuInfo 测试商品详情 方法
/*	public SkuInfo getSkuInfo(String skuId) {

		SkuInfo skuInfo = skuInfoMapper.selectByPrimaryKey (skuId);

		SkuImage skuImageQuery = new SkuImage ();
		skuImageQuery.setSkuId (skuInfo.getId ());
		List<SkuImage> skuImageList = skuImageMapper.select (skuImageQuery);

		skuInfo.setSkuImageList (skuImageList);

//		SkuAttrValue skuAttrValue = new SkuAttrValue ();
//		skuAttrValue.setSkuId (skuId);
//		List<SkuAttrValue> skuAttrValueList = skuAttrValueMapper.select (skuAttrValue);
//
//		skuInfo.setSkuAttrValueList (skuAttrValueList);

		return skuInfo;
	}*/

	/*		1.sleep调用时不会释放锁。wait调用时会释放锁
		2.sleep是Thread中的方法，wait是Object中的方法
		3.sleep会自动唤醒，wait需要被notify/notifyAll唤醒。*/
	/** 分布式锁 666 因为mysql数据库查询缓慢 如果 在redis 查mysql的时候 高并发查redis redis全部查MySQL 就会雪崩*/
	public SkuInfo getSkuInfo(String skuId) {
		try {
			Jedis jedis = redisUtil.getJedis ();
			//先查询缓存
			String skuKey = RedisConst.SKU_PREFIX + skuId + RedisConst.SKU_SUFFIX;

			String skuInfoJson = jedis.get (skuKey);
			System.err.println (Thread.currentThread ().getName () + "开始查询");
			//缓存如果命中，直接返回结果
			if (skuInfoJson != null && skuInfoJson.length () > 0) {
				if ("empty".equals (skuInfoJson)) {
					return null;
				}

				System.err.println (Thread.currentThread ().getName () + "已命中");
				SkuInfo skuInfo = JSON.parseObject (skuInfoJson, SkuInfo.class);
				jedis.close ();
				return skuInfo;

			} else {//缓存如果没命中，先分布锁redis
				System.err.println (Thread.currentThread ().getName () + "未命中");
				//先检查是否能获得锁，同时尝试获得锁
				String skuLockKey = RedisConst.SKU_PREFIX + skuId + RedisConst.SKULOCK_SUFFIX;
				// set lock3 locked NX EX 10 锁住10秒 返回ok |下次再set 为 null |10秒后放锁
				String ifLocked = jedis.set (skuLockKey, "locked", "NX", "EX", 10);
				if (ifLocked == null) {
					System.err.println (Thread.currentThread ().getName () + "未获得锁，开始自旋");
					try {
						Thread.sleep (1000);//等待1秒线程自旋
					} catch (InterruptedException e) {
						e.printStackTrace ();
					}
					//线程自旋
					return getSkuInfo (skuId);
				} else {
					System.err.println (Thread.currentThread ().getName () + "已获得锁，开始查询数据库");
					//未命中查询数据库
					SkuInfo skuInfoDB = getSkuInfoDB (skuId);
					if (skuInfoDB == null) {
						jedis.setex (skuKey, RedisConst.SKU_TIMEOUT, "empty");
					} else {
						//保存一份到缓存
						String skuInfoJsonNew = JSON.toJSONString (skuInfoDB);
						jedis.setex (skuKey, RedisConst.SKU_TIMEOUT, skuInfoJsonNew);
					}
					jedis.close ();
					return skuInfoDB;
				}

			}
		} catch (JedisConnectionException e) {
			e.printStackTrace ();
		}
		//如果redis 宕机了 直接查mysql
		return getSkuInfoDB (skuId);

	}

	public SkuInfo getSkuInfoDB(String skuId) {

		SkuInfo skuInfo = skuInfoMapper.selectByPrimaryKey (skuId);

		SkuImage skuImageQuery = new SkuImage ();
		skuImageQuery.setSkuId (skuInfo.getId ());
		List<SkuImage> skuImageList = skuImageMapper.select (skuImageQuery);

		skuInfo.setSkuImageList (skuImageList);

		SkuAttrValue skuAttrValue = new SkuAttrValue ();
		skuAttrValue.setSkuId (skuId);
		List<SkuAttrValue> skuAttrValueList = skuAttrValueMapper.select (skuAttrValue);

		skuInfo.setSkuAttrValueList (skuAttrValueList);

		return skuInfo;
	}

	/** 切换属性页面跳转 到指定sku 的逻辑   可以优化 用redis来跳转*/
	public List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpu(String spuId) {
		List<SkuSaleAttrValue> skuSaleAttrValues = skuSaleAttrValueMapper.selectSkuSaleAttrValueListBySpu (Long.parseLong (spuId));

		return skuSaleAttrValues;
	}

	/** ES 的  上架*/
	public void onSale(String skuId) {

		SkuInfo skuInfo = getSkuInfo (skuId);

		SkuInfoEs skuInfoEs = new SkuInfoEs ();

		try {
			BeanUtils.copyProperties (skuInfoEs, skuInfo);
		} catch (IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace ();
		}

		//skuInfoEs 和skuInfo 的属性skuAttrValueEsList不同名 无法拷贝 要手动放入
		List<SkuAttrValueEs> skuAttrValueEsList = new ArrayList<> ();

		List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList ();
		for (SkuAttrValue skuAttrValue : skuAttrValueList) {
			SkuAttrValueEs skuAttrValueEs = new SkuAttrValueEs ();
			skuAttrValueEs.setValueId (skuAttrValue.getValueId ());
			skuAttrValueEsList.add (skuAttrValueEs);
		}

		skuInfoEs.setSkuAttrValueListEs (skuAttrValueEsList);

		/** 实际开发不能这样写
		 *  onSale 极端依赖ES  最好做成 异步的--跨模块的写操作
		 *  因为如果 onSale调用saveSkuInfoEs 其实商品上架也要数据库也要做变更记录
		 *  如果saveSkuInfoEs 没有完成ES操作 或者调不通
		 *  调不通程序就会卡死
		 *  做成异步通信 发一个消息过去  不管listService活着死着都不管 不管listService办没办 继续下面逻辑
		 *  这样就能解耦和
		 *  到消息队列会再提
		 * */
		listService.saveSkuInfoEs (skuInfoEs);//异步 //跨模块写操作 //解耦合

	}

	public List<BaseAttrInfo> getAttrInfoList(List valueIdsList) {
		String valueIds = StringUtils.join (valueIdsList, ',');

		List<BaseAttrInfo> baseAttrInfoList = baseAttrInfoMapper.selectAttrInfoListByValueIds (valueIds);
//		List<BaseAttrInfo> baseAttrInfoList = new ArrayList();
		return baseAttrInfoList;
	}

}
