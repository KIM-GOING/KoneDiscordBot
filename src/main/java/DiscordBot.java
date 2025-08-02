import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder; // JDA 라이브러리
import net.dv8tion.jda.api.requests.GatewayIntent;

import dto.Secret;
import util.SecretLoader;

public class DiscordBot {

    public static void main(String[] args) throws Exception {
        Secret secret = SecretLoader.load();
        String token = secret.discord_token;

        JDA jda = JDABuilder.createDefault(token)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS)
                .addEventListeners(new StickerResponder()) // 먼저 메시지 리스너 등록
                .build()
                .awaitReady(); // 봇 준비 대기

        // SolveAlarmer는 jda가 build된 후 등록
        new SolveAlarmer(jda);
    }

}
