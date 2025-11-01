package tools;

import model.response.AdPlacement;
import org.apache.commons.lang3.StringUtils;

public class BidUtils {

    public static Double getAdPlacementBid(AdPlacement adPlacement) {
        String nowBid = adPlacement.getBid();
        if(StringUtils.isBlank(nowBid) || "null".equalsIgnoreCase(nowBid) || "0.00".equalsIgnoreCase(nowBid) || 0 == Double.parseDouble(nowBid)) {
            if(null != adPlacement.getReal_bid() && 0 != adPlacement.getReal_bid()) {
                return adPlacement.getReal_bid();
            } else if(null != adPlacement.getDefault_bid() && 0 != adPlacement.getDefault_bid()) {
                return adPlacement.getDefault_bid();
            }
            return 0.0;
        }
        return Double.parseDouble(nowBid);
    }
}
