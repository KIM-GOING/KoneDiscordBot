import net.dv8tion.jda.api.JDABuilder;

import dto.Secret;
import util.SecretLoader;

public class DiscordBot {

    public static void main(String[] args) {
        Secret secret = SecretLoader.load();
        String token = secret.discord_token;

        JDABuilder.createDefault(token).build();
    }

}
