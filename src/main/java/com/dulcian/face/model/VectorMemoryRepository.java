package com.dulcian.face.model;

import com.dulcian.face.dto.VectorDto;
import com.dulcian.face.utils.ConversionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Repository
public class VectorMemoryRepository {
    Logger logger = LoggerFactory.getLogger("VectorMemoryRepository");

    private final JdbcTemplate jdbcTemplate;
    private List<VectorDto> store = new ArrayList<>();
    private ScheduledExecutorService scheduler;
    private Instant lastActivityTime;

    public VectorMemoryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public synchronized void add(int id, int employeeId, double[] vector) {
        store.add(new VectorDto(
                id,
                employeeId,
                vector
        ));
    }
    public synchronized List<VectorDto> getAllExcluding(int employeeId) {
        if(store.size() == 0)
            loadToMemory();

        lastActivityTime = Instant.now();
        return store.parallelStream()
                .filter(data -> data.getEmployeeId() != employeeId)
                .collect(Collectors.toList());
    }

    public synchronized void deleteById(int id) {
        store.removeIf(data -> data.getId() == id);
    }

    private void loadToMemory(){
        long start = System.currentTimeMillis();

        int rowCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM VectorModel", Integer.class);

        logger.info("Starting loading vectors( {} ) to memory", rowCount);

        this.store =  jdbcTemplate.query("SELECT IM.id, IM.employeeId, VM.vector FROM ImageModel IM INNER JOIN VectorModel VM ON IM.id = VM.id", rs -> {
            List<VectorDto> store = Collections.synchronizedList(new ArrayList<>(rowCount));
            while (rs.next()){
                int id = rs.getInt("id");
                int employeeId = rs.getInt("employeeId");
                double[] emb = ConversionUtils.toDouble(rs.getBytes("vector"));
                store.add(new VectorDto(id, employeeId, emb));
            }
            return store;
        });

        scheduleUnloading();

        long end = System.currentTimeMillis();

        double totalMemoryMB = (store.size() * 4164) / (1024.0 * 1024.0);

        logger.info("Loading vector( {} ) to memory took {}s and {}MB", store.size(), (end - start) / 1000.0, totalMemoryMB);
    }
    private void scheduleUnloading() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow(); // Ensure no leftover scheduler
        }

        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::unloadFromMemoryIfInactive, 1, 1, TimeUnit.HOURS);

    }

    private void unloadFromMemoryIfInactive(){
        if(Instant.now().isAfter(lastActivityTime.plusSeconds(10800))) { // 3hour
            this.store = new ArrayList<>();
            scheduler.shutdownNow();

            logger.info("Vector database is unloaded due to inactivity");
        }
    }
}
