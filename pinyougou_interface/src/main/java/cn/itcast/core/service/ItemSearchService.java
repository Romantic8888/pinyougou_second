package cn.itcast.core.service;

import java.util.Map;

public interface ItemSearchService {
    /**
     * 搜索
     * @param
     * @return
     */
    Map<String,Object> search(Map searchMap);

}
