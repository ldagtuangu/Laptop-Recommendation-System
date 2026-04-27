package database;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import processor.LaptopData;
import java.util.List;
import java.util.Optional;

public class DatabaseManager {

    private final SessionFactory sessionFactory;

    public DatabaseManager() {
        sessionFactory = new Configuration()
                .configure("hibernate.cfg.xml")
                .addAnnotatedClass(LaptopEntity.class)
                .buildSessionFactory();
        System.out.println("Hibernate SessionFactory created.");
    }

    // ── Insert all ────────────────────────────────────────────────────────────
    public void saveAll(List<LaptopData> laptops) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();

            for (int i = 0; i < laptops.size(); i++) {
                session.persist(LaptopEntity.from(laptops.get(i)));

                // Flush mỗi 20 records để tránh OutOfMemory
                if (i % 20 == 0) {
                    session.flush();
                    session.clear();
                }
            }

            tx.commit();
            System.out.println("Saved " + laptops.size() + " laptops to MySQL.");
        }
    }

    // ── Find by category ──────────────────────────────────────────────────────
    public List<LaptopEntity> findByCategory(String category) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                            "FROM LaptopEntity WHERE category = :cat ORDER BY normGpuScore DESC",
                            LaptopEntity.class)
                    .setParameter("cat", category)
                    .list();
        }
    }

    // ── Find by tag ───────────────────────────────────────────────────────────
    public List<LaptopEntity> findByTag(String tag) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                            "FROM LaptopEntity WHERE tags LIKE :tag",
                            LaptopEntity.class)
                    .setParameter("tag", "%" + tag + "%")
                    .list();
        }
    }

    // ── Find all ──────────────────────────────────────────────────────────────
    public List<LaptopEntity> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                            "FROM LaptopEntity ORDER BY category, normGpuScore DESC",
                            LaptopEntity.class)
                    .list();
        }
    }

    // Lưu kết quả recommendation
    public void saveResult(String cacheKey, String response) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            RecommendResultEntity entity =
                    new RecommendResultEntity(cacheKey, response);
            session.merge(entity);   // insert or update
            tx.commit();
            System.out.println("[DB] Saved result: " + cacheKey);
        }
    }

    // Lấy kết quả đã lưu
    public Optional<String> findResult(String cacheKey) {
        try (Session session = sessionFactory.openSession()) {
            RecommendResultEntity entity =
                    session.get(RecommendResultEntity.class, cacheKey);

            if (entity == null) return Optional.empty();

            // Tăng hit count
            Transaction tx = session.beginTransaction();
            entity.incrementHit();
            session.merge(entity);
            tx.commit();

            return Optional.of(entity.getResponse());
        }
    }

    // Xem thống kê cache DB
    public void printDbCacheStats() {
        try (Session session = sessionFactory.openSession()) {
            Long count = session.createQuery(
                            "SELECT COUNT(*) FROM RecommendResultEntity", Long.class)
                    .uniqueResult();
            System.out.println("[DB Cache] Total entries: " + count);
        }
    }

    // ── Close ─────────────────────────────────────────────────────────────────
    public void close() {
        sessionFactory.close();
        System.out.println("SessionFactory closed.");
    }

    public long countLaptops() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                            "SELECT COUNT(*) FROM LaptopEntity", Long.class)
                    .uniqueResult();
        }
    }
}