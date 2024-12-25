package model.response.goodsPlace;

import lombok.Data;
import model.response.PageResult;

import java.util.List;

@Data
public class GoodsPlacePageResult extends PageResult {

    private List<GoodsPlace> data;
}
