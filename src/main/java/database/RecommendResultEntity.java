package database;

import jakarta.persistence.*;

@Entity
@Table(name = "recommendation_cache")
public class RecommendResultEntity {

    @Id
    @Column(name = "cache_key", length = 200)
    private String cacheKey;

    @Column(name = "response", columnDefinition = "TEXT")
    private String response;

    @Column(name = "created_at")
    private long createdAt;

    @Column(name = "hit_count")
    private int hitCount = 0;

    public RecommendResultEntity() {}

    public RecommendResultEntity(String cacheKey, String response) {
        this.cacheKey  = cacheKey;
        this.response  = response;
        this.createdAt = System.currentTimeMillis();
    }

    public String getCacheKey() { return cacheKey; }
    public String getResponse() { return response; }
    public int    getHitCount() { return hitCount; }
    public void   incrementHit(){ hitCount++; }
}