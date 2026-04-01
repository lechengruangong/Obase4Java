package io.obase.test.domain.functional.serialization;

/**
 * 分析器C
 */
public class AnalyserC extends Analyser {
    /**
     * 初始化分析器
     *
     * @param next 下一个分析器
     */
    public AnalyserC(Analyser next) {
        super("AnalyserC", next);
    }

    /**
     * 反序列化方法
     */
    public AnalyserC() {
        super(null, null);
    }
}
