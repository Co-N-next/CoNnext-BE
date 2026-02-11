package com.umc.connext.domain.member.schedular;

import com.umc.connext.domain.member.entity.Member;
import com.umc.connext.domain.member.enums.MemberStatus;
import com.umc.connext.domain.member.repository.MemberRepository;
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

    @Transactional
    @Scheduled(cron = "0 0 4 * * *")
    public void cleanupDeletedMembers() {

        LocalDateTime threshold = LocalDateTime.now().minusDays(7);

        List<Member> targets = memberRepository.findAllByMemberStatusAndDeletedAtBefore(MemberStatus.DELETED, threshold);

        if (targets.isEmpty()) {
            log.info("Member cleanup skipped: no targets");
            return;
        }

        log.info("Deleted member cleanup started. target count={}", targets.size());
        memberRepository.deleteAll(targets);
        log.info("Deleted member cleanup executed.");
    }
}
