/*
 * Copyright 2017 Tony.lau All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.igeeksky.xcache.test.other;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import redis.clients.jedis.BinaryJedisCluster;
import redis.clients.jedis.Client;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisMovedDataException;

/**
 * @author Tony.Lau
 * @blog: https://my.oschina.net/xcafe
 * @createTime 2017-03-08 20:56:48
 */
public class JedisClusterInfoTest {

	private long start;

	@Before
	public void before() {
		start = System.currentTimeMillis();
	}

	@After
	public void after() {
		System.out.println("耗时：" + (System.currentTimeMillis() - start));
	}

	@Test
	public void testJedisCluster() throws IOException {
		Set<HostAndPort> jedisClusterNodes = new HashSet<HostAndPort>();
		jedisClusterNodes.add(new HostAndPort("192.168.0.11", 7000));
		jedisClusterNodes.add(new HostAndPort("192.168.0.11", 7001));
		jedisClusterNodes.add(new HostAndPort("192.168.0.11", 7002));
		JedisCluster jc = new JedisCluster(jedisClusterNodes);
		for (int i = 1; i <= 10000; i++) {
			long start = System.currentTimeMillis();
			jc.set("k:" + i, "v" + i);
			System.out.print("set " + i + "th value in " + (System.currentTimeMillis() - start) + " ms");
			start = System.currentTimeMillis();
			jc.get("k:" + i);
			System.out.println(", get " + i + "th value in " + (System.currentTimeMillis() - start) + " ms");
		}
		if (null != jc)
			jc.close();
	}

	@Test
	@SuppressWarnings("resource")
	public void testClusterNodes() {
		JedisPool pool1 = new JedisPool("192.168.0.11", 7000);
		JedisPool pool2 = new JedisPool("192.168.0.11", 7001);
		JedisPool pool3 = new JedisPool("192.168.0.11", 7002);
		Jedis jedis = null;
		jedis = pool1.getResource();
		jedis = pool2.getResource();
		jedis = pool3.getResource();
		String clusterNodes = jedis.clusterNodes();
		String[] nodes = clusterNodes.split("\n");

		for (int j = 0; j < nodes.length; j++) {
			String node = nodes[j];
			String[] infos = node.split(" ");
			for (int i = 0; i < infos.length; i++) {
				System.out.println(infos[i]);
			}
		}

		System.out.println(clusterNodes);

		Client cli = jedis.getClient();
		String host = cli.getHost();
		int port = cli.getPort();
		System.out.println("MYSELF：	" + host + ":" + port);

		String clusterInfo = jedis.clusterInfo();
		System.out.println(clusterInfo);

	}

	@Test
	@SuppressWarnings("resource")
	public void testClusterSlots() {
		JedisPool pool1 = new JedisPool("192.168.0.11", 7000);
		Jedis jedis = pool1.getResource();
		List<Object> nodes = jedis.clusterSlots();

		for (int i = 0; i < 3; i++) {
			@SuppressWarnings("unchecked")
			List<Object> node = (List<Object>) nodes.get(i);
			Object start = node.get(0);
			System.out.println("SLOT_START：：			" + start.getClass());
			Object end = node.get(1);
			System.out.println("SLOT_END：：			" + end.getClass());
			ArrayList<?> master = (ArrayList<?>) node.get(2);
			Object masterHost = master.get(0);
			System.out.println("MASTER_HOST_LIST_0_BYTE[]：：	" + new String((byte[]) masterHost));
			Object masterPort = master.get(1);
			System.out.println("MASTER_PORT_LIST_1_LONG：：	" + masterPort);
			Object masterIDs = master.get(2);
			System.out.println("MASTER_IDs_LIST_1_BYTE[]：：	" + new String((byte[]) masterIDs));

			if (node.size() > 3) {
				ArrayList<?> slave1 = (ArrayList<?>) node.get(3);
				Object slave1Host = slave1.get(0);
				System.out.println("SLAVE_HOST_LIST_0_BYTE[]：：	" + new String((byte[]) slave1Host));
				Object slave1Port = slave1.get(1);
				System.out.println("SLAVE_PORT_LIST_1_LONG：：	" + slave1Port);
				Object slave1IDs = slave1.get(2);
				System.out.println("SLAVE_IDS_LIST_1_BYTE[]：：	" + new String((byte[]) slave1IDs));
			}
			System.out.println();
		}
		try {
			System.out.println("GET_RESULT：			" + jedis.get("iiii"));
		} catch (JedisMovedDataException e) {
			System.out.println(e.getTargetNode());
		}

		Client cli = jedis.getClient();
		String host = cli.getHost();
		int port = cli.getPort();
		System.out.println("HOST:PORT：			" + host + ":" + port);

		jedis.close();

		JedisPoolConfig jedisConfig = new JedisPoolConfig();
		jedisConfig.setTestOnBorrow(true);
		Set<HostAndPort> set = new HashSet<HostAndPort>();
		set.add(new HostAndPort("192.168.0.11", 7000));
		set.add(new HostAndPort("192.168.0.11", 7001));
		set.add(new HostAndPort("192.168.0.11", 7002));

		BinaryJedisCluster cluster = new BinaryJedisCluster(set, 10000, 3, jedisConfig);

		try {
			Thread.sleep(10000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

		Map<String, JedisPool> map = cluster.getClusterNodes();

		String[] keys = map.keySet().toArray(new String[3]);

		System.out.println(keys[0]);
		System.out.println(keys[1]);
		System.out.println(keys[2]);

		jedis.clusterSlots();
	}

}
