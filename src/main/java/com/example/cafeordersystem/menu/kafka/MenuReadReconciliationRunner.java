package com.example.cafeordersystem.menu.kafka;

import com.example.cafeordersystem.menu.kafka.OwnerMenuClient;
import com.example.cafeordersystem.menu.dto.OwnerMenuDto;
import com.example.cafeordersystem.menu.service.MenuReadSnapshotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 구매 서버 menu_read 초기화/복구 실행기.
 *
 * Kafka 이벤트는 실시간 증분 반영을 담당한다.
 * 이 클래스는 시작 시점과 주기적인 전체 대조를 담당해,
 * Kafka 이전 데이터 또는 누락 이벤트 때문에 menu_read가 비는 문제를 복구한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MenuReadReconciliationRunner {

    private final OwnerMenuClient ownerMenuClient;
    private final MenuReadSnapshotService menuReadSnapshotService;

    @EventListener(ApplicationReadyEvent.class)
    public void syncWhenApplicationIsReady() {
        syncSafely("APPLICATION_READY");
    }

    @Scheduled(
            initialDelayString = "${app.menu-read-sync.initial-delay-ms:30000}",
            fixedDelayString = "${app.menu-read-sync.fixed-delay-ms:300000}"
    )
    public void syncPeriodically() {
        syncSafely("SCHEDULED");
    }

    private void syncSafely(String trigger) {
        try {
            List<OwnerMenuDto> ownerMenus = ownerMenuClient.getAllMenus();
            int synchronizedCount =
                    menuReadSnapshotService.replaceSnapshot(ownerMenus);

            log.info(
                    "menu_read 전체 동기화 완료 trigger={}, count={}",
                    trigger,
                    synchronizedCount
            );
        } catch (Exception e) {
            // 사장 서버가 잠시 내려가도 구매 서버 전체가 죽지 않도록 한다.
            // 기존 menu_read는 트랜잭션 롤백으로 그대로 유지된다.
            log.error(
                    "menu_read 전체 동기화 실패 trigger={}",
                    trigger,
                    e
            );
        }
    }
}
