package org.leo.demo.recommend;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import com.google.common.collect.Maps;

public class LFM {

    private int F = 100;// 隐因子数
    private double α = 0.02;// 梯度下降学习速率，步长
    private double λ = 0.01;// 正则化参数，防止过拟合
    private int iterations = 50;// 迭代求解次数

    private Map<Integer, Double[]> userF = Maps.newHashMap();// 用户-隐因子矩阵
    private Map<Integer, Double[]> itemF = Maps.newHashMap();// 物品-隐因子矩阵

    public static void main(String[] args) {
        LFM lfm = new LFM();
        Map<Integer, Map<Integer, Double>> userItemRealData = lfm.buildInitData();

    }

    public void train(Map<Integer, Map<Integer, Double>> userItemRealData) {
        for (int step = 0; step < this.iterations; step++) {
            System.out.println("第" + (step + 1) + "次迭代");
            for (int i = 0; i < 100; i++) {
                int userId = i + 1;
                Map<Integer, Double> itemMap = userItemRealData.get(userId);
                for (Entry<Integer, Double> entry : itemMap.entrySet()) {
                    int itemId = entry.getKey();
                    double rating = entry.getValue();
                }
            }
        }
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
        Random rand = new Random();
        for (int i = 0; i < 100; i++) {
            int userId = i + 1;
            for (int j = 0; j < 10; j++) {
                int itemId = rand.nextInt(100) + 1;
                Double rating = rand.nextDouble();

                if (userItemRealData.containsKey(userId)) {
                    userItemRealData.get(userId).put(itemId, rating);
                } else {
                    Map<Integer, Double> itemMap = new HashMap<Integer, Double>();
                    itemMap.put(itemId, rating);
                    userItemRealData.put(userId, itemMap);
                }
                System.out.println(userId + "-" + itemId + ":" + rating);
            }
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
        for (int i = 0; i < 100; i++) {
            // 随机填充用户-隐因子矩阵
            int userId = i + 1;
            Double[] randomUserValue = new Double[this.getF()];
            for (int j = 0; j < this.getF(); j++) {
                randomUserValue[j] = Math.random() / Math.sqrt(this.getF());
            }
            this.getUserF().put(userId, randomUserValue);

            // 随机填充物品-隐因子矩阵
            Map<Integer, Double> itemMap = userItemRealData.get(userId);
            for (Entry<Integer, Double> entry : itemMap.entrySet()) {
                int itemId = entry.getKey();
                if (this.getItemF().containsKey(itemId)) {
                    continue;// 物品-隐因子矩阵已存在，不再做处理
                }
                Double[] randomItemValue = new Double[this.getF()];
                for (int j = 0; j < this.getF(); j++) {
                    randomItemValue[j] = Math.random() / Math.sqrt(this.getF());
                }
                this.getItemF().put(itemId, randomItemValue);
            }
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
        for (int i = 0; i < this.getF(); i++) {
            predictRating += userValue[i] * itemValue[i];
        }
        return predictRating;
    }

    public int getF() {
        return F;
    }

    public void setF(int f) {
        F = f;
    }

    public double getΑ() {
        return α;
    }

    public void setΑ(double α) {
        this.α = α;
    }

    public double getΛ() {
        return λ;
    }

    public void setΛ(double λ) {
        this.λ = λ;
    }

    public int getIterations() {
        return iterations;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

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
