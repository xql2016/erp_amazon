package model.response.autoPlace;

import lombok.Data;
import model.response.PageResult;

import java.util.List;

@Data
public class AutoPlacePageResult extends PageResult {

    private List<AutoPlace> data;
}
