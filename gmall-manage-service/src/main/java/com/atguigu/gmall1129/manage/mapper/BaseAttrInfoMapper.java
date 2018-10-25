package com.atguigu.gmall1129.manage.mapper;

import com.atguigu.gmall1129.bean.BaseAttrInfo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @param
 * @return
 */
public interface BaseAttrInfoMapper extends Mapper<BaseAttrInfo> {

    List<BaseAttrInfo> selectAttrInfoList(Long catalog3Id);//数据库表是bigInt

    List<BaseAttrInfo>  selectAttrInfoListByValueIds(@Param("valueIds") String valueIds);
}
