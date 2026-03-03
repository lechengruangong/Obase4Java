/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：默认的数组哈希生成器,提供生成哈希代码的默认方法.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-27 17:05:36
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core;

import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

/**
 * 默认的数组哈希生成器
 * 提供生成哈希代码的默认方法
 */
public class DefaultArrayHashGenerator implements IArrayHashGenerator {
    /**
     * 将byte[]进行Hash操作
     *
     * @param itemByteArray 字节序列
     * @return 哈希
     */
    private static int mergeByteInt(byte[] itemByteArray) {
        int itemOutInt = 2;

        for (int i = 0; i < itemByteArray.length; i++)
            if (i != 0) {
                if (itemByteArray[i] == 0)
                    itemOutInt = ~itemOutInt; // &&&&& -- > 00000000
                else
                    itemOutInt ^= itemByteArray[i]; // &&&&& -- > 00000000  |||||||| --> 111111111
            } else {
                itemOutInt = itemByteArray[i];
            }

        return itemOutInt;
    }

    /**
     * 生成哈希代码
     *
     * @param members 标识成员序列
     * @return 哈希代码
     */
    @Override
    public int generator(Object[] members) {
        //基础值
        int outInt = 2;

        for (Object item : members) {
            //先将基础类型转为byte处理
            byte[] itemByteArray;
            int itemOutInt;
            //模式匹配 每个都转为byte[]
            if (Integer.class.equals(item.getClass())) {
                int newItem1 = (int) item;
                itemByteArray = BitConverter.getBytes(newItem1);
                itemOutInt = mergeByteInt(itemByteArray);
                outInt ^= itemOutInt;
            } else if (String.class.equals(item.getClass())) {
                String newItem2 = (String) item;
                itemByteArray = BitConverter.getBytes(newItem2);
                itemOutInt = mergeByteInt(itemByteArray);
                outInt ^= itemOutInt;
            } else if (Double.class.equals(item.getClass())) {
                double newItem3 = (double) item;
                itemByteArray = BitConverter.getBytes(newItem3);
                itemOutInt = mergeByteInt(itemByteArray);
                outInt |= itemOutInt;
            } else if (Float.class.equals(item.getClass())) {
                float newItem4 = (float) item;
                itemByteArray = BitConverter.getBytes(newItem4);
                itemOutInt = mergeByteInt(itemByteArray);
                outInt ^= itemOutInt;
            } else if (Boolean.class.equals(item.getClass())) {
                boolean newItem = (boolean) item;
                itemByteArray = BitConverter.getBytes(newItem);
                itemOutInt = mergeByteInt(itemByteArray);
                outInt ^= itemOutInt;
            } else {
                itemOutInt = item.hashCode();
                outInt ^= itemOutInt;
            }
        }

        return outInt;
    }

    /**
     * 数字转字节数组工具类
     */
    public static class BitConverter {

        /**
         * 以字节数组的形式返回指定的布尔值
         *
         * @param data 一个布尔值
         * @return 长度为 1 的字节数组
         */
        public static byte[] getBytes(boolean data) {
            byte[] bytes = new byte[1];
            bytes[0] = (byte) (data ? 1 : 0);
            return bytes;
        }

        /**
         * 以字节数组的形式返回指定的 32 位有符号整数值
         *
         * @param data 要转换的数字
         * @return 长度为 4 的字节数组
         */
        public static byte[] getBytes(int data) {
            byte[] bytes = new byte[4];
            if (isLittleEndian()) {
                bytes[0] = (byte) (data & 0xff);
                bytes[1] = (byte) ((data & 0xff00) >> 8);
                bytes[2] = (byte) ((data & 0xff0000) >> 16);
                bytes[3] = (byte) ((data & 0xff000000) >> 24);
            } else {
                bytes[3] = (byte) (data & 0xff);
                bytes[2] = (byte) ((data & 0xff00) >> 8);
                bytes[1] = (byte) ((data & 0xff0000) >> 16);
                bytes[0] = (byte) ((data & 0xff000000) >> 24);
            }
            return bytes;
        }

        /**
         * 以字节数组的形式返回指定的 64 位有符号整数值
         *
         * @param data 要转换的数字
         * @return 长度为 8 的字节数组
         */
        public static byte[] getBytes(long data) {
            byte[] bytes = new byte[8];
            if (isLittleEndian()) {
                bytes[0] = (byte) (data & 0xff);
                bytes[1] = (byte) ((data >> 8) & 0xff);
                bytes[2] = (byte) ((data >> 16) & 0xff);
                bytes[3] = (byte) ((data >> 24) & 0xff);
                bytes[4] = (byte) ((data >> 32) & 0xff);
                bytes[5] = (byte) ((data >> 40) & 0xff);
                bytes[6] = (byte) ((data >> 48) & 0xff);
                bytes[7] = (byte) ((data >> 56) & 0xff);
            } else {
                bytes[7] = (byte) (data & 0xff);
                bytes[6] = (byte) ((data >> 8) & 0xff);
                bytes[5] = (byte) ((data >> 16) & 0xff);
                bytes[4] = (byte) ((data >> 24) & 0xff);
                bytes[3] = (byte) ((data >> 32) & 0xff);
                bytes[2] = (byte) ((data >> 40) & 0xff);
                bytes[1] = (byte) ((data >> 48) & 0xff);
                bytes[0] = (byte) ((data >> 56) & 0xff);
            }
            return bytes;
        }

        /**
         * 以字节数组的形式返回指定的单精度浮点值
         *
         * @param data 要转换的数字
         * @return 长度为 4 的字节数组
         */
        public static byte[] getBytes(float data) {
            return getBytes(Float.floatToIntBits(data));
        }

        /**
         * 以字节数组的形式返回指定的双精度浮点值
         *
         * @param data 要转换的数字
         * @return 长度为 8 的字节数组
         */
        public static byte[] getBytes(double data) {
            return getBytes(Double.doubleToLongBits(data));
        }

        /**
         * 将指定字符串中的所有字符编码为一个字节序列
         *
         * @param data 包含要编码的字符的字符串
         * @return 一个字节数组，包含对指定的字符集进行编码的结果
         */
        public static byte[] getBytes(String data) {
            return data.getBytes(StandardCharsets.UTF_8);
        }

        /**
         * 判断 CPU Endian 是否为 Little
         *
         * @return 判断结果
         */
        private static boolean isLittleEndian() {
            return ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN;
        }
    }
}