package za.co.entelect.challenge;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import za.co.entelect.challenge.domain.command.Command;
import za.co.entelect.challenge.domain.command.PlaceShipCommand;
import za.co.entelect.challenge.domain.state.GameState;
import za.co.entelect.challenge.domain.state.OpponentShip;

import java.io.*;
import java.net.URISyntaxException;
import java.util.Arrays;

public class Bot {

    private static final Logger logger = LoggerFactory.getLogger(Bot.class);

    private String workingDirectory;

    private String key;

    private final String commandFileName = "command.txt";

    private final String placeShipFileName = "place.txt";

    private final String stateFileName = "state.json";

    private final String botStateFileName = "bot-state.json";

    private final String inputStateFilename = "input-state.json";

    private File execDir;

    private final Gson gson;

    private BotState botState;
    private GameState gameState;
    private BotStrategy botStrategy;

    public Bot(String key, String workingDirectory) throws URISyntaxException {
        this.workingDirectory = workingDirectory;
        this.key = key;
        this.gson = new Gson();

        execDir = new File(System.getProperty("user.dir"));
    }

    public void execute() {
        String state = loadState();
        writeInputState(state);
        gameState = gson.fromJson(new StringReader(state), GameState.class);

        if (gameState.Phase == 1) {
            botStrategy = new BotStrategy(gameState, botState);
            PlaceShipCommand placeShipCommand = botStrategy.placeShips();
            writePlaceShips(placeShipCommand);
            logger.info("Placing ships " + placeShipCommand);
            if (hasBotState()) {
                deleteBotState();
            }
        } else {
            botState = new BotState();
            if (hasBotState()) {
                botState = loadBotState();
            } else {
                for (OpponentShip ship : gameState.OpponentMap.Ships) {
                    botState.LastOpponentShipStatus.put(ship.ShipType, ship.Destroyed);
                }
            }
            botStrategy = new BotStrategy(gameState, botState);
            Command command = botStrategy.getFireCommand();
            writeMove(command);
            botState.LastShot = command.getCoordinate();
            writeBotState(execDir);
            writeBotState(new File(workingDirectory));
        }
    }

    private boolean hasBotState() {
        File file = new File(execDir, botStateFileName);
        return file.exists();
    }

    private void deleteBotState(){
        new File(execDir, botStateFileName).delete();
    }

    private BotState loadBotState() {
        try {
            StringBuilder jsonText = new StringBuilder();
            BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(execDir, botStateFileName)));
            String line = bufferedReader.readLine();
            while (line != null) {
                jsonText.append(line);
                jsonText.append("\r\n");
                line = bufferedReader.readLine();
            }

            return gson.fromJson(jsonText.toString(), BotState.class);
        } catch (IOException e) {
            log(String.format("Unable to read bot state file: %s/%s", workingDirectory, botStateFileName));
            log(String.format("Stacktrace: %s", Arrays.toString(e.getStackTrace())));
            return null;
        }
    }

    private void writeBotState(File dir) {
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(dir, botStateFileName)));
            String botStateStr = gson.toJson(botState);
            bufferedWriter.write(botStateStr);
            bufferedWriter.flush();
            bufferedWriter.close();
            log("Bot state " + botStateStr);
        } catch (IOException e) {
            log(String.format("Unable to write bot state file file: %s/%s", dir, botStateFileName));
            log(String.format("Stacktrace: %s", Arrays.toString(e.getStackTrace())));
        }
    }

    private String loadState() {
        try {
            StringBuilder jsonText = new StringBuilder();
            BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(workingDirectory, stateFileName)));
            String line = bufferedReader.readLine();
            while (line != null) {
                jsonText.append(line);
                jsonText.append("\r\n");
                line = bufferedReader.readLine();
            }

            return jsonText.toString();
        } catch (IOException e) {
            log(String.format("Unable to read state file: %s/%s", workingDirectory, stateFileName));
            log(String.format("Stacktrace: %s", Arrays.toString(e.getStackTrace())));
            return null;
        }
    }

    private void writeInputState(String state) {
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(workingDirectory, inputStateFilename)));
            bufferedWriter.write(state);
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (IOException e) {
            log(String.format("Unable to write input state file: %s/%s", workingDirectory, placeShipFileName));
            log(String.format("Stacktrace: %s", Arrays.toString(e.getStackTrace())));
        }
    }

    private void writeMove(Command command) {
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(workingDirectory, commandFileName)));
            bufferedWriter.write(command.toString());
            bufferedWriter.flush();
            bufferedWriter.close();
            log("Command " + command);
        } catch (IOException e) {
            log(String.format("Unable to write command file: %s/%s", workingDirectory, placeShipFileName));
            log(String.format("Stacktrace: %s", Arrays.toString(e.getStackTrace())));
        }
    }

    private void writePlaceShips(PlaceShipCommand placeShipCommand) {

        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new FileWriter(new File(workingDirectory, placeShipFileName)));
            bufferedWriter.write(placeShipCommand.toString());
            bufferedWriter.flush();
            bufferedWriter.close();
            log("Command " + placeShipCommand);
        } catch (IOException e) {
            log(String.format("Unable to write command file: %s/%s", workingDirectory, placeShipFileName));
            log(String.format("Stacktrace: %s", Arrays.toString(e.getStackTrace())));
        }
    }

    private void log(String message) {

        System.out.println(String.format("[BOT]\t%s", message));
    }
}
