package tools;

import org.apache.commons.lang3.StringUtils;

public class HubUtils {

    public static String getHubName(String profile_id) {
        if(StringUtils.isBlank(profile_id)) {
            return "";
        }
        if("2872359474906836".equalsIgnoreCase(profile_id)) {
            return "泰州乐睿-DE";
        } else if("3694859621919747".equalsIgnoreCase(profile_id)) {
            return "泰州乐睿-FR";
        } else if("2900750670247683".equalsIgnoreCase(profile_id)) {
            return "泰州乐睿-IT";
        } else if("3028812319743577".equalsIgnoreCase(profile_id)) {
            return "泰州乐睿-ES";
        } else {
            return "";
        }
    }
}
