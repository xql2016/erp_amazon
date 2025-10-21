package tools;

import model.response.AdPlacement;
import org.apache.commons.lang3.StringUtils;

public class BidUtils {

    public static Double getAdPlacementBid(AdPlacement adPlacement) {
        String nowBid = adPlacement.getBid();
        if(StringUtils.isBlank(nowBid) || "null".equalsIgnoreCase(nowBid)) {
            return adPlacement.getReal_bid();
        }
        return Double.parseDouble(nowBid);
    }
}
