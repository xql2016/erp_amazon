package tools;

import lombok.SneakyThrows;
import model.constant.ErrorMessage;
import model.enums.autoPlace.AutoPlaceCompareValueType;

/**
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/12/21
 */
public class CompareUtils {

    @SneakyThrows
    public static boolean compareAutoPlaceCompareValue(double value, String autoPlaceCompareValueType, Double biggerThan, Double smallerThan, Double equalTo) {
        switch (AutoPlaceCompareValueType.getByValue(autoPlaceCompareValueType)) {
            case BIGGER_OR_EQUAL_TO_SMALLER:
                return value >= biggerThan && value < smallerThan;
            case BIGGER_OR_EQUAL_TO_SMALLER_OR_EQUAL:
                return value >= biggerThan && value <= smallerThan;
            case BIGGER_TO_SMALLER_OR_EQUAL:
                return value > biggerThan && value <= smallerThan;
            case BIGGER_TO_SMALLER:
                return value > biggerThan && value < smallerThan;
            case EQUALS_VALUE:
                return value == equalTo;
        }
        throw new Exception(ErrorMessage.CONFIG_ERROR);
    }
}
