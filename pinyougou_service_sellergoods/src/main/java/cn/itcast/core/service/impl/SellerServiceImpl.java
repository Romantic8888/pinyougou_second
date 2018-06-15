package cn.itcast.core.service.impl;

import cn.itcast.core.dao.seller.SellerDao;
import cn.itcast.core.pojo.seller.Seller;
import cn.itcast.core.pojo.seller.SellerQuery;
import cn.itcast.core.service.SellerService;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@Transactional
public class SellerServiceImpl implements SellerService {
    @Autowired
    private SellerDao sellerDao;

    /**
     * 商家申请入驻
     *
     * @param seller
     */
    @Override
    public void add(Seller seller) {
        //密码加密
        seller.setPassword(new BCryptPasswordEncoder().encode(seller.getPassword()));
        //未审核的商品
        seller.setStatus("0");
        //商品 注册 的时间
        seller.setCreateTime(new Date());
        sellerDao.insertSelective(seller);
    }

    //查询所有分页 条件
    public PageResult search(Integer page,Integer rows, Seller seller){
        PageHelper.startPage(page, rows);
        SellerQuery query = new SellerQuery();
        SellerQuery.Criteria createCriteria = query.createCriteria();
        if(null != seller.getStatus() && !"".equals(seller.getStatus())){
            createCriteria.andStatusEqualTo(seller.getStatus());
        }
        Page<Seller> p = (Page<Seller>) sellerDao.selectByExample(query);
        return new PageResult(p.getTotal(), p.getResult());
    }

    //查询一个商家
    public Seller findOne(String id){
        return sellerDao.selectByPrimaryKey(id);
    }
    //审核开始
    public void updateStatus(String sellerId,String status){
        Seller seller = new Seller();
        seller.setSellerId(sellerId);
        seller.setStatus(status);
        //update tb_seller set status = 1 where seller_id = 315
        sellerDao.updateByPrimaryKeySelective(seller);
    }

}
