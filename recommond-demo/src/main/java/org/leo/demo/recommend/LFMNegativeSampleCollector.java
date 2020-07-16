package org.leo.demo.recommend;

import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;

public class LFMNegativeSampleCollector {

    private static final int MAX = 1000;
    private static final int MIN = 1;

    public static void main(String[] args) {
        LFMNegativeSampleCollector t = new LFMNegativeSampleCollector();
        List<Integer> hotItems = t.buildHotItem(100);
        int sampleSize=30;
        List<Integer> userSamples = t.buildUserSamples(sampleSize);
        List<Integer> userNegativeSamples = Lists.newArrayListWithCapacity(sampleSize);

        //遍历或者随机获取热门物品列表。本例使用的都是随机数字，所以就直接遍历连
        for (Integer hotItem : hotItems) {
            //如果在用户正样本中，略过
            if(userSamples.contains(hotItem)) {
                continue;
            }
            //否则加入负样本
            userNegativeSamples.add(hotItem);
            //负样本数量等于正样本数量，跳出
            if(userNegativeSamples.size()==sampleSize) {
                break;
            }
        }
        
        System.out.println("用户正样本：");
        System.out.println(userSamples.toString());
        System.out.println("用户负样本：");
        System.out.println(userNegativeSamples.toString());
    }

    /**
     * 生成用户正样本
     * @param size
     * @return
     */
    public List<Integer> buildUserSamples(int size) {
        List<Integer> userSamples = Lists.newArrayListWithCapacity(size);
        Random rand = new Random();
        for (int i = 0; i < size; i++) {
            userSamples.add(rand.nextInt(LFMNegativeSampleCollector.MAX - LFMNegativeSampleCollector.MIN + 1)
                    + LFMNegativeSampleCollector.MIN);
        }
        return userSamples;
    }

    /**
     * 生成热门物品列表
     * 
     * @param size
     * @return
     */
    public List<Integer> buildHotItem(int size) {
        List<Integer> hotItems = Lists.newArrayListWithCapacity(size);
        Random rand = new Random();
        for (int i = 0; i < size; i++) {
            hotItems.add(rand.nextInt(LFMNegativeSampleCollector.MAX - LFMNegativeSampleCollector.MIN + 1)
                    + LFMNegativeSampleCollector.MIN);
        }
        return hotItems;
    }

}
