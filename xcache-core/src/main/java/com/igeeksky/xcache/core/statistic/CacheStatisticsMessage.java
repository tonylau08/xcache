package com.igeeksky.xcache.core.statistic;

/**
 * @author Patrick.Lau
 * @date 2021-06-26
 */
public class CacheStatisticsMessage {

    private final String namespace;
    private final String application;
    private final String name;
    private final long collectTimeMillis;
    private long nullHits;
    private long notNullHits;
    private long misses;

    public CacheStatisticsMessage(String namespace, String application, String name) {
        this.namespace = namespace;
        this.application = application;
        this.name = name;
        this.collectTimeMillis = System.currentTimeMillis();
    }

    public String getNamespace() {
        return namespace;
    }

    public String getApplication() {
        return application;
    }

    public String getName() {
        return name;
    }

    public long getCollectTimeMillis() {
        return collectTimeMillis;
    }

    public long getNullHits() {
        return nullHits;
    }

    public void setNullHits(long nullHits) {
        this.nullHits = nullHits;
    }

    public long getNotNullHits() {
        return notNullHits;
    }

    public void setNotNullHits(long notNullHits) {
        this.notNullHits = notNullHits;
    }

    public long getMisses() {
        return misses;
    }

    public void setMisses(long misses) {
        this.misses = misses;
    }

    public double getHitRatio() {
        double hits = (double) (nullHits + notNullHits);
        return hits / (hits + misses);
    }

    @Override
    public String toString() {
        return new StringBuilder(256).append("{\"namespace\":\"")
                .append(getNamespace())
                .append("\", \"application\":\"")
                .append(getApplication())
                .append("\", \"name\":\"")
                .append(getName())
                .append("\", \"collectTimeMillis\":")
                .append(getCollectTimeMillis())
                .append(", \"nullHits\":")
                .append(getNullHits())
                .append(", \"notNullHits\":")
                .append(getNotNullHits())
                .append(", \"misses\":")
                .append(getMisses())
                .append(", \"hitRatio\":")
                .append(getHitRatio())
                .append("}")
                .toString();
    }
}
