package com.lfx.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lfx.demo.entity.Article;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户表 数据层
 *
 * @author dream
 */
@Mapper
public interface ArticleMapper extends BaseMapper<Article> {

}
