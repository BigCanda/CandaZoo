package com.newcoder.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {
    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    // 替换符号
    private static final String REPLACEMENT = "***";

    // 根节点
    public TrieNode rootNode = new TrieNode();
    @PostConstruct
    public void init() {
        try (
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt")
        ) {
            assert is != null;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))
                    ) {

                String keyword;
                while ((keyword = reader.readLine()) != null) {
                    this.addKeyword(keyword);
                }

            }
        } catch (IOException e) {
            logger.error("加载敏感词文件失败: " + e.getMessage());
        }
    }

    private void addKeyword(String keyword) {
        TrieNode tempNode = rootNode;

        for(int i = 0; i < keyword.length(); i++) {
            char c = keyword.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);

            if (subNode == null) {
                // 初始化子节点
                subNode = new TrieNode();
                tempNode.addSubNode(c, subNode);
            }

            // 子节点进入下一轮循环
            tempNode = subNode;

            //设置结束标识

            if (i == keyword.length() - 1) {
                tempNode.setKeywordEnd(true);
            }
        }
    }

    /*
    过滤敏感词
    @param text待过滤文本
    @return 过滤后的文本
     */
    public String filter(String text) {
       if (StringUtils.isBlank(text)) {
           return null;
       }
       // 指针1
       TrieNode tempNode = rootNode;

       // 指针2
       int begin = 0;

        // 指针3
       int position = 0;

       StringBuilder sb = new StringBuilder();

        while (position < text.length()) {
            char c = text.charAt(position);
            // 跳过符号
            if (isSymbol(c)) {
                // 如果指针1在根节点,将此符号计入结果,让指针2前进
                if (tempNode == rootNode) {
                    sb.append(c);
                    begin++;
                }

                // 无论符号在哪,指针3都要向下遍历
                position++;
                continue;
            }
            // 检查下级节点
            tempNode = tempNode.getSubNode(c);
            if (tempNode == null) {
                // 以begin开头的字符串不是敏感词
                sb.append(text.charAt(begin));
                // 进入下个位置
                position = ++begin;
                // 重新指向根节点
                tempNode = rootNode;
            } else if (tempNode.isKeywordEnd()) {
                // 发现敏感词,替换begin-position
                sb.append(REPLACEMENT);
                // 进入下个位置
                begin = ++position;
                // 重新指向根节点
                tempNode = rootNode;
            } else {
                position++;
            }
        }
        // 记录最后一批字符
        sb.append(text.substring(begin));

        return sb.toString();
    }

    // 判断符号
    private boolean isSymbol (char c) {
        // c > 0X2E80 && c < 0X9FFF东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    // 前缀树的节点
    private static class TrieNode {
        // 关键词结束的标识
        private boolean isKeywordEnd = false;
        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        // 描述当前节点的子节点(key是下级字符,value是下级节点)
        private final Map<Character, TrieNode> subNodes = new HashMap<>();

        // 添加子节点
        public void addSubNode(Character c, TrieNode node) {
            subNodes.put(c, node);
        }

        // 获取子节点
        public TrieNode getSubNode(Character c) {
            return subNodes.get(c);
        }
    }

}
