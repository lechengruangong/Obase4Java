/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：ODM验证器,用于验证ODM的合法性.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-9-20 16:38:11
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

import io.obase.core.odm.builder.ModelBuilder;

import java.util.List;
import java.util.Map;

/**
 * ODM验证器,用于验证ODM的合法性
 */
public abstract class OdmValidator {

    /**
     * 进行验证,返回验证结果
     *
     * @return 验证结果 未通过时为完整性检查错误信息 通过时为完整模型信息
     */
    public ValidationResult validate() {
        //创建模型建造器 不指定上下文
        ModelBuilder modelBuilder = new ModelBuilder(null);
        modelBuilder.hasIntegrityCheck(true);
        //创建模型配置
        this.createModel(modelBuilder);

        try {
            //开始创建模型
            ObjectDataModel model = modelBuilder.build();
            //使用模型查看器输出模型信息
            StringBuilder message = ObjectDataModelViewer.getFullObjectDataModelMappingView(model);

            return new ValidationResult(true, message.toString());
        } catch (IntegrityCheckFailException exception) {
            StringBuilder messageBuilder = new StringBuilder();
            //遍历错误信息字典 生成错误信息
            for (Map.Entry<String, List<String>> errMessage : exception.getErrorMessageDictionary().entrySet()) {
                //类型名称和此类型下的错误个数
                messageBuilder.append("类型").append(errMessage.getKey()).append("存在").append(errMessage.getValue().size()).append("个完整性检查错误:").append(System.lineSeparator());
                //遍历此类型下的所有错误信息 依次输出
                int seq = 1;
                for (String message : errMessage.getValue())
                    messageBuilder.append(seq++).append(". ").append(message).append(System.lineSeparator());
                //类型之间空一行 便于查看
                messageBuilder.append(System.lineSeparator());
            }

            return new ValidationResult(false, messageBuilder.toString());
        }
    }

    /**
     * 使用指定的建模器创建对象数据模型
     *
     * @param modelBuilder 对象数据模型建造器
     */
    protected abstract void createModel(ModelBuilder modelBuilder);

    /**
     * 验证结果
     */
    public static class ValidationResult {

        /**
         * 是否验证通过
         */
        private final boolean isValid;

        /**
         * 验证结果信息
         */
        private final String message;

        /**
         * 创建验证结果
         *
         * @param isValid 是否验证通过
         * @param message 验证结果信息
         */
        ValidationResult(boolean isValid, String message) {
            this.isValid = isValid;
            this.message = message;
        }

        /**
         * 获取是否验证通过
         *
         * @return 是否验证通过
         */
        public boolean getIsValid() {
            return this.isValid;
        }

        /**
         * 获取验证结果信息
         *
         * @return 验证结果信息
         */
        public String getMessage() {
            return this.message;
        }
    }
}
