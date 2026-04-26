package com.glasses;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.glasses.entity.Customer;
import com.glasses.entity.OptometryRecord;
import com.glasses.entity.SalesRecord;
import com.glasses.entity.SysUser;
import com.glasses.mapper.SysUserMapper;
import com.glasses.service.CustomerService;
import com.glasses.service.OptometryRecordService;
import com.glasses.service.SalesRecordService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SpringBootTest
public class DataGenerationTest {

    @Autowired
    private SalesRecordService salesRecordService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private OptometryRecordService optometryRecordService;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Test
    public void generateFullTestData() {
        // 1. 准备基础数据
        String[] surnames = {"张", "王", "李", "赵", "刘", "陈", "杨", "黄", "孙", "周", "吴", "徐", "朱", "林"};
        String[] names = {"伟", "芳", "秀英", "敏", "静", "强", "军", "勇", "艳", "杰", "磊", "阳", "波", "宁"};
        String[] frameBrands = {"雷朋", "派丽蒙", "暴龙", "陌森", "亓那", "施洛华", "精工"};
        String[] lensBrands = {"蔡司", "依视路", "明月", "凯米", "罗敦斯得", "豪雅"};
        
        List<SysUser> users = sysUserMapper.selectList(null);
        Long operatorId = users.isEmpty() ? 1L : users.get(0).getId();
        Date now = new Date();

        System.out.println("开始生成模拟数据...");

        // 2. 生成 20 个顾客
        List<Customer> customers = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            Customer c = new Customer();
            c.setName(RandomUtil.randomEle(surnames) + RandomUtil.randomEle(names));
            c.setPhone("13" + RandomUtil.randomNumbers(9));
            c.setGender(RandomUtil.randomInt(1, 3)); // 1或2
            c.setBirthday(DateUtil.offsetDay(now, -RandomUtil.randomInt(20*365, 60*365)));
            customerService.save(c);
            customers.add(c);
        }

        // 3. 为每个顾客生成 1-3 条验光记录和 1-2 条销售记录
        for (Customer c : customers) {
            int optoCount = RandomUtil.randomInt(1, 4);
            List<OptometryRecord> optos = new ArrayList<>();
            
            for (int j = 0; j < optoCount; j++) {
                OptometryRecord o = new OptometryRecord();
                o.setCustomerId(c.getId());
                // 随机近视度数 -1.00 到 -8.00
                o.setOdSph(new BigDecimal(-RandomUtil.randomDouble(1.0, 8.0)).setScale(2, RoundingMode.HALF_UP));
                o.setOsSph(new BigDecimal(-RandomUtil.randomDouble(1.0, 8.0)).setScale(2, RoundingMode.HALF_UP));
                o.setOdCyl(new BigDecimal(-RandomUtil.randomDouble(0, 2.0)).setScale(2, RoundingMode.HALF_UP));
                o.setOsCyl(new BigDecimal(-RandomUtil.randomDouble(0, 2.0)).setScale(2, RoundingMode.HALF_UP));
                o.setOdAxis(RandomUtil.randomInt(0, 181));
                o.setOsAxis(RandomUtil.randomInt(0, 181));
                o.setOdVa("1.0");
                o.setOsVa("1.0");
                o.setOdPd(new BigDecimal(RandomUtil.randomDouble(30, 35)).setScale(1, RoundingMode.HALF_UP));
                o.setOsPd(new BigDecimal(RandomUtil.randomDouble(30, 35)).setScale(1, RoundingMode.HALF_UP));
                o.setPdFar(o.getOdPd().add(o.getOsPd()));
                
                // 随机日期（过去半年）
                Date optoDate = DateUtil.offsetDay(now, -RandomUtil.randomInt(180));
                o.setCreateTime(optoDate);
                optometryRecordService.save(o);
                optos.add(o);
            }

            // 生成销售记录
            int salesCount = RandomUtil.randomInt(1, 3);
            for (int k = 0; k < salesCount; k++) {
                SalesRecord s = new SalesRecord();
                s.setCustomerId(c.getId());
                
                // 50% 概率关联最近一条验光单
                if (RandomUtil.randomBoolean() && !optos.isEmpty()) {
                    s.setOptometryId(optos.get(optos.size() - 1).getId());
                }
                
                Date salesDate = DateUtil.offsetDay(now, -RandomUtil.randomInt(180));
                s.setRecordNo("SR" + DateUtil.format(salesDate, "yyyyMMddHHmmss") + RandomUtil.randomNumbers(3));
                s.setFrameBrand(RandomUtil.randomEle(frameBrands));
                s.setFrameModel("F-" + RandomUtil.randomInt(1000, 9999));
                s.setFramePrice(new BigDecimal(RandomUtil.randomInt(200, 2000)));
                s.setLensBrand(RandomUtil.randomEle(lensBrands));
                s.setLensParams(RandomUtil.randomEle(new String[]{"1.60 非球面", "1.67 数码片", "1.74 超薄", "1.56 变色"}));
                s.setLensPrice(new BigDecimal(RandomUtil.randomInt(300, 5000)));
                s.setTotalAmount(s.getFramePrice().add(s.getLensPrice()));
                s.setSalesDate(salesDate);
                s.setOperatorId(operatorId);
                s.setCreateTime(salesDate);
                
                salesRecordService.save(s);
            }
        }

        System.out.println("数据填充完成！");
        System.out.println("生成顾客: " + customers.size() + " 位");
    }
}
