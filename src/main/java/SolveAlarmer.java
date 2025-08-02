import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class SolveAlarmer extends ListenerAdapter {

    private final JDA jda;
    private final String channelName = "일반";
    private final String botName = "코네";
    private final String[] numberEmojis = {"0️⃣", "1️⃣", "2️⃣", "3️⃣", "4️⃣", "5️⃣", "6️⃣", "7️⃣", "8️⃣", "9️⃣"};
    private final List<LocalTime> alarmTimes = List.of(
            LocalTime.of(18, 29), // 오후 10시
            LocalTime.MIDNIGHT,  // 자정
            LocalTime.of(2, 0)   // 새벽 2시
    );

    public SolveAlarmer(JDA jda) {
        this.jda = jda;
        startAlarms();
    }

    @Override
    public void onReady(ReadyEvent event) {
        System.out.println("[SolveAlarmer] 스케줄 시작!");
        startAlarms();
    }

    private void startAlarms() {
        System.out.println("[SolveAlarmer] 스케줄 시작!");

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        for (LocalTime targetTime : alarmTimes) {
            long initialDelay = computeInitialDelay(targetTime);
            scheduler.scheduleAtFixedRate(
                    this::checkAndAnnounce,
                    initialDelay,
                    TimeUnit.DAYS.toSeconds(1),
                    TimeUnit.SECONDS
            );
        }
    }

    private long computeInitialDelay(LocalTime targetTime) {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime nextRun = now.with(targetTime);
        if (now.isAfter(nextRun)) {
            nextRun = nextRun.plusDays(1);
        }
        return Duration.between(now, nextRun).toSeconds();
    }

    private void checkAndAnnounce() {
        System.out.println("[SolveAlarmer] 미인증자 체크 시작");

        TextChannel channel = jda.getTextChannelsByName(channelName, true).stream().findFirst().orElse(null);
        if (channel == null) {
            System.err.println("[SolveAlarmer] 채널을 찾을 수 없습니다.");
            return;
        }

        channel.getHistory().retrievePast(100).queue(messages -> {
            Set<User> verifiedUsers = new HashSet<>();

            for (Message msg : messages) {
                if (!isToday(msg.getTimeCreated())) continue;

                for (MessageReaction reaction : msg.getReactions()) {
                    if (!isNumberEmoji(reaction.getEmoji())) continue;

                    reaction.retrieveUsers().queue(users -> {
                        for (User u : users) {
                            if (u.getName().equals(botName)) {
                                verifiedUsers.add(msg.getAuthor());
                            }
                        }
                    });
                }
            }

            jda.getGuilds().get(0).loadMembers().onSuccess(members -> {
                List<Member> targets = members.stream()
                        .filter(m -> !m.getUser().isBot())
                        .filter(m -> !verifiedUsers.contains(m.getUser()))
                        .collect(Collectors.toList());

                if (!targets.isEmpty()) {
                    String mentions = targets.stream()
                            .map(Member::getAsMention)
                            .collect(Collectors.joining(" "));

                    String message = mentions + " 1일1솔도 안하는 거 보니 가망 없는듯 ㅋㅋ";
                    channel.sendMessage(message).queue();
                } else {
                    System.out.println("[SolveAlarmer] 모두 인증 완료 🎉");
                }
            });
        });
    }

    private boolean isNumberEmoji(Emoji emoji) {
        for (String num : numberEmojis) {
            if (emoji.getName().equals(num)) return true;
        }
        return false;
    }

    private boolean isToday(OffsetDateTime time) {
        LocalDateTime t = time.toLocalDateTime();
        LocalDate logicalDate = t.getHour() < 6 ? t.toLocalDate().minusDays(1) : t.toLocalDate();
        LocalDate now = LocalDateTime.now().getHour() < 6 ? LocalDate.now().minusDays(1) : LocalDate.now();
        return logicalDate.equals(now);
    }
}
