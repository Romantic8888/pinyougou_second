package cn.itcast.core.service;

import cn.itcast.core.pojo.seller.Seller;
import entity.PageResult;

public interface SellerService {
    /**
     * 商家申请入驻
     * @param seller
     */
    void  add(Seller seller);

    PageResult search(Integer page, Integer rows, Seller seller);
    Seller findOne(String id);
    void updateStatus(String sellerId,String status);
}
