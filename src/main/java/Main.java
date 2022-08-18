
import Models.CommandAction.PlayerCommand;
import Models.EngineConfig.EngineConfig;
import Models.GameState.GameState;
import Services.*;
import com.microsoft.signalr.*;


import java.util.*;

public class Main {
    public static void main(String[] args) throws Exception {
        BotService botService = new BotService();
        String token = System.getenv("Token");

        token = (token != null) ? token : UUID.randomUUID().toString();

        String environmentIp = System.getenv("RUNNER_IPV4");

        String ip = (environmentIp != null && !environmentIp.isBlank()) ? environmentIp : "localhost";
        ip = ip.startsWith("http://") ? ip : "http://" + ip;

        String url = ip + ":" + "5000" + "/runnerhub";
        HubConnection hubConnection = HubConnectionBuilder.create(url).build();

        hubConnection.on("Disconnect", (id) -> {
            System.out.println("Disconnected:");
            botService.setShouldQuit(true);
            hubConnection.stop();
        }, UUID.class);

        hubConnection.on("Registered", (id) -> System.out.println("Registered with the runner, bot ID is: " + id), UUID.class);

        hubConnection.on("ReceiveBotState", botService::updateBotState, GameState.class);

        hubConnection.on("ReceiveConfigValues", botService::setEngineConfig, EngineConfig.class);

        hubConnection.start().blockingAwait();

        Thread.sleep(1000);
        System.out.println("Registering with the runner...");
        hubConnection.send("Register", token, "RobotSquid");

        hubConnection.on("ReceiveGameComplete", (state) -> System.out.println("Game complete"), String.class);

        hubConnection.start().subscribe(()-> {
            while (!botService.getShouldQuit()) {
                Thread.sleep(20);
                if (botService.getReceivedBotState()) {
                   PlayerCommand playerCommand = botService.computeNextPlayerAction();
                   if (hubConnection.getConnectionState() == HubConnectionState.CONNECTED && playerCommand != null) {
                       hubConnection.send("SendPlayerCommand", playerCommand);
                   }
                }
            }
        });
        hubConnection.stop();
    }
}
