package pt.ipbeja.app.ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import pt.ipbeja.app.model.Model;
import pt.ipbeja.app.model.Move;
import javafx.scene.image.Image;
import pt.ipbeja.app.model.Position;
import pt.ipbeja.app.model.View;

import java.util.ArrayList;
import java.util.List;

/**
 * The fifteen main view
 *
 * @author João Paulo Barros e Rui Pais
 * @version 2014/05/19 - 2016/04/03 - 2017/04/19 - 2019/05/06 - 2021/05/18
 */
public class FifteenGUI extends Application implements View {
    private final String ICON_FILE = "/resources/images/puzzle15.jpg";
    private List<PieceButton> buttons;
    private final Model model;
    private final Scene scene;
    private Button solveButton;
    private GridPane panBtns;
    private Label timeLabel;
    //private static Map<KeyCode, Direction> directionMap = new HashMap<>();
//    static {
//        directionMap.put(KeyCode.UP, Direction.UP);
//        directionMap.put(KeyCode.DOWN, Direction.DOWN);
//        directionMap.put(KeyCode.LEFT, Direction.LEFT);
//        directionMap.put(KeyCode.RIGHT, Direction.RIGHT);
//        directionMap.put(KeyCode.ENTER, Direction.ENTER);
//    }

    /**
     * Create window with board
     */
    public FifteenGUI() {
        this.buttons = new ArrayList<>();
        this.model = new Model(this);
        this.scene = this.createScene();
        this.panBtns = new GridPane();
    }

    @Override
    public void start(Stage stage) {
       // this.mixModel();

        stage.setTitle("Fifteen Puzzle");
        this.setAppIcon(stage, ICON_FILE);
        stage.setScene(scene);
        stage.show();
        stage.toFront();

        this.model.startTimer();
    }

    /**
     * Executed on exit to stop all threads
     */
    @Override
    public void stop() {
        System.out.println("END");
        System.exit(0);
    }

    private void setAppIcon(Stage stage, String filename) {
        try {
            Image ico = new Image(filename);
            stage.getIcons().add(ico);
        } catch (Exception ex) {
        }
    }

    private Pane createButtonsUI() {
        int nRows = Model.N_LINES;
        int nCols = Model.N_COLS;
        this.panBtns = new GridPane();
        this.panBtns.setAlignment(Pos.CENTER);

        for (int row = 0; row < nRows; row++) {
            for (int col = 0; col < nCols; col++) {
                Position pos = new Position(row, col);
                String text = this.model.pieceTextAt(pos);
                PieceButton b = new PieceButton(text, pos);
                this.panBtns.add(b, col, row);
                this.buttons.add(b);
                b.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                //b.setPadding(new Insets(5));
                b.setPrefHeight(Double.MAX_VALUE);
                b.setPrefSize(100, 100);
                b.setOnMouseClicked(this::handleMouseClick);
                GridPane.setVgrow(b, Priority.ALWAYS);
                GridPane.setHgrow(b, Priority.ALWAYS);
            }
        }
        return panBtns;
    }

    /**
     * Handle button press by asking the model to execute the respective actions
     * The model is then responsible to notify this (and other) views
     */
    public void handleMouseClick(MouseEvent e) {
        PieceButton btn = (PieceButton) e.getSource();
        Position pos = btn.position();
        model.pieceSelected(pos); // inform model
    }

    void setKeyHandle(Scene scnMain) {
        scnMain.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.ENTER) {
                    Node focusedNode = scene.getFocusOwner();
                    if (focusedNode instanceof PieceButton) {
                        PieceButton btn = (PieceButton) focusedNode;
                        Position pos = btn.position();
                        model.pieceSelected(pos);
                    }
                }
                else {
                    // nothing to do
                }
            };
        });
    }

    private Scene createScene() {
        VBox vbxMain = new VBox();
        this.solveButton = new Button("Solve!");
        this.solveButton.setMaxWidth(Integer.MAX_VALUE);
        this.solveButton.setStyle("-fx-background-color: #ffff33; ");
        this.solveButton.setOnAction(event -> {
            panBtns.setDisable(true);
            this.solveButton.setDisable(true);
            model.solve();
        });
        this.timeLabel = new Label(this.model.getTimerValue() + "");
        vbxMain.getChildren().addAll(solveButton, this.timeLabel, this.createButtonsUI());
        Scene scnMain = new Scene(vbxMain);
        this.setKeyHandle(scnMain);

        return scnMain;
    }

    /**
     * Makes a number of moves to mix the puzzle. The mix is not very smart as
     * the moves can be symmetric in consecutive moments.
     */
    private void mixModel() {
        this.model.mix(5, 10);
    }

    /**
     * Updates the pieces content by asking the model
     */
    private void updateAllLayout() {
        for (PieceButton b : this.buttons) {
            String btnText = this.model.pieceTextAt(b.position());
            b.setTextAndImage(btnText);
        }
        this.timeLabel.setText(this.model.getTimerValue() + "");
        this.solveButton.setDisable(false);
    }

    public void updateLayoutAfterMove(Move lastMove) {
        if (lastMove != null) {
            int line1 = lastMove.begin().line();
            int col1 = lastMove.begin().col();
            PieceButton b1 = this.buttons.get(line1 * Model.N_COLS + col1);
            String text1 = b1.getText();

            int line2 = lastMove.end().line();
            int col2 = lastMove.end().col();
            PieceButton b2 = this.buttons.get(line2 * Model.N_COLS + col2);
            String text2 = b2.getText();

            b1.setTextAndImage(text2);
            b2.setTextAndImage(text1);
        }
    }

    @Override
    public void notifyView(Move lastMove, Boolean wins, int timerValue) {
        Platform.runLater(() -> {

            if (lastMove != null) {
                this.updateLayoutAfterMove(lastMove);
            }

            if (wins) {
                this.model.stopTimer();
                new Alert(Alert.AlertType.INFORMATION, "You win! ").showAndWait();
                this.mixModel();
                this.panBtns.setDisable(false);
                this.model.startTimer();
                this.updateAllLayout();
            }

            this.timeLabel.setText(timerValue + "");
        });
    }

    /**
     * Start program
     * @param args currently not used
     */
    public static void main(String[] args) {
        Application.launch(args);
    }
}
