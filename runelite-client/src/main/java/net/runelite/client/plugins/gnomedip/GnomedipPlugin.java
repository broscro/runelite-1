package net.runelite.client.plugins.gnomedip;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.MessageNode;
import net.runelite.api.Player;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.OverheadTextChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import java.io.*;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;

@PluginDescriptor(
        name = "Gnomedip",
        description = "Gnomedip's stream plugin.",
        enabledByDefault = false
)
@Slf4j
public class GnomedipPlugin extends Plugin {
    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;

    @Override
    protected void startUp()
    {
        clientThread.invoke(() ->
        {
            return true;
        });
    }

    @Subscribe
    public void onChatMessage(ChatMessage chatMessage)
    {
        if (client.getGameState() != GameState.LOGGED_IN)
        {
            return;
        }

        switch (chatMessage.getType())
        {
            case PUBLICCHAT:
            case MODCHAT:
            case FRIENDSCHAT:
            case CLAN_CHAT:
            case CLAN_GUEST_CHAT:
            case CLAN_GIM_CHAT:
            case PRIVATECHAT:
            case PRIVATECHATOUT:
            case MODPRIVATECHAT:
                break;
            default:
                return;
        }

        final MessageNode messageNode = chatMessage.getMessageNode();
        final String message = messageNode.getValue();
        final String updatedMessage = message + ":-:";
        executePost("http://127.0.0.1:3000/msg", message);

        if (updatedMessage == null)
        {
            return;
        }

        messageNode.setValue(updatedMessage);
    }

    @Subscribe
    public void onOverheadTextChanged(final @NotNull OverheadTextChanged event)
    {
        if (!(event.getActor() instanceof Player))
        {
            return;
        }

        final String message = event.getOverheadText();
        final String updatedMessage = message + "-:-";

        if (updatedMessage == null)
        {
            return;
        }

        event.getActor().setOverheadText(updatedMessage);
    }

    public void executePost(String targetURL, String msg){
        try {
            URL url = new URL(targetURL);
            HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
            httpCon.setDoOutput(true);
            httpCon.setRequestMethod("PUT");
            OutputStreamWriter out = new OutputStreamWriter(
                    httpCon.getOutputStream());
            out.write(msg);
            out.close();
            httpCon.getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
