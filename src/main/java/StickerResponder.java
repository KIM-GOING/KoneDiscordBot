import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public class StickerResponder extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        // 1. 봇이 보낸 메시지는 무시
        if (event.getAuthor().isBot()) return;

        // 2. 채널 이름 검사
        String channelName = event.getChannel().getName();
        if (!channelName.equals("일반")) return;

        // 3. 메시지의 논리 날짜 계산 (6시 이전이면 전날로 간주)
        OffsetDateTime messageTime = event.getMessage().getTimeCreated();
        LocalDateTime messageDateTime = messageTime.toLocalDateTime();
        LocalDate logicalMessageDate = messageDateTime.getHour() < 6
                ? messageDateTime.toLocalDate().minusDays(1)
                : messageDateTime.toLocalDate();

        // 4. 현재 시간의 논리 날짜 계산 (6시 이전이면 전날로 간주)
        LocalDateTime now = LocalDateTime.now();
        LocalDate logicalNowDate = now.getHour() < 6
                ? now.toLocalDate().minusDays(1)
                : now.toLocalDate();

        if (!logicalMessageDate.isEqual(logicalNowDate)) return;

        // 5. 오늘 날짜 기준 숫자 이모지 반응
        int todayDigit = logicalNowDate.getDayOfMonth() % 10;
        String[] emojis = {"0️⃣", "1️⃣", "2️⃣", "3️⃣", "4️⃣", "5️⃣", "6️⃣", "7️⃣", "8️⃣", "9️⃣"};

        event.getMessage().addReaction(Emoji.fromUnicode(emojis[todayDigit])).queue();
    }
}