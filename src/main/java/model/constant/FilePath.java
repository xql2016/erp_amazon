package model.constant;

/**
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/9/28
 */
public class FilePath {

    public static String COMMON_PATH = "src/main/resources/";
    //public static String COMMON_PATH = "";

    public static String inputConfigurationToken_Name = "InputConfigurationToken.txt";
    public static String inputConfigurationDetail_Name = "InputConfigurationDetail.txt";
    public static String inputConfigurationCustom_NAME = "InputConfigurationCustom.txt";
    // v5垃圾流量配置
    public static String spamTrafficFromPlaceConfiguration_Name = "SpamTrafficFromPlaceConfiguration.txt";
    // v6有效流量配置
    public static String spamValidFromPlaceConfiguration_Name = "SpamValidFromPlaceConfiguration.txt";
    // v7广告活动入口垃圾流量配置
    public static String spamTrafficFromAdGroupConfiguration_Name = "SpamTrafficFromAdGroupConfiguration.txt";
    // v1产品层面控制流量Excel文件
    public static String v1SkuControlExcel_Name = "v1skuControl.xlsx";
    // v1产品层面导入流量Excel文件
    public static String v1SkuAddExcel_Name = "v1skuAdd.xlsx";


    public static String inputConfigurationToken = COMMON_PATH + inputConfigurationToken_Name;
    public static String inputConfigurationDetail = COMMON_PATH + inputConfigurationDetail_Name;
    public static String inputConfigurationCustom = COMMON_PATH + inputConfigurationCustom_NAME;
    public static String spamTrafficFromPlaceConfiguration = COMMON_PATH + spamTrafficFromPlaceConfiguration_Name;
    public static String spamValidFromPlaceConfiguration = COMMON_PATH + spamValidFromPlaceConfiguration_Name;
    public static String spamTrafficFromAdGroupConfiguration = COMMON_PATH + spamTrafficFromAdGroupConfiguration_Name;
    public static String v1SkuControlExcel = COMMON_PATH + v1SkuControlExcel_Name;
    public static String v1SkuAddExcel = COMMON_PATH + v1SkuAddExcel_Name;

}
