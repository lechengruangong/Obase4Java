package io.obase.test.domain.association.implement;

/**
 * 特殊的我的自行车B 有一个额外的旗子
 */
public class MyBikeB extends Bike {

    /**
     * 车筐
     */
    private BikeBucket bucket;

    /**
     * 车筐编码
     */
    private String bucketCode;

    /**
     * 获取车筐
     *
     * @return 车筐
     */
    public BikeBucket getBucket() {
        return this.bucket;
    }

    /**
     * 设置车筐
     *
     * @param bucket 车筐
     */
    public void setBucket(BikeBucket bucket) {
        this.bucket = bucket;
    }

    /**
     * 获取车筐编码
     *
     * @return 车筐编码
     */
    public String getBucketCode() {
        return this.bucketCode;
    }

    /**
     * 设置车筐编码
     *
     * @param bucketCode 车筐编码
     */
    public void setBucketCode(String bucketCode) {
        this.bucketCode = bucketCode;
    }
}
