package za.co.entelect.challenge.swing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import za.co.entelect.challenge.*;
import za.co.entelect.challenge.domain.command.*;
import za.co.entelect.challenge.domain.command.Point;
import za.co.entelect.challenge.domain.command.ship.ShipType;
import za.co.entelect.challenge.domain.state.*;
import za.co.entelect.challenge.strategy.placement.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GUI extends JFrame {

    private static final Logger logger = LoggerFactory.getLogger(GUI.class);

    public static final int DEFAULT_WIDTH = 1960;
    public static final int DEFAULT_HEIGHT = 1240;

    private GameState gameState;
    private GameState gameStateBoth;
    private BotState botState;
    private BotStrategy botStrategy;

    private ProbabilityMap huntMap;
    private ProbabilityMap targetMap;

    private JPanel mainPanel;
    private JPanel contentPanel;
    private JPanel replayPanel;
    private JButton lastReplayButton;
    private JPanel playerPanel;
    private JPanel opponentPanel;
    private JButton[][] playerMapButtons;
    private JButton[][] opponentMapButtons;

    private File replayDir;

    private BotMode botMode = BotMode.HUNT;

    public GUI() {
        createMenus();
        createMainLayout();
        setTitle(Constants.APP_TITLE);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
        pack();
    }

    private ProbabilityMap getProbabilityMap() {
        if (botMode == BotMode.HUNT) {
            return huntMap;
        }
        return targetMap;
    }

    public void setGameState(GameState gameState, BotState botState, GameState gameStateBoth) {
        this.gameStateBoth = gameStateBoth;
        this.gameState = gameState;
        this.botState = botState;
        this.botStrategy = new BotStrategy(gameState, botState);
        this.huntMap = new ProbabilityMap(gameState, ProbabilityMapType.HUNT, botState);
        this.targetMap = new ProbabilityMap(gameState, ProbabilityMapType.TARGET, botState);
        updatePlayerMapLayout();
        updateOpponentMapLayout();
        pack();
    }

    public void setReplayDir(File replayDir) {
        this.replayDir = replayDir;
        updateReplaySelectors();
        pack();
    }

    private void updateReplaySelectors() {
        if (replayPanel != null) {
            clearPanel(replayPanel);
        } else {
            GridLayout gridLayout = new GridLayout();
            replayPanel = new JPanel();
            replayPanel.setLayout(gridLayout);
            mainPanel.add(replayPanel, BorderLayout.PAGE_START);

            contentPanel = new JPanel();
            contentPanel.setLayout(new GridLayout(1, 2));
            mainPanel.add(contentPanel, BorderLayout.CENTER);
        }
        Pattern pattern = Pattern.compile("Phase (\\d) - Round (\\d+)");
        String[] directories = replayDir.list((current, name) -> new File(current, name).isDirectory());
        Arrays.sort(directories, (a, b) -> {
            Matcher am = pattern.matcher(a);
            Matcher bm = pattern.matcher(b);

            if (am.matches() && bm.matches()) {

            }

            return Integer.valueOf(am.group(2)).compareTo(Integer.valueOf(bm.group(2)));
        });

        logger.info(Arrays.toString(directories));

        for (String dir : directories) {
            JButton btn = new JButton();
            btn.setToolTipText(dir);
            btn.addActionListener(e -> {
                if (lastReplayButton != null) {
                    lastReplayButton.setBackground(Color.black);
                }
                lastReplayButton = btn;
                btn.setBackground(Color.red);
                File subDir = new File(replayDir, dir);
                File inputState = new File(subDir, "A/input-state.json");
                File botStateFile = new File(subDir, "A/bot-state.json");

                if (!inputState.exists()){
                    inputState = new File(subDir, "B/input-state.json");
                    botStateFile = new File(subDir, "B/bot-state.json");
                }

                File gameStateBothFile = new File(subDir, "state.json");
                setGameState(Util.loadState(inputState), Util.loadBotState(botStateFile), Util.loadState(gameStateBothFile));

                if (botState != null) {
                    logger.info(botState.toString());
                }
            });
            replayPanel.add(btn);
        }
    }

    private void createMenus(){
        JMenuBar menuBar = new JMenuBar();
        JMenu menuFile = new JMenu("File");
        menuBar.add(menuFile);

        JMenuItem menuItemOpenReplays = new JMenuItem("Open");
        menuFile.add(menuItemOpenReplays);
        menuItemOpenReplays.addActionListener(e -> {
            final JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Choose Replay Directory");
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fc.setAcceptAllFileFilterUsed(false);
            int returnVal = fc.showOpenDialog(getParent());
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                setReplayDir(fc.getSelectedFile());
            }
        });

        setJMenuBar(menuBar);
    }

    private void createMainLayout(){
        setLayout(new BorderLayout());
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createPlayerShipPanel() {
        PlayerMap playerMap = gameState.PlayerMap;
        BattleshipPlayer player = playerMap.Owner;
        ArrayList<Ship> ships = player.Ships;

        JPanel playerShipPanel = new JPanel();
        playerShipPanel.setLayout(new BoxLayout(playerShipPanel, BoxLayout.Y_AXIS));
        
        playerShipPanel.add(new JLabel(player.Name));
        playerShipPanel.add(new JLabel("Failed Placement: " + player.FailedFirstRoundCommands));
        playerShipPanel.add(new JLabel("Energy: " + player.Energy));
        playerShipPanel.add(new JLabel("Winner: " + player.IsWinner));
        playerShipPanel.add(new JLabel("Killed: " + player.Killed));
        playerShipPanel.add(new JLabel("Key: " + player.Key));
        playerShipPanel.add(new JLabel("Ships Left: " + player.ShipsRemaining));
        playerShipPanel.add(new JLabel("Fired: " + player.ShotsFired));
        playerShipPanel.add(new JLabel("Hit: " + player.ShotsHit));
        playerShipPanel.add(new JLabel("Points: " + player.Points));
        playerShipPanel.add(new JLabel("Round: " + gameState.Round));

        for (Ship ship : ships) {
            playerShipPanel.add(new JLabel(ship.ShipType + " " + (ship.Destroyed ? "(Destroyed)" : "" )));
        }

        PlacementMap map = new PlacementMap(gameState);
        JButton button = new JButton("Placement Probabilities");
        button.addActionListener(e -> {
            setPlacementProbabilities(map.getPossibilities(), map.getProbabilities());
        });
        playerShipPanel.add(button);

        playerShipPanel.add(new JLabel("Random Placements"));

        for (Ship ship : ships) {
            if (ship.Placed) {
                continue;
            }

            JButton b = new JButton("Place Random " + ship.ShipType);
            b.addActionListener(e -> {
                PlacementStrategy placementStrategy = new RandomPlacementStrategy(gameState, ship.ShipType);
                Placement placement = placementStrategy.getPlacement();
                List<Cell> cells = gameState.PlayerMap.getAllCellsInDirection(placement.start, placement.direction, ship.ShipType.getSize());
                for (Cell cell : cells) {
                    playerMapButtons[cell.X][cell.Y].setBackground(Color.cyan);
                }
            });
            playerShipPanel.add(b);
        }

        playerShipPanel.add(new JLabel("Lowest Placements"));

        for (Ship ship : ships) {
            if (ship.Placed) {
                continue;
            }

            JButton b = new JButton("Place Lowest " + ship.ShipType);
            b.addActionListener(e -> {
                PlacementStrategy placementStrategy = new LowestPlacementStrategy(gameState, ship.ShipType);
                Placement placement = placementStrategy.getPlacement();
                List<Cell> cells = gameState.PlayerMap.getAllCellsInDirection(placement.start, placement.direction, ship.ShipType.getSize());
                for (Cell cell : cells) {
                    playerMapButtons[cell.X][cell.Y].setBackground(Color.magenta);
                }
            });
            playerShipPanel.add(b);
        }

        playerShipPanel.add(new JLabel("Highest Placements"));


        for (Ship ship : ships) {
            if (ship.Placed) {
                continue;
            }

            JButton b = new JButton("Place Highest " + ship.ShipType);
            b.addActionListener(e -> {
                PlacementStrategy placementStrategy = new HighestPlacementStrategy(gameState, ship.ShipType);
                Placement placement = placementStrategy.getPlacement();
                List<Cell> cells = gameState.PlayerMap.getAllCellsInDirection(placement.start, placement.direction, ship.ShipType.getSize());
                for (Cell cell : cells) {
                    playerMapButtons[cell.X][cell.Y].setBackground(Color.orange);
                }
            });
            playerShipPanel.add(b);
        }

        return playerShipPanel;
    }

    private JPanel createOpponentShipPanel() {
        OpponentMap opponentMap = gameState.OpponentMap;
        ArrayList<OpponentShip> ships = opponentMap.Ships;

        JPanel opponentShipPanel = new JPanel();
        opponentShipPanel.setLayout(new BoxLayout(opponentShipPanel, BoxLayout.Y_AXIS));
        opponentShipPanel.add(new JLabel(opponentMap.Name));
        opponentShipPanel.add(new JLabel("Alive: " + opponentMap.Alive));
        opponentShipPanel.add(new JLabel("Points: " + opponentMap.Points));
        if (botState != null) {
            opponentShipPanel.add(new JLabel("Last Destroyed Ship: " + botState.LastDestroyedShip));
        }


        for (OpponentShip ship : ships) {
            JButton button = new JButton(ship.ShipType + " " + (ship.Destroyed ? "(Destroyed)" : "" ));
            button.addActionListener(e -> {
                ProbabilityMap probabilityMap = getProbabilityMap();
                setOpponentProbabilities(probabilityMap.getShipPossibilities(ship.ShipType), probabilityMap.getShipProbabilities(ship.ShipType));
            });
            opponentShipPanel.add(button);
        }

        JButton button = new JButton("Probabilities");
        button.addActionListener(e -> {
            ProbabilityMap probabilityMap = getProbabilityMap();
            setOpponentProbabilities(probabilityMap.getPossibilities(), probabilityMap.getProbabilities());
        });
        opponentShipPanel.add(button);

        JButton botModeButton = new JButton(botMode.toString());
        botModeButton.addActionListener(e -> {
            if (botMode == BotMode.HUNT) {
                botMode = BotMode.TARGET;
            } else if (botMode == BotMode.TARGET) {
                botMode = BotMode.HUNT;
            }
            botModeButton.setText(botMode.toString());
        });
        opponentShipPanel.add(botModeButton);

        JButton bestShotButton = new JButton("Best Shot");
        bestShotButton.addActionListener(e -> {
            highlightBestShot();
        });
        opponentShipPanel.add(bestShotButton);

        JButton lastTrackerHits = new JButton("Last Tracker Hits");
        lastTrackerHits.addActionListener(e -> {
            highlightLastTrackerHits();
        });
        opponentShipPanel.add(lastTrackerHits);

        JButton highlightDestroyedShipPossibilitiesButton = new JButton("Destroyed Ship Possibilities");
        highlightDestroyedShipPossibilitiesButton.addActionListener(e -> {
            highlightDestroyedShipPossibilities();
        });
        opponentShipPanel.add(highlightDestroyedShipPossibilitiesButton);

        if (botState != null) {
            opponentShipPanel.add(new JLabel("Mode: " + botState.Mode));
        }

        return opponentShipPanel;
    }

    private void highlightDestroyedShipPossibilities() {
        if (botState.LastDestroyedShip == null) {
            return;
        }
        List<List<Point>> p = botStrategy.getDestroyedShipPossibilities(botState.LastDestroyedShip.getSize());
        int x = 0;
        for (List<Point> pp : p) {
            for (Point ppp : pp) {
                opponentMapButtons[ppp.getX()][ppp.getY()].setBackground(Color.ORANGE);
                opponentMapButtons[ppp.getX()][ppp.getY()].setText("Pot " + x);
            }
            x++;
        }
    }

    private void setPlacementProbabilities(int[][] possibilities, float[][] probabilities) {
        int mapDimension = gameState.MapDimension;
        for (int i = 0; i < mapDimension; i++) {
            for (int j = 0; j < mapDimension; j++) {
                JButton opponentButton = playerMapButtons[j][i];
                opponentButton.setText(String.format("%d", possibilities[j][i]));

                Cell cell = gameState.PlayerMap.getCellAt(j, i);
                Color color = Color.getHSBColor(0.66f, 1, Math.min(1f, probabilities[j][i]));
                if (cell.Occupied) {
                    color = Color.green;
                }
                opponentButton.setBackground(color);
            }
        }
    }

    private void setOpponentProbabilities(int[][] possibilities, float[][] probabilities) {
        int mapDimension = gameState.MapDimension;
        for (int i = 0; i < mapDimension; i++) {
            for (int j = 0; j < mapDimension; j++) {
                JButton opponentButton = opponentMapButtons[j][i];
                opponentButton.setText(String.format("%d", possibilities[j][i]));

                OpponentCell cell = gameState.OpponentMap.getCellAt(j, i);
                Color color = Color.getHSBColor(0.66f, 1, Math.min(1f, probabilities[j][i]));
                if (cell.Damaged) {
                    color =  Color.green;
                } else if (cell.Missed) {
                    color = Color.red;
                }
                opponentButton.setBackground(color);
            }
        }
    }

    private void updatePlayerMapLayout(){
        if (playerPanel != null) {
            clearPanel(playerPanel);
        } else {
            playerPanel = new JPanel();
            playerPanel.setLayout(new BorderLayout());
            contentPanel.add(playerPanel);
        }
        
        PlayerMap playerMap = gameState.PlayerMap;
        int mapDimension = gameState.MapDimension;

        JPanel playerMapPanel = new JPanel();
        playerMapPanel.setBackground(Color.black);
        GridLayout playerMapLayout = new GridLayout(mapDimension, mapDimension);
        playerMapLayout.setHgap(2);
        playerMapLayout.setVgap(2);
        playerMapPanel.setLayout(playerMapLayout);
        
        playerPanel.add(createPlayerShipPanel(), BorderLayout.WEST);
        playerPanel.add(playerMapPanel, BorderLayout.CENTER);

        playerMapButtons = new JButton[mapDimension][mapDimension];

        for (int i = 0; i < mapDimension; i++) {
            for (int j = 0; j < mapDimension; j++) {
                JButton cellButton = new JButton();
                cellButton.setContentAreaFilled(false);
                cellButton.setOpaque(true);
                cellButton.setBackground(getPlayerMapColor(playerMap.getCellAt(j, i)));
                playerMapPanel.add(cellButton);
                playerMapButtons[j][i] = cellButton;
            }
        }

        if (playerMap.Owner.Ships != null) {
            for (Ship ship : playerMap.Owner.Ships) {
                if (ship.Cells.isEmpty()) {
                    continue;
                }
                for (Cell cell : ship.Cells) {
                    if (cell == null) {
                        continue;
                    }
                    playerMapButtons[cell.X][cell.Y].setText(ship.ShipType.getShortVal());
                }
            }
        }
    }

    private void highlightBestShot(){
        Command fireCommand = botStrategy.getFireCommand();
        Point loc = fireCommand.getCoordinate();
        JButton button  = opponentMapButtons[loc.x][loc.y];
        button.setBackground(Color.CYAN);
        button.setText("X");
    }

    private void highlightLastTrackerHits(){
        if (botState != null) {
            for (Point p : botState.LastTrackerHits) {
                JButton button  = opponentMapButtons[p.x][p.y];
                button.setBackground(Color.BLUE);
                button.setText("*");
            }
        }
    }

    private PlayerMap getOpponentPlayerMap() {
        if (gameState.PlayerMap.Owner.Key == 'A') {
            return gameStateBoth.Player2Map;
        }
        return gameStateBoth.Player1Map;
    }

    private void updateOpponentMapLayout(){
        if (opponentPanel != null) {
            clearPanel(opponentPanel);
        } else {
            opponentPanel = new JPanel();
            opponentPanel.setLayout(new BorderLayout());
            contentPanel.add(opponentPanel);
        }

        OpponentMap opponentMap = gameState.OpponentMap;
        int mapDimension = gameState.MapDimension;

        JPanel opponentMapPanel = new JPanel();
        opponentMapPanel.setBackground(Color.black);
        GridLayout opponentMapLayout = new GridLayout(mapDimension, mapDimension);
        opponentMapLayout.setHgap(2);
        opponentMapLayout.setVgap(2);
        opponentMapPanel.setLayout(opponentMapLayout);

        opponentPanel.add(createOpponentShipPanel(), BorderLayout.WEST);
        opponentPanel.add(opponentMapPanel, BorderLayout.CENTER);

        opponentMapButtons = new JButton[mapDimension][mapDimension];

        PlayerMap opponentPlayerMap = getOpponentPlayerMap();
        for (int i = 0; i < mapDimension; i++) {
            for (int j = 0; j < mapDimension; j++) {
                Cell cell = opponentPlayerMap.getCellAt(j, i);
                JButton cellButton = new JButton(cell.Occupied ? "X" : "");
                cellButton.setContentAreaFilled(false);
                cellButton.setOpaque(true);
                OpponentCell opponentCell = opponentMap.getCellAt(j, i);
                cellButton.setBackground(getOpponentMapColor(opponentCell));
                cellButton.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            if (SwingUtilities.isRightMouseButton(e)) {
                                opponentCell.Damaged = !opponentCell.Damaged;
                            } else {
                                opponentCell.Missed = !opponentCell.Missed;
                            }

                            huntMap.update();
                            targetMap.update();
                        }
                    }
                );
                opponentMapPanel.add(cellButton);
                opponentMapButtons[j][i] = cellButton;
            }
        }

        highlightLastTrackerHits();

        if (opponentPlayerMap.Owner.Ships != null) {
            for (Ship ship : opponentPlayerMap.Owner.Ships) {
                for (Cell cell : ship.Cells) {
                    if (cell == null) continue;
                    opponentMapButtons[cell.X][cell.Y].setText(ship.ShipType.getShortVal());
                }
            }
        }
    }
    
    private void clearPanel(JPanel panel) {
        panel.removeAll();
        panel.revalidate();
        panel.repaint();
    }

    private Color getPlayerMapColor(Cell cell) {
        if (cell.Hit) {
            if (cell.Occupied) {
                return Color.red;
            } else {
                return Color.yellow;
            }
        }

        if (cell.Occupied) {
            return Color.green;
        }

        return Color.white;
    }

    private Color getOpponentMapColor(OpponentCell cell) {
        if (cell.Damaged) {
            return Color.green;
        }

        if (cell.Missed) {
            return Color.yellow;
        }

        return Color.white;
    }

    private void setPlayerMapButtonColor(int x, int y, Color color) {
        playerMapButtons[x][y].setBackground(color);
    }
}
