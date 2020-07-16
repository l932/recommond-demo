package org.leo.demo.recommend;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class LFM {

    private double α = 0.02;// 梯度下降学习速率，步长
    private static final int F = 100;// 隐因子数
    private static final double λ = 0.01;// 正则化参数，防止过拟合
    private static final int iterations = 100;// 迭代求解次数

    private Map<Integer, Double[]> userF = Maps.newHashMap();// 用户-隐因子矩阵
    private Map<Integer, Double[]> itemF = Maps.newHashMap();// 物品-隐因子矩阵

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        LFM lfm = new LFM();
        Map<Integer, Map<Integer, Double>> userItemRealData = lfm.buildInitData();
        lfm.initUserItemFactorMatrix(userItemRealData);
        lfm.train(userItemRealData);
        lfm.getRecommondItemByUserId(1, 10, userItemRealData);
        // lfm.getRecommondItemByUserId(2, 10, userItemRealData);
        // lfm.getRecommondItemByUserId(3, 10, userItemRealData);
        long end = System.currentTimeMillis();
        System.out.println("耗时" + (end - start) / 1000 + "秒");
    }

    /**
     * 构建初始数据<br/>
     * 实际开发中，应该读取日志，格式类似：userId,itemId,评分<br/>
     * 本例采用随机数
     * 
     * @return
     */
    public Map<Integer, Map<Integer, Double>> buildInitData() {
        Map<Integer, Map<Integer, Double>> userItemRealData = Maps.newHashMap();
        System.out.println("读取数据......");
        String dataPath = "/Users/liupeng/git/repository/recommond-demo/recommond-demo/src/main/resources/ratings.csv";
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(dataPath), "utf-8"));
            String line = null;
            Map<Integer, Double> itemRating = null;
            while ((line = br.readLine()) != null) {
                String[] dataRating = line.split(",");
                Integer userID = Integer.valueOf(dataRating[0].trim());
                Integer itemID = Integer.valueOf(dataRating[1].trim());
                double rating = Double.parseDouble(dataRating[2]);

                if (userItemRealData.containsKey(userID)) {
                    itemRating = userItemRealData.get(userID);
                    itemRating.put(itemID, rating);
                } else {
                    itemRating = Maps.newHashMap();
                    itemRating.put(itemID, rating);
                    userItemRealData.put(userID, itemRating);
                }
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return userItemRealData;
    }

    /**
     * 初始化用户-隐因子、物品-隐因子矩阵<br/>
     * 使用随机数进行填充，随机数应与1/sqrt(隐因子数量)成正比
     * 
     * @param userItemRealData
     */
    public void initUserItemFactorMatrix(Map<Integer, Map<Integer, Double>> userItemRealData) {
        for (Entry<Integer, Map<Integer, Double>> userEntry : userItemRealData.entrySet()) {
            // 随机填充用户-隐因子矩阵
            int userId = userEntry.getKey();
            Double[] randomUserValue = new Double[LFM.F];
            for (int j = 0; j < LFM.F; j++) {
                randomUserValue[j] = Math.random() / Math.sqrt(LFM.F);
            }
            this.getUserF().put(userId, randomUserValue);

            // 随机填充物品-隐因子矩阵
            Map<Integer, Double> itemMap = userItemRealData.get(userId);
            for (Entry<Integer, Double> entry : itemMap.entrySet()) {
                int itemId = entry.getKey();
                if (this.getItemF().containsKey(itemId)) {
                    continue;// 物品-隐因子矩阵已存在，不再做处理
                }
                Double[] randomItemValue = new Double[LFM.F];
                for (int j = 0; j < LFM.F; j++) {
                    randomItemValue[j] = Math.random() / Math.sqrt(LFM.F);
                }
                this.getItemF().put(itemId, randomItemValue);
            }
        }
    }

    /**
     * 训练
     * @param userItemRealData
     */
    public void train(Map<Integer, Map<Integer, Double>> userItemRealData) {
        for (int step = 0; step < LFM.iterations; step++) {
            System.out.println("第" + (step + 1) + "次迭代");
            for (Entry<Integer, Map<Integer, Double>> userEntry : userItemRealData.entrySet()) {
                int userId = userEntry.getKey();
                Map<Integer, Double> itemMap = userItemRealData.get(userId);
                for (Entry<Integer, Double> entry : itemMap.entrySet()) {
                    int itemId = entry.getKey();// 物品ID
                    double realRating = entry.getValue();// 真实偏好度
                    double predictRating = this.predict(userId, itemId);// 预测偏好度
                    double error = realRating - predictRating;// 偏好度误差
                    Double[] userVal = this.getUserF().get(userId);
                    Double[] itemVal = this.getItemF().get(itemId);

                    for (int j = 0; j < LFM.F; j++) {
                        double uv = userVal[j];
                        double iv = itemVal[j];

                        uv += this.α * (error * iv - LFM.λ * uv);
                        iv += this.α * (error * uv - LFM.λ * iv);

                        userVal[j] = uv;
                        itemVal[j] = iv;
                    }
                }
            }
            this.α *= 0.9;// 按照随机梯度下降算法的要求，学习速率每步都要进行衰减，目的是使算法尽快收敛
        }
    }

    /**
     * 获取用户对物品的预测评分
     * 
     * @param userId
     * @param itemId
     * @return
     */
    public double predict(Integer userId, Integer itemId) {
        double predictRating = 0.0;// 预测评分
        Double[] userValue = this.getUserF().get(userId);
        Double[] itemValue = this.getItemF().get(itemId);
        for (int i = 0; i < LFM.F; i++) {
            predictRating += userValue[i] * itemValue[i];
        }
        return predictRating;
    }

    /**
     * 获取用户TopN推荐
     * @param userId
     * @param count
     * @param userItemRealData
     * @return
     */
    public List<Map.Entry<Integer, Double>> getRecommondItemByUserId(int userId, int count,
            Map<Integer, Map<Integer, Double>> userItemRealData) {
        Map<Integer, Double> result = Maps.newHashMap();
        Map<Integer, Double> realItemVal = userItemRealData.get(userId);
        Map<Integer, Double[]> predictItemVal = this.getItemF();
        // double lowestVal = Double.NEGATIVE_INFINITY;// 最小偏好度，初始为负无穷大
        for (Integer itemId : predictItemVal.keySet()) {
            if (realItemVal.containsKey(itemId)) {
                continue;// 预测偏好度的物品在真实偏好度物品中，不处理
            }
            double predictRating = this.predict(userId, itemId);// 预测值
            if (predictRating < 0) {
                continue;
            }
            result.put(itemId, predictRating);
        }

        List<Map.Entry<Integer, Double>> list = Lists.newArrayList(result.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<Integer, Double>>() {
            // 降序
            public int compare(Map.Entry<Integer, Double> o1, Map.Entry<Integer, Double> o2) {
                if (o2.getValue() > o1.getValue()) {
                    return 1;
                } else if (o2.getValue() < o1.getValue()) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });
        List<Map.Entry<Integer, Double>> topN = list.subList(0, count);
        System.out.println("用户" + userId + "前" + count + "个推荐结果：");
        for (Entry<Integer, Double> entry : topN) {
            System.out.println(entry.getKey() + ":" + entry.getValue());
        }
        return topN;
    }

////////////////////////////////////////////////////////////
    public Map<Integer, Double[]> getUserF() {
        return userF;
    }

    public void setUserF(Map<Integer, Double[]> userF) {
        this.userF = userF;
    }

    public Map<Integer, Double[]> getItemF() {
        return itemF;
    }

    public void setItemF(Map<Integer, Double[]> itemF) {
        this.itemF = itemF;
    }

}
