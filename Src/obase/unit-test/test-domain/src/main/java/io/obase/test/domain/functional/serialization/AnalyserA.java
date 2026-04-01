package io.obase.test.domain.functional.serialization;

/**
 * 分析器A
 */
public class AnalyserA extends Analyser {
    /**
     * 初始化分析器
     *
     * @param next 下一个分析器
     */
    public AnalyserA(Analyser next) {
        super("AnalyserA", next);
    }

    /**
     * 反序列化方法
     */
    public AnalyserA() {
        super(null, null);
    }
}
