package com.lb.aiagent.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lb.aiagent.mapper.ProhibitedWordsMapper;
import com.lb.aiagent.model.entity.ProhibitedWords;
import com.lb.aiagent.utils.JacksonUtil;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Transactional(rollbackFor = Exception.class)
public class ProhibitedWordsService extends ServiceImpl<ProhibitedWordsMapper, ProhibitedWords> {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public static final String PROHIBITED_WORDS_KEY = "prohibitedWords";

    public boolean add(ProhibitedWords prohibitedWords) {
        return save(prohibitedWords);
    }

    public boolean add(String word) {
        return save(new ProhibitedWords(word, new Date()));
    }

    public void batchAdd(Collection<String> words) {
        Date date = new Date();
        List<ProhibitedWords> list = words.stream().map(v -> new ProhibitedWords(v, date)).toList();
        saveBatch(list);
    }

    public void delete(Long id) {
        removeById(id);
        stringRedisTemplate.delete(PROHIBITED_WORDS_KEY);
    }

    public List<String> getAllWords() {
        String wordsStr = stringRedisTemplate.opsForValue().get(PROHIBITED_WORDS_KEY);
        if (StrUtil.isNotBlank(wordsStr)) {
            return JSONUtil.toList(wordsStr, String.class);
        }
        List<ProhibitedWords> prohibitedWords = super.baseMapper.selectList(
                Wrappers.lambdaQuery(ProhibitedWords.class)
                        .select(ProhibitedWords::getWord));
        if (CollUtil.isEmpty(prohibitedWords)) {
            return Collections.emptyList();
        }
        List<String> words = prohibitedWords.stream().map(ProhibitedWords::getWord).toList();
        wordsStr = JacksonUtil.toJsonString(words);
        stringRedisTemplate.opsForValue().set(PROHIBITED_WORDS_KEY, wordsStr, 60 * 60 * 24 * 15, TimeUnit.SECONDS);
        return words;
    }
}
