package snakegamev1;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class Main extends Application {
    
    private static final int BLOCK_SIZE = 20;
    private static final int APP_W = 20 * BLOCK_SIZE;
    private static final int APP_H = 20 * BLOCK_SIZE;
    private int score = 0;
    private enum Direction { UP, RIGHT, DOWN, LEFT }
    private Direction direction;
    private ObservableList<Node> snake = FXCollections.observableArrayList();
    private boolean running = true;
    private Button startBtn, exitBtn;
    private Group snakeBody;
    private Rectangle head, food, body;
    private Pane board;
    private Timeline timeline = new Timeline();
    private Slider speed;
    private Label scoreLbl;
    private HBox control;
    private double dragOffsetX;
    private double dragOffsetY;

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Scene scene = new Scene(createContent());
        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                    case W:
                        if (direction != Direction.DOWN) direction = Direction.UP;
                        break;
                    case D:
                        if (direction != Direction.LEFT) direction = Direction.RIGHT;
                        break;
                    case S:
                        if (direction != Direction.UP) direction = Direction.DOWN;
                        break;
                    case A:
                        if (direction != Direction.RIGHT) direction = Direction.LEFT;
                        break;
                }
        });
        scene.setOnMousePressed((MouseEvent event) -> {
            dragOffsetX = event.getScreenX() - primaryStage.getX();
            dragOffsetY = event.getScreenY() - primaryStage.getY();
        });
        scene.setOnMouseDragged((MouseEvent event) -> {
            primaryStage.setX(event.getScreenX() - dragOffsetX);
            primaryStage.setY(event.getScreenY() - dragOffsetY);
        });
        primaryStage.setScene(scene);
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.show();
    }

    private Parent createContent() {
        VBox root = new VBox();
        root.setStyle("-fx-padding: 10;" + 
                      "-fx-border-style: solid inside;" + 
                      "-fx-border-width: 2;" +
                      "-fx-border-insets: 5;" + 
                      "-fx-border-radius: 5;" + 
                      "-fx-border-color: blue;");
        board = new Pane();
        Rectangle rect = new Rectangle(APP_W, APP_H, Color.gray(0.9));
        board.getChildren().add(rect);
        for (int x = 0; x <= APP_W; x += BLOCK_SIZE) {
            Line line = new Line(x, 0, x, APP_H);
            board.getChildren().add(line);
        }
        for (int y = 0; y <= APP_H; y += BLOCK_SIZE) {
            Line line = new Line(0, y, APP_W, y);
            board.getChildren().add(line);
        }
        control = new HBox(15);
        control.setAlignment(Pos.CENTER);
        Insets margin = new Insets(10, 5, 10, 5);
        startBtn = new Button("Start");
        startBtn.setOnAction(event -> {
            if (running) {
                startGame();
                KeyFrame frame = new KeyFrame(Duration.millis(speed.getValue()), event1 -> {
                    snake = snakeBody.getChildren();
                    boolean toRemove = snake.size() > 1;
                    Node tail = toRemove ? snake.remove(snake.size() - 1) : snake.get(0);
                    double tailX = tail.getTranslateX();
                    double tailY = tail.getTranslateY();
                    switch(direction) {
                        case UP:
                            tail.setTranslateX(snake.get(0).getTranslateX());
                            tail.setTranslateY(snake.get(0).getTranslateY() - BLOCK_SIZE);
                            break;
                        case RIGHT:
                            tail.setTranslateX(snake.get(0).getTranslateX() + BLOCK_SIZE);
                            tail.setTranslateY(snake.get(0).getTranslateY());
                            break;
                        case DOWN:
                            tail.setTranslateX(snake.get(0).getTranslateX());
                            tail.setTranslateY(snake.get(0).getTranslateY() + BLOCK_SIZE);
                            break;
                        case LEFT:
                            tail.setTranslateX(snake.get(0).getTranslateX() - BLOCK_SIZE);
                            tail.setTranslateY(snake.get(0).getTranslateY());
                            break;
                    }
                    if (toRemove) snake.add(0, tail);
                    for (int i = 0; i < snake.size() - 1; i++) {
                        if (snake.get(i) != tail && tail.getTranslateX() == snake.get(i).getTranslateX()
                                && tail.getTranslateY() == snake.get(i).getTranslateY()) {
                            snake.remove(i, snake.size() - 1);
                        }
                    }
                    if (tail.getTranslateX() < 0) tail.setTranslateX(APP_W - BLOCK_SIZE);
                    if (tail.getTranslateX() > (APP_W - BLOCK_SIZE)) tail.setTranslateX(0);
                    if (tail.getTranslateY() < 0) tail.setTranslateY(APP_H - BLOCK_SIZE);
                    if (tail.getTranslateY() > (APP_H - BLOCK_SIZE)) tail.setTranslateY(0);
                    if (tail.getTranslateX() == food.getTranslateX() && tail.getTranslateY() == food.getTranslateY()) {
                        score++;
                        scoreLbl.setText("Score: " + score);
                        setApplePosition(snake);
                        body = new Rectangle(BLOCK_SIZE, BLOCK_SIZE, Color.GREENYELLOW);
                        body.setTranslateX(tailX);
                        body.setTranslateY(tailY);
                        snake.add(body);
                    }
                });
                timeline.getKeyFrames().add(frame);
                timeline.setCycleCount(Timeline.INDEFINITE);
                board.getChildren().addAll(snakeBody, food);
                timeline.play();
            } else {
                stopGame();
            }
        });
        exitBtn = new Button("Exit");
        exitBtn.setOnAction(event -> Platform.exit());
        Text text = new Text("Rate:");
        speed = new Slider(100, 1000, 200);
        speed.setShowTickMarks(true);
        speed.setShowTickLabels(true);
        speed.setMajorTickUnit(300);
        scoreLbl = new Label("Score: " + score);
        control.getChildren().addAll(startBtn, exitBtn, text, speed, scoreLbl);
        HBox.setMargin(startBtn, margin);
        HBox.setMargin(exitBtn, margin);
        HBox.setMargin(scoreLbl, margin);
        root.getChildren().addAll(board, control);
        return root;
    }

    private void startGame() {
        direction = Direction.RIGHT;
        Rectangle head = new Rectangle(BLOCK_SIZE, BLOCK_SIZE, Color.GREENYELLOW );
        snake.add(head);
        running = false;
        startBtn.setText("Stop");
        snakeBody = new Group();
        snakeBody.getChildren().addAll(snake);
        food = new Rectangle(BLOCK_SIZE, BLOCK_SIZE, Color.RED);
        setApplePosition(snake);
    }
    
    private void stopGame() {
        timeline.stop();
        score = 0;
        scoreLbl.setText("Score: " + score);
        running = true;
        startBtn.setText("Start");
        snake.clear();
        board.getChildren().removeAll(snakeBody, food);
    }
    
    private void setApplePosition(ObservableList<Node> snake) {
        food.setTranslateX((int) (Math.random() * (APP_W - BLOCK_SIZE)) / BLOCK_SIZE * BLOCK_SIZE);
        food.setTranslateY((int) (Math.random() * (APP_H - BLOCK_SIZE)) / BLOCK_SIZE * BLOCK_SIZE);
        for (int i = 0; i < snake.size(); i++) {
            if (snake.get(i).getTranslateX() == food.getTranslateX() && snake.get(i).getTranslateY() == food.getTranslateY()) {
                setApplePosition(snake);
            }
        }
    }
}
