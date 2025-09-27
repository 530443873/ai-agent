package com.lb.aiagent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lb.aiagent.model.entity.ProhibitedWords;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProhibitedWordsMapper extends BaseMapper<ProhibitedWords> {
}
