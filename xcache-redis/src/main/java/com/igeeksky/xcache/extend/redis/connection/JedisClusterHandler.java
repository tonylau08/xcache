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

package com.igeeksky.xcache.extend.redis.connection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.igeeksky.xcache.extend.redis.connection.RedisNode.MasterSlave;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.util.JedisClusterCRC16;

/**
 * @author Tony.Lau
 * @blog: https://my.oschina.net/xcafe
 * @createTime 2017-03-03 15:11:50
 */
public class JedisClusterHandler {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	public static final int CLUSTER_SLOT_MAXNUM = 16384;

	private static final int MIN_CLUSTER_NODE_NUM = 3;

	private final JedisPoolConfig jedisPoolConfig;

	private Set<String> redisClusterNodes;

	/** IP:Port | RedisNode */
	private Map<String, RedisNode> redisNodesMap = new HashMap<String, RedisNode>();

	//private final Integer[] slotsIndexLock = new Integer[CLUSTER_SLOT_MAXNUM];
	
	/** Slot | HostAndPort */
	private final String[] slotsCache = new String[CLUSTER_SLOT_MAXNUM];

	/** IP:Port | JedisPool */
	private final Map<String, JedisPool> poolsCache = new HashMap<String, JedisPool>();

	private volatile boolean isRefresh = false;
	
	private final AtomicInteger allRedirects = new AtomicInteger(0);
	
	private final ThreadLocalRandom random = ThreadLocalRandom.current();

	public JedisClusterHandler(Set<String> redisClusterNodes, JedisPoolConfig jedisPoolConfig) {
		this.redisClusterNodes = redisClusterNodes;
		this.jedisPoolConfig = jedisPoolConfig;
		/*for(Integer i=0; i<CLUSTER_SLOT_MAXNUM; i++){
			slotsIndexLock[i] = i;
		}*/
		refreshNode();
	}

	void refreshNode() {
		if (isRefresh) {
			return;
		}
		try {
			synchronized (slotsCache) {
				if (isRefresh) {
					return;
				}
				isRefresh = true;
				boolean refreshStatus = refresh(redisClusterNodes);
				if(!refreshStatus){
					refresh(redisNodesMap.keySet());
				}
				//redisClusterNodes.clear();
				//redisClusterNodes = redisNodesMap.keySet();
			}
		} finally {
			isRefresh = false;
		}
	}

	private boolean refresh(Set<String> set) {
		boolean refreshStatus = false;
		for(String ipPort : set){
			HostAndPort hp = HostAndPort.parseString(ipPort);
			String myelfHost = hp.getHost();
			int myselfPort = hp.getPort();
			Jedis jedis = null;
			try {
				jedis = new Jedis(myelfHost, myselfPort);
				String clusterNodes = jedis.clusterNodes();
				if(!initRedisNodes(myelfHost, myselfPort, clusterNodes)){
					continue;
				}
				initSlotAndPool();
				removeInvaidPool();
				allRedirects.set(0);
				refreshStatus = true;
				break;
			} catch (Exception e) {
				logger.error("init jedis pool error:	" + e.getMessage(), e);
				continue;
			} finally {
				if(null != jedis) jedis.close();
			}
		}
		return refreshStatus;
	}

	/**
	 * 初始化节点
	 * 
	 * @param myselfHost
	 * @param myselfPort
	 * @param clusterNodes
	 */
	private boolean initRedisNodes(String myselfHost, int myselfPort, String clusterNodes) {
		String[] nodes = clusterNodes.split("\n");
		int nodesNum = nodes.length;
		if(nodesNum < MIN_CLUSTER_NODE_NUM){
			return false;
		}
		Map<String, RedisNode> ipPortMap = new HashMap<String, RedisNode>(nodesNum * 2);
		Map<String, RedisNode> idMap = new HashMap<String, RedisNode>(nodesNum);
		for (int i = 0; i < nodesNum; i++) {
			String node = nodes[i];
			String[] info = node.split(" ");
			RedisNode redisNode = new RedisNode();
			redisNode.setId(info[0]);
			redisNode.setIpPort(info[1]);
			redisNode.setFlags(info[2]);
			redisNode.setMaster(info[3]);
			redisNode.setPingSent(Long.valueOf(info[4]));
			redisNode.setPongRecv(Long.valueOf(info[5]));
			redisNode.setConfigEpoch(info[6]);
			redisNode.setLinkState(info[7]);
			redisNode.setSlot(info[8]);
			// 处理Redis CLUSTER NODES命令返回当前连接IP地址变为127.0.0.1，而非实际地址的问题
			if (redisNode.isMyself()) {
				if (redisNode.getHost().startsWith("127.0.0.1")) {
					Jedis jedis = null;
					try {
						jedis = new Jedis(redisNode.getHost(), redisNode.getPort());
						jedis.ping();
					} catch (JedisConnectionException e) {
						logger.warn("Jedis host error, RedisServer return current connection's ip:port is " + redisNode.getIpPort()
								+ ", redisNode change to used real ip:port: " + myselfHost + ":" + myselfPort + " now");
						redisNode.setHost(myselfHost);
						redisNode.setPort(myselfPort);
					} finally {
						if (null != jedis) jedis.close();
					}
				}
			}
			//如果IP和端口不为空，存入节点列表
			if(redisNode.validateIpPort()){
				ipPortMap.put(redisNode.getIpPort(), redisNode);
			}
			// 保存Master节点
			if (redisNode.validateIpPort() && redisNode.getMasterSlave() == MasterSlave.MASTER) {
				idMap.put(redisNode.getId(), redisNode);
			}
		}

		// 遍历nodeMap，将Slave节点加入Master节点
		Iterator<Entry<String, RedisNode>> ipPortMapIterator = ipPortMap.entrySet().iterator();
		while (ipPortMapIterator.hasNext()) {
			Entry<String, RedisNode> entry = ipPortMapIterator.next();
			RedisNode node = entry.getValue();
			String masterId = node.getMaster();
			if (masterId != null) {
				RedisNode masterNode = idMap.get(masterId);
				if (null != masterNode) {
					node.setSlotStart(masterNode.getSlotStart());
					node.setSlotEnd(masterNode.getSlotEnd());
					masterNode.addSlave(node);
					idMap.put(masterId, masterNode);
				}
			}
		}
		redisNodesMap = ipPortMap;
		return true;
	}

	/**
	 * 遍历nodeMap，检查并初始化JedisPool，将slot映射置入
	 */
	private void initSlotAndPool() {
		Iterator<Entry<String, RedisNode>> nodeMapIterator = redisNodesMap.entrySet().iterator();
		while (nodeMapIterator.hasNext()) {
			Entry<String, RedisNode> entry = nodeMapIterator.next();
			String ipPort = entry.getKey();
			RedisNode node = entry.getValue();
			if (node.validateIpPort()) {
				JedisPool pool = poolsCache.get(ipPort);
				if (null == pool) {
					pool = new JedisPool(jedisPoolConfig, node.getHost(), node.getPort());
					poolsCache.put(ipPort, pool);
				}
			}

			if (node.getMasterSlave() == MasterSlave.MASTER && node.validateSlot()) {
				int slotStart = node.getSlotStart();
				int slotEnd = node.getSlotEnd();
				for (int i = slotStart; i <= slotEnd; i++) {
					slotsCache[i] = node.getIpPort();
				}
			}
		}
	}

	/**
	 * 移除失效的JedisPool
	 */
	private void removeInvaidPool() {
		Iterator<Entry<String, JedisPool>> poolsIterator = poolsCache.entrySet().iterator();
		while (poolsIterator.hasNext()) {
			Entry<String, JedisPool> entry = poolsIterator.next();
			String ipPort = entry.getKey();
			if (redisNodesMap.get(ipPort) == null) {
				JedisPool pool = entry.getValue();
				pool.close();
				pool.destroy();
				poolsIterator.remove();
			}
		}
	}

	public RedisNode getRedisNode(String hostAndPort) {
		return redisNodesMap.get(hostAndPort);
	}

	@SuppressWarnings("unchecked")
	public Map<String, List<byte[]>> getHostsByKeys(byte[][] keys) {
		int length;
		if (null == keys || (length = keys.length) == 0) {
			return Collections.EMPTY_MAP;
		}

		Map<String, List<byte[]>> map = new HashMap<String, List<byte[]>>();

		for (int i = 0; i < length; i++) {
			byte[] key = keys[i];
			String ipPort = getHost(key);
			List<byte[]> keyList = map.get(ipPort);
			if (null == keyList) {
				keyList = new ArrayList<byte[]>();
				map.put(ipPort, keyList);
			}
			keyList.add(key);
		}
		return map;
	}
	
	public String getHost(byte[] key) {
		if (null == key || key.length == 0) {
			throw new IllegalArgumentException("key must not be null or empty");
		}
		return slotsCache[JedisClusterCRC16.getSlot(key)];
	}

	public Jedis getJedis(byte[] key) {
		return getJedis(getHost(key));
	}
	
	public Jedis getJedis(String ipPort) {
		JedisPool pool = poolsCache.get(ipPort);
		if(null != pool){
			return pool.getResource();
		}
		return null;
	}

	public Jedis refreshSlotAndGet(int slot, HostAndPort targetNode) {
		if (slot < 0 || slot >= CLUSTER_SLOT_MAXNUM) {
			throw new IllegalArgumentException("slot: " + slot + "less than 0 or greater than 16383");
		}
		if (null == targetNode) {
			throw new IllegalArgumentException("targetNode must not be null or empty");
		}

		String ipPort = targetNode.getHost().trim() + ":" + targetNode.getPort();
		JedisPool pool = poolsCache.get(ipPort);

		if (pool == null) {
			HostAndPort hp = HostAndPort.parseString(ipPort);
			pool = new JedisPool(jedisPoolConfig, hp.getHost(), hp.getPort());
			poolsCache.put(ipPort, pool);
			redisClusterNodes.add(ipPort);
		}
		slotsCache[slot] = ipPort;
		
		if(allRedirects.addAndGet(1) > 20){
			refreshNode();
		}
		
		/*Integer LOCK = slotsIndexLock[slot];
		synchronized (LOCK) {
			
		}*/
		
		return getJedis(ipPort);
	}

	public Jedis getOtherHostJedis(byte[] key, HostAndPort currentNode) {
		String masterIpPort = getHost(key);
		String currentIpPort = null; 
		if(currentNode != null){
			currentIpPort = currentNode.toString();
		}
		
		for(int i=0; i<30; i++){
			int slot = random.nextInt(CLUSTER_SLOT_MAXNUM);
			String otherHost = slotsCache[slot];
			if(!otherHost.equals(masterIpPort) && !otherHost.equals(currentIpPort)){
				Jedis jedis = getJedis(otherHost);
				if(jedis != null){
					return jedis;
				}
			}
		}
		return null;
	}

}
