package com.umc.connext.domain.member.schedular;

import com.umc.connext.domain.member.enums.MemberStatus;
import com.umc.connext.domain.member.repository.MemberNotificationSettingRepository;
import com.umc.connext.domain.member.repository.MemberRepository;
import com.umc.connext.domain.member.repository.MemberTermRepository;
import com.umc.connext.domain.member.repository.MemberVisibilitySettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class MemberCleanupScheduler {
    private final MemberRepository memberRepository;
    private final MemberTermRepository memberTermRepository;
    private final MemberVisibilitySettingRepository memberVisibilitySettingRepository;
    private final MemberNotificationSettingRepository memberNotificationSettingRepository;

    @Transactional
    @Scheduled(cron = "0 0 4 * * *")
    public void cleanupDeletedMembers() {

        LocalDateTime threshold = LocalDateTime.now().minusDays(7);

        List<Long> memberIds =
                memberRepository.findIdsByMemberStatusAndDeletedAtBefore(
                        MemberStatus.DELETED, threshold
                );

        if (memberIds.isEmpty()) {
            log.info("Member cleanup skipped: no targets");
            return;
        }

        //연관 데이터 삭제
        memberTermRepository.deleteByMemberIds(memberIds);
        memberVisibilitySettingRepository.deleteByMemberIds(memberIds);
        memberNotificationSettingRepository.deleteByMemberIds(memberIds);

        log.info("Deleted member cleanup started. target count={}", memberIds.size());

        memberRepository.hardDeletedMembers(threshold);
    }
}
