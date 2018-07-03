package cn.itcast.core.service;

import cn.itcast.core.pojo.address.Address;

import java.util.List;

public interface AddressService {
    //查询
     List<Address> findListByLoginUser(String name);
}
