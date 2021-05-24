package com.igeeksky.xcache.extend.redis.connection;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.igeeksky.xcache.core.util.StringUtils;

public class RedisNode {

	private ThreadLocalRandom random = ThreadLocalRandom.current();

	private String id;
	private String ipPort;
	private String flags;
	private String master;
	private Long pingSent;
	private Long pongRecv;
	private String configEpoch;
	private LinkState linkState;
	private String slot;
	private String host;
	private Integer port;
	private Integer slotStart;
	private Integer slotEnd;
	private Status status;
	private MasterSlave masterSlave;
	private boolean isMyself = false;
	private List<RedisNode> slaves = new ArrayList<RedisNode>();

	public boolean validateSlot() {
		if (slotStart == null || slotEnd == null) {
			return false;
		}
		if (slotEnd < slotStart) {
			return false;
		}
		return true;
	}
	
	public boolean validateIpPort() {
		if (null != host && null != port && ipPort != null) {
			return true;
		}
		return false;
	}

	public RedisNode() {
	}

	public RedisNode(String host, Integer port, String id) {
		setHost(host);
		setPort(port);
		setId(id);
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		if (StringUtils.isEmpty(host)) {
			throw new IllegalArgumentException("host must not be null or empty");
		}
		this.host = host.trim();
		this.ipPort = host +":" + port;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		if (null == port) {
			throw new IllegalArgumentException("port must not be null or empty");
		}
		this.port = port;
		this.ipPort = host +":" + port;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		if (StringUtils.isEmpty(id)) {
			throw new IllegalArgumentException("host must not be null or empty");
		}
		this.id = id;
	}

	public String getIpPort() {
		return ipPort;
	}

	public void setIpPort(String ipPort) {
		if (StringUtils.isNotEmpty(ipPort)) {
			String[] temp = ipPort.split(":");
			setHost(temp[0]);
			setPort(Integer.valueOf(temp[1].trim()));
		} else {
			this.ipPort = null;
		}
	}

	public String getFlags() {
		return flags;
	}

	public void setFlags(String flags) {
		if (StringUtils.isEmpty(flags)) {
			throw new IllegalArgumentException("flags must not be null or empty");
		}
		this.flags = flags.trim().toLowerCase();
		if (this.flags.indexOf("master") >= 0 || this.flags.indexOf("slave") >= 0) {
			if (this.flags.indexOf("master") >= 0) {
				this.masterSlave = MasterSlave.MASTER;
			}
			if (this.flags.indexOf("slave") >= 0) {
				this.masterSlave = MasterSlave.SLAVE;
			}
		} else {
			this.masterSlave = null;
		}
		if (this.flags.indexOf("fail") >= 0) {
			if (this.flags.indexOf("fail") >= 0) {
				this.status = Status.FAIL;
			}
			if (this.flags.indexOf("fail?") >= 0) {
				this.status = Status.PFAIL;
			}
		} else if (this.flags.indexOf("handshake") >= 0) {
			this.status = Status.HANDSHAKE;
		} else {
			this.status = null;
		}
		if (this.flags.indexOf("myself") >= 0) {
			this.isMyself = true;
		} else {
			this.isMyself = false;
		}
		// noaddr //noflags
	}

	public String getMaster() {
		return master;
	}

	public void setMaster(String master) {
		if(StringUtils.isEmpty(master)){
			this.master = null;
			return;
		}
		if("-".equals(master.trim())){
			this.master = null;
			return;
		}
		this.master = master;
	}

	public Long getPingSent() {
		return pingSent;
	}

	public void setPingSent(Long pingSent) {
		this.pingSent = pingSent;
	}

	public Long getPongRecv() {
		return pongRecv;
	}

	public void setPongRecv(Long pongRecv) {
		this.pongRecv = pongRecv;
	}

	public String getConfigEpoch() {
		return configEpoch;
	}

	public void setConfigEpoch(String configEpoch) {
		this.configEpoch = configEpoch;
	}

	public LinkState getLinkState() {
		return linkState;
	}

	public void setLinkState(String linkState) {
		this.linkState = LinkState.getConnected(linkState);
	}

	public String getSlot() {
		return slot;
	}

	public void setSlot(String slot) {
		if (StringUtils.isNotEmpty(slot)) {
			String[] temp = slot.split("-");
			if (temp.length == 2) {
				setSlotStart(Integer.valueOf(temp[0]));
				setSlotEnd(Integer.valueOf(temp[1]));
			} else {
				setSlotStart(Integer.valueOf(temp[0]));
				setSlotEnd(Integer.valueOf(temp[0]));
			}
			this.slot = slot;
		} else {
			this.slot = null;
			this.slotStart = null;
			this.slotEnd = null;
		}
	}

	public Integer getSlotStart() {
		return slotStart;
	}

	public void setSlotStart(Integer slotStart) {
		if (null == slotStart) {
			throw new IllegalArgumentException("slotStart must not be null or empty");
		}
		if (slotStart < 0 || slotStart >= JedisClusterHandler.CLUSTER_SLOT_MAXNUM) {
			throw new IllegalArgumentException(
					"slotStart：" + slotStart + " is less than zero or greater than max slot num");
		}
		this.slotStart = slotStart;
		this.slot = slotStart + "-" + slotEnd;
	}

	public Integer getSlotEnd() {
		return slotEnd;
	}

	public void setSlotEnd(Integer slotEnd) {
		if (null == slotEnd) {
			throw new IllegalArgumentException("slotStart must not be null or empty");
		}
		if (slotEnd < 0 || slotEnd >= JedisClusterHandler.CLUSTER_SLOT_MAXNUM) {
			throw new IllegalArgumentException(
					"slotEnd：" + slotEnd + " is less than zero or greater than max slot num");
		}
		this.slotEnd = slotEnd;
		this.slot = slotStart + "-" + slotEnd;
	}

	public boolean isMyself() {
		return isMyself;
	}

	public void setMyself(boolean isMyself) {
		this.isMyself = isMyself;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public MasterSlave getMasterSlave() {
		return masterSlave;
	}

	public void setMasterSlave(MasterSlave masterSlave) {
		this.masterSlave = masterSlave;
	}

	public void setLinkState(LinkState linkState) {
		this.linkState = linkState;
	}

	public List<RedisNode> getSlaves() {
		return slaves;
	}

	public void addSlave(RedisNode slave) {
		this.slaves.add(slave);
	}

	/** 随机获取从节点用于读取数据，用于容许短暂不一致性但对性能要求严格的场合 */
	public RedisNode randomGetSlave() {
		int size = slaves.size();
		for (int i = 0; i < size; i++) {
			RedisNode slave = slaves.get(random.nextInt(slaves.size()));
			if (slave.validateSlot() && slave.validateIpPort()) {
				return slave;
			}
		}
		return null;
	}

	public static enum LinkState {
		CONNECTED, DISCONNECTED;

		public static LinkState getConnected(String connected) {
			try {
				return LinkState.valueOf(connected.trim().toUpperCase());
			} catch (IllegalArgumentException e) {
				return null;
			}
		}
	}

	public static enum Status {
		NORMAL, FAIL, PFAIL, HANDSHAKE;
	}

	public static enum MasterSlave {
		MASTER, SLAVE;
	}

}