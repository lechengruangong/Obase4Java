package io.obase.test.domain.functional.serialization;

/**
 * 分析器B
 */
public class AnalyserB extends Analyser {
    /**
     * 初始化分析器
     *
     * @param next 下一个分析器
     */
    public AnalyserB(Analyser next) {
        super("AnalyserB", next);
    }

    /**
     * 反序列化方法
     */
    public AnalyserB() {
        super(null, null);
    }
}
