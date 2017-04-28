package za.co.entelect.challenge;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import za.co.entelect.challenge.domain.command.Point;
import za.co.entelect.challenge.domain.state.GameState;

import java.io.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class Util {

    public static final Logger logger = LoggerFactory.getLogger(Util.class);

    public static Random R = new Random();
    /*
    static {
        JmxReporter.forRegistry(metrics).registerWith(ManagementFactory.getPlatformMBeanServer()).build().start();
    }
    */

    public static int manhattanDist(Point start, Point end) {
        return manhattanDist(start.x, start.y, end.x, end.y);
    }

    public static int manhattanDist(int startX, int startY, int endX, int endY) {
        return Math.abs(startX - endX) + Math.abs(startY - endY);
    }

    public static float falloff(int dist) {
        //return (float)Math.max(0, 1 - dist * 0.2);
        return 1 / (float)Math.sqrt(dist + 1);
        //return Math.min(1, 1 - dist / (Constants.MAX_MAZE_DIST));
       // return (float) Math.pow(0.9, dist + 1);
        //return 1f;
    }

    public static boolean isInBounds(Point pos) {
        return isInBounds(pos.x, pos.y);
    }

    public static boolean isInBounds(int x, int y) {
        //return x >= 0 && x < Constants.WIDTH && y >= 0 && y < Constants.HEIGHT;
        throw new NotImplementedException();
    }

    public static float[][] mult(float[][] input, float val) {
        float[][] result = new float[input.length][input[0].length];
        for (int i = 0; i < input.length; i++) {
            for (int j = 0; j < input[0].length; j++) {
                result[i][j] = input[i][j] * val;
            }
        }
        return result;
    }

    public static char[][] clone(char[][] val) {
        char[][] clone = new char[val.length][val[0].length];
        for (int i = 0; i < val.length; i++) {
            System.arraycopy(val[i], 0, clone[i], 0, val[0].length);
        }
        return clone;
    }

    public static int[][] clone(int[][] val) {
        int[][] clone = new int[val.length][val[0].length];
        for (int i = 0; i < val.length; i++) {
            System.arraycopy(val[i], 0, clone[i], 0, val[0].length);
        }
        return clone;
    }

    public static float[][] clone(float[][] val) {
        throw new NotImplementedException();
        /*
        float[][] clone = new float[val.length][val[0].length];
        for (int i = 0; i < Constants.WIDTH; i++) {
            for (int j = 0; j < Constants.HEIGHT; j++) {
                clone[i][j] = val[i][j];
            }
        }
        return clone;
        */
    }

    public static byte[] clone(byte[] source) {
        byte[] clone = new byte[source.length];
        System.arraycopy(source, 0, clone, 0, source.length);
        return clone;
    }

    public static char[] clone(char[] source) {
        char[] clone = new char[source.length];
        System.arraycopy(source, 0, clone, 0, source.length);
        return clone;
    }

    public static Set clone(Set val) {
        Set clone = new HashSet<>();
        for (Object item : val) {
            clone.add(item);
        }
        return clone;
    }

    public static String toSymbol(int gamma) {
        if (gamma == Integer.MIN_VALUE) {
            return "-∞";
        } else if (gamma == Integer.MAX_VALUE) {
            return "∞";
        }
        return String.valueOf(gamma);
    }

    private static final Gson gson = new Gson();

    public static BotState loadBotState(File file) {
        try {
            StringBuilder jsonText = new StringBuilder();
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String line = bufferedReader.readLine();
            while (line != null) {
                jsonText.append(line);
                jsonText.append("\r\n");
                line = bufferedReader.readLine();
            }

            BotState botState = gson.fromJson(new StringReader(jsonText.toString()), BotState.class);
            return botState;
        } catch (IOException e) {
            logger.error(String.format("Unable to read bot state file: %s", file));
            logger.error(String.format("Stacktrace: %s", Arrays.toString(e.getStackTrace())));
            return null;
        }
    }

    public static GameState loadState(File file) {
        try {
            StringBuilder jsonText = new StringBuilder();
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String line = bufferedReader.readLine();
            while (line != null) {
                jsonText.append(line);
                jsonText.append("\r\n");
                line = bufferedReader.readLine();
            }

            GameState gameState = gson.fromJson(new StringReader(jsonText.toString()), GameState.class);
            return gameState;
        } catch (IOException e) {
            logger.error(String.format("Unable to read state file: %s", file));
            logger.error(String.format("Stacktrace: %s", Arrays.toString(e.getStackTrace())));
            return null;
        }
    }

    public static int dist(Point a, Point b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }
}
