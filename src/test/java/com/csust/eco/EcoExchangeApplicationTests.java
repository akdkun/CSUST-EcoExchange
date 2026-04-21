package com.csust.eco;

import com.csust.eco.dto.OrderCreateDTO;
import com.csust.eco.entity.Item;
import com.csust.eco.mapper.ItemMapper;
import com.csust.eco.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@SpringBootTest
class EcoExchangeApplicationTests {

	@Autowired
	private OrderService orderService;

	@Autowired
	private ItemMapper itemMapper;

	@Test
	void testConcurrentOrder() throws InterruptedException {
		// 1. 制造一条测试数据
		Item testItem = new Item();
		testItem.setSellerId(999L);
		testItem.setTitle("测试二手高数课本");
		testItem.setPrice(new BigDecimal("15.50"));
		testItem.setStock(1);
		testItem.setStatus((byte) 0);
		itemMapper.insert(testItem);

		Long targetItemId = testItem.getId();
		log.info("====== 测试商品初始化完毕, ID: {}, 库存: 1 ======", targetItemId);

		// 2. 并发准备
		int threadCount = 100;
		ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
		CountDownLatch startLatch = new CountDownLatch(1);
		CountDownLatch endLatch = new CountDownLatch(threadCount);

		for (int i = 1; i <= threadCount; i++) {
			final Long buyerId = (long) i;
			executorService.submit(() -> {
				try {
					startLatch.await(); // 阻塞等待发令枪

					OrderCreateDTO dto = new OrderCreateDTO();
					dto.setItemId(targetItemId);

					orderService.createOrder(dto, buyerId);
					log.info("恭喜! 买家 {} 抢购成功! ", buyerId);
				} catch (Exception e) {
					log.warn("买家 {} 抢购失败: {}", buyerId, e.getMessage());
				} finally {
					endLatch.countDown();
				}
			});
		}

		// 3. 冲击系统
		log.info("====== 砰! 抢购开始, 100线程并发冲击! ======");
		startLatch.countDown();

		endLatch.await();
		executorService.shutdown();

		// 4. 物理校验
		Item finalItem = itemMapper.selectById(targetItemId);
		long orderCount = orderService.query().eq("item_id", targetItemId).count();

		log.info("====== 抢购结束物理统计 ======");
		log.info("最终商品库存: {}", finalItem.getStock());
		log.info("该商品生成的订单总数: {}", orderCount);

		Thread.sleep(15000);
	}
}